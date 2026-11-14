package com.example.data

import com.example.ui.theme.AppThemeMode

enum class ListDensity(val label: String) {
  COMFORTABLE("Comfortable"),
  COMPACT("Compact"),
}

enum class TextSizePreset(val label: String, val sizeSp: Int) {
  SMALL("Small", 13),
  NORMAL("Normal", 15),
  LARGE("Large", 17),
  XL("XL", 19),
}

enum class SubjectAccent {
  BLUE,
  GREEN,
  RED,
  ORANGE,
  PURPLE,
  TEAL,
}

data class ArticleBanner(
  val eyebrow: String,
  val headline: String,
  val subheadline: String,
  val summary: String,
)

data class StudyPost(
  val id: String,
  val title: String,
  val classTag: String,
  val subjectTag: String,
  val categoryLabel: String,
  val subLabel: String,
  val materialCategory: String,
  val publishedDate: String,
  val fullPublishedDate: String,
  val estimatedReadTime: String,
  val snippet: String,
  val htmlContent: String,
  val postUrl: String,
  val labels: List<String>,
  val authorName: String = "GYAN PETIKA",
  val thumbnailUrl: String? = null,
  val commentCount: Int = 0,
  val banner: ArticleBanner? = null,
  val isBundled: Boolean = false,
)

data class ClassHubInfo(
  val id: String,
  val className: String,
  val defaultBadge: String,
  val subtitle: String,
)

data class SubjectItem(
  val id: String,
  val name: String,
  val accent: SubjectAccent,
)

data class StudyMaterialCategory(
  val id: String,
  val title: String,
)

data class UserPreferences(
  val themeMode: AppThemeMode = AppThemeMode.LIGHT,
  val listDensity: ListDensity = ListDensity.COMFORTABLE,
  val textSizePreset: TextSizePreset = TextSizePreset.NORMAL,
  val customFontSizeSp: Int = 15,
  val eyeComfortPaperMode: Boolean = false,
  val autoCacheOffline: Boolean = true,
  val savedPostIds: Set<String> = emptySet(),
  val lastSyncEpochMs: Long = 0L,
)

data class FeedSyncStatus(
  val isSyncing: Boolean = false,
  val isLiveConnected: Boolean = false,
  val cachedPostsCount: Int = 0,
  val lastError: String? = null,
)
