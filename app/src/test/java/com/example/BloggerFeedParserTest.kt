package com.example

import com.example.data.BloggerFeedParser
import com.example.data.BundledContent
import com.example.ui.components.ArticleBlock
import com.example.ui.components.ArticleHtmlParser
import com.example.ui.screens.buildPrintablePostHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BloggerFeedParserTest {

  @Test
  fun `parses blogger json feed and cleans html entities`() {
    val sampleJson =
      """
      {
        "feed": {
          "entry": [
            {
              "id": {"${'$'}t": "tag:blogger.com,1999:blog-1.post-101"},
              "published": {"${'$'}t": "2026-11-13T10:00:00.000Z"},
              "title": {"${'$'}t": "Part B &mdash; Unit 4: Health &bull; Safety"},
              "content": {"${'$'}t": "<p>Ergonomics &amp; fire safety notes.</p>"},
              "category": [
                {"term": "Class 10"},
                {"term": "Information Technology"},
                {"term": "Class 10 Information Technology Chapter-wise Notes"}
              ],
              "link": [
                {"rel": "alternate", "href": "https://gyanpetika.blogspot.com/2026/11/unit-4.html"}
              ]
            }
          ]
        }
      }
      """
        .trimIndent()

    val posts = BloggerFeedParser.parseFeedJson(sampleJson)
    assertEquals(1, posts.size)
    val post = posts.first()
    assertTrue(post.title.contains("Unit 4"))
    assertTrue(!post.title.contains("&bull;"))
    assertEquals("Class 10", post.classTag)
    assertEquals("Information Technology", post.subjectTag)
    assertEquals("Chapter-wise Notes", post.materialCategory)
    assertEquals("https://gyanpetika.blogspot.com/2026/11/unit-4.html", post.postUrl)
  }

  @Test
  fun `parses blogger metadata author thumbnail comments and non class labels`() {
    val sampleJson =
      """
      {
        "feed": {
          "openSearch${'$'}totalResults": {"${'$'}t": "42"},
          "entry": [
            {
              "id": {"${'$'}t": "tag:blogger.com,1999:blog-1.post-202"},
              "published": {"${'$'}t": "2026-11-12T08:30:00.000Z"},
              "title": {"${'$'}t": "Weekly Current Affairs Digest &bull; November"},
              "content": {"${'$'}t": "<p>National and international updates.</p>"},
              "author": [{"name": {"${'$'}t": "Gyan Petika Editorial"}}],
              "media${'$'}thumbnail": {"url": "https://blogger.googleusercontent.com/img/s72-c/thumb.png"},
              "thr${'$'}total": {"${'$'}t": "7"},
              "category": [
                {"term": "Current Affairs"}
              ],
              "link": [
                {"rel": "alternate", "href": "https://gyanpetika.blogspot.com/2026/11/current-affairs.html"}
              ]
            }
          ]
        }
      }
      """
        .trimIndent()

    val parsed = BloggerFeedParser.parseFeedWithMetadata(sampleJson)
    assertEquals(42, parsed.totalResults)
    assertEquals(1, parsed.posts.size)
    val post = parsed.posts.first()
    assertEquals("Current Affairs", post.classTag)
    assertEquals("Current Affairs", post.subjectTag)
    assertEquals("Gyan Petika Editorial", post.authorName)
    assertEquals(7, post.commentCount)
    assertEquals("https://blogger.googleusercontent.com/img/s400/thumb.png", post.thumbnailUrl)
  }

  @Test
  fun `parses bundled sample article with banner sections and mcqs`() {
    val sample = BundledContent.fallbackPosts.first()
    val blocks = ArticleHtmlParser.parse(sample)

    assertTrue(blocks.any { it is ArticleBlock.HeroBannerBlock })
    assertTrue(blocks.any { it is ArticleBlock.SectionHeaderBlock && it.number == "01" })
    assertTrue(blocks.any { it is ArticleBlock.McqQuestionBlock })
    assertTrue(blocks.any { it is ArticleBlock.McqAnswerBlock })
  }

  @Test
  fun `printable post html excludes topic title header and includes whole post with mcq reveal`() {
    val sample = BundledContent.fallbackPosts.first()
    val printable = buildPrintablePostHtml(sample.htmlContent)

    assertFalse(printable.contains("<h1>${sample.title}</h1>"))
    assertFalse(printable.contains("<div class=\"meta\">"))
    assertTrue(printable.contains(sample.htmlContent))
    assertTrue(printable.contains("prepareMcqsForPrint"))
    assertTrue(printable.contains("revealAnswerElements"))
    assertTrue(printable.contains("gp-print-answer"))
    assertTrue(printable.contains("data-gp-print-state=\"wrong\""))
    assertTrue(printable.contains("data-gp-print-state=\"correct\""))
    assertTrue(printable.contains("data-gp-print-state=\"attempted\""))
    assertTrue(printable.contains(".mcq-q"))
    assertTrue(printable.contains(".mcq-a"))
  }

  @Test
  fun `printable post html preserves attempted snapshot without re-running snapshot scripts`() {
    val liveSnapshot =
      """
      <div class="question">
        <div class="option wrong" data-gp-print-state="wrong">Option A</div>
        <div class="option correct" data-gp-print-state="correct">Option B</div>
      </div>
      <script>renderQuiz();</script>
      """.trimIndent()
    val printable =
      buildPrintablePostHtml(
        rawHtml = liveSnapshot,
        extraHeadStyles = "<style>.wrong{color:red;}</style>",
        preserveAttemptedSnapshot = true,
      )

    assertTrue(printable.contains("id=\"gp-frozen-snapshot\""))
    assertTrue(printable.contains("<style>.wrong{color:red;}</style>"))
    assertTrue(printable.contains("restoreAttemptedSnapshotIfWiped"))
    val templateSection = printable.substringAfter("id=\"gp-frozen-snapshot\">").substringBefore("</template>")
    assertFalse(templateSection.contains("renderQuiz();"))
    assertTrue(templateSection.contains("data-gp-print-state=\"wrong\""))
    assertTrue(templateSection.contains("data-gp-print-state=\"correct\""))
  }

  @Test
  fun `top filter pills place Board Notice after All without class 9 to 12`() {
    val pills = BundledContent.classFilterPills
    assertEquals("All", pills[0])
    assertEquals("Board Notice", pills[1])
    assertFalse(pills.contains("Class 9"))
    assertFalse(pills.contains("Class 10"))
    assertFalse(pills.contains("Class 11"))
    assertFalse(pills.contains("Class 12"))
  }
}
