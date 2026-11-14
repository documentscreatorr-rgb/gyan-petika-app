package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BundledContent
import com.example.data.ClassHubInfo
import com.example.data.StudyPost
import com.example.ui.MainUiState
import com.example.ui.components.GyanBrandHeader
import com.example.ui.components.SearchTriggerCard
import com.example.ui.components.StudyPostRowCard
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.BadgePurple
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy

@Composable
fun HomeScreen(
  uiState: MainUiState,
  onSettingsClick: () -> Unit,
  onSearchClick: () -> Unit,
  onSelectClassPill: (String) -> Unit,
  onOpenClassHub: (String) -> Unit,
  onViewAllNoticesClick: () -> Unit,
  onOpenPost: (StudyPost) -> Unit,
  onToggleBookmark: (String) -> Unit,
  onSyncFeed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val filteredPosts = uiState.recentFilteredPosts
  val isHomeOverview = uiState.selectedClassFilter == "All"

  BackHandler(enabled = !isHomeOverview) {
    onSelectClassPill("All")
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      GyanBrandHeader(onSettingsClick = onSettingsClick)
    }

    item {
      SearchTriggerCard(
        placeholder = stringResource(R.string.search_placeholder),
        onClick = onSearchClick,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }

    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(BundledContent.classFilterPills) { pill ->
          val selected = uiState.selectedClassFilter == pill
          Surface(
            onClick = { onSelectClassPill(pill) },
            shape = RoundedCornerShape(999.dp),
            color =
              if (selected) PrimaryNavy
              else MaterialTheme.colorScheme.surface,
            border =
              if (selected) null
              else BorderStroke(1.dp, extended.border),
            modifier = Modifier.minimumInteractiveComponentSize().testTag("class_pill_$pill"),
          ) {
            Text(
              text = pill,
              style = MaterialTheme.typography.labelLarge,
              color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
          }
        }
      }
    }

    if (isHomeOverview) {
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Text(
            text = stringResource(R.string.explore_by_class),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Spacer(modifier = Modifier.height(10.dp))
          val classes = BundledContent.exploreByClass

          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              ClassGridCard(
                info = classes[0],
                badgeText = classes[0].defaultBadge,
                badgeColor = PrimaryNavy,
                onClick = { onOpenClassHub(classes[0].className) },
                modifier = Modifier.weight(1f),
              )
              ClassGridCard(
                info = classes[1],
                badgeText = classes[1].defaultBadge,
                badgeColor = BadgeGreen,
                onClick = { onOpenClassHub(classes[1].className) },
                modifier = Modifier.weight(1f),
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              ClassGridCard(
                info = classes[2],
                badgeText = classes[2].defaultBadge,
                badgeColor = BadgePurple,
                onClick = { onOpenClassHub(classes[2].className) },
                modifier = Modifier.weight(1f),
              )
              ClassGridCard(
                info = classes[3],
                badgeText = classes[3].defaultBadge,
                badgeColor = Color(0xFFEA580C),
                onClick = { onOpenClassHub(classes[3].className) },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      item {
        HomeNoticeSection(
          allNotices = uiState.noticePosts,
          onOpenNotice = onOpenPost,
          onViewAllClick = onViewAllNoticesClick,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }

      item {
        HomeCopyrightFooter(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
      }
    } else {
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = uiState.selectedClassFilter,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.Public,
                contentDescription = null,
                tint = if (uiState.syncStatus.isLiveConnected) BadgeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${filteredPosts.size} posts & study files in ${uiState.selectedClassFilter}",
                style = MaterialTheme.typography.bodySmall,
                color =
                  if (uiState.syncStatus.isLiveConnected) BadgeGreen
                  else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Surface(
            onClick = onSyncFeed,
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, extended.border),
            modifier = Modifier.minimumInteractiveComponentSize().testTag("home_sync_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (uiState.syncStatus.isSyncing) {
                CircularProgressIndicator(
                  modifier = Modifier.size(14.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary,
                )
              } else {
                Icon(
                  imageVector = Icons.Rounded.Refresh,
                  contentDescription = "Refresh feed",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (uiState.syncStatus.isSyncing) "Syncing" else "Sync",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }

      if (filteredPosts.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, extended.border),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector = Icons.Rounded.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No notes found for ${uiState.selectedClassFilter}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap Sync above or switch filter back to All.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        items(filteredPosts, key = { it.id }) { post ->
          StudyPostRowCard(
            post = post,
            isSaved = post.id in uiState.preferences.savedPostIds,
            density = uiState.preferences.listDensity,
            onPostClick = { onOpenPost(post) },
            onBookmarkToggle = { onToggleBookmark(post.id) },
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun HomeNoticeSection(
  allNotices: List<StudyPost>,
  onOpenNotice: (StudyPost) -> Unit,
  onViewAllClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val recentNotices = allNotices.take(5)

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(30.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(Color(0xFFEA580C).copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.NotificationsActive,
            contentDescription = null,
            tint = Color(0xFFEA580C),
            modifier = Modifier.size(17.dp),
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.notice_section_title),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }
      if (allNotices.isNotEmpty()) {
        Surface(
          shape = RoundedCornerShape(999.dp),
          color = Color(0xFFEA580C).copy(alpha = 0.12f),
        ) {
          Text(
            text = "${allNotices.size} Notices",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFEA580C),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, extended.border),
      modifier = Modifier.fillMaxWidth().testTag("home_notice_card"),
    ) {
      if (recentNotices.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.no_notice_yet),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("home_no_notice_text"),
          )
        }
      } else {
        Column(modifier = Modifier.fillMaxWidth()) {
          recentNotices.forEachIndexed { index, notice ->
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .clickable { onOpenNotice(notice) }
                  .padding(horizontal = 14.dp, vertical = 12.dp)
                  .testTag("home_notice_item_$index"),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(
                modifier =
                  Modifier.size(26.dp)
                    .clip(CircleShape)
                    .background(PrimaryNavy.copy(alpha = if (extended.isDark) 0.35f else 0.10f)),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "${index + 1}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (extended.isDark) Color(0xFF93C5FD) else PrimaryNavy,
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = notice.title,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = notice.publishedDate,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
            if (index < recentNotices.lastIndex) {
              HorizontalDivider(
                color = extended.border,
                modifier = Modifier.padding(horizontal = 14.dp),
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
    ) {
      Surface(
        onClick = onViewAllClick,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.border),
        modifier = Modifier.minimumInteractiveComponentSize().testTag("notice_view_all_button"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.notice_view_all),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun HomeCopyrightFooter(modifier: Modifier = Modifier) {
  val extended = LocalGyanExtendedColors.current
  Column(
    modifier = modifier.fillMaxWidth().padding(top = 8.dp).testTag("home_copyright_footer"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    HorizontalDivider(color = extended.border)
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = stringResource(R.string.home_copyright_notice),
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Text(
      text = stringResource(R.string.home_copyright_subtitle),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun ClassGridCard(
  info: ClassHubInfo,
  badgeText: String,
  badgeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, extended.border),
    modifier =
      modifier
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("explore_${info.id}"),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = badgeColor.copy(alpha = 0.12f),
      ) {
        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = if (extended.isDark) badgeColor.copy(alpha = 0.95f) else badgeColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = info.className,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = info.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Open",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(14.dp),
        )
      }
    }
  }
}
