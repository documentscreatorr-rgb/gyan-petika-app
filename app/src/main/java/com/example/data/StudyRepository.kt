package com.example.data

import android.content.Context
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class StudyRepository(private val context: Context) {

  private val httpClient =
    OkHttpClient.Builder()
      .connectTimeout(12, TimeUnit.SECONDS)
      .readTimeout(20, TimeUnit.SECONDS)
      .build()

  private val feedCacheFile: File
    get() = File(context.filesDir, "blogger_feed_cache.json")

  private val savedPostsCacheFile: File
    get() = File(context.filesDir, "saved_posts_cache.json")

  suspend fun loadCachedOrBundledPosts(): List<StudyPost> =
    withContext(Dispatchers.IO) {
      val cached =
        runCatching {
          if (feedCacheFile.exists()) {
            parseCachedFile(feedCacheFile.readText())
          } else {
            emptyList()
          }
        }.getOrDefault(emptyList())

      val saved = loadSavedPostsSnapshot()
      if (cached.isNotEmpty()) {
        mergePosts(liveOrCached = cached, savedSnapshot = saved, bundled = emptyList())
      } else {
        mergePosts(liveOrCached = emptyList(), savedSnapshot = saved, bundled = BundledContent.fallbackPosts)
      }
    }

  suspend fun syncLiveFeed(autoCacheOffline: Boolean): Result<List<StudyPost>> =
    withContext(Dispatchers.IO) {
      runCatching {
        val allLivePosts = mutableListOf<StudyPost>()
        val rawPages = mutableListOf<String>()

        val firstRaw = fetchUrl(BundledContent.feedPageUrl(startIndex = 1, maxResults = 150))
        val firstParsed = BloggerFeedParser.parseFeedWithMetadata(firstRaw)
        allLivePosts.addAll(firstParsed.posts)
        rawPages.add(firstRaw)

        // Fetch additional pages if the blog has more than 150 posts
        var nextStart = allLivePosts.size + 1
        var guard = 0
        while (firstParsed.totalResults > allLivePosts.size && firstParsed.posts.isNotEmpty() && guard < 4) {
          guard++
          val pageRaw = fetchUrl(BundledContent.feedPageUrl(startIndex = nextStart, maxResults = 150))
          val pageParsed = BloggerFeedParser.parseFeedWithMetadata(pageRaw)
          if (pageParsed.posts.isEmpty()) break
          allLivePosts.addAll(pageParsed.posts)
          rawPages.add(pageRaw)
          nextStart = allLivePosts.size + 1
        }

        if (allLivePosts.isEmpty()) {
          error("No posts found on gyanpetika.blogspot.com")
        }

        if (autoCacheOffline) {
          val pagesJson = JSONArray(rawPages)
          feedCacheFile.writeText(pagesJson.toString())
        }

        val saved = loadSavedPostsSnapshot()
        mergePosts(liveOrCached = allLivePosts, savedSnapshot = saved, bundled = emptyList())
      }
    }

  private fun fetchUrl(url: String): String {
    val request =
      Request.Builder()
        .url(url)
        .header("Accept", "application/json")
        .header("User-Agent", "GyanPetikaAndroid/1.0")
        .build()

    val body =
      httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          error("Feed returned HTTP ${response.code}")
        }
        response.body?.string().orEmpty()
      }

    if (body.isBlank()) {
      error("Empty response from Blogger feed")
    }
    return body
  }

  private fun parseCachedFile(content: String): List<StudyPost> {
    val trimmed = content.trim()
    return if (trimmed.startsWith("[")) {
      val array = JSONArray(trimmed)
      buildList {
        for (i in 0 until array.length()) {
          addAll(BloggerFeedParser.parseFeedJson(array.getString(i)))
        }
      }
    } else {
      BloggerFeedParser.parseFeedJson(trimmed)
    }
  }

  suspend fun rebuildCache(): Result<List<StudyPost>> =
    withContext(Dispatchers.IO) {
      runCatching {
        if (feedCacheFile.exists()) {
          feedCacheFile.delete()
        }
      }
      syncLiveFeed(autoCacheOffline = true)
    }

  suspend fun persistSavedPostsSnapshot(savedPosts: List<StudyPost>) =
    withContext(Dispatchers.IO) {
      runCatching {
        val array = JSONArray()
        savedPosts.forEach { post ->
          val obj =
            JSONObject().apply {
              put("id", post.id)
              put("title", post.title)
              put("classTag", post.classTag)
              put("subjectTag", post.subjectTag)
              put("categoryLabel", post.categoryLabel)
              put("subLabel", post.subLabel)
              put("materialCategory", post.materialCategory)
              put("publishedDate", post.publishedDate)
              put("fullPublishedDate", post.fullPublishedDate)
              put("estimatedReadTime", post.estimatedReadTime)
              put("snippet", post.snippet)
              put("htmlContent", post.htmlContent)
              put("postUrl", post.postUrl)
              put("labels", JSONArray(post.labels))
              put("authorName", post.authorName)
              put("thumbnailUrl", post.thumbnailUrl ?: "")
              put("commentCount", post.commentCount)
              put("isBundled", post.isBundled)
            }
          array.put(obj)
        }
        savedPostsCacheFile.writeText(array.toString())
      }
    }

  private fun loadSavedPostsSnapshot(): List<StudyPost> {
    if (!savedPostsCacheFile.exists()) return emptyList()
    return runCatching {
      val array = JSONArray(savedPostsCacheFile.readText())
      buildList {
        for (i in 0 until array.length()) {
          val obj = array.optJSONObject(i) ?: continue
          val labelsArray = obj.optJSONArray("labels")
          val labels =
            if (labelsArray != null) {
              List(labelsArray.length()) { idx -> labelsArray.optString(idx) }
            } else {
              emptyList()
            }
          add(
            StudyPost(
              id = obj.optString("id"),
              title = obj.optString("title"),
              classTag = obj.optString("classTag"),
              subjectTag = obj.optString("subjectTag"),
              categoryLabel = obj.optString("categoryLabel"),
              subLabel = obj.optString("subLabel"),
              materialCategory = obj.optString("materialCategory"),
              publishedDate = obj.optString("publishedDate"),
              fullPublishedDate = obj.optString("fullPublishedDate"),
              estimatedReadTime = obj.optString("estimatedReadTime"),
              snippet = obj.optString("snippet"),
              htmlContent = obj.optString("htmlContent"),
              postUrl = obj.optString("postUrl"),
              labels = labels,
              authorName = obj.optString("authorName").ifBlank { "GYAN PETIKA" },
              thumbnailUrl = obj.optString("thumbnailUrl").ifBlank { null },
              commentCount = obj.optInt("commentCount", 0),
              banner = null,
              isBundled = obj.optBoolean("isBundled", false),
            )
          )
        }
      }
    }.getOrDefault(emptyList())
  }

  private fun mergePosts(
    liveOrCached: List<StudyPost>,
    savedSnapshot: List<StudyPost>,
    bundled: List<StudyPost>,
  ): List<StudyPost> {
    val result = mutableListOf<StudyPost>()
    val seenIds = mutableSetOf<String>()
    val seenTitles = mutableSetOf<String>()

    fun normalizeTitle(t: String): String =
      t.lowercase().replace(Regex("[^a-z0-9]"), "")

    fun addIfNew(post: StudyPost) {
      val normTitle = normalizeTitle(post.title)
      if (post.id !in seenIds && normTitle !in seenTitles) {
        seenIds.add(post.id)
        seenTitles.add(normTitle)
        result.add(post)
      }
    }

    liveOrCached.forEach(::addIfNew)
    savedSnapshot.forEach(::addIfNew)
    bundled.forEach(::addIfNew)
    return result
  }
}
