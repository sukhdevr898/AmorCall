package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AppTheme

private val DuoraDarkColorScheme = darkColorScheme(
    primary = DuoraRaspberry,
    onPrimary = Color.White,
    primaryContainer = DuoraDarkElevatedSurface,
    onPrimaryContainer = DuoraPaleSky,
    secondary = DuoraPaleSky,
    onSecondary = DuoraDarkBg,
    secondaryContainer = DuoraDarkElevatedSurface,
    onSecondaryContainer = DuoraPaleSky,
    tertiary = LoveGold,
    onTertiary = Color.Black,
    background = DuoraDarkBg,
    onBackground = DuoraDarkPrimaryText,
    surface = DuoraDarkSurface,
    onSurface = DuoraDarkPrimaryText,
    surfaceVariant = DuoraDarkElevatedSurface,
    onSurfaceVariant = DuoraDarkSecondaryText,
    outline = DuoraDarkOutline
)

private val SlateDarkColorScheme = darkColorScheme(
    primary = DuoraRaspberry,
    onPrimary = Color.White,
    primaryContainer = DuoraDarkElevatedSurface,
    onPrimaryContainer = DuoraPaleSky,
    secondary = DuoraPaleSky,
    onSecondary = Color.Black,
    secondaryContainer = DuoraDarkElevatedSurface,
    onSecondaryContainer = DuoraDarkSecondaryText,
    tertiary = LoveGold,
    onTertiary = Color.Black,
    background = DuoraDarkBg,
    onBackground = DuoraDarkPrimaryText,
    surface = DuoraDarkSurface,
    onSurface = DuoraDarkPrimaryText,
    surfaceVariant = DuoraDarkElevatedSurface,
    onSurfaceVariant = DuoraDarkSecondaryText,
    outline = DuoraDarkOutline
)

private val DuoraLightColorScheme = lightColorScheme(
    primary = DuoraHotPink,
    onPrimary = Color.White,
    primaryContainer = DuoraPaleSky,
    onPrimaryContainer = DuoraHotPink,
    secondary = DuoraRose,
    onSecondary = Color.White,
    secondaryContainer = DuoraPaleSky,
    onSecondaryContainer = DuoraLightPrimaryText,
    tertiary = LoveGold,
    onTertiary = Color.Black,
    background = DuoraLightBg,
    onBackground = DuoraLightPrimaryText,
    surface = DuoraLightSurface,
    onSurface = DuoraLightPrimaryText,
    surfaceVariant = DuoraSoftBlush,
    onSurfaceVariant = DuoraLightSecondaryText,
    outline = DuoraLightOutline
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
