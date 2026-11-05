package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonBlueDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = DarkOledBackground,
    primaryContainer = NeonBlueContainer,
    onPrimaryContainer = NeonBlue,
    secondary = NeonBlueGlow,
    onSecondary = DarkOledBackground,
    secondaryContainer = DarkOledSurfaceVariant,
    onSecondaryContainer = NeonBlueGlow,
    tertiary = NeonBlueOperatorActive,
    onTertiary = DarkOledBackground,
    background = DarkOledBackground,
    onBackground = TextWhite,
    surface = DarkOledSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkOledSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = NeonBlueBorder,
    error = Color(0xFFFF5252),
    onError = Color.White
)

@Composable
fun KalkulatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonBlueDarkColorScheme,
        typography = Typography,
        content = content
    )
}
