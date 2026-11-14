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
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.data.UserPreferences
import com.example.ui.components.ArticleBlock
import com.example.ui.components.ArticleHtmlParser
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BadgePurple
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleReaderScreen(
  post: StudyPost,
  preferences: UserPreferences,
  onBackClick: () -> Unit,
  onFontDecrease: () -> Unit,
  onFontIncrease: () -> Unit,
  onTogglePaperMode: () -> Unit,
  onToggleBookmark: () -> Unit,
  onLabelClick: (String) -> Unit,
  onInternalPostLinkClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val extended = LocalGyanExtendedColors.current
  val isSaved = post.id in preferences.savedPostIds
  val baseFontSp = preferences.customFontSizeSp
  var useFullPostView by rememberSaveable(post.id) { mutableStateOf(true) }
  var activePostWebView by remember(post.id) { mutableStateOf<WebView?>(null) }
  val cleanBlocks = remember(post.id, post.htmlContent) { ArticleHtmlParser.parse(post) }

  DisposableEffect(post.id, useFullPostView) {
    if (!useFullPostView) {
      activePostWebView = null
    }
    onDispose {
      activePostWebView = null
    }
  }

  Column(
    modifier = modifier.fillMaxSize().background(extended.readerBackground),
  ) {
    ReaderFloatingToolbar(
      isSaved = isSaved,
      isPaperMode = preferences.eyeComfortPaperMode,
      onBackClick = onBackClick,
      onFontDecrease = onFontDecrease,
      onFontIncrease = onFontIncrease,
      onTogglePaperMode = onTogglePaperMode,
      onToggleBookmark = onToggleBookmark,
      onPrintClick = {
        printStudyPost(
          context = context,
          post = post,
          liveWebView = if (useFullPostView) activePostWebView else null,
        )
      },
      onShareClick = { shareStudyPost(context, post) },
    )

    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
      // Metadata Header
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
          text = post.title,
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

        // Full Post vs Clean Reader Mode Switcher
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ViewModeChip(
            label = "Full Post (Rich HTML)",
            selected = useFullPostView,
            onClick = { useFullPostView = true },
            modifier = Modifier.weight(1f).testTag("mode_full_post"),
          )
          ViewModeChip(
            label = "Clean Reader",
            selected = !useFullPostView,
            onClick = { useFullPostView = false },
            modifier = Modifier.weight(1f).testTag("mode_clean_reader"),
          )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = extended.readerBorder)
      }

      if (post.banner != null) {
        UnitHeroBanner(block = ArticleBlock.HeroBannerBlock(post.banner))
      }

      if (useFullPostView) {
        FullPostHtmlWebView(
          post = post,
          fontSizeSp = baseFontSp,
          isDark = extended.isDark,
          isPaperMode = extended.isPaperMode,
          onLabelClick = onLabelClick,
          onInternalPostLinkClick = onInternalPostLinkClick,
          onWebViewReady = { activePostWebView = it },
        )
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          cleanBlocks.filterNot { it is ArticleBlock.HeroBannerBlock }.forEach { block ->
            RenderCleanBlock(block = block, baseFontSp = baseFontSp, postTitle = post.title)
          }
        }
      }

      // Labels & Original Post Footer
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = extended.readerSurface,
        border = BorderStroke(1.dp, extended.readerBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (post.labels.isNotEmpty()) {
            Text(
              text = "Post Topics & Labels",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = extended.readerTextPrimary,
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              post.labels.forEach { label ->
                Surface(
                  onClick = { onLabelClick(label) },
                  shape = RoundedCornerShape(999.dp),
                  color = extended.cardSubtle,
                  border = BorderStroke(1.dp, extended.readerBorder),
                  modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  )
                }
              }
            }
          }

          OutlinedButton(
            onClick = {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.postUrl))
              context.startActivity(intent)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("open_original_post"),
          ) {
            Text("View Original Post & Comments on Gyan Petika")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))
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
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun FullPostHtmlWebView(
  post: StudyPost,
  fontSizeSp: Int,
  isDark: Boolean,
  isPaperMode: Boolean,
  onLabelClick: (String) -> Unit,
  onInternalPostLinkClick: (String) -> Unit,
  onWebViewReady: (WebView?) -> Unit,
) {
  val context = LocalContext.current
  val density = LocalDensity.current.density
  var contentHeightPx by remember(post.id) { mutableIntStateOf((320 * density).toInt()) }

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

  val latestFontSizeSp by rememberUpdatedState(fontSizeSp)
  val latestBgHex by rememberUpdatedState(bgHex)
  val latestTextHex by rememberUpdatedState(textHex)
  val latestCardBgHex by rememberUpdatedState(cardBgHex)
  val latestBorderHex by rememberUpdatedState(borderHex)
  val latestIsDark by rememberUpdatedState(isDark)
  val latestIsPaperMode by rememberUpdatedState(isPaperMode)
  val latestOnLabelClick by rememberUpdatedState(onLabelClick)
  val latestOnInternalPostLinkClick by rememberUpdatedState(onInternalPostLinkClick)

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
      )
    }

  val minHeightDp = with(LocalDensity.current) { contentHeightPx.toDp() }.coerceAtLeast(240.dp)

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.Transparent,
    border = BorderStroke(1.dp, LocalGyanExtendedColors.current.readerBorder),
    modifier = Modifier.fillMaxWidth(),
  ) {
    AndroidView(
      modifier = Modifier.fillMaxWidth().height(minHeightDp).clip(RoundedCornerShape(16.dp)),
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams =
            ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT,
            )
          setBackgroundColor(AndroidColor.TRANSPARENT)
          isVerticalScrollBarEnabled = false
          isHorizontalScrollBarEnabled = false
          isNestedScrollingEnabled = false

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
              fun onHeightChanged(heightCssPx: Float) {
                val nextPx = (heightCssPx * density + 24f).toInt()
                if (nextPx > 120) {
                  post { contentHeightPx = nextPx }
                }
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
                view?.evaluateJavascript(
                  """
                  (function() {
                    function report() {
                      if (window.GyanBridge && document.body) {
                        GyanBridge.onHeightChanged(document.documentElement.scrollHeight || document.body.scrollHeight);
                      }
                    }
                    report();
                    setTimeout(report, 250);
                    setTimeout(report, 800);
                  })();
                  """.trimIndent(),
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
      },
    )
  }
}

private fun buildRichPostHtml(
  rawHtml: String,
  fontSizeSp: Int,
  bgHex: String,
  textHex: String,
  cardBgHex: String,
  borderHex: String,
  isDark: Boolean,
  isPaperMode: Boolean,
): String {
  val rootClass = if (isPaperMode) " class=\"gp-eye-comfort\"" else ""
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
          overflow-x: hidden;
          word-wrap: break-word;
        }
        body { padding: 10px 8px; }
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
        button {
          padding: 8px 14px;
          border-radius: 8px;
          border: 1px solid var(--gp-border);
          background: #0F345B;
          color: #FFFFFF;
          font-weight: 600;
          cursor: pointer;
        }
      </style>
    </head>
    <body>
      <div id="gp-post-root">$rawHtml</div>
      <script>
        (function() {
          // Wrap wide tables for smooth horizontal scrolling
          document.querySelectorAll('table').forEach(function(t) {
            if (!t.parentElement || !t.parentElement.classList.contains('gp-table-wrap')) {
              var w = document.createElement('div');
              w.className = 'gp-table-wrap';
              t.parentNode.insertBefore(w, t);
              w.appendChild(t);
            }
          });
          function notifyHeight() {
            if (window.GyanBridge && document.documentElement) {
              window.GyanBridge.onHeightChanged(document.documentElement.scrollHeight);
            }
          }
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
            document.querySelectorAll('a').forEach(function(a) {
              a.style.color = darkMode ? '#60A5FA' : '#0F345B';
            });
            setTimeout(notifyHeight, 60);
          };
          if (window.ResizeObserver) {
            new ResizeObserver(notifyHeight).observe(document.body);
          }
          if (window.MutationObserver) {
            new MutationObserver(notifyHeight).observe(document.body, { childList: true, subtree: true, attributes: true });
          }
          window.addEventListener('load', notifyHeight);
          setTimeout(notifyHeight, 120);
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
  onBackClick: () -> Unit,
  onFontDecrease: () -> Unit,
  onFontIncrease: () -> Unit,
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
    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
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
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_print_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Print,
            contentDescription = stringResource(R.string.cd_print),
            tint = extended.readerTextPrimary,
          )
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

@SuppressLint("SetJavaScriptEnabled")
private fun printStudyPost(
  context: Context,
  post: StudyPost,
  liveWebView: WebView? = null,
) {
  if (liveWebView != null) {
    try {
      liveWebView.evaluateJavascript(CAPTURE_LIVE_DOM_JS) { rawJson ->
        val parsed =
          runCatching {
            if (rawJson.isNullOrBlank() || rawJson == "null") null else org.json.JSONObject(rawJson)
          }.getOrNull()
        val useFallback = parsed == null || parsed.optBoolean("fallback", true)
        if (useFallback) {
          launchPrintWebView(
            context = context,
            post = post,
            rawHtml = post.htmlContent,
            extraHeadStyles = "",
            preserveAttemptedSnapshot = false,
          )
        } else {
          launchPrintWebView(
            context = context,
            post = post,
            rawHtml = parsed.optString("bodyHtml", post.htmlContent).ifBlank { post.htmlContent },
            extraHeadStyles = parsed.optString("headStyles", ""),
            preserveAttemptedSnapshot = parsed.optBoolean("hasAttempted", false),
          )
        }
      }
      return
    } catch (_: Throwable) {
      // Fall back cleanly to raw post HTML below
    }
  }

  launchPrintWebView(
    context = context,
    post = post,
    rawHtml = post.htmlContent,
    extraHeadStyles = "",
    preserveAttemptedSnapshot = false,
  )
}

@SuppressLint("SetJavaScriptEnabled")
private fun launchPrintWebView(
  context: Context,
  post: StudyPost,
  rawHtml: String,
  extraHeadStyles: String,
  preserveAttemptedSnapshot: Boolean,
) {
  val activity = context.findActivity() ?: context
  val mainHandler = Handler(Looper.getMainLooper())
  val printWebView = WebView(activity)
  activePrintWebView = printWebView
  var printTriggered = false

  fun triggerSystemPrint() {
    if (printTriggered) return
    printTriggered = true
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
        view?.evaluateJavascript("window.gpPrepareMcqsForPrint && window.gpPrepareMcqsForPrint();") {
          mainHandler.postDelayed({ triggerSystemPrint() }, 400L)
        }
        mainHandler.postDelayed({ triggerSystemPrint() }, 1200L)
      }
    }

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
        h1, h2, h3, h4 { line-height: 1.35; margin-top: 1em; margin-bottom: 0.45em; break-after: avoid; page-break-after: avoid; }
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
          [data-gp-qcard="1"], tr, blockquote, .mcq-q, .mcq-a, .gp-print-answer,
          [data-gp-print-state="correct"], [data-gp-print-state="wrong"] {
            break-inside: avoid;
            page-break-inside: avoid;
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
              }
            });
          }

          window.gpPrepareMcqsForPrint = prepareMcqsForPrint;
          prepareMcqsForPrint();
          window.addEventListener('load', prepareMcqsForPrint);
        })();
      </script>
    </body>
    </html>
  """.trimIndent()
}
