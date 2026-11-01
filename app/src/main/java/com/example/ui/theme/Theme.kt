package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = TerracottaOnPrimary,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = TerracottaOnContainer,
    secondary = WarmSandSecondary,
    onSecondary = Color(0xFF1F1B1A),
    secondaryContainer = WarmSandContainer,
    onSecondaryContainer = WarmCreamText,
    tertiary = BurgundyBadgeText,
    tertiaryContainer = BurgundyBadgeBg,
    onTertiaryContainer = BurgundyBadgeText,
    background = ObsidianBackground,
    onBackground = WarmCreamText,
    surface = ObsidianSurface,
    onSurface = WarmCreamText,
    surfaceVariant = ObsidianSurfaceContainer,
    onSurfaceVariant = WarmMutedText,
    surfaceContainerLowest = Color(0xFF0C0A0A),
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianSurfaceContainer,
    surfaceContainerHigh = ObsidianSurfaceBright,
    surfaceContainerHighest = ObsidianSurfaceHighest,
    outline = WarmDivider,
    outlineVariant = Color(0xFF3B3332)
)

private val WarmPaperLightColorScheme = lightColorScheme(
    primary = PaperPrimary,
    onPrimary = PaperOnPrimary,
    primaryContainer = PaperPrimaryContainer,
    onPrimaryContainer = PaperOnPrimaryContainer,
    secondary = Color(0xFF6B5C58),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6DBD7),
    onSecondaryContainer = PaperText,
    tertiary = PaperPrimary,
    tertiaryContainer = Color(0xFFF7D8D4),
    onTertiaryContainer = Color(0xFF4A1511),
    background = PaperBackground,
    onBackground = PaperText,
    surface = PaperSurface,
    onSurface = PaperText,
    surfaceVariant = PaperSurfaceContainer,
    onSurfaceVariant = PaperMutedText,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PaperBackground,
    surfaceContainer = PaperSurface,
    surfaceContainerHigh = PaperSurfaceContainer,
    surfaceContainerHighest = Color(0xFFDFD4CF),
    outline = Color(0xFFD6C9C4),
    outlineVariant = Color(0xFFE5DBD7)
)

@Composable
fun OrionPdfTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) ObsidianDarkColorScheme else WarmPaperLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    OrionPdfTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}
