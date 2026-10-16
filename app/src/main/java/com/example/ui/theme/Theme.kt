package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = SurfaceCreamStrong,
    onPrimaryContainer = CoralPrimaryActive,
    secondary = CoralPrimary,
    onSecondary = Color.White,
    background = CanvasCream,
    onBackground = TextInk,
    surface = CanvasCream,
    onSurface = TextInk,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextBody,
    outline = HairlineBorder,
    outlineVariant = HairlineBorderSoft,
    error = SemanticError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = SurfaceDarkElevated,
    onPrimaryContainer = CoralPrimary,
    secondary = CoralPrimary,
    onSecondary = Color.White,
    background = SurfaceDark,
    onBackground = OnDarkText,
    surface = SurfaceDark,
    onSurface = OnDarkText,
    surfaceVariant = SurfaceDarkSoft,
    onSurfaceVariant = OnDarkSoft,
    outline = SurfaceDarkElevated,
    outlineVariant = SurfaceDarkElevated,
    error = SemanticError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally prioritize the Warm Cream Editorial theme requested by user
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
