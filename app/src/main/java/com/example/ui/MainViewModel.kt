package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.BundledContent
import com.example.data.FeedSyncStatus
import com.example.data.FirebaseAiRepository
import com.example.data.ListDensity
import com.example.data.PreferencesRepository
import com.example.data.PrintTopicLayoutPlan
import com.example.data.StudyPost
import com.example.data.StudyRepository
import com.example.data.TextSizePreset
import com.example.data.UserPreferences
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "MainViewModel"

sealed interface AiPrintUiState {
  data object Idle : AiPrintUiState

  data object Loading : AiPrintUiState

  data class Success(val plan: PrintTopicLayoutPlan) : AiPrintUiState

  data class Error(val message: String) : AiPrintUiState
}

data class MainUiState(
  val posts: List<StudyPost> = BundledContent.fallbackPosts,
  val selectedClassFilter: String = "All",
  val searchQuery: String = "",
  val searchClassFilter: String = "All",
  val preferences: UserPreferences = UserPreferences(),
  val syncStatus: FeedSyncStatus = FeedSyncStatus(cachedPostsCount = BundledContent.fallbackPosts.size),
  val statusBannerMessage: String? = null,
) {
  val recentFilteredPosts: List<StudyPost>
    get() =
      if (selectedClassFilter == "All") {
        posts
      } else {
        posts.filter { post -> matchesFilterPill(post, selectedClassFilter) }
      }

  val searchResults: List<StudyPost>
    get() {
      val q = searchQuery.trim().lowercase()
      return posts.filter { post ->
        val classMatches =
          searchClassFilter == "All" || matchesFilterPill(post, searchClassFilter)
        val queryMatches =
          q.isBlank() ||
            post.title.lowercase().contains(q) ||
            post.subjectTag.lowercase().contains(q) ||
            post.categoryLabel.lowercase().contains(q) ||
            post.subLabel.lowercase().contains(q) ||
            post.snippet.lowercase().contains(q) ||
            post.labels.any { it.lowercase().contains(q) }
        classMatches && queryMatches
      }
    }

  private fun matchesFilterPill(post: StudyPost, filter: String): Boolean {
    if (filter.equals("Study Material", ignoreCase = true)) {
      return post.labels.any {
        it.equals("Study Material", ignoreCase = true) ||
          it.contains("Study Material", ignoreCase = true)
      }
    }

    val labelOrTitleMatch =
      post.classTag.equals(filter, ignoreCase = true) ||
        post.subjectTag.equals(filter, ignoreCase = true) ||
        post.materialCategory.equals(filter, ignoreCase = true) ||
        post.labels.any { it.equals(filter, ignoreCase = true) || it.contains(filter, ignoreCase = true) }

    if (labelOrTitleMatch) return true

    return when {
      filter.equals("Notice", ignoreCase = true) || filter.equals("Board Notice", ignoreCase = true) ->
        post.classTag.equals("Notice", ignoreCase = true) ||
          post.classTag.equals("Board Notice", ignoreCase = true) ||
          post.labels.any { it.contains("notice", ignoreCase = true) || it.contains("circular", ignoreCase = true) } ||
          post.title.contains("board notice", ignoreCase = true) ||
          post.title.contains("cbse notice", ignoreCase = true) ||
          post.title.contains("date sheet", ignoreCase = true) ||
          Regex("\\bnotices?\\b", RegexOption.IGNORE_CASE).containsMatchIn(post.title)
      filter.equals("Question Papers", ignoreCase = true) ->
        post.materialCategory.contains("Question Paper", ignoreCase = true) ||
          post.labels.any {
            it.contains("question paper", ignoreCase = true) ||
              it.contains("pyq", ignoreCase = true) ||
              it.contains("sample paper", ignoreCase = true)
          } ||
          post.title.contains("question paper", ignoreCase = true) ||
          post.title.contains("sample paper", ignoreCase = true)
      filter.equals("Current Affairs", ignoreCase = true) ->
        post.title.contains("current affairs", ignoreCase = true)
      else -> false
    }
  }

  val noticePosts: List<StudyPost>
    get() =
      posts.filter { post ->
        post.labels.any { label ->
          label.trim().equals("Notice", ignoreCase = true)
        }
      }

  val savedPosts: List<StudyPost>
    get() = posts.filter { it.id in preferences.savedPostIds }

  fun countForClass(className: String): Int =
    posts.count { post ->
      post.classTag.equals(className, ignoreCase = true) ||
        post.labels.any { it.contains(className, ignoreCase = true) }
    }

  val class10Count: Int
    get() {
      val actual = countForClass("Class 10")
      return if (syncStatus.isLiveConnected && actual > 0) actual else actual.coerceAtLeast(16)
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val studyRepository = StudyRepository(application)
  private val prefsRepository = PreferencesRepository(application)
  private val aiRepository: AiRepository = FirebaseAiRepository()

  private val _posts = MutableStateFlow(BundledContent.fallbackPosts)
  private val _selectedClassFilter = MutableStateFlow("All")
  private val _searchQuery = MutableStateFlow("")
  private val _searchClassFilter = MutableStateFlow("All")
  private val _syncStatus =
    MutableStateFlow(FeedSyncStatus(cachedPostsCount = BundledContent.fallbackPosts.size))
  private val _statusBannerMessage = MutableStateFlow<String?>(null)
  private val _aiPrintUiState = MutableStateFlow<AiPrintUiState>(AiPrintUiState.Idle)
  val aiPrintUiState: StateFlow<AiPrintUiState> = _aiPrintUiState.asStateFlow()
  private var aiPrintJob: Job? = null

  val preferences: StateFlow<UserPreferences> =
    prefsRepository.preferencesFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserPreferences(),
    )

  val uiState: StateFlow<MainUiState> =
    combine(
      _posts,
      _selectedClassFilter,
      _searchQuery,
      _searchClassFilter,
      preferences,
      _syncStatus,
      _statusBannerMessage,
    ) { values ->
      @Suppress("UNCHECKED_CAST")
      val posts = values[0] as List<StudyPost>
      val classFilter = values[1] as String
      val searchQuery = values[2] as String
      val searchClassFilter = values[3] as String
      val prefs = values[4] as UserPreferences
      val sync = values[5] as FeedSyncStatus
      val banner = values[6] as String?

      MainUiState(
        posts = posts,
        selectedClassFilter = classFilter,
        searchQuery = searchQuery,
        searchClassFilter = searchClassFilter,
        preferences = prefs,
        syncStatus = sync.copy(cachedPostsCount = posts.size),
        statusBannerMessage = banner,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = MainUiState(),
    )

  init {
    viewModelScope.launch {
      val cached = studyRepository.loadCachedOrBundledPosts()
      _posts.value = cached
      _syncStatus.update { it.copy(cachedPostsCount = cached.size) }
      syncFeed(silent = true)
    }
  }

  fun selectClassFilter(classLabel: String) {
    _selectedClassFilter.value = classLabel
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun updateSearchClassFilter(classLabel: String) {
    _searchClassFilter.value = classLabel
  }

  fun searchByLabel(label: String) {
    _searchClassFilter.value = "All"
    _searchQuery.value = label
  }

  fun toggleBookmark(postId: String) {
    viewModelScope.launch {
      prefsRepository.toggleBookmark(postId)
      val updatedIds = prefsRepository.preferencesFlow.first().savedPostIds
      val savedSnapshot = _posts.value.filter { it.id in updatedIds }
      studyRepository.persistSavedPostsSnapshot(savedSnapshot)
    }
  }

  fun findPostById(postId: String): StudyPost? = _posts.value.firstOrNull { it.id == postId }

  fun findPostByUrl(url: String): StudyPost? {
    fun normalizeUrl(raw: String): String =
      raw
        .replaceFirst("http://", "https://", ignoreCase = true)
        .substringBefore("?")
        .substringBefore("#")
        .trimEnd('/')
    val cleanTarget = normalizeUrl(url)
    return _posts.value.firstOrNull { post ->
      normalizeUrl(post.postUrl).equals(cleanTarget, ignoreCase = true)
    }
  }

  fun postsForSubject(className: String, subjectName: String, categoryTitle: String? = null): List<StudyPost> {
    val exactSubject =
      _posts.value.filter { post ->
        val classMatch =
          post.classTag.equals(className, ignoreCase = true) ||
            post.labels.any { it.contains(className, ignoreCase = true) }
        val subjectMatch =
          post.subjectTag.equals(subjectName, ignoreCase = true) ||
            post.labels.any { it.contains(subjectName, ignoreCase = true) } ||
            post.title.contains(subjectName, ignoreCase = true)
        classMatch && subjectMatch
      }
    if (categoryTitle.isNullOrBlank() || categoryTitle == "All Posts") {
      return exactSubject
    }
    return exactSubject.filter { post ->
      post.materialCategory.equals(categoryTitle, ignoreCase = true) ||
        post.labels.any { it.contains(categoryTitle, ignoreCase = true) }
    }
  }

  fun syncFeed(silent: Boolean = false) {
    viewModelScope.launch {
      _syncStatus.update { it.copy(isSyncing = true, lastError = null) }
      val autoCache = preferences.value.autoCacheOffline
      val result = studyRepository.syncLiveFeed(autoCacheOffline = autoCache)
      result
        .onSuccess { livePosts ->
          _posts.value = livePosts
          prefsRepository.updateLastSync(System.currentTimeMillis())
          _syncStatus.update {
            it.copy(
              isSyncing = false,
              isLiveConnected = true,
              cachedPostsCount = livePosts.size,
              lastError = null,
            )
          }
          if (!silent) {
            _statusBannerMessage.value = "Synced ${livePosts.size} live posts from gyanpetika.blogspot.com."
          }
        }
        .onFailure { err ->
          _syncStatus.update {
            it.copy(
              isSyncing = false,
              isLiveConnected = false,
              lastError = err.message,
            )
          }
          if (!silent) {
            _statusBannerMessage.value = "Offline mode active — showing cached study notes."
          }
        }
    }
  }

  fun rebuildCache() {
    viewModelScope.launch {
      _syncStatus.update { it.copy(isSyncing = true, lastError = null) }
      val result = studyRepository.rebuildCache()
      result
        .onSuccess { livePosts ->
          _posts.value = livePosts
          prefsRepository.updateLastSync(System.currentTimeMillis())
          _syncStatus.update {
            it.copy(
              isSyncing = false,
              isLiveConnected = true,
              cachedPostsCount = livePosts.size,
              lastError = null,
            )
          }
          _statusBannerMessage.value = "Cache rebuilt with ${livePosts.size} live posts."
        }
        .onFailure {
          val fallback = studyRepository.loadCachedOrBundledPosts()
          _posts.value = fallback
          _syncStatus.update { it.copy(isSyncing = false, isLiveConnected = false) }
          _statusBannerMessage.value = "Unable to reach live blog. Kept offline study notes."
        }
    }
  }

  fun dismissStatusMessage() {
    _statusBannerMessage.value = null
  }

  fun setThemeMode(mode: AppThemeMode) {
    viewModelScope.launch { prefsRepository.setThemeMode(mode) }
  }

  fun setListDensity(density: ListDensity) {
    viewModelScope.launch { prefsRepository.setListDensity(density) }
  }

  fun setTextSizePreset(preset: TextSizePreset) {
    viewModelScope.launch { prefsRepository.setTextSizePreset(preset) }
  }

  fun stepReaderFontSize(deltaSp: Int) {
    viewModelScope.launch { prefsRepository.stepFontSize(deltaSp) }
  }

  fun setEyeComfortPaperMode(enabled: Boolean) {
    viewModelScope.launch { prefsRepository.setEyeComfortPaperMode(enabled) }
  }

  fun setAutoCacheOffline(enabled: Boolean) {
    viewModelScope.launch { prefsRepository.setAutoCacheOffline(enabled) }
  }

  fun requestSmartPrintLayout(
    postTitle: String,
    rawHtml: String,
    onPlanReady: (PrintTopicLayoutPlan) -> Unit,
  ) {
    aiPrintJob?.cancel()
    aiPrintJob =
      viewModelScope.launch {
        _aiPrintUiState.value = AiPrintUiState.Loading
        try {
          val plan =
            withTimeout(60_000L) {
              aiRepository.planSmartPrintLayout(postTitle = postTitle, rawHtml = rawHtml)
            }
          _aiPrintUiState.value = AiPrintUiState.Success(plan)
          onPlanReady(plan)
        } catch (e: TimeoutCancellationException) {
          Log.e(TAG, "Gemini AI print layout timed out", e)
          _aiPrintUiState.value =
            AiPrintUiState.Error(
              "Gemini AI print optimization timed out after 60 seconds. Please try again."
            )
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          Log.e(TAG, "Gemini AI print layout failed", e)
          _aiPrintUiState.value =
            AiPrintUiState.Error(
              e.localizedMessage ?: "Failed to optimize print layout with Gemini AI."
            )
        }
      }
  }

  fun clearAiPrintState() {
    aiPrintJob?.cancel()
    aiPrintJob = null
    _aiPrintUiState.value = AiPrintUiState.Idle
  }

  fun resetDefaults() {
    viewModelScope.launch {
      prefsRepository.resetDefaults()
      _statusBannerMessage.value = "Preferences reset to defaults."
    }
  }
}
