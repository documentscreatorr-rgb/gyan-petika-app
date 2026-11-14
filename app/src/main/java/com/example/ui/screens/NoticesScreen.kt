package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StudyPost
import com.example.ui.MainUiState
import com.example.ui.components.StudyPostRowCard
import com.example.ui.components.SubScreenHeader
import com.example.ui.theme.LocalGyanExtendedColors

@Composable
fun NoticesScreen(
  uiState: MainUiState,
  onBackClick: () -> Unit,
  onOpenPost: (StudyPost) -> Unit,
  onToggleBookmark: (String) -> Unit,
  onAutoSync: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val noticePosts = uiState.noticePosts

  LaunchedEffect(Unit) {
    if (!uiState.syncStatus.isSyncing) {
      onAutoSync()
    }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SubScreenHeader(
        eyebrow = stringResource(R.string.brand_title),
        title = stringResource(R.string.notices_screen_title),
        subtitle =
          if (noticePosts.isEmpty()) stringResource(R.string.no_notice_yet)
          else "${noticePosts.size} notice updates available",
        onBackClick = onBackClick,
      )
    }

    if (noticePosts.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.border),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.NotificationsNone,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = stringResource(R.string.no_notice_yet),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("notices_empty_text"),
            )
          }
        }
      }
    } else {
      items(noticePosts, key = { it.id }) { post ->
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
