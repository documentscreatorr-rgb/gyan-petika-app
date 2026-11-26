package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.BundledContent
import com.example.data.StudyPost
import com.example.data.TranslationLanguage
import com.example.data.TranslationLanguages
import com.example.data.TranslationResult
import com.example.data.UserPreferences
import com.example.ui.TranslateUiState
import com.example.ui.components.ArticleBlock
import com.example.ui.components.ArticleHtmlParser
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BadgePurple
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun ArticleReaderScreen(
  post: StudyPost,
  preferences: UserPreferences,
  translateUiState: TranslateUiState = TranslateUiState.Idle,
  onBackClick: () -> Unit,
  onFontDecrease: () -> Unit,
  onFontIncrease: () -> Unit,
  onTogglePaperMode: () -> Unit,
  onToggleBookmark: () -> Unit,
  onLabelClick: (String) -> Unit,
  onInternalPostLinkClick: (String) -> Unit,
  onTranslatePost: (
    modeKey: String,
    language: TranslationLanguage,
    title: String,
    segments: List<String>,
    onTranslated: (TranslationResult) -> Unit,
  ) -> Unit = { _, _, _, _, _ -> },
  onResetTranslation: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val extended = LocalGyanExtendedColors.current
  val readerScrollState = rememberScrollState()
  val isSaved = post.id in preferences.savedPostIds
  val baseFontSp = preferences.customFontSizeSp
  var useFullPostView by rememberSaveable(post.id) { mutableStateOf(true) }
  var activePostWebView by remember(post.id) { mutableStateOf<WebView?>(null) }
  var isDispatchingPrint by remember(post.id) { mutableStateOf(false) }
  var showTranslateSheet by remember(post.id) { mutableStateOf(false) }
  var translatedCleanBlocks by remember(post.id) { mutableStateOf<List<ArticleBlock>?>(null) }
  var isTopNavVisible by rememberSaveable(post.id) { mutableStateOf(true) }
  val cleanBlocks = remember(post.id, post.htmlContent) { ArticleHtmlParser.parse(post) }
  val isTranslating = translateUiState is TranslateUiState.Loading
  val activeTranslationLanguage =
    (translateUiState as? TranslateUiState.Success)?.result?.targetLanguage
  val displayTitle =
    (translateUiState as? TranslateUiState.Success)?.result?.translatedTitle ?: post.title

  fun toggleTopNav() {
    isTopNavVisible = !isTopNavVisible
  }

  DisposableEffect(post.id) {
    onResetTranslation()
    translatedCleanBlocks = null
    onDispose {
      onResetTranslation()
    }
  }

  DisposableEffect(post.id, useFullPostView) {
    if (!useFullPostView) {
      activePostWebView = null
    }
    onDispose {
      activePostWebView = null
    }
  }

  fun restoreOriginalLanguage() {
    translatedCleanBlocks = null
    activePostWebView?.evaluateJavascript(
      "window.gpRestoreOriginalTexts && window.gpRestoreOriginalTexts();",
      null,
    )
    onResetTranslation()
  }

  fun translateCurrentPost(language: TranslationLanguage) {
    if (useFullPostView) {
      val webView = activePostWebView
      if (webView != null) {
        webView.evaluateJavascript(
          "window.gpExtractTranslatableSegments ? window.gpExtractTranslatableSegments() : '[]'"
        ) { rawResult ->
          val segments = parseJsStringArray(rawResult)
          onTranslatePost("html", language, post.title, segments) { result ->
            val escapedJson = JSONArray(result.translatedSegments).toString()
            webView.evaluateJavascript(
              "window.gpApplyTranslatedSegments && window.gpApplyTranslatedSegments($escapedJson);",
              null,
            )
          }
        }
        return
      }
    }

    val nonBannerBlocks = cleanBlocks.filterNot { it is ArticleBlock.HeroBannerBlock }
    val segments = extractCleanBlockSegments(nonBannerBlocks)
    onTranslatePost("clean", language, post.title, segments) { result ->
      translatedCleanBlocks =
        applyCleanBlockTranslations(nonBannerBlocks, result.translatedSegments)
    }
  }

  fun startStandardPrint() {
    if (isDispatchingPrint) return
    isDispatchingPrint = true
    capturePrintableSnapshot(
      post = post,
      liveWebView = if (useFullPostView) activePostWebView else null,
    ) { snapshot ->
      launchPrintWebView(
        context = context,
        post = post,
        rawHtml = snapshot.rawHtml,
        extraHeadStyles = snapshot.extraHeadStyles,
        preserveAttemptedSnapshot = snapshot.preserveAttemptedSnapshot,
        onPrintDispatched = { isDispatchingPrint = false },
      )
    }
  }

  Box(
    modifier = modifier.fillMaxSize().background(extended.readerBackground),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Navigation Bar: toggles smoothly on single tap; keeps WebView & sticky MCQ timer below it when visible.
      AnimatedVisibility(
        visible = isTopNavVisible,
        enter = expandVertically(animationSpec = tween(220)) + fadeIn(tween(220)),
        exit = shrinkVertically(animationSpec = tween(220)) + fadeOut(tween(220)),
        modifier = Modifier.fillMaxWidth().testTag("reader_top_navigation"),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ReaderFloatingToolbar(
            isSaved = isSaved,
            isPaperMode = preferences.eyeComfortPaperMode,
            isTranslating = isTranslating,
            isTranslated = activeTranslationLanguage != null,
            isPreparingPrint = isDispatchingPrint,
            onBackClick = onBackClick,
            onFontDecrease = onFontDecrease,
            onFontIncrease = onFontIncrease,
            onTranslateClick = { showTranslateSheet = true },
            onTogglePaperMode = onTogglePaperMode,
            onToggleBookmark = onToggleBookmark,
            onPrintClick = { startStandardPrint() },
            onShareClick = { shareStudyPost(context, post) },
          )

          AnimatedVisibility(visible = translateUiState !is TranslateUiState.Idle) {
            TranslateStatusBanner(
              uiState = translateUiState,
              onRestoreOriginal = { restoreOriginalLanguage() },
              onRetry = { lang -> translateCurrentPost(lang) },
              onDismiss = {
                if (translateUiState is TranslateUiState.Error) {
                  onResetTranslation()
                } else {
                  restoreOriginalLanguage()
                }
              },
            )
          }
        }
      }

      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        if (useFullPostView) {
          FullPostHtmlWebView(
            post = post,
            displayTitle = displayTitle,
            fontSizeSp = baseFontSp,
            isDark = extended.isDark,
            isPaperMode = extended.isPaperMode,
            isTopNavVisible = isTopNavVisible,
            onLabelClick = onLabelClick,
            onInternalPostLinkClick = onInternalPostLinkClick,
            onWebViewReady = { activePostWebView = it },
            onSwitchToCleanReader = {
              useFullPostView = false
              restoreOriginalLanguage()
            },
            onToggleTopNav = ::toggleTopNav,
            modifier = Modifier.fillMaxSize(),
          )
        } else {
          Column(
            modifier =
              Modifier.fillMaxSize()
                .verticalScroll(readerScrollState)
                .pointerInput(post.id) {
                  detectTapGestures(onTap = { toggleTopNav() })
                }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            ArticleMetadataHeader(
              post = post,
              displayTitle = displayTitle,
              baseFontSp = baseFontSp,
              useFullPostView = false,
              onSelectFullPost = {
                useFullPostView = true
                restoreOriginalLanguage()
              },
              onSelectCleanReader = {},
            )

            if (post.banner != null) {
              UnitHeroBanner(block = ArticleBlock.HeroBannerBlock(post.banner))
            }

            val blocksToRender =
              translatedCleanBlocks ?: cleanBlocks.filterNot { it is ArticleBlock.HeroBannerBlock }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
              blocksToRender.forEach { block ->
                RenderCleanBlock(block = block, baseFontSp = baseFontSp, postTitle = displayTitle)
              }
            }

            Spacer(modifier = Modifier.height(18.dp))
          }
        }
      }
    }

    if (extended.isPaperMode) {
      Box(
        modifier =
          Modifier.matchParentSize()
            .background(
              if (extended.isDark) Color(0x12FFB74D)
              else Color(0x0DFFB74D)
            )
            .testTag("eye_comfort_shield_overlay"),
      )
    }

    if (showTranslateSheet) {
      TranslateLanguageBottomSheet(
        selectedLanguage = activeTranslationLanguage,
        onSelectOriginal = {
          showTranslateSheet = false
          restoreOriginalLanguage()
        },
        onSelectLanguage = { language ->
          showTranslateSheet = false
          translateCurrentPost(language)
        },
        onDismiss = { showTranslateSheet = false },
      )
    }
  }
}

@Composable
private fun ArticleMetadataHeader(
  post: StudyPost,
  displayTitle: String,
  baseFontSp: Int,
  useFullPostView: Boolean,
  onSelectFullPost: () -> Unit,
  onSelectCleanReader: () -> Unit,
) {
  val extended = LocalGyanExtendedColors.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = PrimaryNavy.copy(alpha = if (extended.isDark) 0.4f else 0.1f),
      ) {
        Text(
          text = post.classTag,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = if (extended.isDark) Color(0xFF93C5FD) else PrimaryNavy,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = BadgePurple.copy(alpha = 0.12f),
      ) {
        Text(
          text = post.subjectTag,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = if (extended.isDark) Color(0xFFC4B5FD) else BadgePurple,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
      Text(
        text = "• ${post.estimatedReadTime}",
        style = MaterialTheme.typography.labelSmall,
        color = extended.readerTextSecondary,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = displayTitle,
      style =
        MaterialTheme.typography.headlineMedium.copy(
          fontSize = (baseFontSp + 7).sp,
          lineHeight = (baseFontSp + 14).sp,
          fontWeight = FontWeight.ExtraBold,
        ),
      color = extended.readerTextPrimary,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Rounded.Person,
          contentDescription = null,
          tint = extended.readerTextSecondary,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${post.authorName} • ${post.fullPublishedDate}",
          style = MaterialTheme.typography.bodySmall,
          color = extended.readerTextSecondary,
        )
      }

      if (post.commentCount > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.Comment,
            contentDescription = null,
            tint = extended.readerTextSecondary,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${post.commentCount}",
            style = MaterialTheme.typography.labelSmall,
            color = extended.readerTextSecondary,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ViewModeChip(
        label = "Full Post (Rich HTML)",
        selected = useFullPostView,
        onClick = onSelectFullPost,
        modifier = Modifier.weight(1f).testTag("mode_full_post"),
      )
      ViewModeChip(
        label = "Clean Reader",
        selected = !useFullPostView,
        onClick = onSelectCleanReader,
        modifier = Modifier.weight(1f).testTag("mode_clean_reader"),
      )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = extended.readerBorder)
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun FullPostHtmlWebView(
  post: StudyPost,
  displayTitle: String,
  fontSizeSp: Int,
  isDark: Boolean,
  isPaperMode: Boolean,
  isTopNavVisible: Boolean,
  onLabelClick: (String) -> Unit,
  onInternalPostLinkClick: (String) -> Unit,
  onWebViewReady: (WebView?) -> Unit,
  onSwitchToCleanReader: () -> Unit,
  onToggleTopNav: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current

  val bgHex =
    when {
      isPaperMode && !isDark -> "#FAF6EE"
      isPaperMode && isDark -> "#201B16"
      isDark -> "#0B1220"
      else -> "#FFFFFF"
    }
  val textHex =
    when {
      isPaperMode && !isDark -> "#26211B"
      isPaperMode && isDark -> "#F3EAE0"
      isDark -> "#E2E8F0"
      else -> "#111827"
    }
  val cardBgHex =
    when {
      isPaperMode && !isDark -> "#F4EFE4"
      isPaperMode && isDark -> "#2A231C"
      isDark -> "#131D2E"
      else -> "#F8FAFC"
    }
  val borderHex =
    when {
      isPaperMode && !isDark -> "#E3DDD0"
      isPaperMode && isDark -> "#3F352B"
      isDark -> "#25354E"
      else -> "#E2E8F0"
    }

  val latestDisplayTitle by rememberUpdatedState(displayTitle)
  val latestFontSizeSp by rememberUpdatedState(fontSizeSp)
  val latestBgHex by rememberUpdatedState(bgHex)
  val latestTextHex by rememberUpdatedState(textHex)
  val latestCardBgHex by rememberUpdatedState(cardBgHex)
  val latestBorderHex by rememberUpdatedState(borderHex)
  val latestIsDark by rememberUpdatedState(isDark)
  val latestIsPaperMode by rememberUpdatedState(isPaperMode)
  val latestOnLabelClick by rememberUpdatedState(onLabelClick)
  val latestOnInternalPostLinkClick by rememberUpdatedState(onInternalPostLinkClick)
  val latestOnSwitchToCleanReader by rememberUpdatedState(onSwitchToCleanReader)
  val latestOnToggleTopNav by rememberUpdatedState(onToggleTopNav)

  val wrappedHtml =
    remember(post.id, post.htmlContent) {
      buildRichPostHtml(
        rawHtml = post.htmlContent,
        fontSizeSp = fontSizeSp,
        bgHex = bgHex,
        textHex = textHex,
        cardBgHex = cardBgHex,
        borderHex = borderHex,
        isDark = isDark,
        isPaperMode = isPaperMode,
        post = post,
        displayTitle = displayTitle,
      )
    }

  Surface(
    shape = RoundedCornerShape(if (isTopNavVisible) 16.dp else 0.dp),
    color = Color.Transparent,
    border =
      if (isTopNavVisible) BorderStroke(1.dp, LocalGyanExtendedColors.current.readerBorder)
      else null,
    modifier =
      modifier
        .padding(
          start = if (isTopNavVisible) 8.dp else 0.dp,
          end = if (isTopNavVisible) 8.dp else 0.dp,
          bottom = if (isTopNavVisible) 8.dp else 0.dp,
        ),
  ) {
    AndroidView(
      modifier =
        Modifier.fillMaxSize()
          .clip(RoundedCornerShape(if (isTopNavVisible) 16.dp else 0.dp)),
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT,
            )
          setBackgroundColor(AndroidColor.TRANSPARENT)
          isVerticalScrollBarEnabled = true
          isHorizontalScrollBarEnabled = false
          isNestedScrollingEnabled = true
          overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            builtInZoomControls = false
            displayZoomControls = false
          }

          addJavascriptInterface(
            object {
              @JavascriptInterface
              fun onSwitchToCleanReader() {
                post { latestOnSwitchToCleanReader() }
              }

              @JavascriptInterface
              fun onToggleTopNav() {
                post { latestOnToggleTopNav() }
              }
            },
            "GyanBridge",
          )

          webChromeClient = WebChromeClient()
          webViewClient =
            object : WebViewClient() {
              override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
              ): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) {
                  if (url.contains("gyanpetika.blogspot.com") && url.contains("/search/label/")) {
                    val rawLabel =
                      url.substringAfter("/search/label/").substringBefore("?").substringBefore("#")
                    val decodedLabel = Uri.decode(rawLabel)
                    if (decodedLabel.isNotBlank()) {
                      latestOnLabelClick(decodedLabel)
                      return true
                    }
                  }
                  if (
                    url.contains("gyanpetika.blogspot.com") &&
                      !url.contains("/search") &&
                      !url.contains("#") &&
                      !url.endsWith(".pdf", ignoreCase = true)
                  ) {
                    latestOnInternalPostLinkClick(url)
                  } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                  }
                  return true
                }
                return false
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val themeJs =
                  "window.gpApplyTheme && window.gpApplyTheme($latestFontSizeSp, '$latestBgHex', '$latestTextHex', '$latestCardBgHex', '$latestBorderHex', $latestIsDark, $latestIsPaperMode);"
                view?.evaluateJavascript(themeJs, null)
                val quotedTitle = JSONObject.quote(latestDisplayTitle)
                view?.evaluateJavascript(
                  "window.gpSetArticleTitle && window.gpSetArticleTitle($quotedTitle);",
                  null,
                )
                view?.evaluateJavascript(
                  "window.gpDiscoverStickyBars && window.gpDiscoverStickyBars();",
                  null,
                )
              }
            }

          loadDataWithBaseURL(
            BundledContent.BLOG_BASE_URL,
            wrappedHtml,
            "text/html",
            "UTF-8",
            null,
          )
        }.also { onWebViewReady(it) }
      },
      update = { webView ->
        onWebViewReady(webView)
        val js =
          "window.gpApplyTheme && window.gpApplyTheme($fontSizeSp, '$bgHex', '$textHex', '$cardBgHex', '$borderHex', $isDark, $isPaperMode);"
        webView.evaluateJavascript(js, null)
        val quotedTitle = JSONObject.quote(displayTitle)
        webView.evaluateJavascript(
          "window.gpSetArticleTitle && window.gpSetArticleTitle($quotedTitle);",
          null,
        )
      },
    )
  }
}

private fun escapeHtml(text: String): String =
  buildString(text.length) {
    for (ch in text) {
      when (ch) {
        '&' -> append("&amp;")
        '<' -> append("&lt;")
        '>' -> append("&gt;")
        '"' -> append("&quot;")
        '\'' -> append("&#39;")
        else -> append(ch)
      }
    }
  }

internal fun buildRichPostHtml(
  rawHtml: String,
  fontSizeSp: Int,
  bgHex: String,
  textHex: String,
  cardBgHex: String,
  borderHex: String,
  isDark: Boolean,
  isPaperMode: Boolean,
  post: StudyPost? = null,
  displayTitle: String = post?.title.orEmpty(),
): String {
  val rootClass = if (isPaperMode) " class=\"gp-eye-comfort\"" else ""
  val headerHtml =
    if (post != null) {
      val commentBadge =
        if (post.commentCount > 0) {
          "<span class=\"gp-hdr-comments\">&#128172; ${post.commentCount}</span>"
        } else {
          ""
        }
      val bannerHtml =
        post.banner?.let { banner ->
          """
          <div class="gp-hero-banner">
            <div class="gp-hero-eyebrow">${escapeHtml(banner.eyebrow)}</div>
            <div class="gp-hero-headline">${escapeHtml(banner.headline)}</div>
            <div class="gp-hero-sub">${escapeHtml(banner.subheadline)}</div>
            <div class="gp-hero-summary">${escapeHtml(banner.summary)}</div>
          </div>
          """.trimIndent()
        } ?: ""

      """
      <header id="gp-native-header">
        <div class="gp-hdr-pills">
          <span class="gp-pill-class">${escapeHtml(post.classTag)}</span>
          <span class="gp-pill-subject">${escapeHtml(post.subjectTag)}</span>
          <span class="gp-hdr-read-time">&bull; ${escapeHtml(post.estimatedReadTime)}</span>
        </div>
        <h1 id="gp-article-title">${escapeHtml(displayTitle)}</h1>
        <div class="gp-hdr-meta">
          <span>&#128100; ${escapeHtml(post.authorName)} &bull; ${escapeHtml(post.fullPublishedDate)}</span>
          $commentBadge
        </div>
        <div class="gp-mode-row">
          <button type="button" class="gp-mode-chip gp-mode-selected" disabled>Full Post (Rich HTML)</button>
          <button type="button" class="gp-mode-chip" onclick="window.GyanBridge && window.GyanBridge.onSwitchToCleanReader()">Clean Reader</button>
        </div>
        <hr class="gp-hdr-divider" />
        $bannerHtml
      </header>
      """.trimIndent()
    } else {
      ""
    }

  return """
    <!DOCTYPE html>
    <html$rootClass>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
      <style id="gp-theme-style">
        :root {
          --gp-bg: $bgHex;
          --gp-text: $textHex;
          --gp-card: $cardBgHex;
          --gp-border: $borderHex;
          --gp-font-size: ${fontSizeSp}px;
          --gp-dark: ${if (isDark) "1" else "0"};
        }
        * { box-sizing: border-box; max-width: 100%; }
        html, body {
          margin: 0;
          padding: 0;
          background-color: var(--gp-bg);
          color: var(--gp-text);
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          font-size: var(--gp-font-size);
          line-height: 1.65;
          overflow-x: clip;
          word-wrap: break-word;
        }
        body { padding: 8px 8px 18px 8px; }
        #gp-native-header {
          margin-bottom: 12px;
        }
        .gp-hdr-pills {
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 8px;
        }
        .gp-pill-class {
          display: inline-block;
          padding: 4px 10px;
          border-radius: 999px;
          font-size: 11px;
          font-weight: 700;
          background: ${if (isDark) "rgba(15, 52, 91, 0.45)" else "rgba(15, 52, 91, 0.10)"};
          color: ${if (isDark) "#93C5FD" else "#0F345B"};
        }
        .gp-pill-subject {
          display: inline-block;
          padding: 4px 10px;
          border-radius: 999px;
          font-size: 11px;
          font-weight: 700;
          background: rgba(109, 40, 217, 0.12);
          color: ${if (isDark) "#C4B5FD" else "#6D28D9"};
        }
        .gp-hdr-read-time {
          font-size: 11px;
          opacity: 0.75;
        }
        #gp-article-title {
          margin: 10px 0 8px 0;
          font-size: calc(var(--gp-font-size) + 7px);
          line-height: 1.3;
          font-weight: 800;
          color: var(--gp-text);
        }
        .gp-hdr-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 8px;
          font-size: 12px;
          opacity: 0.78;
        }
        .gp-mode-row {
          display: flex;
          gap: 8px;
          margin-top: 10px;
        }
        .gp-mode-chip {
          flex: 1 1 0;
          min-height: 44px;
          padding: 8px 10px;
          border-radius: 10px;
          border: 1px solid var(--gp-border);
          background: var(--gp-card);
          color: var(--gp-text);
          font-size: 12px;
          font-weight: 600;
          cursor: pointer;
          opacity: 1;
        }
        .gp-mode-chip.gp-mode-selected {
          background: #0F345B;
          border-color: #0F345B;
          color: #FFFFFF;
        }
        .gp-hdr-divider {
          border: 0;
          border-top: 1px solid var(--gp-border);
          margin: 10px 0 12px 0;
        }
        .gp-hero-banner {
          border-radius: 18px;
          padding: 18px;
          background: linear-gradient(135deg, #0F345B 0%, #1B4E84 55%, #2563EB 100%);
          color: #FFFFFF;
          margin-bottom: 12px;
        }
        .gp-hero-eyebrow {
          font-size: 11px;
          font-weight: 700;
          letter-spacing: 0.8px;
          opacity: 0.9;
        }
        .gp-hero-headline {
          font-size: 20px;
          font-weight: 800;
          margin-top: 6px;
        }
        .gp-hero-sub {
          font-size: 14px;
          font-weight: 600;
          color: #F5B82E;
          margin-top: 4px;
        }
        .gp-hero-summary {
          font-size: 12px;
          opacity: 0.9;
          margin-top: 8px;
        }
        html.gp-eye-comfort #gp-post-root {
          filter: sepia(0.06) brightness(0.985) contrast(0.98);
        }
        img, video, iframe {
          max-width: 100% !important;
          height: auto !important;
          border-radius: 10px;
        }
        iframe { min-height: 210px; width: 100% !important; border: 0; }
        a { color: ${if (isDark) "#60A5FA" else "#0F345B"}; font-weight: 600; }
        h1, h2, h3, h4 {
          line-height: 1.35;
          margin-top: 1.05em;
          margin-bottom: 0.45em;
        }
        p { margin: 0.6em 0; }
        ul, ol { padding-left: 1.25em; margin: 0.55em 0; }
        li { margin: 0.35em 0; }
        .gp-table-wrap { width: 100%; overflow-x: auto; margin: 10px 0; border-radius: 10px; }
        table {
          width: 100%;
          border-collapse: collapse;
          font-size: 0.92em;
        }
        th, td {
          border: 1px solid var(--gp-border);
          padding: 7px 9px;
          text-align: left;
        }
        th { background: var(--gp-card); font-weight: 700; }
        blockquote {
          margin: 10px 0;
          padding: 10px 12px;
          border-left: 4px solid #F5B82E;
          background: var(--gp-card);
          border-radius: 0 10px 10px 0;
        }
        .gp-section {
          margin: 12px 0;
          padding: 10px;
          border: 1px solid var(--gp-border);
          border-radius: 12px;
          background: var(--gp-card);
        }
        .gp-section::before {
          content: attr(data-num);
          display: inline-block;
          background: #0F345B;
          color: #F5B82E;
          font-weight: 800;
          font-size: 0.8em;
          padding: 3px 9px;
          border-radius: 8px;
          margin-bottom: 6px;
        }
        .mcq-q {
          color: #D32F2F !important;
          font-weight: 700;
          background: rgba(211, 47, 47, 0.08);
          padding: 10px 12px;
          border-radius: 10px;
          border: 1px solid rgba(211, 47, 47, 0.25);
        }
        .mcq-a {
          color: #2E7D32 !important;
          font-weight: 600;
          background: rgba(46, 125, 50, 0.10);
          padding: 10px 12px;
          border-radius: 10px;
          border: 1px solid rgba(46, 125, 50, 0.28);
        }
        details {
          margin: 10px 0;
          padding: 10px 12px;
          border: 1px solid var(--gp-border);
          border-radius: 10px;
          background: var(--gp-card);
        }
        summary { font-weight: 700; cursor: pointer; }
        :where(#gp-post-root button) {
          padding: 8px 14px;
          border-radius: 8px;
          border: 1px solid var(--gp-border);
          background: #0F345B;
          color: #FFFFFF;
          font-weight: 600;
          cursor: pointer;
        }
        /* Native Chromium compositor sticky lock at viewport top (0px) with zero JS scroll animation/jitter */
        [data-gp-sticky-nav="1"] {
          position: -webkit-sticky !important;
          position: sticky !important;
          top: 0px !important;
          bottom: auto !important;
          left: auto !important;
          right: auto !important;
          z-index: 9999 !important;
          box-shadow: 0 3px 12px rgba(15, 23, 42, 0.10) !important;
        }
        [data-gp-sticky-nav="1"][data-gp-sticky-auto-bg="1"] {
          background-color: var(--gp-bg) !important;
        }
      </style>
    </head>
    <body>
      $headerHtml
      <div id="gp-post-root">$rawHtml</div>
      <script>
        (function() {
          // Wrap wide tables for smooth horizontal scrolling
          document.querySelectorAll('#gp-post-root table').forEach(function(t) {
            if (!t.parentElement || !t.parentElement.classList.contains('gp-table-wrap')) {
              var w = document.createElement('div');
              w.className = 'gp-table-wrap';
              t.parentNode.insertBefore(w, t);
              w.appendChild(t);
            }
          });

          // --- Native CSS Sticky MCQ Timer Navigation Discovery (Zero JS Scroll Transform) ---
          var stickyBars = [];
          var stickyRefreshTimer = null;

          var STICKY_CLASS_REGEX = /(timer[-_]?(bar|nav|header|box|panel|wrap|strip|container|row)|quiz[-_]?(nav|header|bar|top|hud|sticky|controls|status|panel|strip|meta)|mcq[-_]?(nav|header|bar|top|hud|sticky|controls|status|panel)|test[-_]?(nav|header|bar|top|hud|controls)|sticky[-_]?(nav|header|bar|top|timer)|top[-_]?bar|nav[-_]?bar|hud[-_]?bar|score[-_]?bar|exam[-_]?(bar|nav|header))/i;
          var TIMER_NODE_REGEX = /(timer|countdown|clock|time[-_]?left|time[-_]?display|stopwatch)/i;
          var TIMER_TEXT_REGEX = /\b\d{1,2}\s*:\s*\d{2}\b|⏱|⏰|⏳/;
          var QUESTION_CONTAINER_SELECTOR = '.mcq-q, .question, .q-card, .quiz-item, .question-card, input[type="radio"]';

          function isTransparentBg(bg) {
            if (!bg || bg === 'transparent') return true;
            var m = bg.match(/rgba\(\s*\d+\s*,\s*\d+\s*,\s*\d+\s*,\s*([\d.]+)\s*\)/i);
            return !!(m && parseFloat(m[1]) < 0.12);
          }

          function hasMultipleQuestions(el) {
            return el.querySelectorAll && el.querySelectorAll(QUESTION_CONTAINER_SELECTOR).length > 1;
          }

          function isQuestionElement(el) {
            var cls = (el.className && typeof el.className === 'string') ? el.className : '';
            return /\b(mcq-q|mcq-a|question-card|q-card|quiz-item|option|opt|choice)\b/i.test(cls);
          }

          function isVisibleElement(el) {
            if (!el || el.nodeType !== 1) return false;
            var cs = window.getComputedStyle(el);
            if (!cs || cs.display === 'none' || cs.visibility === 'hidden' || parseFloat(cs.opacity) === 0) return false;
            var rect = el.getBoundingClientRect();
            return rect.width >= 80 && rect.height >= 24;
          }

          function promoteToBarContainer(startEl, root, rootWidth) {
            var best = startEl;
            var cur = startEl;
            for (var depth = 0; depth < 8 && cur && cur !== root && cur !== document.body; depth++) {
              var parent = cur.parentElement;
              if (!parent || parent === root || parent === document.body) break;
              var pTag = parent.tagName;
              if (pTag === 'TABLE' || pTag === 'TBODY' || pTag === 'TR' || pTag === 'UL' || pTag === 'OL') break;
              if (isQuestionElement(parent) || hasMultipleQuestions(parent)) break;
              if (parent.querySelector && parent.querySelector(QUESTION_CONTAINER_SELECTOR)) break;
              var pRect = parent.getBoundingClientRect();
              if (pRect.height >= 26 && pRect.height <= 220 && pRect.width >= 100) {
                best = parent;
              } else {
                break;
              }
              cur = parent;
            }
            return best;
          }

          function ensureAncestorsDoNotClip(el) {
            var cur = el.parentElement;
            while (cur && cur !== document.documentElement) {
              var cls = (cur.className && typeof cur.className === 'string') ? cur.className : '';
              if (!/\bgp-table-wrap\b/.test(cls)) {
                var cs = window.getComputedStyle(cur);
                if (cs) {
                  if (cs.overflowY === 'hidden' || cs.overflowY === 'clip' || cs.overflowY === 'auto' || cs.overflowY === 'scroll') {
                    cur.style.setProperty('overflow-y', 'visible', 'important');
                  }
                  if (cs.overflowX === 'hidden' || cs.overflowX === 'auto' || cs.overflowX === 'scroll') {
                    cur.style.setProperty('overflow-x', 'clip', 'important');
                  }
                }
              }
              cur = cur.parentElement;
            }
          }

          function clearOldStickyBindings() {
            stickyBars.forEach(function(el) {
              if (!el) return;
              el.removeAttribute('data-gp-sticky-nav');
              el.removeAttribute('data-gp-sticky-auto-bg');
            });
            stickyBars = [];
          }

          function discoverStickyBars() {
            var root = document.getElementById('gp-post-root');
            if (!root) return;

            var rootRect = root.getBoundingClientRect();
            var rootWidth = Math.max(rootRect.width || window.innerWidth || 320, 240);
            var candidates = [];

            function addCandidate(el) {
              if (!el || el === root || el === document.body) return;
              var promoted = promoteToBarContainer(el, root, rootWidth);
              if (!promoted || promoted === root || promoted === document.body) return;
              if (candidates.indexOf(promoted) !== -1) return;
              if (!isVisibleElement(promoted)) return;
              if (isQuestionElement(promoted) || hasMultipleQuestions(promoted)) return;
              var r = promoted.getBoundingClientRect();
              if (r.height < 24 || r.height > 220 || r.width < 100) return;
              candidates.push(promoted);
            }

            var allElements = root.querySelectorAll('div, nav, header, section, aside, span, p');
            for (var i = 0; i < allElements.length; i++) {
              var el = allElements[i];
              var cs = window.getComputedStyle(el);
              if (!cs || cs.display === 'none' || cs.visibility === 'hidden') continue;

              // 1. Explicitly sticky or top-fixed elements in post CSS
              var pos = cs.position;
              if (pos === 'sticky' || pos === '-webkit-sticky') {
                addCandidate(el);
                continue;
              }
              if (pos === 'fixed') {
                var rFixed = el.getBoundingClientRect();
                var isBottomDocked = cs.top === 'auto' && cs.bottom !== 'auto' && parseFloat(cs.bottom) <= 80;
                if (!isBottomDocked && rFixed.height <= 210 && rFixed.height >= 24) {
                  addCandidate(el);
                }
                continue;
              }

              // 2. Named timer / quiz navigation bars
              var clsStr = (el.className && typeof el.className === 'string') ? el.className : '';
              var clsId = clsStr + ' ' + (el.id || '');
              if (STICKY_CLASS_REGEX.test(clsId)) {
                var rNamed = el.getBoundingClientRect();
                if (rNamed.width >= rootWidth * 0.45 && rNamed.height >= 26 && rNamed.height <= 210 &&
                    !el.querySelector(QUESTION_CONTAINER_SELECTOR)) {
                  addCandidate(el);
                  continue;
                }
              }

              // 3. Live timer elements -> promote to their navigation bar container
              var isTimerByAttr = TIMER_NODE_REGEX.test(clsId);
              var ownText = (el.textContent || '').trim();
              var isTimerByText = ownText.length > 0 && ownText.length <= 40 &&
                el.children.length <= 2 && TIMER_TEXT_REGEX.test(ownText);
              if (isTimerByAttr || isTimerByText) {
                addCandidate(el);
              }
            }

            // Deduplicate: drop any candidate contained inside another candidate
            var deduped = candidates.filter(function(cand) {
              for (var j = 0; j < candidates.length; j++) {
                var other = candidates[j];
                if (other !== cand && other.contains(cand)) {
                  return false;
                }
              }
              return true;
            });

            var sameSet = deduped.length === stickyBars.length &&
              deduped.every(function(el, idx) { return stickyBars[idx] === el; });

            if (!sameSet) {
              clearOldStickyBindings();
              deduped.forEach(function(el) {
                var cs = window.getComputedStyle(el);
                if (isTransparentBg(cs.backgroundColor)) {
                  el.setAttribute('data-gp-sticky-auto-bg', '1');
                }
                el.setAttribute('data-gp-sticky-nav', '1');
                ensureAncestorsDoNotClip(el);
                stickyBars.push(el);
              });
            } else {
              stickyBars.forEach(ensureAncestorsDoNotClip);
            }
          }

          function scheduleStickyRefresh() {
            if (stickyRefreshTimer) clearTimeout(stickyRefreshTimer);
            stickyRefreshTimer = setTimeout(function() {
              stickyRefreshTimer = null;
              discoverStickyBars();
            }, 120);
          }

          // --- Live DOM Text-Node Translation Helpers (preserves interactive MCQs, timers & styling) ---
          var SKIP_TRANSLATE_TAGS = { SCRIPT: 1, STYLE: 1, NOSCRIPT: 1, CODE: 1, PRE: 1, SVG: 1, TEMPLATE: 1, IFRAME: 1 };
          var PURE_NUMERIC_OR_CLOCK_REGEX = /^[\d\s:.,/%+\-–—()[\]⏱⏰⏳•|]+${'$'}/;

          function collectTranslatableNodes() {
            var root = document.getElementById('gp-post-root');
            if (!root) return [];
            if (window.__gpTranslatableNodes && window.__gpOriginalNodeTexts &&
                window.__gpTranslatableNodes.length > 0 &&
                window.__gpTranslatableNodes[0] && window.__gpTranslatableNodes[0].isConnected) {
              return window.__gpTranslatableNodes;
            }
            var nodes = [];
            var originals = [];
            var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, null, false);
            var current;
            while ((current = walker.nextNode())) {
              var parent = current.parentElement;
              if (!parent) continue;
              if (SKIP_TRANSLATE_TAGS[parent.tagName]) continue;
              if (parent.closest && parent.closest('script, style, code, pre, svg, noscript')) continue;
              var raw = current.nodeValue || '';
              var trimmed = raw.trim();
              if (trimmed.length < 2) continue;
              if (PURE_NUMERIC_OR_CLOCK_REGEX.test(trimmed)) continue;
              nodes.push(current);
              originals.push(raw);
            }
            window.__gpTranslatableNodes = nodes;
            window.__gpOriginalNodeTexts = originals;
            return nodes;
          }

          window.gpExtractTranslatableSegments = function() {
            collectTranslatableNodes();
            var originals = window.__gpOriginalNodeTexts || [];
            return JSON.stringify(originals.map(function(s) { return s.trim(); }));
          };

          window.gpApplyTranslatedSegments = function(translatedList) {
            if (!Array.isArray(translatedList)) return;
            var nodes = collectTranslatableNodes();
            var originals = window.__gpOriginalNodeTexts || [];
            var count = Math.min(nodes.length, translatedList.length);
            for (var i = 0; i < count; i++) {
              var orig = originals[i] || '';
              var translated = translatedList[i];
              if (typeof translated === 'string' && translated.trim().length > 0) {
                var leadMatch = orig.match(/^\s*/);
                var trailMatch = orig.match(/\s*${'$'}/);
                var lead = leadMatch ? leadMatch[0] : '';
                var trail = trailMatch ? trailMatch[0] : '';
                nodes[i].nodeValue = lead + translated.trim() + trail;
              }
            }
            setTimeout(discoverStickyBars, 60);
          };

          window.gpRestoreOriginalTexts = function() {
            var nodes = window.__gpTranslatableNodes || [];
            var originals = window.__gpOriginalNodeTexts || [];
            for (var i = 0; i < Math.min(nodes.length, originals.length); i++) {
              if (nodes[i] && nodes[i].isConnected) {
                nodes[i].nodeValue = originals[i];
              }
            }
            setTimeout(discoverStickyBars, 60);
          };

          window.gpSetArticleTitle = function(nextTitle) {
            var titleEl = document.getElementById('gp-article-title');
            if (titleEl && typeof nextTitle === 'string' && nextTitle.length > 0) {
              titleEl.textContent = nextTitle;
            }
          };

          function isInteractiveTapTarget(startEl) {
            if (!startEl) return false;
            var sel = window.getSelection ? window.getSelection().toString() : '';
            if (sel && sel.trim().length > 0) return true;

            var el = startEl.nodeType === 1 ? startEl : startEl.parentElement;
            if (!el) return false;
            if (el.closest && el.closest(
              'a, button, input, select, textarea, label, option, summary, [role="button"], [role="link"], [role="option"], [role="radio"], [role="checkbox"], [role="tab"], [onclick], [data-gp-sticky-nav="1"], [data-correct], [data-is-correct], [data-option], [data-choice], [data-index]'
            )) {
              return true;
            }
            var cur = el;
            for (var d = 0; d < 4 && cur && cur !== document.body && cur.id !== 'gp-post-root' && cur.id !== 'gp-native-header'; d++) {
              var cls = (cur.className && typeof cur.className === 'string') ? cur.className : '';
              if (/\b(opt|option|options|choice|choices|btn|button|badge|pill|tab|pager|palette|q-num|qnum|nav-item|submit|reset|next|prev|toggle|expand|collapse|show-answer|quiz-btn|mcq-btn)\b/i.test(cls)) {
                return true;
              }
              var cs = window.getComputedStyle(cur);
              var rect = cur.getBoundingClientRect ? cur.getBoundingClientRect() : null;
              if (cs && cs.cursor === 'pointer' && rect && rect.height <= 220) {
                return true;
              }
              cur = cur.parentElement;
            }
            return false;
          }

          // Handle in-page #anchor jumps natively inside WebView and toggle top navigation on single non-interactive tap
          document.addEventListener('click', function(ev) {
            var anchor = ev.target && ev.target.closest ? ev.target.closest('a[href^="#"]') : null;
            if (anchor) {
              var hash = anchor.getAttribute('href') || '';
              if (hash.length > 1) {
                var target = document.getElementById(hash.slice(1));
                if (target) {
                  ev.preventDefault();
                  var stickyOffset = stickyBars.length > 0 ? (stickyBars[0].getBoundingClientRect().height + 10) : 8;
                  var targetTop = Math.max(0, target.getBoundingClientRect().top + window.scrollY - stickyOffset);
                  window.scrollTo({ top: targetTop, behavior: 'smooth' });
                  return;
                }
              }
            }
            if (ev.defaultPrevented || isInteractiveTapTarget(ev.target)) return;
            if (window.GyanBridge && window.GyanBridge.onToggleTopNav) {
              window.GyanBridge.onToggleTopNav();
            }
          });

          window.gpDiscoverStickyBars = discoverStickyBars;

          window.gpApplyTheme = function(fontPx, bg, text, card, border, darkMode, paperMode) {
            var root = document.documentElement.style;
            root.setProperty('--gp-font-size', fontPx + 'px');
            root.setProperty('--gp-bg', bg);
            root.setProperty('--gp-text', text);
            root.setProperty('--gp-card', card);
            root.setProperty('--gp-border', border);
            if (paperMode) {
              document.documentElement.classList.add('gp-eye-comfort');
            } else {
              document.documentElement.classList.remove('gp-eye-comfort');
            }
            document.querySelectorAll('#gp-post-root a').forEach(function(a) {
              a.style.color = darkMode ? '#60A5FA' : '#0F345B';
            });
            document.querySelectorAll('#gp-native-header .gp-pill-class').forEach(function(el) {
              el.style.background = darkMode ? 'rgba(15, 52, 91, 0.45)' : 'rgba(15, 52, 91, 0.10)';
              el.style.color = darkMode ? '#93C5FD' : '#0F345B';
            });
            document.querySelectorAll('#gp-native-header .gp-pill-subject').forEach(function(el) {
              el.style.color = darkMode ? '#C4B5FD' : '#6D28D9';
            });
            setTimeout(discoverStickyBars, 60);
          };

          if (window.MutationObserver) {
            var postRoot = document.getElementById('gp-post-root') || document.body;
            new MutationObserver(function(mutations) {
              for (var m = 0; m < mutations.length; m++) {
                var mut = mutations[m];
                if (mut.type === 'attributes' &&
                    (mut.attributeName === 'data-gp-sticky-nav' ||
                     mut.attributeName === 'data-gp-sticky-auto-bg')) {
                  continue;
                }
                var targetEl = mut.target && mut.target.nodeType === 1 ? mut.target : (mut.target ? mut.target.parentElement : null);
                var insideTrackedSticky = !!(targetEl && targetEl.closest && targetEl.closest('[data-gp-sticky-nav="1"]'));
                if (!insideTrackedSticky) {
                  scheduleStickyRefresh();
                  break;
                }
              }
            }).observe(postRoot, { childList: true, subtree: true, attributes: true });
          }

          window.addEventListener('load', discoverStickyBars);
          setTimeout(discoverStickyBars, 120);
          setTimeout(discoverStickyBars, 600);
        })();
      </script>
    </body>
    </html>
  """.trimIndent()
}

@Composable
private fun ViewModeChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(10.dp),
    color = if (selected) PrimaryNavy else extended.readerSurface,
    border = if (selected) null else BorderStroke(1.dp, extended.readerBorder),
    modifier = modifier.minimumInteractiveComponentSize(),
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (selected) Color.White else extended.readerTextPrimary,
      )
    }
  }
}

@Composable
private fun RenderCleanBlock(
  block: ArticleBlock,
  baseFontSp: Int,
  postTitle: String,
) {
  val extended = LocalGyanExtendedColors.current
  when (block) {
    is ArticleBlock.HeroBannerBlock -> UnitHeroBanner(block = block)
    is ArticleBlock.SectionHeaderBlock ->
      NumberedSectionHeader(
        number = block.number,
        heading = block.heading,
        fontSizeSp = baseFontSp,
      )
    is ArticleBlock.HeadingBlock ->
      Text(
        text = block.text,
        style =
          MaterialTheme.typography.titleLarge.copy(
            fontSize = (baseFontSp + (5 - block.level).coerceAtLeast(1)).sp,
            lineHeight = (baseFontSp + 10).sp,
            fontWeight = FontWeight.Bold,
          ),
        color = extended.readerTextPrimary,
      )
    is ArticleBlock.ParagraphBlock ->
      Text(
        text = block.text,
        style =
          MaterialTheme.typography.bodyLarge.copy(
            fontSize = baseFontSp.sp,
            lineHeight = (baseFontSp * 1.55f).sp,
          ),
        color = extended.readerTextPrimary,
      )
    is ArticleBlock.BulletListBlock ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = extended.readerSurface,
        border = BorderStroke(1.dp, extended.readerBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          block.items.forEach { itemText ->
            Row(verticalAlignment = Alignment.Top) {
              Box(
                modifier =
                  Modifier.padding(top = 8.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = itemText,
                style =
                  MaterialTheme.typography.bodyLarge.copy(
                    fontSize = baseFontSp.sp,
                    lineHeight = (baseFontSp * 1.5f).sp,
                  ),
                color = extended.readerTextPrimary,
              )
            }
          }
        }
      }
    is ArticleBlock.McqQuestionBlock ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = extended.mcqQuestion.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, extended.mcqQuestion.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = block.text,
          style =
            MaterialTheme.typography.bodyLarge.copy(
              fontSize = baseFontSp.sp,
              lineHeight = (baseFontSp * 1.48f).sp,
              fontWeight = FontWeight.Bold,
            ),
          color = extended.mcqQuestion,
          modifier = Modifier.padding(12.dp),
        )
      }
    is ArticleBlock.McqAnswerBlock ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = extended.mcqAnswer.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, extended.mcqAnswer.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = extended.mcqAnswer,
            modifier = Modifier.padding(top = 2.dp).size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = block.text,
            style =
              MaterialTheme.typography.bodyLarge.copy(
                fontSize = baseFontSp.sp,
                lineHeight = (baseFontSp * 1.45f).sp,
                fontWeight = FontWeight.SemiBold,
              ),
            color = extended.mcqAnswer,
          )
        }
      }
    is ArticleBlock.ImageBlock ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = extended.readerSurface,
        border = BorderStroke(1.dp, extended.readerBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        AsyncImage(
          model = block.url,
          contentDescription = block.alt.ifBlank { postTitle },
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
        )
      }
    is ArticleBlock.TableBlock ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = extended.readerSurface,
        border = BorderStroke(1.dp, extended.readerBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(10.dp)) {
          block.rows.forEachIndexed { rowIndex, row ->
            Row(
              modifier =
                Modifier.background(
                  if (rowIndex == 0) extended.cardSubtle else Color.Transparent
                )
                  .padding(vertical = 6.dp),
            ) {
              row.forEach { cell ->
                Text(
                  text = cell,
                  style =
                    MaterialTheme.typography.bodyMedium.copy(
                      fontSize = (baseFontSp - 1).coerceAtLeast(12).sp,
                      fontWeight = if (rowIndex == 0) FontWeight.Bold else FontWeight.Normal,
                    ),
                  color = extended.readerTextPrimary,
                  modifier = Modifier.widthIn(min = 100.dp, max = 220.dp).padding(horizontal = 8.dp),
                )
              }
            }
            if (rowIndex < block.rows.lastIndex) {
              HorizontalDivider(color = extended.readerBorder)
            }
          }
        }
      }
  }
}

@Composable
private fun ReaderFloatingToolbar(
  isSaved: Boolean,
  isPaperMode: Boolean,
  isTranslating: Boolean,
  isTranslated: Boolean,
  isPreparingPrint: Boolean,
  onBackClick: () -> Unit,
  onFontDecrease: () -> Unit,
  onFontIncrease: () -> Unit,
  onTranslateClick: () -> Unit,
  onTogglePaperMode: () -> Unit,
  onToggleBookmark: () -> Unit,
  onPrintClick: () -> Unit,
  onShareClick: () -> Unit,
) {
  val extended = LocalGyanExtendedColors.current
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = extended.readerSurface,
    border = BorderStroke(1.dp, extended.readerBorder),
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 4.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_back_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = stringResource(R.string.cd_back),
          tint = extended.readerTextPrimary,
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          onClick = onFontDecrease,
          shape = RoundedCornerShape(10.dp),
          color = extended.cardSubtle,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_font_decrease"),
        ) {
          Text(
            text = "A-",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = extended.readerTextPrimary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
          )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Surface(
          onClick = onFontIncrease,
          shape = RoundedCornerShape(10.dp),
          color = extended.cardSubtle,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_font_increase"),
        ) {
          Text(
            text = "A+",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = extended.readerTextPrimary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onTranslateClick,
          enabled = !isTranslating,
          modifier =
            Modifier.minimumInteractiveComponentSize()
              .clip(CircleShape)
              .background(
                if (isTranslated) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else Color.Transparent
              )
              .testTag("reader_translate_button"),
        ) {
          if (isTranslating) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary,
            )
          } else {
            Icon(
              imageVector = Icons.Rounded.Translate,
              contentDescription = stringResource(R.string.cd_translate),
              tint =
                if (isTranslated) MaterialTheme.colorScheme.primary
                else extended.readerTextSecondary,
            )
          }
        }
        IconButton(
          onClick = onTogglePaperMode,
          modifier =
            Modifier.minimumInteractiveComponentSize()
              .clip(CircleShape)
              .background(if (isPaperMode) AccentGold.copy(alpha = 0.22f) else Color.Transparent)
              .testTag("reader_paper_toggle"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Contrast,
            contentDescription = stringResource(R.string.cd_paper_mode),
            tint = if (isPaperMode) AccentGold else extended.readerTextSecondary,
          )
        }
        IconButton(
          onClick = onToggleBookmark,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_bookmark_toggle"),
        ) {
          Icon(
            imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = stringResource(R.string.cd_bookmark),
            tint = if (isSaved) AccentGold else extended.readerTextPrimary,
          )
        }
        IconButton(
          onClick = onPrintClick,
          enabled = !isPreparingPrint,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_print_button"),
        ) {
          if (isPreparingPrint) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary,
            )
          } else {
            Icon(
              imageVector = Icons.Rounded.Print,
              contentDescription = stringResource(R.string.cd_print),
              tint = extended.readerTextPrimary,
            )
          }
        }
        IconButton(
          onClick = onShareClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_share_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Share,
            contentDescription = stringResource(R.string.cd_share),
            tint = extended.readerTextPrimary,
          )
        }
      }
    }
  }
}

@Composable
private fun TranslateStatusBanner(
  uiState: TranslateUiState,
  onRestoreOriginal: () -> Unit,
  onRetry: (TranslationLanguage) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val isError = uiState is TranslateUiState.Error
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (isError) MaterialTheme.colorScheme.errorContainer else extended.readerSurface,
    border =
      BorderStroke(
        1.dp,
        if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else extended.readerBorder,
      ),
    modifier = modifier.fillMaxWidth().testTag("translate_status_banner"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      when (uiState) {
        TranslateUiState.Idle -> Unit
        is TranslateUiState.Loading -> {
          CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text =
              stringResource(
                R.string.translate_loading,
                "${uiState.targetLanguage.displayName} (${uiState.targetLanguage.nativeName})",
              ),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = extended.readerTextPrimary,
            modifier = Modifier.weight(1f),
          )
        }
        is TranslateUiState.Success -> {
          val lang = uiState.result.targetLanguage
          Icon(
            imageVector = Icons.Rounded.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text =
              stringResource(
                R.string.translate_active_label,
                "${lang.displayName} (${lang.nativeName})",
              ),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = extended.readerTextPrimary,
            modifier = Modifier.weight(1f),
          )
          TextButton(
            onClick = onRestoreOriginal,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("translate_restore_button"),
          ) {
            Text(
              text = stringResource(R.string.translate_original),
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            )
          }
        }
        is TranslateUiState.Error -> {
          Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
          )
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = stringResource(R.string.translate_error_title),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
              text = uiState.message,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.testTag("translate_error_message"),
            )
            OutlinedButton(
              onClick = { onRetry(uiState.targetLanguage) },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("translate_retry_button"),
            ) {
              Text(
                text = stringResource(R.string.translate_retry),
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("dismiss_translate_banner"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = stringResource(R.string.cd_dismiss_notice),
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslateLanguageBottomSheet(
  selectedLanguage: TranslationLanguage?,
  onSelectOriginal: () -> Unit,
  onSelectLanguage: (TranslationLanguage) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Translate,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Column {
          Text(
            text = stringResource(R.string.translate_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = stringResource(R.string.translate_sheet_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (selectedLanguage != null) {
        Surface(
          onClick = onSelectOriginal,
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.secondaryContainer,
          modifier =
            Modifier.fillMaxWidth()
              .minimumInteractiveComponentSize()
              .testTag("translate_option_original"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Refresh,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = stringResource(R.string.translate_restore_original),
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      HorizontalDivider()

      TranslationLanguages.supported.forEach { lang ->
        val isSelected = selectedLanguage?.code == lang.code
        Surface(
          onClick = { onSelectLanguage(lang) },
          shape = RoundedCornerShape(12.dp),
          color =
            if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier =
            Modifier.fillMaxWidth()
              .minimumInteractiveComponentSize()
              .testTag("translate_option_${lang.code}"),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                text = lang.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color =
                  if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                  else MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = lang.nativeName,
                style = MaterialTheme.typography.bodySmall,
                color =
                  if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (isSelected) {
              Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }
  }
}

private fun parseJsStringArray(rawJsResult: String?): List<String> {
  if (rawJsResult.isNullOrBlank() || rawJsResult == "null") return emptyList()
  val unwrapped =
    runCatching {
      val trimmed = rawJsResult.trim()
      if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
        org.json.JSONTokener(trimmed).nextValue() as? String ?: trimmed
      } else {
        trimmed
      }
    }.getOrDefault(rawJsResult)
  return runCatching {
    val arr = JSONArray(unwrapped)
    List(arr.length()) { idx -> arr.optString(idx, "") }
  }.getOrDefault(emptyList())
}

private fun extractCleanBlockSegments(blocks: List<ArticleBlock>): List<String> =
  buildList {
    blocks.forEach { block ->
      when (block) {
        is ArticleBlock.HeroBannerBlock -> Unit
        is ArticleBlock.SectionHeaderBlock -> add(block.heading)
        is ArticleBlock.HeadingBlock -> add(block.text)
        is ArticleBlock.ParagraphBlock -> add(block.text)
        is ArticleBlock.BulletListBlock -> block.items.forEach { add(it) }
        is ArticleBlock.McqQuestionBlock -> add(block.text)
        is ArticleBlock.McqAnswerBlock -> add(block.text)
        is ArticleBlock.ImageBlock -> Unit
        is ArticleBlock.TableBlock -> block.rows.forEach { row -> row.forEach { add(it) } }
      }
    }
  }

private fun applyCleanBlockTranslations(
  blocks: List<ArticleBlock>,
  translated: List<String>,
): List<ArticleBlock> {
  var cursor = 0
  fun next(fallback: String): String =
    translated.getOrNull(cursor++)?.takeIf { it.isNotBlank() } ?: fallback

  return blocks.map { block ->
    when (block) {
      is ArticleBlock.HeroBannerBlock -> block
      is ArticleBlock.SectionHeaderBlock -> block.copy(heading = next(block.heading))
      is ArticleBlock.HeadingBlock -> block.copy(text = next(block.text))
      is ArticleBlock.ParagraphBlock -> block.copy(text = next(block.text))
      is ArticleBlock.BulletListBlock -> block.copy(items = block.items.map { next(it) })
      is ArticleBlock.McqQuestionBlock -> block.copy(text = next(block.text))
      is ArticleBlock.McqAnswerBlock -> block.copy(text = next(block.text))
      is ArticleBlock.ImageBlock -> block
      is ArticleBlock.TableBlock ->
        block.copy(rows = block.rows.map { row -> row.map { next(it) } })
    }
  }
}

@Composable
private fun UnitHeroBanner(block: ArticleBlock.HeroBannerBlock) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(
          Brush.linearGradient(
            colors = listOf(HeroGradientStart, HeroGradientMid, HeroGradientEnd)
          )
        )
        .padding(20.dp),
  ) {
    Column {
      Text(
        text = block.banner.eyebrow,
        style =
          MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
          ),
        color = Color.White.copy(alpha = 0.9f),
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = block.banner.headline,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
        color = Color.White,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = block.banner.subheadline,
        style = MaterialTheme.typography.titleSmall,
        color = AccentGold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = block.banner.summary,
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.9f),
      )
    }
  }
}

@Composable
private fun NumberedSectionHeader(
  number: String,
  heading: String,
  fontSizeSp: Int,
) {
  val extended = LocalGyanExtendedColors.current
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = PrimaryNavy,
    ) {
      Text(
        text = number,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = AccentGold,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = heading,
      style =
        MaterialTheme.typography.titleMedium.copy(
          fontSize = (fontSizeSp + 3).sp,
          lineHeight = (fontSizeSp + 8).sp,
          fontWeight = FontWeight.Bold,
        ),
      color = extended.readerTextPrimary,
    )
  }
}

private fun shareStudyPost(context: Context, post: StudyPost) {
  val text = "${post.title}\n\n${post.snippet}\n\nRead on GYAN PETIKA: ${post.postUrl}"
  val sendIntent =
    Intent(Intent.ACTION_SEND).apply {
      putExtra(Intent.EXTRA_SUBJECT, post.title)
      putExtra(Intent.EXTRA_TEXT, text)
      type = "text/plain"
    }
  context.startActivity(Intent.createChooser(sendIntent, "Share Study Notes"))
}

private tailrec fun Context.findActivity(): Activity? =
  when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }

private var activePrintWebView: WebView? = null

private const val CAPTURE_LIVE_DOM_JS =
  """
  (function() {
    try {
      var root = document.getElementById('gp-post-root');
      if (!root) return { fallback: true };

      // 1. Sync live form states so innerHTML preserves user selections
      root.querySelectorAll('input').forEach(function(inp) {
        var type = (inp.type || '').toLowerCase();
        if (type === 'radio' || type === 'checkbox') {
          if (inp.checked) inp.setAttribute('checked', 'checked');
          else inp.removeAttribute('checked');
        } else if (inp.value !== undefined && inp.value !== null) {
          inp.setAttribute('value', inp.value);
        }
      });
      root.querySelectorAll('textarea').forEach(function(ta) {
        ta.textContent = ta.value || '';
      });
      root.querySelectorAll('option').forEach(function(opt) {
        if (opt.selected) opt.setAttribute('selected', 'selected');
        else opt.removeAttribute('selected');
      });

      // Clear any active text selection or focus highlight before snapshotting
      if (window.getSelection) {
        try { window.getSelection().removeAllRanges(); } catch (e) {}
      }
      if (document.activeElement && document.activeElement.blur) {
        try { document.activeElement.blur(); } catch (e) {}
      }

      // 2. Capture live attempted-wrong & corrected visual states from classes and computed colors
      root.querySelectorAll('[data-gp-print-state]').forEach(function(el) {
        el.removeAttribute('data-gp-print-state');
      });

      function parseRgb(str) {
        if (!str || str === 'transparent') return null;
        var m = str.match(/rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)/i);
        if (!m) return null;
        var a = m[4] !== undefined ? parseFloat(m[4]) : 1;
        if (a < 0.12) return null;
        return { r: parseInt(m[1], 10), g: parseInt(m[2], 10), b: parseInt(m[3], 10), a: a };
      }
      function isGreenColor(c) { return c && (c.g - c.r >= 18) && (c.g - c.b >= 14); }
      function isRedColor(c) { return c && (c.r - c.g >= 26) && (c.r - c.b >= 26) && (c.g - c.b <= 35); }

      var hasAttempted = false;
      function inspectAndTag(el) {
        if (el.closest && el.closest('[data-gp-print-state]')) return;
        var cls = (el.className && typeof el.className === 'string') ? el.className : '';
        if (/\b(mcq-q|mcq-question|gp-section|gp-table-wrap)\b/i.test(cls)) return;
        if (el.closest && el.closest('.mcq-q, .mcq-question')) return;
        if (/^H[1-6]${'$'}/.test(el.tagName)) return;
        // Skip large multi-option containers so we only highlight specific options/feedback
        if (el.querySelectorAll && el.querySelectorAll('input[type="radio"], input[type="checkbox"]').length > 1) return;
        if (el.querySelectorAll && el.querySelectorAll('.option, .opt, .choice').length > 1) return;
        if (el.children && el.children.length > 5) return;

        var lowerCls = ' ' + cls.toLowerCase() + ' ';
        var state = null;
        if (/\b(wrong|incorrect|is-wrong|ans-wrong|wrong-answer|option-wrong|user-wrong|btn-danger|alert-danger|error-opt)\b/.test(lowerCls)) {
          state = 'wrong';
        } else if (/\b(correct|right|is-correct|ans-correct|correct-answer|option-correct|mcq-a|btn-success|alert-success|true-ans)\b/.test(lowerCls)) {
          state = 'correct';
        }

        if (!state) {
          var cs = window.getComputedStyle(el);
          if (cs.display === 'none' || cs.visibility === 'hidden') return;
          var bg = parseRgb(cs.backgroundColor);
          var fg = parseRgb(cs.color);
          var bd = parseRgb(cs.borderTopColor) || parseRgb(cs.borderLeftColor);
          var looksQuizEl = /\b(opt|option|choice|mcq|quiz|ans|answer|feedback|result|status|badge)\b/i.test(cls) ||
            el.tagName === 'LABEL' || el.tagName === 'LI' || el.tagName === 'BUTTON' ||
            !!el.querySelector('input[type="radio"], input[type="checkbox"]');
          if (looksQuizEl) {
            if (isGreenColor(bg) || isGreenColor(bd) || isGreenColor(fg)) {
              state = 'correct';
            } else if (isRedColor(bg) || isRedColor(bd) || isRedColor(fg)) {
              state = 'wrong';
            }
          }
        }

        if (state) {
          el.setAttribute('data-gp-print-state', state);
          hasAttempted = true;
        }
      }

      // Inspect outer option cards before inner spans so circular badges & icons stay untouched
      root.querySelectorAll('button, li, label, div, p, td, a').forEach(inspectAndTag);
      root.querySelectorAll('span').forEach(inspectAndTag);

      root.querySelectorAll('input[type="radio"]:checked, input[type="checkbox"]:checked').forEach(function(inp) {
        hasAttempted = true;
      });

      var headStyles = '';
      document.head.querySelectorAll('style').forEach(function(st) {
        if (st.id !== 'gp-theme-style') {
          headStyles += st.outerHTML + '\n';
        }
      });

      return {
        fallback: false,
        headStyles: headStyles,
        bodyHtml: root.innerHTML,
        hasAttempted: hasAttempted
      };
    } catch (e) {
      return { fallback: true, error: String(e) };
    }
  })();
  """

private data class PrintableSnapshot(
  val rawHtml: String,
  val extraHeadStyles: String,
  val preserveAttemptedSnapshot: Boolean,
)

@SuppressLint("SetJavaScriptEnabled")
private fun capturePrintableSnapshot(
  post: StudyPost,
  liveWebView: WebView? = null,
  onCaptured: (PrintableSnapshot) -> Unit,
) {
  val mainHandler = Handler(Looper.getMainLooper())
  var completed = false

  fun deliver(snapshot: PrintableSnapshot) {
    if (completed) return
    completed = true
    onCaptured(snapshot)
  }

  val fallbackSnapshot =
    PrintableSnapshot(
      rawHtml = post.htmlContent,
      extraHeadStyles = "",
      preserveAttemptedSnapshot = false,
    )

  if (liveWebView != null) {
    // Safety fallback if the live WebView is detached or stalled while evaluating JS
    mainHandler.postDelayed({ deliver(fallbackSnapshot) }, 1500L)
    try {
      liveWebView.evaluateJavascript(CAPTURE_LIVE_DOM_JS) { rawJson ->
        val parsed =
          runCatching {
            if (rawJson.isNullOrBlank() || rawJson == "null") null else org.json.JSONObject(rawJson)
          }.getOrNull()
        val useFallback = parsed == null || parsed.optBoolean("fallback", true)
        if (useFallback) {
          deliver(fallbackSnapshot)
        } else {
          deliver(
            PrintableSnapshot(
              rawHtml = parsed.optString("bodyHtml", post.htmlContent).ifBlank { post.htmlContent },
              extraHeadStyles = parsed.optString("headStyles", ""),
              preserveAttemptedSnapshot = parsed.optBoolean("hasAttempted", false),
            )
          )
        }
      }
      return
    } catch (_: Throwable) {
      // Fall back cleanly to raw post HTML below
    }
  }

  deliver(fallbackSnapshot)
}

@SuppressLint("SetJavaScriptEnabled")
private fun launchPrintWebView(
  context: Context,
  post: StudyPost,
  rawHtml: String,
  extraHeadStyles: String,
  preserveAttemptedSnapshot: Boolean,
  onPrintDispatched: () -> Unit = {},
) {
  val activity = context.findActivity() ?: context
  val mainHandler = Handler(Looper.getMainLooper())
  val printWebView = WebView(activity)
  activePrintWebView = printWebView
  var printTriggered = false

  fun triggerSystemPrint() {
    if (printTriggered) return
    printTriggered = true
    onPrintDispatched()
    val printManager =
      (activity.getSystemService(Context.PRINT_SERVICE)
        ?: context.getSystemService(Context.PRINT_SERVICE)) as? PrintManager
    if (printManager == null) {
      Toast.makeText(context, "Print service is unavailable on this device.", Toast.LENGTH_SHORT).show()
      return
    }
    val jobName = "GYAN PETIKA - ${post.title}"
    val adapter = printWebView.createPrintDocumentAdapter(jobName)
    printManager.print(jobName, adapter, PrintAttributes.Builder().build())
  }

  printWebView.settings.apply {
    javaScriptEnabled = true
    domStorageEnabled = true
    loadsImagesAutomatically = true
    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
  }

  printWebView.webViewClient =
    object : WebViewClient() {
      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.evaluateJavascript(
          "window.gpPrepareMcqsForPrint && window.gpPrepareMcqsForPrint();"
        ) {
          mainHandler.postDelayed({ triggerSystemPrint() }, 400L)
        }
        mainHandler.postDelayed({ triggerSystemPrint() }, 1300L)
      }
    }

  // Outer safety guard in case an external image or script delays onPageFinished
  mainHandler.postDelayed(
    {
      if (!printTriggered) {
        printWebView.evaluateJavascript(
          "window.gpPrepareMcqsForPrint && window.gpPrepareMcqsForPrint();"
        ) {
          triggerSystemPrint()
        }
        mainHandler.postDelayed({ triggerSystemPrint() }, 350L)
      }
    },
    4000L,
  )

  val htmlDoc =
    buildPrintablePostHtml(
      rawHtml = rawHtml,
      extraHeadStyles = extraHeadStyles,
      preserveAttemptedSnapshot = preserveAttemptedSnapshot,
    )
  printWebView.loadDataWithBaseURL(
    BundledContent.BLOG_BASE_URL,
    htmlDoc,
    "text/html",
    "UTF-8",
    null,
  )
}

private val SCRIPT_TAG_REGEX = Regex("(?is)<script\\b[^>]*>.*?</script>")

internal fun buildPrintablePostHtml(
  rawHtml: String,
  extraHeadStyles: String = "",
  preserveAttemptedSnapshot: Boolean = false,
): String {
  val frozenTemplateHtml =
    if (preserveAttemptedSnapshot) {
      val noScripts = SCRIPT_TAG_REGEX.replace(rawHtml, "")
      "<template id=\"gp-frozen-snapshot\">$noScripts</template>"
    } else {
      ""
    }
  return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      $extraHeadStyles
      <style>
        :root {
          color-scheme: light;
          --gp-bg: #FFFFFF;
          --gp-text: #111827;
          --gp-card: #F8FAFC;
          --gp-border: #CBD5E1;
          --gp-font-size: 14px;
        }
        * {
          box-sizing: border-box;
          max-width: 100%;
          -webkit-print-color-adjust: exact !important;
          color-adjust: exact !important;
          print-color-adjust: exact !important;
          -webkit-tap-highlight-color: transparent !important;
          outline: none !important;
        }
        ::selection {
          background: transparent !important;
          color: inherit !important;
        }
        html, body {
          margin: 0;
          padding: 0;
          background: #FFFFFF !important;
          color: #111827 !important;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          font-size: 14px;
          line-height: 1.6;
          word-wrap: break-word;
        }
        body { padding: 10px; }
        img, video { max-width: 100% !important; height: auto !important; border-radius: 8px; }
        iframe { max-width: 100% !important; }
        :where(a) { color: #0F345B; font-weight: 600; text-decoration: none; }
        h1, h2, h3, h4, h5, h6, [data-gp-topic-heading="1"] {
          line-height: 1.35;
          margin-top: 1em;
          margin-bottom: 0.45em;
          break-after: avoid !important;
          page-break-after: avoid !important;
        }
        p, li { orphans: 3; widows: 3; }
        .gp-topic-group, .gp-topic-pair { display: block; }
        [data-gp-keep-together="1"], .gp-topic-pair {
          break-inside: avoid !important;
          page-break-inside: avoid !important;
        }
        p { margin: 0.55em 0; }
        ul, ol { padding-left: 1.35em; margin: 0.5em 0; }
        li { margin: 0.3em 0; }
        table { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 0.92em; }
        th, td { border: 1px solid #CBD5E1; padding: 7px 9px; text-align: left; }
        th { background: #F1F5F9; font-weight: 700; }
        blockquote {
          margin: 10px 0;
          padding: 9px 12px;
          border-left: 4px solid #F5B82E;
          background: #F8FAFC;
          border-radius: 0 8px 8px 0;
        }
        .gp-section {
          margin: 12px 0;
          padding: 12px;
          border: 1px solid #CBD5E1;
          border-radius: 12px;
          background: #F8FAFC;
        }
        .gp-section::before {
          content: attr(data-num);
          display: inline-block;
          background: #0F345B;
          color: #F5B82E;
          font-weight: 800;
          font-size: 0.8em;
          padding: 3px 9px;
          border-radius: 7px;
          margin-bottom: 6px;
        }
        .mcq-q {
          color: #B91C1C !important;
          font-weight: 700;
          background-color: #FEF2F2 !important;
          padding: 9px 11px;
          border-radius: 8px;
          border: 1px solid #FECACA !important;
          break-inside: avoid;
          page-break-inside: avoid;
        }
        /* Zero-specificity :where() fallbacks preserve native post badges, grids, and option styles */
        :where(.wrong, .incorrect, .is-wrong, .ans-wrong, .wrong-answer, .option-wrong, .btn-danger, .alert-danger, [data-gp-print-state="wrong"]) {
          color: #991B1B;
          font-weight: 700;
          background-color: #FEE2E2;
          border: 1.5px solid #EF4444;
          border-radius: 10px;
        }
        :where(.mcq-a, .correct, .right, .is-correct, .ans-correct, .correct-answer, .option-correct, .btn-success, .alert-success, [data-correct="true" i], [data-correct="1"], [data-is-correct="true" i], [data-is-correct="1"], [data-right="true" i], [data-gp-print-state="correct"]) {
          color: #14532D;
          font-weight: 700;
          background-color: #DCFCE7;
          border: 1.5px solid #22C55E;
          border-radius: 10px;
        }
        .gp-print-answer {
          color: #14532D !important;
          font-weight: 700 !important;
          background-color: #DCFCE7 !important;
          padding: 8px 11px;
          border-radius: 8px;
          border: 1.5px solid #22C55E !important;
          border-left: 5px solid #16A34A !important;
          break-inside: avoid;
          page-break-inside: avoid;
        }
        input[type="radio"], input[type="checkbox"] {
          accent-color: #0F345B !important;
        }
        input[type="radio"]:checked + label,
        input[type="checkbox"]:checked + label {
          font-weight: 700 !important;
        }
        [class*="mcq" i], [id*="mcq" i], [class*="quiz" i], [id*="quiz" i],
        [class*="answer" i], [id*="answer" i], [class*="solution" i], [class*="explanation" i] {
          overflow: visible !important;
          max-height: none !important;
          height: auto !important;
        }
        details {
          margin: 8px 0;
          padding: 9px 11px;
          border: 1px solid #CBD5E1;
          border-radius: 8px;
          background: #F8FAFC;
        }
        summary { font-weight: 700; }
        :where(button) {
          padding: 6px 12px;
          border-radius: 7px;
          border: 1px solid #94A3B8;
          background: #F8FAFC;
          color: #0F172A;
          font-weight: 600;
          margin: 3px 4px 3px 0;
        }
        @media print {
          * {
            -webkit-print-color-adjust: exact !important;
            color-adjust: exact !important;
            print-color-adjust: exact !important;
          }
          body { padding: 6px; background: #FFFFFF !important; }
          details { display: block !important; }
          details > * { display: block !important; }
          [class*="mcq" i], [id*="mcq" i], [class*="quiz" i], [id*="quiz" i],
          [class*="question" i], [class*="option" i], [class*="choice" i],
          [class*="answer" i], [id*="answer" i], [class*="solution" i],
          [class*="explanation" i], [class*="key" i], [class*="correct" i], [class*="wrong" i] {
            max-height: none !important;
            height: auto !important;
            overflow: visible !important;
          }
          [data-gp-qcard="1"], [data-gp-keep-together="1"], .gp-topic-pair,
          tr, blockquote, figure, pre, .mcq-q, .mcq-a, .gp-print-answer,
          [data-gp-print-state="correct"], [data-gp-print-state="wrong"] {
            break-inside: avoid !important;
            page-break-inside: avoid !important;
            -webkit-column-break-inside: avoid !important;
            overflow: visible !important;
          }
          .gp-topic-group, .gp-section, section, article {
            overflow: visible !important;
          }
          h1, h2, h3, h4, h5, h6, [data-gp-topic-heading="1"] {
            break-after: avoid !important;
            page-break-after: avoid !important;
          }
          [data-gp-sticky-nav="1"] {
            position: static !important;
            translate: none !important;
            transform: none !important;
            box-shadow: none !important;
          }
          p, li {
            orphans: 3;
            widows: 3;
          }
        }
      </style>
    </head>
    <body>
      <div id="gp-print-root">$rawHtml</div>
      $frozenTemplateHtml
      <script>
        (function() {
          function restoreAttemptedSnapshotIfWiped() {
            var tpl = document.getElementById('gp-frozen-snapshot');
            var root = document.getElementById('gp-print-root');
            if (!tpl || !root) return;
            var liveMarkers = root.querySelectorAll('[data-gp-print-state], input:checked').length;
            var frozenMarkers = tpl.content ? tpl.content.querySelectorAll('[data-gp-print-state], input[checked]').length : 0;
            if (frozenMarkers > 0 && liveMarkers === 0) {
              root.innerHTML = tpl.innerHTML;
            }
          }

          function syncCheckedProperties(root) {
            root.querySelectorAll('input[checked]').forEach(function(inp) {
              inp.checked = true;
            });
            root.querySelectorAll('option[selected]').forEach(function(opt) {
              opt.selected = true;
            });
          }

          function hasRevealedAnswer(container) {
            if (!container) return false;
            var found = container.querySelector(
              '[data-gp-print-state="correct"], .correct, .right, .is-correct, .ans-correct, .correct-answer, .option-correct, .mcq-a, .gp-print-answer'
            );
            if (found) {
              var cs = window.getComputedStyle(found);
              if (cs.display !== 'none' && cs.visibility !== 'hidden') return true;
            }
            return false;
          }

          function hasAttemptedState(container) {
            if (!container) return false;
            return !!container.querySelector(
              '[data-gp-print-state], .wrong, .incorrect, .correct, .right, .selected, .attempted, input:checked'
            );
          }

          function revealAnswerElements() {
            var root = document.getElementById('gp-print-root') || document.body;
            root.querySelectorAll('details').forEach(function(d) {
              d.open = true;
              d.setAttribute('open', '');
            });

            var selectors = [
              '[class="ans" i]',
              '[class^="ans" i]',
              '[class*=" ans" i]',
              '[class*="answer" i]',
              '[id*="answer" i]',
              '[class*="mcq-a" i]',
              '[class*="ans-" i]',
              '[class*="-ans" i]',
              '[class*="ans_" i]',
              '[class*="_ans" i]',
              '[id*="ans" i]',
              '[class*="solution" i]',
              '[id*="solution" i]',
              '[class="sol" i]',
              '[class^="sol" i]',
              '[class*=" sol" i]',
              '[class*="explanation" i]',
              '[id*="explanation" i]',
              '[class*="key" i]',
              '[id*="key" i]',
              '[class*="reveal" i]',
              '[data-answer]',
              '[data-ans]',
              '[data-correct-answer]'
            ].join(',');

            root.querySelectorAll(selectors).forEach(function(el) {
              var tag = el.tagName;
              if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'LINK' || tag === 'META' || tag === 'TEMPLATE') return;
              var clsId = ((el.className || '') + ' ' + (el.id || '')).toLowerCase();
              // Avoid unhiding hidden "Wrong! Try again" error templates unless attempted
              if (/wrong|incorrect|try-again|error/.test(clsId) && !el.getAttribute('data-gp-print-state')) return;

              el.removeAttribute('hidden');
              el.removeAttribute('aria-hidden');
              if (el.classList) {
                el.classList.remove('d-none', 'hidden');
                if (el.classList.contains('collapse')) {
                  el.classList.add('show');
                }
              }
              var cs = window.getComputedStyle(el);
              if (cs.display === 'none') {
                var targetDisplay = (tag === 'SPAN' || tag === 'A' || tag === 'STRONG' || tag === 'B') ? 'inline-block' : 'block';
                el.style.setProperty('display', targetDisplay, 'important');
              }
              if (cs.visibility === 'hidden') {
                el.style.setProperty('visibility', 'visible', 'important');
              }
              if (cs.opacity === '0') {
                el.style.setProperty('opacity', '1', 'important');
              }
              el.style.setProperty('max-height', 'none', 'important');
              el.style.setProperty('height', 'auto', 'important');
              el.style.setProperty('overflow', 'visible', 'important');
            });
          }

          function prepareMcqsForPrint() {
            restoreAttemptedSnapshotIfWiped();
            var root = document.getElementById('gp-print-root') || document.body;
            syncCheckedProperties(root);

            // 1. Click answer/solution/submit reveal buttons once when safe
            var revealRegex = /(show|view|reveal|check|display|see|get|toggle|all)\s*(all\s*)?(the\s*)?(answer|answers|ans|solution|solutions|explanation|key|result|score)|^(answer|answers|ans|solution|solutions|answer key|show|reveal|check|submit|submit quiz|submit test|finish quiz)${'$'}/i;
            var blockRegex = /hide|reset|clear|retry|restart|again|prev|next|back|home|share|print/i;
            root.querySelectorAll('button, a, [role="button"], input[type="button"], input[type="submit"], .show-answer, .btn-answer, .submit-quiz').forEach(function(btn) {
              if (btn.dataset && btn.dataset.gpClicked === '1') return;
              if (btn.tagName === 'A') {
                var href = (btn.getAttribute('href') || '').trim();
                if (href && href !== '#' && href.indexOf('javascript:') !== 0) return;
              }
              var label = ((btn.textContent || btn.value || '') + ' ' + (btn.className || '') + ' ' + (btn.id || '')).trim();
              if (!revealRegex.test(label) || blockRegex.test(label)) return;

              var qBox = btn.closest('.mcq, .question, .quiz-item, .gp-mcq, [class*="question" i], [class*="mcq" i]');
              if (qBox && hasRevealedAnswer(qBox)) return;

              if (btn.dataset) btn.dataset.gpClicked = '1';
              try { btn.click(); } catch (e) {}
            });

            // 2. For UNATTEMPTED questions, click & highlight explicitly marked correct options
            root.querySelectorAll('[data-correct], [data-is-correct="true" i], [data-is-correct="1"], [data-right="true" i], [data-right="1"], [data-valid="true" i]').forEach(function(opt) {
              var dc = (opt.getAttribute('data-correct') || '').trim().toLowerCase();
              var isBooleanTrue = (dc === '' && opt.hasAttribute('data-is-correct')) ||
                dc === 'true' || dc === '1' || dc === 'yes' ||
                (opt.getAttribute('data-is-correct') || '').toLowerCase() === 'true' ||
                opt.getAttribute('data-is-correct') === '1' ||
                (opt.getAttribute('data-right') || '').toLowerCase() === 'true' ||
                opt.getAttribute('data-right') === '1' ||
                (opt.getAttribute('data-valid') || '').toLowerCase() === 'true';
              if (!isBooleanTrue) return;

              var qBox = opt.closest('.mcq, .question, .quiz-item, .gp-mcq, [class*="question" i], [class*="mcq" i]');
              if (!qBox || !hasAttemptedState(qBox)) {
                if (opt.dataset && opt.dataset.gpClicked !== '1') {
                  opt.dataset.gpClicked = '1';
                  try { opt.click(); } catch (e) {}
                }
              }
              opt.classList.add('gp-print-answer');
              opt.setAttribute('data-gp-print-state', 'correct');
            });

            // Also detect inline onclick handlers passing true (e.g., checkAnswer(this, true)) on unattempted questions
            root.querySelectorAll('[onclick]').forEach(function(opt) {
              var oc = opt.getAttribute('onclick') || '';
              if (!/,\s*true\s*\)|'correct'|"correct"/i.test(oc)) return;
              var qBox = opt.closest('.mcq, .question, .quiz-item, .gp-mcq, [class*="question" i], [class*="mcq" i]');
              if (!qBox || !hasAttemptedState(qBox)) {
                if (opt.dataset && opt.dataset.gpClicked !== '1') {
                  opt.dataset.gpClicked = '1';
                  try { opt.click(); } catch (e) {}
                }
              }
              opt.classList.add('gp-print-answer');
              opt.setAttribute('data-gp-print-state', 'correct');
            });

            // 3. Render data-answer / data-ans / data-correct-answer / value-style data-correct visibly
            root.querySelectorAll('[data-answer], [data-ans], [data-correct-answer], [data-correct]').forEach(function(node) {
              if (node.dataset && node.dataset.gpInjected === '1') return;
              var rawDc = (node.getAttribute('data-correct') || '').trim();
              var dcAnswer = (rawDc && !/^(true|false|1|0|yes|no)$/i.test(rawDc)) ? rawDc : '';
              var ans = (node.getAttribute('data-answer') || node.getAttribute('data-ans') || node.getAttribute('data-correct-answer') || dcAnswer || '').trim();
              if (!ans) return;
              var txt = (node.textContent || '').toLowerCase();
              if (txt.indexOf('answer') === -1 && txt.indexOf('ans:') === -1) {
                var ansDiv = document.createElement('div');
                ansDiv.className = 'mcq-a gp-print-answer';
                ansDiv.setAttribute('data-gp-print-state', 'correct');
                ansDiv.textContent = 'Answer: ' + ans;
                node.appendChild(ansDiv);
              }
              if (node.dataset) node.dataset.gpInjected = '1';
            });

            // 4. Open all details & unhide all answer/solution/key containers
            revealAnswerElements();

            // 5. Mark single-question cards so they don't split across pages when printing
            root.querySelectorAll('.mcq, .question, .quiz-item, .q-card, .question-card, .quiz-card, .mcq-card, [class*="question" i], [class*="q-card" i]').forEach(function(card) {
              if (card.querySelectorAll('[class*="question" i], .q-card, .mcq').length === 0 &&
                  card.querySelectorAll('button, input[type="radio"], .option, .opt, .choice').length <= 8) {
                card.setAttribute('data-gp-qcard', '1');
                card.setAttribute('data-gp-keep-together', '1');
              }
            });
          }

          window.gpPrepareMcqsForPrint = prepareMcqsForPrint;
          prepareMcqsForPrint();
          window.addEventListener('load', function() {
            prepareMcqsForPrint();
          });
        })();
      </script>
    </body>
    </html>
  """.trimIndent()
}
