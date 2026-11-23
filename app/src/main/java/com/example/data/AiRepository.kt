package com.example.data

import androidx.core.text.HtmlCompat
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

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
  val isPlaceholder: Boolean = false,
  val firebaseNotice: String? = null,
)

/**
 * Single AI boundary for cloud generative features.
 */
interface AiRepository {
  suspend fun planSmartPrintLayout(
    postTitle: String,
    rawHtml: String,
  ): PrintTopicLayoutPlan
}

/**
 * Production implementation powered by Firebase AI Logic (`Firebase.ai`) using Gemini 3.8 Flash
 * with strict structured JSON output via [Schema.obj].
 */
class FirebaseAiRepository : AiRepository {

  private val printLayoutSchema =
    Schema.obj(
      properties =
        mapOf(
          "groupedTopicCount" to
            Schema.integer(
              description = "Total number of distinct topics or sections detected in the study post."
            ),
          "keepSectionsTogether" to
            Schema.boolean(
              description =
                "True when each topic heading and its notes, tables, or MCQs should stay together without splitting across print pages."
            ),
          "preventOrphanHeadings" to
            Schema.boolean(
              description =
                "True when headings must stay attached to their opening content block so no heading sits alone at the bottom of a page."
            ),
          "maxUnbrokenBlockHeightPx" to
            Schema.integer(
              description =
                "Recommended max height in CSS pixels (between 520 and 980) for keeping an individual topic group unbroken on a single A4 print page."
            ),
          "topicTitles" to
            Schema.array(
              items = Schema.string(),
              description =
                "Exact visible topic or subheading text lines from the post that begin each logical topic group."
            ),
          "summaryMessage" to
            Schema.string(
              description =
                "Concise one-sentence summary explaining how Gemini organized the post's topics for printing."
            ),
        )
    )

  private val model by lazy {
    Firebase.ai.generativeModel(
      modelName = MODEL_NAME,
      generationConfig =
        generationConfig {
          responseMimeType = "application/json"
          responseSchema = printLayoutSchema
        },
    )
  }

  override suspend fun planSmartPrintLayout(
    postTitle: String,
    rawHtml: String,
  ): PrintTopicLayoutPlan {
    val compactHtml = prepareCompactStructureForPrompt(rawHtml)
    val prompt =
      """
      You are a print pagination layout assistant for an Indian CBSE student study notes app (GYAN PETIKA).
      Analyze the study post HTML below and identify every logical topic/section boundary so that when printed to paper or PDF, related content on the same topic (heading + explanation + bullets + table + MCQ question/options/answer) stays together cleanly without awkward mid-topic page breaks or orphaned headings.

      Rules:
      - Return exact visible topic titles from the post in `topicTitles` (up to 25 topics), including plain bold or styled <div>/<p> topic headings that do not use <h1>-<h4> tags.
      - Choose `maxUnbrokenBlockHeightPx` between 520 and 980 based on how dense the topics are (e.g., 720-880 for medium study sections, 600-740 for long MCQ/table sections).
      - Set `keepSectionsTogether` to true and `preventOrphanHeadings` to true.
      - Keep `summaryMessage` concise (1 sentence, plain English) mentioning how many topic sections were grouped.

      Post Title: ${postTitle.ifBlank { "Study Notes" }}

      Post HTML Excerpt:
      $compactHtml
      """.trimIndent()

    val response = withTimeout(INFERENCE_TIMEOUT_MS) { model.generateContent(prompt) }
    val jsonString =
      response.text?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("Gemini returned an empty print layout response.")

    // Direct JSON parsing guaranteed by responseMimeType = "application/json" and responseSchema.
    val json = JSONObject(jsonString)
    val topicTitlesArray = json.optJSONArray("topicTitles")
    val topicTitles =
      buildList {
        if (topicTitlesArray != null) {
          for (i in 0 until topicTitlesArray.length()) {
            val item = topicTitlesArray.optString(i, "").trim()
            if (item.length >= 2) add(item)
          }
        }
      }.distinct().take(25)

    val rawCount = json.optInt("groupedTopicCount", topicTitles.size.coerceAtLeast(1))
    val groupedTopicCount = maxOf(rawCount, topicTitles.size, 1)
    val maxHeightPx = json.optInt("maxUnbrokenBlockHeightPx", 820).coerceIn(520, 980)
    val keepTogether = json.optBoolean("keepSectionsTogether", true)
    val preventOrphans = json.optBoolean("preventOrphanHeadings", true)
    val summary =
      json.optString("summaryMessage", "").trim().ifBlank {
        "Adjusted print layout to keep $groupedTopicCount topic section${if (groupedTopicCount == 1) "" else "s"} together cleanly."
      }

    return PrintTopicLayoutPlan(
      groupedTopicCount = groupedTopicCount,
      keepSectionsTogether = keepTogether,
      preventOrphanHeadings = preventOrphans,
      maxUnbrokenBlockHeightPx = maxHeightPx,
      topicTitles = topicTitles,
      summaryMessage = summary,
      isPlaceholder = false,
      firebaseNotice = null,
    )
  }

  private fun prepareCompactStructureForPrompt(rawHtml: String): String {
    if (rawHtml.isBlank()) return ""
    val stripped =
      rawHtml
        .replace(SCRIPT_OR_STYLE_REGEX, " ")
        .replace(HTML_COMMENT_REGEX, " ")
        .replace(INLINE_STYLE_ATTR_REGEX, "")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
    return if (stripped.length <= MAX_PROMPT_HTML_CHARS) {
      stripped
    } else {
      stripped.take(MAX_PROMPT_HTML_CHARS)
    }
  }

  private companion object {
    const val MODEL_NAME = "gemini-3.8-flash"
    const val INFERENCE_TIMEOUT_MS = 60_000L
    const val MAX_PROMPT_HTML_CHARS = 12_000
    val SCRIPT_OR_STYLE_REGEX = Regex("(?is)<(?:script|style|svg|noscript)\\b[^>]*>.*?</(?:script|style|svg|noscript)>")
    val HTML_COMMENT_REGEX = Regex("(?is)<!--.*?-->")
    val INLINE_STYLE_ATTR_REGEX = Regex("(?i)\\sstyle\\s*=\\s*(?:\"[^\"]*\"|'[^']*')")
    val WHITESPACE_REGEX = Regex("\\s+")
  }
}

/**
 * Deterministic test/offline stub kept for unit tests.
 * Production code uses [FirebaseAiRepository].
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
