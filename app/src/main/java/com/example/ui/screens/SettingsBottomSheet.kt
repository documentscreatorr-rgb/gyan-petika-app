package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BundledContent
import com.example.data.FeedSyncStatus
import com.example.data.ListDensity
import com.example.data.TextSizePreset
import com.example.data.UserPreferences
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.LocalGyanExtendedColors
import com.example.ui.theme.PaperBackground
import com.example.ui.theme.PaperBorder
import com.example.ui.theme.PaperTextPrimary
import com.example.ui.theme.PrimaryNavy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
  preferences: UserPreferences,
  syncStatus: FeedSyncStatus,
  onDismiss: () -> Unit,
  onThemeChange: (AppThemeMode) -> Unit,
  onDensityChange: (ListDensity) -> Unit,
  onTextSizeChange: (TextSizePreset) -> Unit,
  onPaperModeChange: (Boolean) -> Unit,
  onAutoCacheChange: (Boolean) -> Unit,
  onSyncFeed: () -> Unit,
  onRebuildCache: () -> Unit,
  onResetDefaults: () -> Unit,
) {
  val context = LocalContext.current
  val extended = LocalGyanExtendedColors.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_close_icon"),
        ) {
          Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close settings")
        }
      }

      HorizontalDivider(color = extended.border)

      // Section 1: Appearance & Display
      SectionLabel("APPEARANCE & DISPLAY")

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "App Theme",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AppThemeMode.entries.forEach { mode ->
            val selected = preferences.themeMode == mode
            SegmentedChoiceChip(
              label = mode.label,
              selected = selected,
              onClick = { onThemeChange(mode) },
              modifier = Modifier.weight(1f).testTag("theme_${mode.name}"),
            )
          }
        }

        Text(
          text = "List Density",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ListDensity.entries.forEach { density ->
            val selected = preferences.listDensity == density
            SegmentedChoiceChip(
              label = density.label,
              selected = selected,
              onClick = { onDensityChange(density) },
              modifier = Modifier.weight(1f).testTag("density_${density.name}"),
            )
          }
        }
      }

      HorizontalDivider(color = extended.border)

      // Section 2: Reading & Study Preferences
      SectionLabel("READING & STUDY PREFERENCES")

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Notes Text Size (${preferences.customFontSizeSp}px)",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          TextSizePreset.entries.forEach { preset ->
            val selected = preferences.textSizePreset == preset
            SegmentedChoiceChip(
              label = preset.label,
              selected = selected,
              onClick = { onTextSizeChange(preset) },
              modifier = Modifier.weight(1f).testTag("text_size_${preset.name}"),
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color =
            if (preferences.eyeComfortPaperMode) PaperBackground else extended.cardSubtle,
          border =
            BorderStroke(
              1.dp,
              if (preferences.eyeComfortPaperMode) PaperBorder else extended.border,
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = stringResource(R.string.sample_preview_text),
            style =
              MaterialTheme.typography.bodyLarge.copy(
                fontSize = preferences.customFontSizeSp.sp,
                lineHeight = (preferences.customFontSizeSp * 1.48f).sp,
              ),
            color =
              if (preferences.eyeComfortPaperMode) PaperTextPrimary
              else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(14.dp),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
              text = "Eye-Comfort Paper Mode",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Warm paper tint while reading notes",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = preferences.eyeComfortPaperMode,
            onCheckedChange = onPaperModeChange,
            modifier = Modifier.testTag("switch_paper_mode"),
          )
        }
      }

      HorizontalDivider(color = extended.border)

      // Section 3: Offline Access & Storage
      SectionLabel("OFFLINE ACCESS & STORAGE")

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
              text = "Auto-Cache Feed Offline",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Save latest study notes automatically for offline study",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Switch(
            checked = preferences.autoCacheOffline,
            onCheckedChange = onAutoCacheChange,
            modifier = Modifier.testTag("switch_auto_cache"),
          )
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = extended.cardSubtle,
          border = BorderStroke(1.dp, extended.border),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "Device Cache Summary",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                "Cached study notes: ${syncStatus.cachedPostsCount} • Saved bookmarks: ${preferences.savedPostIds.size}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text =
                if (preferences.lastSyncEpochMs > 0L) {
                  val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.US)
                  "Last synced: ${fmt.format(Date(preferences.lastSyncEpochMs))}"
                } else {
                  "Using bundled + cached study notes"
                },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onSyncFeed,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).minimumInteractiveComponentSize().testTag("sync_feed_btn"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Sync,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sync Feed")
          }
          OutlinedButton(
            onClick = onRebuildCache,
            shape = RoundedCornerShape(12.dp),
            modifier =
              Modifier.weight(1f).minimumInteractiveComponentSize().testTag("rebuild_cache_btn"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Cached,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Rebuild Cache")
          }
        }
      }

      HorizontalDivider(color = extended.border)

      // Section 4: App Installation & Links
      SectionLabel("APP INSTALLATION")

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = BadgeGreen.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, BadgeGreen.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = BadgeGreen,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.standalone_status),
            style = MaterialTheme.typography.labelLarge,
            color = BadgeGreen,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BundledContent.BLOG_BASE_URL))
            context.startActivity(intent)
          },
          modifier = Modifier.minimumInteractiveComponentSize().testTag("external_site_link"),
        ) {
          Text("gyanpetika.blogspot.com")
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
        }

        TextButton(
          onClick = onResetDefaults,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("reset_defaults_btn"),
        ) {
          Icon(
            imageVector = Icons.Rounded.RestartAlt,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reset Defaults")
        }
      }

      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(14.dp),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = PrimaryNavy,
            contentColor = Color.White,
          ),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().testTag("settings_done_btn"),
      ) {
        Text(
          text = "Done",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
private fun SectionLabel(title: String) {
  Text(
    text = title,
    style =
      MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
      ),
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun SegmentedChoiceChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val extended = LocalGyanExtendedColors.current
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    color = if (selected) PrimaryNavy else extended.cardSubtle,
    border = if (selected) null else BorderStroke(1.dp, extended.border),
    modifier = modifier.minimumInteractiveComponentSize(),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
      color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
    )
  }
}
