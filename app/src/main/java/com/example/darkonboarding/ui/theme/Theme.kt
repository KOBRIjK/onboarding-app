package com.example.darkonboarding.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    background = Bg,
    surface = CardBg,
    primary = AccentPurple,
    secondary = AccentCyan,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = Typography,
        content = content
    )
}
