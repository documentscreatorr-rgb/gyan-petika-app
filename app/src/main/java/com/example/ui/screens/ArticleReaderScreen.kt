package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
  val cleanBlocks = remember(post.id, post.htmlContent) { ArticleHtmlParser.parse(post) }

  Column(
    modifier = modifier.fillMaxSize().background(extended.readerBackground),
  ) {
    ReaderFloatingToolbar(
      isSaved = isSaved,
      isPaperMode = preferences.eyeComfortPaperMode,
      fontSizeSp = baseFontSp,
      onBackClick = onBackClick,
      onFontDecrease = onFontDecrease,
      onFontIncrease = onFontIncrease,
      onTogglePaperMode = onTogglePaperMode,
      onToggleBookmark = onToggleBookmark,
      onPrintClick = { printStudyPost(context, post) },
      onShareClick = { shareStudyPost(context, post) },
    )

    Column(
      modifier =
        Modifier.weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
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
) {
  val context = LocalContext.current
  val density = LocalDensity.current.density
  var contentHeightPx by remember(post.id) { mutableIntStateOf((320 * density).toInt()) }

  val bgHex =
    when {
      isPaperMode && !isDark -> "#FBF7F0"
      isPaperMode && isDark -> "#1F1B16"
      isDark -> "#0B1220"
      else -> "#FFFFFF"
    }
  val textHex =
    when {
      isPaperMode && !isDark -> "#2B2118"
      isPaperMode && isDark -> "#F3EAE0"
      isDark -> "#E2E8F0"
      else -> "#111827"
    }
  val cardBgHex =
    when {
      isPaperMode && !isDark -> "#F6EFE4"
      isPaperMode && isDark -> "#28221B"
      isDark -> "#131D2E"
      else -> "#F8FAFC"
    }
  val borderHex =
    when {
      isPaperMode && !isDark -> "#E6DCCB"
      isPaperMode && isDark -> "#3D3329"
      isDark -> "#25354E"
      else -> "#E2E8F0"
    }

  val latestFontSizeSp by rememberUpdatedState(fontSizeSp)
  val latestBgHex by rememberUpdatedState(bgHex)
  val latestTextHex by rememberUpdatedState(textHex)
  val latestCardBgHex by rememberUpdatedState(cardBgHex)
  val latestBorderHex by rememberUpdatedState(borderHex)
  val latestIsDark by rememberUpdatedState(isDark)
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
                  if (url.contains("gyanpetika.blogspot.com") && !url.contains("/search") && !url.contains("#")) {
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
                  "window.gpApplyTheme && window.gpApplyTheme($latestFontSizeSp, '$latestBgHex', '$latestTextHex', '$latestCardBgHex', '$latestBorderHex', $latestIsDark);"
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
        }
      },
      update = { webView ->
        val js =
          "window.gpApplyTheme && window.gpApplyTheme($fontSizeSp, '$bgHex', '$textHex', '$cardBgHex', '$borderHex', $isDark);"
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
): String {
  return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
      <style>
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
        body { padding: 16px; }
        img, video, iframe {
          max-width: 100% !important;
          height: auto !important;
          border-radius: 10px;
        }
        iframe { min-height: 210px; width: 100% !important; border: 0; }
        a { color: ${if (isDark) "#60A5FA" else "#0F345B"}; font-weight: 600; }
        h1, h2, h3, h4 {
          line-height: 1.35;
          margin-top: 1.1em;
          margin-bottom: 0.5em;
        }
        p { margin: 0.65em 0; }
        ul, ol { padding-left: 1.35em; margin: 0.6em 0; }
        li { margin: 0.35em 0; }
        .gp-table-wrap { width: 100%; overflow-x: auto; margin: 12px 0; border-radius: 10px; }
        table {
          width: 100%;
          border-collapse: collapse;
          font-size: 0.92em;
        }
        th, td {
          border: 1px solid var(--gp-border);
          padding: 8px 10px;
          text-align: left;
        }
        th { background: var(--gp-card); font-weight: 700; }
        blockquote {
          margin: 12px 0;
          padding: 10px 14px;
          border-left: 4px solid #F5B82E;
          background: var(--gp-card);
          border-radius: 0 10px 10px 0;
        }
        .gp-section {
          margin: 14px 0;
          padding: 14px;
          border: 1px solid var(--gp-border);
          border-radius: 14px;
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
          window.gpApplyTheme = function(fontPx, bg, text, card, border, darkMode) {
            var root = document.documentElement.style;
            root.setProperty('--gp-font-size', fontPx + 'px');
            root.setProperty('--gp-bg', bg);
            root.setProperty('--gp-text', text);
            root.setProperty('--gp-card', card);
            root.setProperty('--gp-border', border);
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
  fontSizeSp: Int,
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
    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${fontSizeSp}px",
          style = MaterialTheme.typography.labelSmall,
          color = extended.readerTextSecondary,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onTogglePaperMode,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reader_paper_toggle"),
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

private fun printStudyPost(context: Context, post: StudyPost) {
  val webView = WebView(context)
  webView.webViewClient =
    object : WebViewClient() {
      override fun onPageFinished(view: WebView?, url: String?) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "GYAN PETIKA - ${post.title}"
        val adapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
      }
    }
  val htmlDoc =
    """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <style>
        body { font-family: sans-serif; color: #111827; padding: 24px; line-height: 1.6; }
        h1 { color: #0F345B; font-size: 22px; margin-bottom: 6px; }
        .meta { color: #6B7280; font-size: 13px; margin-bottom: 18px; }
      </style>
    </head>
    <body>
      <h1>${post.title}</h1>
      <div class="meta">${post.classTag} • ${post.subjectTag} • ${post.fullPublishedDate}</div>
      ${post.htmlContent}
    </body>
    </html>
    """
      .trimIndent()
  webView.loadDataWithBaseURL(BundledContent.BLOG_BASE_URL, htmlDoc, "text/html", "UTF-8", null)
}
