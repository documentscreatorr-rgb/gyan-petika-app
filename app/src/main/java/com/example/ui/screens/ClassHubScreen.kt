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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.TrendingUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BundledContent
import com.example.data.SubjectAccent
import com.example.data.SubjectItem
import com.example.ui.components.SubScreenHeader
import com.example.ui.theme.BadgeBlue
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.BadgeOrange
import com.example.ui.theme.BadgePurple
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.BadgeTeal
import com.example.ui.theme.LocalGyanExtendedColors

@Composable
fun ClassHubScreen(
  className: String,
  onBackClick: () -> Unit,
  onSelectSubject: (SubjectItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val subjects = BundledContent.subjectsForClass(className)

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item(span = { GridItemSpan(2) }) {
      SubScreenHeader(
        eyebrow = "CBSE Curriculum",
        title = className,
        subtitle = "Choose a subject to explore",
        onBackClick = onBackClick,
        modifier = Modifier.padding(horizontal = 0.dp),
      )
    }

    items(subjects, key = { it.id }) { subject ->
      SubjectGridCard(
        subject = subject,
        onClick = { onSelectSubject(subject) },
      )
    }
  }
}

@Composable
private fun SubjectGridCard(
  subject: SubjectItem,
  onClick: () -> Unit,
) {
  val extended = LocalGyanExtendedColors.current
  val accentColor = accentColorFor(subject.accent)
  val icon = iconForSubject(subject.id)

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, extended.border),
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("subject_card_${subject.id}"),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(accentColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(22.dp),
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = subject.name,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2,
      )

      Spacer(modifier = Modifier.height(8.dp))

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

private fun accentColorFor(accent: SubjectAccent): Color =
  when (accent) {
    SubjectAccent.BLUE -> BadgeBlue
    SubjectAccent.GREEN -> BadgeGreen
    SubjectAccent.RED -> BadgeRed
    SubjectAccent.ORANGE -> BadgeOrange
    SubjectAccent.PURPLE -> BadgePurple
    SubjectAccent.TEAL -> BadgeTeal
  }

private fun iconForSubject(id: String): ImageVector =
  when (id) {
    "mathematics" -> Icons.Rounded.Calculate
    "science", "chemistry" -> Icons.Rounded.Science
    "biology" -> Icons.Rounded.Biotech
    "english" -> Icons.AutoMirrored.Rounded.MenuBook
    "hindi" -> Icons.Rounded.Book
    "social-science", "geography", "political-science" -> Icons.Rounded.Public
    "information-technology", "physics" -> Icons.Rounded.Computer
    "economics", "accountancy", "business-studies" -> Icons.Rounded.TrendingUp
    "history" -> Icons.Rounded.AccountBalance
    else -> Icons.AutoMirrored.Rounded.MenuBook
  }
