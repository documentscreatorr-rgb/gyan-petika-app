package com.example.data

import androidx.core.text.HtmlCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.json.JSONObject

data class ParsedBloggerFeed(
  val posts: List<StudyPost>,
  val totalResults: Int,
)

object BloggerFeedParser {

  private val isoFormats =
    listOf(
      "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
      "yyyy-MM-dd'T'HH:mm:ssXXX",
      "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      "yyyy-MM-dd'T'HH:mm:ssZ",
    )

  fun parseFeedWithMetadata(rawJson: String): ParsedBloggerFeed {
    val root = JSONObject(rawJson)
    val feed = root.optJSONObject("feed") ?: return ParsedBloggerFeed(emptyList(), 0)
    val totalResults =
      feed.optJSONObject("openSearch\$totalResults")?.optString("\$t")?.toIntOrNull() ?: 0
    val entries = feed.optJSONArray("entry") ?: return ParsedBloggerFeed(emptyList(), totalResults)

    val posts = ArrayList<StudyPost>(entries.length())
    for (i in 0 until entries.length()) {
      val entry = entries.optJSONObject(i) ?: continue
      val post = parseEntry(entry)
      if (post != null) {
        posts.add(post)
      }
    }
    return ParsedBloggerFeed(posts = posts, totalResults = totalResults.coerceAtLeast(posts.size))
  }

  fun parseFeedJson(rawJson: String): List<StudyPost> = parseFeedWithMetadata(rawJson).posts

  private fun parseEntry(entry: JSONObject): StudyPost? {
    val rawTitle = entry.optJSONObject("title")?.optString("\$t").orEmpty()
    val title = cleanText(rawTitle).ifBlank { return null }

    val id =
      entry.optJSONObject("id")?.optString("\$t")?.ifBlank { title.hashCode().toString() }
        ?: title.hashCode().toString()

    val htmlContent =
      entry.optJSONObject("content")?.optString("\$t")?.takeIf { it.isNotBlank() }
        ?: entry.optJSONObject("summary")?.optString("\$t").orEmpty()

    val publishedRaw = entry.optJSONObject("published")?.optString("\$t").orEmpty()
    val (shortDate, longDate) = formatPublishedDate(publishedRaw)

    val authorName =
      entry
        .optJSONArray("author")
        ?.optJSONObject(0)
        ?.optJSONObject("name")
        ?.optString("\$t")
        ?.trim()
        ?.takeIf { it.isNotBlank() } ?: "GYAN PETIKA"

    val commentCount =
      entry.optJSONObject("thr\$total")?.optString("\$t")?.toIntOrNull() ?: 0

    var alternateUrl = BundledContent.BLOG_BASE_URL
    val links = entry.optJSONArray("link")
    if (links != null) {
      for (j in 0 until links.length()) {
        val linkObj = links.optJSONObject(j) ?: continue
        if (linkObj.optString("rel") == "alternate") {
          val href = linkObj.optString("href")
          if (href.isNotBlank()) {
            alternateUrl = href
            break
          }
        }
      }
    }

    val mediaThumb =
      entry
        .optJSONObject("media\$thumbnail")
        ?.optString("url")
        ?.takeIf { it.startsWith("http") }
        ?.replace("/s72-c/", "/s400/")
    val firstImgThumb =
      if (mediaThumb == null) {
        Regex("(?is)<img[^>]+src=[\"']([^\"']+)[\"']")
          .find(htmlContent)
          ?.groupValues
          ?.getOrNull(1)
          ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
      } else {
        null
      }
    val thumbnailUrl = mediaThumb ?: firstImgThumb

    val labels = mutableListOf<String>()
    val categories = entry.optJSONArray("category")
    if (categories != null) {
      for (j in 0 until categories.length()) {
        val term = categories.optJSONObject(j)?.optString("term").orEmpty().trim()
        if (term.isNotBlank()) labels.add(term)
      }
    }

    val classTag = detectClassTag(labels, title)
    val subjectTag = detectSubjectTag(labels, title)
    val materialCategory = detectMaterialCategory(labels, title)
    val subLabel = detectSubLabel(labels, title, materialCategory)
    val categoryLabel = buildCategoryLabel(classTag, subjectTag)

    val plainText = cleanText(stripHtml(htmlContent))
    val snippet =
      if (plainText.length > 180) {
        plainText.take(177).trimEnd() + "…"
      } else {
        plainText.ifBlank { "$classTag • $subjectTag study notes and revision material." }
      }

    val wordCount = plainText.split(Regex("\\s+")).count { it.isNotBlank() }
    val minutes = (wordCount / 180).coerceAtLeast(3)
    val readTime = "$minutes min read"

    return StudyPost(
      id = id,
      title = title,
      classTag = classTag,
      subjectTag = subjectTag,
      categoryLabel = categoryLabel,
      subLabel = subLabel,
      materialCategory = materialCategory,
      publishedDate = shortDate,
      fullPublishedDate = longDate,
      estimatedReadTime = readTime,
      snippet = snippet,
      htmlContent = htmlContent.ifBlank { "<p>$snippet</p>" },
      postUrl = alternateUrl,
      labels = labels,
      authorName = authorName,
      thumbnailUrl = thumbnailUrl,
      commentCount = commentCount,
      banner = null,
      isBundled = false,
    )
  }

  fun cleanText(input: String): String {
    if (input.isBlank()) return ""
    val decoded = HtmlCompat.fromHtml(input, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    return decoded
      .replace('\u00A0', ' ')
      .replace(Regex("\\s+"), " ")
      .trim()
  }

  private fun stripHtml(html: String): String {
    if (html.isBlank()) return ""
    val noScripts =
      html
        .replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
        .replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
    return HtmlCompat.fromHtml(noScripts, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
  }

  private fun formatPublishedDate(raw: String): Pair<String, String> {
    for (pattern in isoFormats) {
      try {
        val parser = SimpleDateFormat(pattern, Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(raw) ?: continue
        val shortFmt = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val longFmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return shortFmt.format(date) to longFmt.format(date)
      } catch (_: Exception) {}
    }
    return "Nov 13, 2026" to "Friday, November 13, 2026"
  }

  private fun detectClassTag(labels: List<String>, title: String): String {
    val combined = (labels.joinToString(" ") + " " + title).lowercase(Locale.US)
    return when {
      Regex("\\bclass\\s*10\\b|\\b10th\\b|\\b402\\b|employability\\s*skills\\s*-\\s*ii").containsMatchIn(combined) -> "Class 10"
      Regex("\\bclass\\s*9\\b|\\b9th\\b").containsMatchIn(combined) -> "Class 9"
      Regex("\\bclass\\s*11\\b|\\b11th\\b").containsMatchIn(combined) -> "Class 11"
      Regex("\\bclass\\s*12\\b|\\b12th\\b").containsMatchIn(combined) -> "Class 12"
      combined.contains("current affairs") -> "Current Affairs"
      combined.contains("question paper") -> "Question Papers"
      else -> "Study Material"
    }
  }

  private fun detectSubjectTag(labels: List<String>, title: String): String {
    val combined = (labels.joinToString(" ") + " " + title).lowercase(Locale.US)
    val orderedSubjects =
      listOf(
        "Information Technology" to listOf("information technology", "it (402)", "402", "employability", "digital documentation", "spreadsheet", "database management", "entrepreneurship", "health, safety"),
        "Mathematics" to listOf("mathematics", "maths", "math"),
        "Science" to listOf("science"),
        "Social Science" to listOf("social science", "sst", "civics"),
        "English" to listOf("english"),
        "Hindi" to listOf("hindi"),
        "Physics" to listOf("physics"),
        "Chemistry" to listOf("chemistry"),
        "Biology" to listOf("biology"),
        "Accountancy" to listOf("accountancy"),
        "Business Studies" to listOf("business studies"),
        "Economics" to listOf("economics"),
        "History" to listOf("history"),
        "Political Science" to listOf("political science"),
        "Geography" to listOf("geography"),
        "Psychology" to listOf("psychology"),
        "Sociology" to listOf("sociology"),
        "Current Affairs" to listOf("current affairs"),
      )
    for ((subject, keywords) in orderedSubjects) {
      if (keywords.any { combined.contains(it) }) {
        return subject
      }
    }
    return labels.firstOrNull() ?: "Study Material"
  }

  private fun detectMaterialCategory(labels: List<String>, title: String): String {
    val combined = (labels.joinToString(" ") + " " + title).lowercase(Locale.US)
    return when {
      combined.contains("mcq") -> "MCQs Practice"
      combined.contains("previous year") || combined.contains("question paper") || combined.contains("pyq") ->
        "Previous Year Question Papers"
      combined.contains("ncert") -> "NCERT Book and Solutions"
      combined.contains("important question") -> "Important Questions"
      else -> "Chapter-wise Notes"
    }
  }

  private fun detectSubLabel(labels: List<String>, title: String, fallback: String): String {
    val unitMatch = Regex("(Part\\s*[AB]\\s*[-—:]*\\s*Unit\\s*\\d+|Unit\\s*\\d+|Session\\s*[:\\s]*\\d+)", RegexOption.IGNORE_CASE)
      .find(title)
    if (unitMatch != null) return unitMatch.value.replace(Regex("\\s+"), " ").trim()
    val conciseLabel = labels.firstOrNull { !it.startsWith("Class ", ignoreCase = true) }
    return conciseLabel ?: fallback
  }

  private fun buildCategoryLabel(classTag: String, subjectTag: String): String {
    return if (classTag == "Class 10" && subjectTag == "Information Technology") {
      "CBSE Class 10 IT (402)"
    } else if (classTag == subjectTag) {
      "GYAN PETIKA • $subjectTag"
    } else {
      "CBSE $classTag • $subjectTag"
    }
  }
}
