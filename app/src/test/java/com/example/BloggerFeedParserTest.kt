package com.example

import com.example.data.BloggerFeedParser
import com.example.data.BundledContent
import com.example.data.StudyPost
import com.example.data.TranslationLanguages
import com.example.ui.MainUiState
import com.example.ui.components.ArticleBlock
import com.example.ui.components.ArticleHtmlParser
import com.example.ui.screens.buildPrintablePostHtml
import com.example.ui.screens.buildRichPostHtml
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
    assertFalse(printable.contains("#DBEAFE"))
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
  fun `top filter pills exclude Notice Board Notice and class 9 to 12`() {
    val pills = BundledContent.classFilterPills
    assertEquals(listOf("All", "Study Material", "Question Papers", "Current Affairs"), pills)
    assertFalse(pills.contains("Notice"))
    assertFalse(pills.contains("Board Notice"))
    assertFalse(pills.contains("Class 9"))
    assertFalse(pills.contains("Class 10"))
    assertFalse(pills.contains("Class 11"))
    assertFalse(pills.contains("Class 12"))
  }

  @Test
  fun `study material filter includes only posts labelled Study Material`() {
    val unlabelledJson =
      """
      {
        "feed": {
          "entry": [
            {
              "id": {"${'$'}t": "post-unlabelled"},
              "published": {"${'$'}t": "2026-11-13T10:00:00.000Z"},
              "title": {"${'$'}t": "General Update"},
              "content": {"${'$'}t": "<p>No labels here.</p>"}
            }
          ]
        }
      }
      """
        .trimIndent()
    val parsedUnlabelled = BloggerFeedParser.parseFeedJson(unlabelledJson).first()
    assertFalse(parsedUnlabelled.classTag.equals("Study Material", ignoreCase = true))
    assertFalse(parsedUnlabelled.subjectTag.equals("Study Material", ignoreCase = true))

    val class10OnlyPost =
      StudyPost(
        id = "p-class10",
        title = "Class 10 Notes",
        classTag = "Class 10",
        subjectTag = "Information Technology",
        categoryLabel = "Class 10 • IT",
        subLabel = "Unit 1",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 13, 2026",
        fullPublishedDate = "Friday, November 13, 2026",
        estimatedReadTime = "5 min read",
        snippet = "Notes",
        htmlContent = "<p>Notes</p>",
        postUrl = "https://gyanpetika.blogspot.com/p/1",
        labels = listOf("Class 10", "Information Technology"),
      )
    val studyMaterialPost =
      class10OnlyPost.copy(
        id = "p-study-material",
        title = "Science Study Material",
        labels = listOf("Class 10", "Study Material"),
      )

    val state =
      MainUiState(
        posts = listOf(parsedUnlabelled, class10OnlyPost, studyMaterialPost),
        selectedClassFilter = "Study Material",
      )
    assertEquals(listOf("p-study-material"), state.recentFilteredPosts.map { it.id })
  }

  @Test
  fun `noticePosts returns only exact Notice labelled posts ordered newest first for top 5`() {
    val base =
      StudyPost(
        id = "base",
        title = "Regular Notes",
        classTag = "Class 10",
        subjectTag = "Science",
        categoryLabel = "Class 10 • Science",
        subLabel = "Unit 1",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 10, 2026",
        fullPublishedDate = "Tuesday, November 10, 2026",
        estimatedReadTime = "3 min read",
        snippet = "Notes",
        htmlContent = "<p>Notes</p>",
        postUrl = "https://gyanpetika.blogspot.com/p/base",
        labels = listOf("Class 10", "Study Material"),
      )
    val boardNoticeOnly =
      base.copy(
        id = "board-notice-only",
        title = "Old Board Notice",
        labels = listOf("Board Notice"),
      )
    val notices =
      (1..7).map { idx ->
        base.copy(
          id = "notice-$idx",
          title = "Notice #$idx",
          labels = listOf("Notice"),
        )
      }
    val state = MainUiState(posts = listOf(base, boardNoticeOnly) + notices)
    assertEquals(7, state.noticePosts.size)
    assertFalse(state.noticePosts.any { it.id == "board-notice-only" })
    assertEquals(listOf("notice-1", "notice-2", "notice-3", "notice-4", "notice-5"), state.noticePosts.take(5).map { it.id })
  }

  @Test
  fun `rich post html uses native sticky timer without scroll animation and supports one-tap toggle and translation`() {
    val mcqHtml =
      """
      <div class="quiz-top-bar" style="position: sticky; top: 0;">
        <span id="timer">15:00</span>
        <span class="score">Score: 0/10</span>
      </div>
      <div class="q-card"><p class="mcq-q">Q1. Sample question?</p></div>
      """.trimIndent()
    val samplePost = BundledContent.fallbackPosts.first()
    val richHtml =
      buildRichPostHtml(
        rawHtml = mcqHtml,
        fontSizeSp = 16,
        bgHex = "#FFFFFF",
        textHex = "#111827",
        cardBgHex = "#F8FAFC",
        borderHex = "#E2E8F0",
        isDark = false,
        isPaperMode = false,
        post = samplePost,
      )

    assertTrue(richHtml.contains("position: sticky !important;"))
    assertTrue(richHtml.contains("top: 0px !important;"))
    assertFalse(richHtml.contains("gpSyncStickyNav"))
    assertTrue(richHtml.contains("gpDiscoverStickyBars"))
    assertTrue(richHtml.contains("id=\"gp-native-header\""))
    assertTrue(richHtml.contains("id=\"gp-post-root\""))
    assertTrue(richHtml.contains("gpSetArticleTitle"))
    assertTrue(richHtml.contains("onSwitchToCleanReader"))
    assertTrue(richHtml.contains("onToggleTopNav"))
    assertTrue(richHtml.contains("isInteractiveTapTarget"))
    assertTrue(richHtml.contains("gpExtractTranslatableSegments"))
    assertTrue(richHtml.contains("gpApplyTranslatedSegments"))
    assertTrue(richHtml.contains("gpRestoreOriginalTexts"))
    assertTrue(richHtml.contains("data-gp-sticky-nav"))
    assertTrue(richHtml.contains("overflow-x: clip"))
    assertTrue(richHtml.contains(":where(#gp-post-root button)"))
    assertFalse(richHtml.contains("#gp-post-root button {"))
    assertEquals(11, TranslationLanguages.supported.size)
    assertTrue(TranslationLanguages.supported.any { it.code == "hi" })
    assertTrue(TranslationLanguages.supported.any { it.code == "pa" })
    assertTrue(TranslationLanguages.supported.any { it.code == "ml" })

    val printableHtml = buildPrintablePostHtml(rawHtml = mcqHtml)
    assertTrue(printableHtml.contains("[data-gp-sticky-nav=\"1\"]"))
    assertTrue(printableHtml.contains("translate: none !important"))
    assertFalse(printableHtml.contains("gpGroupTopicsForPrint"))
  }

  @Test
  fun `lastDayNotices filters only notices uploaded within last 24 hours`() {
    val nowMs = 1_795_700_000_000L
    val oneHourAgoMs = nowMs - (1L * 60L * 60L * 1000L)
    val twelveHoursAgoMs = nowMs - (12L * 60L * 60L * 1000L)
    val twoDaysAgoMs = nowMs - (48L * 60L * 60L * 1000L)

    val template =
      StudyPost(
        id = "n",
        title = "Notice",
        classTag = "Notice",
        subjectTag = "Notice",
        categoryLabel = "GYAN PETIKA • Notice",
        subLabel = "Notice",
        materialCategory = "Chapter-wise Notes",
        publishedDate = "Nov 25, 2026",
        fullPublishedDate = "Wednesday, November 25, 2026",
        estimatedReadTime = "3 min read",
        snippet = "Notice snippet",
        htmlContent = "<p>Notice</p>",
        postUrl = "https://gyanpetika.blogspot.com/p/notice",
        labels = listOf("Notice"),
      )

    val oldNotice = template.copy(id = "old", title = "Old Notice", publishedEpochMs = twoDaysAgoMs)
    val recent12h = template.copy(id = "recent-12h", title = "12h Notice", publishedEpochMs = twelveHoursAgoMs)
    val recent1h = template.copy(id = "recent-1h", title = "1h Notice", publishedEpochMs = oneHourAgoMs)

    val state = MainUiState(posts = listOf(oldNotice, recent12h, recent1h))
    assertEquals(3, state.noticePosts.size)
    assertEquals(listOf("recent-1h", "recent-12h"), state.lastDayNotices(nowMs).map { it.id })
  }

  @Test
  fun `computer science subject is available in all classes and detected by feed parser`() {
    listOf("Class 9", "Class 10").forEach { className ->
      val names = BundledContent.subjectsForClass(className).map { it.name }
      val hindiIdx = names.indexOf("Hindi")
      val sanskritIdx = names.indexOf("Sanskrit")
      assertTrue("Hindi should exist in $className", hindiIdx >= 0)
      assertEquals("Sanskrit should follow Hindi in $className", hindiIdx + 1, sanskritIdx)
    }

    listOf("Class 9", "Class 10", "Class 11", "Class 12").forEach { className ->
      val subjects = BundledContent.subjectsForClass(className)
      assertTrue(
        "$className should include Computer Science",
        subjects.any { it.id == "computer-science" && it.name == "Computer Science" },
      )
    }

    val sampleJson =
      """
      {
        "feed": {
          "entry": [
            {
              "id": {"${'$'}t": "post-cs-11"},
              "published": {"${'$'}t": "2026-11-25T10:15:00.000+05:30"},
              "title": {"${'$'}t": "Class 11 Computer Science Python Loops"},
              "content": {"${'$'}t": "<p>Python for and while loops.</p>"},
              "category": [
                {"term": "Class 11"},
                {"term": "Computer Science"}
              ]
            }
          ]
        }
      }
      """
        .trimIndent()

    val post = BloggerFeedParser.parseFeedJson(sampleJson).first()
    assertEquals("Class 11", post.classTag)
    assertEquals("Computer Science", post.subjectTag)
    assertTrue(post.publishedEpochMs > 0L)
  }
}
