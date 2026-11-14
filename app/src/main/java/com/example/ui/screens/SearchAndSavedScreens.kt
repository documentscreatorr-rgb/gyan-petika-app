package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BundledContent
import com.example.data.StudyPost
import com.example.ui.MainUiState
import com.example.ui.components.GyanBrandHeader
import com.example.ui.components.StudyPostRowCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy

@Composable
fun SearchScreen(
  uiState: MainUiState,
  onQueryChange: (String) -> Unit,
  onClassFilterChange: (String) -> Unit,
  onSettingsClick: () -> Unit,
  onOpenPost: (StudyPost) -> Unit,
  onToggleBookmark: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val results = uiState.searchResults
  val suggestedTopics =
    listOf("Entrepreneurship", "Database", "Spreadsheet", "Digital Documentation", "Safety")

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      GyanBrandHeader(onSettingsClick = onSettingsClick)
    }

    item {
      OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = onQueryChange,
        placeholder = {
          Text(text = stringResource(R.string.search_placeholder))
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
          )
        },
        trailingIcon = {
          if (uiState.searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onQueryChange("") },
              modifier = Modifier.minimumInteractiveComponentSize().testTag("clear_search_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear search",
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = PrimaryNavy,
            unfocusedBorderColor = extended.border,
          ),
        modifier =
          Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("search_input_field"),
      )
    }

    item {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(BundledContent.searchFilterPills) { pill ->
          val selected = uiState.searchClassFilter == pill
          Surface(
            onClick = { onClassFilterChange(pill) },
            shape = RoundedCornerShape(999.dp),
            color = if (selected) PrimaryNavy else MaterialTheme.colorScheme.surface,
            border = if (selected) null else BorderStroke(1.dp, extended.border),
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Text(
              text = pill,
              style = MaterialTheme.typography.labelLarge,
              color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            )
          }
        }
      }
    }

    if (uiState.searchQuery.isBlank()) {
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Text(
            text = "Popular Topics",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestedTopics) { topic ->
              Surface(
                onClick = { onQueryChange(topic) },
                shape = RoundedCornerShape(10.dp),
                color = extended.cardSubtle,
                border = BorderStroke(1.dp, extended.border),
                modifier = Modifier.minimumInteractiveComponentSize(),
              ) {
                Text(
                  text = topic,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
              }
            }
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            if (uiState.searchQuery.isBlank()) "All Study Notes (${results.size})"
            else "${results.size} results for \"${uiState.searchQuery}\"",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }
    }

    if (results.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.border),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
          Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.SearchOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No matching notes found",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Try searching another chapter, unit, or switch the class filter to All.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      items(results, key = { it.id }) { post ->
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
fun SavedScreen(
  uiState: MainUiState,
  onSettingsClick: () -> Unit,
  onOpenPost: (StudyPost) -> Unit,
  onToggleBookmark: (String) -> Unit,
  onBrowseHomeClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  val saved = uiState.savedPosts

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      GyanBrandHeader(onSettingsClick = onSettingsClick)
    }

    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.border),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Saved Bookmarks",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${saved.size} study notes cached offline on this device",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Icon(
            imageVector = Icons.Rounded.BookmarkAdded,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(28.dp),
          )
        }
      }
    }

    if (saved.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.border),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
          Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Outlined.BookmarkBorder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(42.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No saved notes yet",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap the bookmark icon on any chapter or question bank to keep it ready offline.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              onClick = onBrowseHomeClick,
              shape = RoundedCornerShape(12.dp),
              color = PrimaryNavy,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("browse_notes_button"),
            ) {
              Text(
                text = "Browse Study Notes",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
              )
            }
          }
        }
      }
    } else {
      items(saved, key = { it.id }) { post ->
        StudyPostRowCard(
          post = post,
          isSaved = true,
          density = uiState.preferences.listDensity,
          onPostClick = { onOpenPost(post) },
          onBookmarkToggle = { onToggleBookmark(post.id) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
    }
  }
}
