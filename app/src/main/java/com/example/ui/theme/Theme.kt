package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LoveColorScheme = darkColorScheme(
    primary = LoveRose,
    onPrimary = Color.White,
    primaryContainer = LoveCrimson,
    onPrimaryContainer = Color.White,
    secondary = LoveGold,
    onSecondary = Color.Black,
    secondaryContainer = LoveCardSurface,
    onSecondaryContainer = LoveBlush,
    tertiary = LoveBlush,
    onTertiary = Color.Black,
    background = LoveDarkBg,
    onBackground = LoveWhite,
    surface = LoveDarkSurface,
    onSurface = LoveWhite,
    surfaceVariant = LoveCardSurface,
    onSurfaceVariant = LoveTextMuted,
    outline = LoveCardBorder
)

@Composable
fun AmourCallTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LoveColorScheme,
        typography = Typography,
        content = content
    )
}
