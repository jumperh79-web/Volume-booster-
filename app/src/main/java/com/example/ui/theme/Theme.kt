package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AudioConsoleColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = NeonSky,
    onSecondary = Color.Black,
    tertiary = NeonPurple,
    background = CyberObsidian,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    error = NeonCoral
)

@Composable
fun VolumeBoosterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AudioConsoleColorScheme,
        typography = Typography,
        content = content
    )
}

