package com.example.data

import androidx.core.text.HtmlCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Print pagination plan describing how a post's topic sections should be grouped
 * so related headings, notes, tables, and MCQs stay together across print page breaks.
 */
data class PrintTopicLayoutPlan(
  val groupedTopicCount: Int,
  val keepSectionsTogether: Boolean,
  val preventOrphanHeadings: Boolean,
  val maxUnbrokenBlockHeightPx: Int,
  val topicTitles: List<String>,
  val summaryMessage: String,
  val isPlaceholder: Boolean,
  val firebaseNotice: String? = null,
)

/**
 * Single AI boundary for cloud generative features.
 * Backed by [PlaceholderAiRepository] when Firebase AI Logic is not enabled.
 */
interface AiRepository {
  suspend fun planSmartPrintLayout(
    postTitle: String,
    rawHtml: String,
  ): PrintTopicLayoutPlan
}

/**
 * Placeholder implementation used when Firebase setup was declined.
 * Returns clearly labeled sample AI print-layout guidance while applying deterministic
 * topic-grouping rules so headings and related content stay together during printing.
 */
class PlaceholderAiRepository : AiRepository {

  override suspend fun planSmartPrintLayout(
    postTitle: String,
    rawHtml: String,
  ): PrintTopicLayoutPlan =
    withContext(Dispatchers.Default) {
      val detectedTopics = extractTopicHeadings(rawHtml)
      val sectionTagCount = SECTION_BLOCK_REGEX.findAll(rawHtml).count()
      val groupedCount = maxOf(detectedTopics.size, sectionTagCount, 1)

      val sampleTopics =
        if (detectedTopics.isNotEmpty()) {
          detectedTopics.take(6)
        } else {
          listOf(postTitle.ifBlank { "Post Content" })
        }

      PrintTopicLayoutPlan(
        groupedTopicCount = groupedCount,
        keepSectionsTogether = true,
        preventOrphanHeadings = true,
        maxUnbrokenBlockHeightPx = 860,
        topicTitles = sampleTopics,
        summaryMessage =
          "Sample AI print layout ready: keeping $groupedCount topic section${if (groupedCount == 1) "" else "s"} together on print pages.",
        isPlaceholder = true,
        firebaseNotice =
          "AI features require Firebase. Using sample placeholder layout adjustment until Firebase AI Logic is enabled.",
      )
    }

  private fun extractTopicHeadings(html: String): List<String> {
    if (html.isBlank()) return emptyList()
    return HEADING_TAG_REGEX
      .findAll(html)
      .map { match ->
        val rawInner = match.groupValues.getOrNull(1).orEmpty()
        HtmlCompat.fromHtml(rawInner, HtmlCompat.FROM_HTML_MODE_LEGACY)
          .toString()
          .replace('\u00A0', ' ')
          .replace(Regex("\\s+"), " ")
          .trim()
      }
      .filter { it.length >= 2 }
      .distinct()
      .toList()
  }

  private companion object {
    val HEADING_TAG_REGEX = Regex("(?is)<h[1-4]\\b[^>]*>(.*?)</h[1-4]>")
    val SECTION_BLOCK_REGEX =
      Regex("(?is)<(?:section|article)\\b|class=[\"'][^\"']*(?:gp-section|topic|card|q-card|question)[^\"']*[\"']")
  }
}
