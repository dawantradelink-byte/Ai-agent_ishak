package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Custom Material 3 ColorScheme using the Claude Warm Editorial palette.
 * Anchored on a tinted cream canvas (#FAF9F5) with warm coral primary accents (#CC785C),
 * editorial ink typography (#141413), and light cream card surfaces (#EFE9DE).
 */
val WarmEditorialLightColorScheme: ColorScheme = lightColorScheme(
    primary = EditorialPrimaryCoral,
    onPrimary = EditorialOnPrimary,
    primaryContainer = EditorialSurfaceCard,
    onPrimaryContainer = EditorialPrimaryCoralActive,
    inversePrimary = EditorialPrimaryCoralActive,
    secondary = EditorialAccentTeal,
    onSecondary = Color.White,
    secondaryContainer = EditorialSurfaceSoft,
    onSecondaryContainer = EditorialBodyStrong,
    tertiary = EditorialAccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = EditorialSurfaceCreamStrong,
    onTertiaryContainer = EditorialInk,
    background = EditorialCanvas,
    onBackground = EditorialInk,
    surface = EditorialCanvas,
    onSurface = EditorialInk,
    surfaceVariant = EditorialSurfaceCard,
    onSurfaceVariant = EditorialBody,
    surfaceTint = EditorialPrimaryCoral,
    inverseSurface = EditorialSurfaceDark,
    inverseOnSurface = EditorialOnDark,
    outline = EditorialHairline,
    outlineVariant = EditorialHairlineSoft,
    error = EditorialError,
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E8),
    onErrorContainer = EditorialError
)

/**
 * Custom Material 3 Dark ColorScheme for dark product surfaces (#181715)
 * with warm coral primary accents (#CC785C) and cream text on dark (#FAF9F5).
 */
val WarmEditorialDarkColorScheme: ColorScheme = darkColorScheme(
    primary = EditorialPrimaryCoral,
    onPrimary = EditorialOnPrimary,
    primaryContainer = EditorialSurfaceDarkElevated,
    onPrimaryContainer = EditorialPrimaryCoral,
    inversePrimary = EditorialPrimaryCoralActive,
    secondary = EditorialAccentTeal,
    onSecondary = EditorialSurfaceDark,
    secondaryContainer = EditorialSurfaceDarkSoft,
    onSecondaryContainer = EditorialOnDark,
    tertiary = EditorialAccentAmber,
    onTertiary = EditorialSurfaceDark,
    tertiaryContainer = EditorialSurfaceDarkElevated,
    onTertiaryContainer = EditorialAccentAmber,
    background = EditorialSurfaceDark,
    onBackground = EditorialOnDark,
    surface = EditorialSurfaceDark,
    onSurface = EditorialOnDark,
    surfaceVariant = EditorialSurfaceDarkElevated,
    onSurfaceVariant = EditorialOnDarkSoft,
    surfaceTint = EditorialPrimaryCoral,
    inverseSurface = EditorialCanvas,
    inverseOnSurface = EditorialInk,
    outline = Color(0xFF383531),
    outlineVariant = Color(0xFF2A2825),
    error = EditorialError,
    onError = Color.White,
    errorContainer = Color(0xFF442020),
    onErrorContainer = Color(0xFFFFB4AB)
)

// Backward-compatible aliases
val LightColorScheme: ColorScheme = WarmEditorialLightColorScheme
val DarkColorScheme: ColorScheme = WarmEditorialDarkColorScheme

/**
 * Application Theme applying the Warm Editorial Material 3 ColorScheme.
 *
 * @param darkTheme When false (default), applies the signature warm editorial cream canvas (#FAF9F5)
 *                  with warm coral primary accents (#CC785C).
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WarmEditorialDarkColorScheme else WarmEditorialLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
