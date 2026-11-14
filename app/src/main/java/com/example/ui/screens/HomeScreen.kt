package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.BadgePurple
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyDark
import com.example.ui.theme.PrimaryNavyLight

@Composable
fun HomeScreen(
  uiState: MainUiState,
  onSettingsClick: () -> Unit,
  onSearchClick: () -> Unit,
  onSelectClassPill: (String) -> Unit,
  onExploreItNotesClick: () -> Unit,
  onOpenClassHub: (String) -> Unit,
  onOpenPost: (StudyPost) -> Unit,
  onToggleBookmark: (String) -> Unit,
  onSyncFeed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val filteredPosts = uiState.recentFilteredPosts

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

    item {
      FeaturedHeroCard(
        onExploreClick = onExploreItNotesClick,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }

    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
          text = stringResource(R.string.explore_by_class),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(10.dp))
        val classes = BundledContent.exploreByClass
        val c9Count = uiState.countForClass(classes[0].className)
        val c11Count = uiState.countForClass(classes[2].className)
        val c12Count = uiState.countForClass(classes[3].className)

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            ClassGridCard(
              info = classes[0],
              badgeText = if (c9Count > 0) "$c9Count Live Notes" else classes[0].defaultBadge,
              badgeColor = PrimaryNavy,
              onClick = { onOpenClassHub(classes[0].className) },
              modifier = Modifier.weight(1f),
            )
            ClassGridCard(
              info = classes[1],
              badgeText = "${uiState.class10Count} Live Notes",
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
              badgeText = if (c11Count > 0) "$c11Count Live Notes" else classes[2].defaultBadge,
              badgeColor = BadgePurple,
              onClick = { onOpenClassHub(classes[2].className) },
              modifier = Modifier.weight(1f),
            )
            ClassGridCard(
              info = classes[3],
              badgeText = if (c12Count > 0) "$c12Count Live Notes" else classes[3].defaultBadge,
              badgeColor = Color(0xFFEA580C),
              onClick = { onOpenClassHub(classes[3].className) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.recent_updates),
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
              text =
                if (uiState.syncStatus.isLiveConnected) {
                  "Live • gyanpetika.blogspot.com (${filteredPosts.size})"
                } else {
                  "Offline cache • ${filteredPosts.size} notes"
                },
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

@Composable
private fun FeaturedHeroCard(
  onExploreClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(
          Brush.linearGradient(
            colors = listOf(PrimaryNavyDark, PrimaryNavy, PrimaryNavyLight)
          )
        )
        .padding(20.dp),
  ) {
    Column {
      Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.14f),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier.size(7.dp).clip(CircleShape).background(AccentGold)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.hero_badge),
            style = MaterialTheme.typography.labelSmall,
            color = AccentGold,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = stringResource(R.string.hero_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
        color = Color.White,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = stringResource(R.string.hero_description),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.85f),
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onExploreClick,
        shape = RoundedCornerShape(12.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = AccentGold,
            contentColor = PrimaryNavyDark,
          ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = Modifier.minimumInteractiveComponentSize().testTag("explore_it_notes_cta"),
      ) {
        Text(
          text = stringResource(R.string.hero_cta),
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }
    }
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
