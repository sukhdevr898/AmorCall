package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppTheme

private val DuoraDarkColorScheme = darkColorScheme(
    primary = DuoraLilac,
    onPrimary = Color(0xFF100D17),
    primaryContainer = DuoraCardSurface,
    onPrimaryContainer = DuoraLilac,
    secondary = DuoraRose,
    onSecondary = Color(0xFF100D17),
    secondaryContainer = DuoraCardSurface,
    onSecondaryContainer = DuoraRose,
    tertiary = LoveGold,
    onTertiary = Color.Black,
    background = DuoraDarkBg,
    onBackground = DuoraLightBg,
    surface = DuoraDarkSurface,
    onSurface = DuoraLightBg,
    surfaceVariant = DuoraCardSurface,
    onSurfaceVariant = LoveTextMuted,
    outline = DuoraCardBorder
)

private val SlateDarkColorScheme = darkColorScheme(
    primary = DuoraLilac,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF28203B),
    onPrimaryContainer = Color.White,
    secondary = DuoraRose,
    onSecondary = Color.Black,
    secondaryContainer = DarkCardSurface,
    onSecondaryContainer = DarkTextSecondary,
    tertiary = LoveGold,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

private val DuoraLightColorScheme = lightColorScheme(
    primary = DuoraLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4FF),
    onPrimaryContainer = DuoraLightPrimary,
    secondary = DuoraRose,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5EFFF),
    onSecondaryContainer = LightTextPrimary,
    tertiary = LoveGold,
    onTertiary = Color.Black,
    background = DuoraLightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF3EEFF),
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFDDD2F5)
)

@Composable
fun DuoraTheme(
    theme: AppTheme = AppTheme.LOVE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LOVE -> DuoraDarkColorScheme
        AppTheme.DARK -> SlateDarkColorScheme
        AppTheme.LIGHT -> DuoraLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatible alias
@Composable
fun AmourCallTheme(
    theme: AppTheme = AppTheme.LOVE,
    content: @Composable () -> Unit
) = DuoraTheme(theme = theme, content = content)
