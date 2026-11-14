package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gyan_petika_prefs")

class PreferencesRepository(private val context: Context) {

  private object Keys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val LIST_DENSITY = stringPreferencesKey("list_density")
    val TEXT_SIZE_PRESET = stringPreferencesKey("text_size_preset")
    val CUSTOM_FONT_SIZE_SP = intPreferencesKey("custom_font_size_sp")
    val EYE_COMFORT_PAPER_MODE = booleanPreferencesKey("eye_comfort_paper_mode")
    val AUTO_CACHE_OFFLINE = booleanPreferencesKey("auto_cache_offline")
    val SAVED_POST_IDS = stringSetPreferencesKey("saved_post_ids")
    val LAST_SYNC_EPOCH_MS = longPreferencesKey("last_sync_epoch_ms")
  }

  val preferencesFlow: Flow<UserPreferences> =
    context.dataStore.data.map { prefs ->
      val theme =
        prefs[Keys.THEME_MODE]?.let { name ->
          AppThemeMode.entries.firstOrNull { it.name == name }
        } ?: AppThemeMode.LIGHT
      val density =
        prefs[Keys.LIST_DENSITY]?.let { name ->
          ListDensity.entries.firstOrNull { it.name == name }
        } ?: ListDensity.COMFORTABLE
      val preset =
        prefs[Keys.TEXT_SIZE_PRESET]?.let { name ->
          TextSizePreset.entries.firstOrNull { it.name == name }
        } ?: TextSizePreset.NORMAL
      val customFont = prefs[Keys.CUSTOM_FONT_SIZE_SP] ?: preset.sizeSp
      UserPreferences(
        themeMode = theme,
        listDensity = density,
        textSizePreset = preset,
        customFontSizeSp = customFont.coerceIn(12, 24),
        eyeComfortPaperMode = prefs[Keys.EYE_COMFORT_PAPER_MODE] ?: false,
        autoCacheOffline = prefs[Keys.AUTO_CACHE_OFFLINE] ?: true,
        savedPostIds = prefs[Keys.SAVED_POST_IDS] ?: emptySet(),
        lastSyncEpochMs = prefs[Keys.LAST_SYNC_EPOCH_MS] ?: 0L,
      )
    }

  suspend fun setThemeMode(mode: AppThemeMode) {
    context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
  }

  suspend fun setListDensity(density: ListDensity) {
    context.dataStore.edit { it[Keys.LIST_DENSITY] = density.name }
  }

  suspend fun setTextSizePreset(preset: TextSizePreset) {
    context.dataStore.edit {
      it[Keys.TEXT_SIZE_PRESET] = preset.name
      it[Keys.CUSTOM_FONT_SIZE_SP] = preset.sizeSp
    }
  }

  suspend fun stepFontSize(deltaSp: Int) {
    context.dataStore.edit { prefs ->
      val current = prefs[Keys.CUSTOM_FONT_SIZE_SP] ?: TextSizePreset.NORMAL.sizeSp
      val next = (current + deltaSp).coerceIn(12, 24)
      prefs[Keys.CUSTOM_FONT_SIZE_SP] = next
      val closestPreset =
        TextSizePreset.entries.minByOrNull { kotlin.math.abs(it.sizeSp - next) }
          ?: TextSizePreset.NORMAL
      prefs[Keys.TEXT_SIZE_PRESET] = closestPreset.name
    }
  }

  suspend fun setEyeComfortPaperMode(enabled: Boolean) {
    context.dataStore.edit { it[Keys.EYE_COMFORT_PAPER_MODE] = enabled }
  }

  suspend fun setAutoCacheOffline(enabled: Boolean) {
    context.dataStore.edit { it[Keys.AUTO_CACHE_OFFLINE] = enabled }
  }

  suspend fun toggleBookmark(postId: String) {
    context.dataStore.edit { prefs ->
      val current = prefs[Keys.SAVED_POST_IDS] ?: emptySet()
      prefs[Keys.SAVED_POST_IDS] =
        if (postId in current) current - postId else current + postId
    }
  }

  suspend fun updateLastSync(epochMs: Long) {
    context.dataStore.edit { it[Keys.LAST_SYNC_EPOCH_MS] = epochMs }
  }

  suspend fun resetDefaults() {
    context.dataStore.edit { prefs ->
      val saved = prefs[Keys.SAVED_POST_IDS] ?: emptySet()
      val lastSync = prefs[Keys.LAST_SYNC_EPOCH_MS] ?: 0L
      prefs.clear()
      prefs[Keys.SAVED_POST_IDS] = saved
      prefs[Keys.LAST_SYNC_EPOCH_MS] = lastSync
    }
  }
}
