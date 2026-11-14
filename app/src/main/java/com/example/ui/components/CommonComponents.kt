package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ListDensity
import com.example.data.StudyPost
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyDark

@Composable
fun GyanBrandHeader(
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f),
    ) {
      Box(
        modifier =
          Modifier.size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PrimaryNavy),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Work,
          contentDescription = stringResource(R.string.cd_app_logo),
          tint = Color.White,
          modifier = Modifier.size(25.dp),
        )
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(6.dp)
              .size(7.dp)
              .clip(CircleShape)
              .background(AccentGold)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = stringResource(R.string.brand_title),
          style =
            MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.6.sp,
            ),
          color = if (extended.isDark) Color.White else PrimaryNavy,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = stringResource(R.string.brand_tagline),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    Surface(
      onClick = onSettingsClick,
      shape = CircleShape,
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, extended.border),
      modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_button"),
    ) {
      Box(
        modifier = Modifier.size(44.dp),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Settings,
          contentDescription = stringResource(R.string.cd_settings),
          tint = if (extended.isDark) Color.White else PrimaryNavy,
          modifier = Modifier.size(22.dp),
        )
      }
    }
  }
}

@Composable
fun SubScreenHeader(
  eyebrow: String,
  title: String,
  subtitle: String? = null,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
  ) {
    Surface(
      onClick = onBackClick,
      shape = RoundedCornerShape(999.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, extended.border),
      modifier = Modifier.minimumInteractiveComponentSize().testTag("back_button"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = stringResource(R.string.cd_back),
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Back",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = eyebrow.uppercase(),
      style =
        MaterialTheme.typography.labelSmall.copy(
          letterSpacing = 1.sp,
          fontWeight = FontWeight.Bold,
        ),
      color = if (extended.isDark) AccentGold else PrimaryNavy,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )
    if (!subtitle.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun SearchTriggerCard(
  placeholder: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, extended.border),
    tonalElevation = 1.dp,
    modifier = modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("home_search_bar"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = placeholder,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun StudyPostRowCard(
  post: StudyPost,
  isSaved: Boolean,
  density: ListDensity,
  onPostClick: () -> Unit,
  onBookmarkToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val verticalPadding = if (density == ListDensity.COMPACT) 10.dp else 14.dp
  val iconBoxSize = if (density == ListDensity.COMPACT) 38.dp else 46.dp

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, extended.border),
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onPostClick)
        .testTag("post_card_${post.id}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = verticalPadding),
      verticalAlignment = Alignment.Top,
    ) {
      if (!post.thumbnailUrl.isNullOrBlank()) {
        AsyncImage(
          model = post.thumbnailUrl,
          contentDescription = post.title,
          contentScale = ContentScale.Crop,
          modifier =
            Modifier.padding(top = 2.dp)
              .size(iconBoxSize)
              .clip(RoundedCornerShape(12.dp))
              .background(extended.cardSubtle),
        )
      } else {
        Box(
          modifier =
            Modifier.padding(top = 2.dp)
              .size(iconBoxSize)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (extended.isDark) Color(0xFF1D3557) else Color(0xFFEAF2FA)
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Description,
            contentDescription = null,
            tint = if (extended.isDark) Color(0xFF93C5FD) else PrimaryNavy,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = post.title,
          style =
            if (density == ListDensity.COMPACT) {
              MaterialTheme.typography.titleSmall
            } else {
              MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 21.sp)
            },
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        if (density == ListDensity.COMFORTABLE && post.snippet.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = post.snippet,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = extended.cardSubtle,
          ) {
            Text(
              text = post.subLabel.ifBlank { post.categoryLabel },
              style = MaterialTheme.typography.labelSmall,
              color = if (extended.isDark) Color(0xFF93C5FD) else PrimaryNavyDark,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            )
          }
          Text(
            text = "• ${post.publishedDate}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          if (post.commentCount > 0) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.Comment,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(12.dp),
            )
            Text(
              text = "${post.commentCount}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      IconButton(
        onClick = onBookmarkToggle,
        modifier = Modifier.minimumInteractiveComponentSize().testTag("bookmark_${post.id}"),
      ) {
        Icon(
          imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
          contentDescription = stringResource(R.string.cd_bookmark),
          tint =
            if (isSaved) AccentGold
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
