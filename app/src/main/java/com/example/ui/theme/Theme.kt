package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    secondary = NeonMagenta,
    tertiary = BrightCyan,
    background = ReelDarkBackground,
    surface = ReelDarkSurface,
    surfaceVariant = ReelDarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonViolet,
    secondary = NeonMagenta,
    tertiary = BrightCyan,
    background = ReelLightBackground,
    surface = ReelLightSurface,
    surfaceVariant = ReelLightCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF12101F),
    onSurface = Color(0xFF12101F)
)

@Composable
fun ReelLocalTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
