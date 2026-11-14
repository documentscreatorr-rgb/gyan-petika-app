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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BundledContent
import com.example.data.ListDensity
import com.example.data.StudyMaterialCategory
import com.example.data.StudyPost
import com.example.ui.components.StudyPostRowCard
import com.example.ui.components.SubScreenHeader
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyDark

@Composable
fun SubjectDetailScreen(
  className: String,
  subjectName: String,
  allSubjectPosts: List<StudyPost>,
  onBackClick: () -> Unit,
  onOpenCategory: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SubScreenHeader(
        eyebrow = className,
        title = subjectName,
        subtitle = "Study resources, notes & exam practice",
        onBackClick = onBackClick,
      )
    }

    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = PrimaryNavy,
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onOpenCategory("All Posts") }
            .testTag("all_posts_card"),
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier.size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.AutoStories,
              contentDescription = null,
              tint = AccentGold,
              modifier = Modifier.size(24.dp),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "All Posts",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Every $subjectName post for $className",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.85f),
            )
          }
          Surface(
            shape = RoundedCornerShape(999.dp),
            color = AccentGold,
          ) {
            Text(
              text = "${allSubjectPosts.size}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = PrimaryNavyDark,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White,
          )
        }
      }
    }

    item {
      Text(
        text = "Study Material",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp),
      )
    }

    items(BundledContent.studyMaterialCategories, key = { it.id }) { category ->
      val count =
        allSubjectPosts.count {
          it.materialCategory.equals(category.title, ignoreCase = true) ||
            it.labels.any { lbl -> lbl.contains(category.title, ignoreCase = true) }
        }
      CategoryRowCard(
        category = category,
        count = count,
        onClick = { onOpenCategory(category.title) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

@Composable
fun CategoryPostsScreen(
  className: String,
  subjectName: String,
  categoryTitle: String,
  filteredPosts: List<StudyPost>,
  allSubjectPosts: List<StudyPost>,
  savedPostIds: Set<String>,
  density: ListDensity,
  onBackClick: () -> Unit,
  onOpenPost: (StudyPost) -> Unit,
  onToggleBookmark: (String) -> Unit,
  onShowAllSubjectPosts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      SubScreenHeader(
        eyebrow = "$className • $subjectName",
        title = categoryTitle,
        subtitle = "${filteredPosts.size} study resources available",
        onBackClick = onBackClick,
      )
    }

    if (filteredPosts.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, extended.border),
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = Icons.Rounded.FolderOpen,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No posts tagged in $categoryTitle yet",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text =
                if (allSubjectPosts.isNotEmpty()) {
                  "You can view all ${allSubjectPosts.size} $subjectName posts for $className."
                } else {
                  "Sync the live feed when online to check for newly published chapters."
                },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (allSubjectPosts.isNotEmpty() && categoryTitle != "All Posts") {
              Spacer(modifier = Modifier.height(14.dp))
              Button(onClick = onShowAllSubjectPosts) {
                Text("View All $subjectName Posts")
              }
            }
          }
        }
      }
    } else {
      items(filteredPosts, key = { it.id }) { post ->
        StudyPostRowCard(
          post = post,
          isSaved = post.id in savedPostIds,
          density = density,
          onPostClick = { onOpenPost(post) },
          onBookmarkToggle = { onToggleBookmark(post.id) },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
    }
  }
}

@Composable
private fun CategoryRowCard(
  category: StudyMaterialCategory,
  count: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, extended.border),
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("category_row_${category.id}"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(extended.cardSubtle),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = iconForCategory(category.id),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = category.title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      if (count > 0) {
        Surface(
          shape = RoundedCornerShape(999.dp),
          color = extended.cardSubtle,
        ) {
          Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
      }
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

private fun iconForCategory(id: String): ImageVector =
  when (id) {
    "chapter-notes" -> Icons.Rounded.Description
    "ncert-solutions" -> Icons.Rounded.MenuBook
    "important-questions" -> Icons.AutoMirrored.Rounded.HelpOutline
    "pyq" -> Icons.Rounded.TableChart
    "mcq-practice" -> Icons.Rounded.CheckBox
    else -> Icons.Rounded.Description
  }
