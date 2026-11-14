package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppThemeMode(val label: String) {
  LIGHT("Light"),
  DARK("Dark"),
  SYSTEM("System"),
}

data class GyanExtendedColors(
  val isDark: Boolean,
  val isPaperMode: Boolean,
  val border: Color,
  val cardSubtle: Color,
  val readerBackground: Color,
  val readerSurface: Color,
  val readerBorder: Color,
  val readerTextPrimary: Color,
  val readerTextSecondary: Color,
  val mcqQuestion: Color,
  val mcqAnswer: Color,
)

val LocalGyanExtendedColors =
  staticCompositionLocalOf {
    GyanExtendedColors(
      isDark = false,
      isPaperMode = false,
      border = BorderLight,
      cardSubtle = SurfaceSubtleLight,
      readerBackground = BackgroundLight,
      readerSurface = SurfaceWhite,
      readerBorder = BorderLight,
      readerTextPrimary = TextPrimaryLight,
      readerTextSecondary = TextSecondaryLight,
      mcqQuestion = McqQuestionRed,
      mcqAnswer = McqAnswerGreen,
    )
  }

private val LightColors =
  lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6EEF7),
    onPrimaryContainer = PrimaryNavyDark,
    secondary = AccentGold,
    onSecondary = PrimaryNavyDark,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = BadgePurple,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceSubtleLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFCBD5E1),
    error = BadgeRed,
  )

private val DarkColors =
  darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = PrimaryNavyDark,
    primaryContainer = Color(0xFF1A3A63),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = AccentGold,
    onSecondary = PrimaryNavyDark,
    secondaryContainer = Color(0xFF423108),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF2E1065),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceSubtleDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171),
  )

private val PaperColors =
  lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4D3),
    onPrimaryContainer = PaperTextPrimary,
    secondary = AccentGold,
    onSecondary = PrimaryNavyDark,
    secondaryContainer = Color(0xFFF5E6C4),
    onSecondaryContainer = Color(0xFF69480A),
    tertiary = BadgePurple,
    onTertiary = Color.White,
    background = PaperBackground,
    onBackground = PaperTextPrimary,
    surface = PaperSurface,
    onSurface = PaperTextPrimary,
    surfaceVariant = Color(0xFFEFE6D8),
    onSurfaceVariant = PaperTextSecondary,
    outline = PaperBorder,
    outlineVariant = Color(0xFFD8CBB5),
    error = BadgeRed,
  )

private val GyanShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun GyanPetikaTheme(
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  eyeComfortPaperMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val isDark =
    when (themeMode) {
      AppThemeMode.LIGHT -> false
      AppThemeMode.DARK -> true
      AppThemeMode.SYSTEM -> systemDark
    }

  val colorScheme =
    when {
      isDark -> DarkColors
      eyeComfortPaperMode -> PaperColors
      else -> LightColors
    }

  val extended =
    GyanExtendedColors(
      isDark = isDark,
      isPaperMode = eyeComfortPaperMode,
      border = if (isDark) BorderDark else if (eyeComfortPaperMode) PaperBorder else BorderLight,
      cardSubtle =
        if (isDark) SurfaceSubtleDark
        else if (eyeComfortPaperMode) PaperSurface else SurfaceSubtleLight,
      readerBackground =
        when {
          eyeComfortPaperMode && !isDark -> PaperBackground
          eyeComfortPaperMode && isDark -> Color(0xFF1F1B16)
          isDark -> BackgroundDark
          else -> BackgroundLight
        },
      readerSurface =
        when {
          eyeComfortPaperMode && !isDark -> PaperSurface
          eyeComfortPaperMode && isDark -> Color(0xFF28221B)
          isDark -> SurfaceDark
          else -> SurfaceWhite
        },
      readerBorder =
        when {
          eyeComfortPaperMode && !isDark -> PaperBorder
          eyeComfortPaperMode && isDark -> Color(0xFF3D3329)
          isDark -> BorderDark
          else -> BorderLight
        },
      readerTextPrimary =
        when {
          eyeComfortPaperMode && !isDark -> PaperTextPrimary
          eyeComfortPaperMode && isDark -> Color(0xFFF3EAE0)
          isDark -> TextPrimaryDark
          else -> TextPrimaryLight
        },
      readerTextSecondary =
        when {
          eyeComfortPaperMode && !isDark -> PaperTextSecondary
          eyeComfortPaperMode && isDark -> Color(0xFFB8A999)
          isDark -> TextSecondaryDark
          else -> TextSecondaryLight
        },
      mcqQuestion = if (isDark) Color(0xFFFF6B6B) else McqQuestionRed,
      mcqAnswer = if (isDark) Color(0xFF66BB6A) else McqAnswerGreen,
    )

  CompositionLocalProvider(LocalGyanExtendedColors provides extended) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = GyanPetikaTypography,
      shapes = GyanShapes,
      content = content,
    )
  }
}
