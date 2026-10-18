package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppTheme

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

private val DarkColorScheme = darkColorScheme(
    primary = LoveRose,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF374151),
    onPrimaryContainer = Color.White,
    secondary = LoveGold,
    onSecondary = Color.Black,
    secondaryContainer = DarkCardSurface,
    onSecondaryContainer = DarkTextSecondary,
    tertiary = LoveBlush,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LoveCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = LoveCrimson,
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = LightTextPrimary,
    tertiary = LoveRose,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = LightTextSecondary,
    outline = LightCardBorder
)

@Composable
fun AmourCallTheme(
    theme: AppTheme = AppTheme.LOVE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LOVE -> LoveColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.LIGHT -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

