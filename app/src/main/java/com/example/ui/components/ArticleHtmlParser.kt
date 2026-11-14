package com.example.ui.components

import androidx.core.text.HtmlCompat
import com.example.data.ArticleBanner
import com.example.data.StudyPost

sealed interface ArticleBlock {
  data class HeroBannerBlock(val banner: ArticleBanner) : ArticleBlock

  data class SectionHeaderBlock(val number: String, val heading: String) : ArticleBlock

  data class HeadingBlock(val level: Int, val text: String) : ArticleBlock

  data class ParagraphBlock(val text: String) : ArticleBlock

  data class BulletListBlock(val items: List<String>) : ArticleBlock

  data class McqQuestionBlock(val text: String) : ArticleBlock

  data class McqAnswerBlock(val text: String) : ArticleBlock

  data class ImageBlock(val url: String, val alt: String) : ArticleBlock

  data class TableBlock(val rows: List<List<String>>) : ArticleBlock
}

object ArticleHtmlParser {

  fun parse(post: StudyPost): List<ArticleBlock> {
    val blocks = mutableListOf<ArticleBlock>()
    if (post.banner != null) {
      blocks.add(ArticleBlock.HeroBannerBlock(post.banner))
    }

    val html =
      post.htmlContent
        .replace(Regex("(?is)<style[^>]*>.*?</style>"), "")
        .replace(Regex("(?is)<script[^>]*>.*?</script>"), "")
        .trim()

    if (html.isBlank()) {
      blocks.add(ArticleBlock.ParagraphBlock(post.snippet))
      return blocks
    }

    // Extract section blocks if structured with gp-section, otherwise parse sequentially
    val sectionRegex =
      Regex("(?is)<div[^>]*class=[\"'][^\"']*gp-section[^\"']*[\"'][^>]*data-num=[\"']([^\"']+)[\"'][^>]*>(.*?)</div>")
    val sectionMatches = sectionRegex.findAll(html).toList()

    if (sectionMatches.isNotEmpty()) {
      for (match in sectionMatches) {
        val secNum = match.groupValues[1].trim()
        val inner = match.groupValues[2]
        val h2Match = Regex("(?is)<h[1-4][^>]*>(.*?)</h[1-4]>").find(inner)
        if (h2Match != null) {
          val headingText = cleanHtmlText(h2Match.groupValues[1])
          if (headingText.isNotBlank()) {
            blocks.add(ArticleBlock.SectionHeaderBlock(secNum, headingText))
          }
          val remainder = inner.replaceFirst(h2Match.value, "")
          parseHtmlFragment(remainder, blocks, autoNumberSections = false)
        } else {
          parseHtmlFragment(inner, blocks, autoNumberSections = false)
        }
      }
    } else {
      parseHtmlFragment(html, blocks, autoNumberSections = true)
    }

    if (blocks.isEmpty() || (blocks.size == 1 && blocks[0] is ArticleBlock.HeroBannerBlock)) {
      blocks.add(ArticleBlock.ParagraphBlock(post.snippet))
    }

    return blocks
  }

  private fun parseHtmlFragment(
    fragment: String,
    blocks: MutableList<ArticleBlock>,
    autoNumberSections: Boolean,
  ) {
    var sectionCounter = 1
    // Tokenize top-level meaningful blocks: h1-h4, ul/ol, table, img, p/div/blockquote/li
    val tokenRegex =
      Regex(
        "(?is)(<h([1-4])[^>]*>.*?</h\\2>|<ul[^>]*>.*?</ul>|<ol[^>]*>.*?</ol>|<table[^>]*>.*?</table>|<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>|<p[^>]*>.*?</p>|<blockquote[^>]*>.*?</blockquote>)"
      )

    var lastIndex = 0
    val matches = tokenRegex.findAll(fragment).toList()

    if (matches.isEmpty()) {
      val lines = cleanHtmlLines(fragment)
      lines.forEach { line -> classifyLine(line, blocks) }
      return
    }

    for (m in matches) {
      if (m.range.first > lastIndex) {
        val gap = fragment.substring(lastIndex, m.range.first)
        cleanHtmlLines(gap).forEach { line -> classifyLine(line, blocks) }
      }
      lastIndex = m.range.last + 1

      val raw = m.value
      when {
        raw.startsWith("<h", ignoreCase = true) -> {
          val level = m.groupValues[2].toIntOrNull() ?: 2
          val text = cleanHtmlText(raw)
          if (text.isNotBlank()) {
            if (autoNumberSections && level <= 2 && sectionCounter <= 20) {
              val numMatch = Regex("^(\\d{1,2})[.:)\\s-]+(.+)$").find(text)
              if (numMatch != null) {
                val n = numMatch.groupValues[1].padStart(2, '0')
                blocks.add(ArticleBlock.SectionHeaderBlock(n, numMatch.groupValues[2].trim()))
              } else {
                blocks.add(
                  ArticleBlock.SectionHeaderBlock(
                    sectionCounter.toString().padStart(2, '0'),
                    text,
                  )
                )
              }
              sectionCounter++
            } else {
              blocks.add(ArticleBlock.HeadingBlock(level, text))
            }
          }
        }

        raw.startsWith("<ul", ignoreCase = true) || raw.startsWith("<ol", ignoreCase = true) -> {
          val liRegex = Regex("(?is)<li[^>]*>(.*?)</li>")
          val items =
            liRegex.findAll(raw).map { cleanHtmlText(it.groupValues[1]) }.filter { it.isNotBlank() }.toList()
          if (items.isNotEmpty()) {
            blocks.add(ArticleBlock.BulletListBlock(items))
          }
        }

        raw.startsWith("<table", ignoreCase = true) -> {
          val rows = parseTable(raw)
          if (rows.isNotEmpty()) {
            blocks.add(ArticleBlock.TableBlock(rows))
          }
        }

        raw.startsWith("<img", ignoreCase = true) -> {
          val src = m.groupValues[3].trim()
          if (src.startsWith("http://") || src.startsWith("https://")) {
            val alt =
              Regex("(?i)alt=[\"']([^\"']*)[\"']").find(raw)?.groupValues?.getOrNull(1).orEmpty()
            blocks.add(ArticleBlock.ImageBlock(src, cleanHtmlText(alt)))
          }
        }

        else -> {
          // Check if paragraph contains an inline image
          val imgInside = Regex("(?is)<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>").find(raw)
          if (imgInside != null) {
            val src = imgInside.groupValues[1].trim()
            if (src.startsWith("http://") || src.startsWith("https://")) {
              blocks.add(ArticleBlock.ImageBlock(src, ""))
            }
          }
          val text = cleanHtmlText(raw)
          if (text.isNotBlank()) {
            val isExplicitQuestion =
              raw.contains("mcq-q", ignoreCase = true) ||
                raw.contains("#D32F2F", ignoreCase = true) ||
                raw.contains("rgb(211, 47, 47)", ignoreCase = true)
            val isExplicitAnswer =
              raw.contains("mcq-a", ignoreCase = true) ||
                raw.contains("#2E7D32", ignoreCase = true) ||
                raw.contains("rgb(46, 125, 50)", ignoreCase = true)

            when {
              isExplicitQuestion -> blocks.add(ArticleBlock.McqQuestionBlock(text))
              isExplicitAnswer -> blocks.add(ArticleBlock.McqAnswerBlock(text))
              else -> classifyLine(text, blocks)
            }
          }
        }
      }
    }

    if (lastIndex < fragment.length) {
      val tail = fragment.substring(lastIndex)
      cleanHtmlLines(tail).forEach { line -> classifyLine(line, blocks) }
    }
  }

  private fun parseTable(rawTable: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val trRegex = Regex("(?is)<tr[^>]*>(.*?)</tr>")
    val cellRegex = Regex("(?is)<t[dh][^>]*>(.*?)</t[dh]>")
    for (tr in trRegex.findAll(rawTable)) {
      val cells = cellRegex.findAll(tr.groupValues[1]).map { cleanHtmlText(it.groupValues[1]) }.toList()
      if (cells.any { it.isNotBlank() }) {
        rows.add(cells)
      }
    }
    return rows
  }

  private fun classifyLine(line: String, blocks: MutableList<ArticleBlock>) {
    val trimmed = line.trim()
    if (trimmed.isBlank()) return
    when {
      Regex("^(Answer|Ans|Correct Answer)\\s*[:\\-]", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ->
        blocks.add(ArticleBlock.McqAnswerBlock(trimmed))
      Regex("^Q\\s*\\d*\\s*[.):]", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ->
        blocks.add(ArticleBlock.McqQuestionBlock(trimmed))
      else -> blocks.add(ArticleBlock.ParagraphBlock(trimmed))
    }
  }

  private fun cleanHtmlLines(raw: String): List<String> {
    val withBreaks =
      raw
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</(div|p|li|tr)>"), "\n")
    val decoded = HtmlCompat.fromHtml(withBreaks, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    return decoded
      .split("\n")
      .map { it.replace('\u00A0', ' ').replace(Regex("\\s+"), " ").trim() }
      .filter { it.isNotBlank() }
  }

  fun cleanHtmlText(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    return decoded.replace('\u00A0', ' ').replace(Regex("\\s+"), " ").trim()
  }
}
