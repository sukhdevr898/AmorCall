package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.model.FloatingHeart
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import kotlin.math.sin

@Composable
fun FloatingHeartsOverlay(
    hearts: List<FloatingHeart>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            SingleFloatingHeart(heart = heart)
        }
    }
}

@Composable
private fun SingleFloatingHeart(heart: FloatingHeart) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val progress = remember { Animatable(0f) }

    LaunchedEffect(heart.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2600, easing = LinearEasing)
        )
    }

    val yOffset = screenHeight * (1f - progress.value * 1.15f)
    val sway = (sin(progress.value * 10f) * 24f).dp
    val xOffset = screenWidth * heart.startXFraction + sway
    val alpha = (1f - progress.value).coerceIn(0f, 1f)

    val color = if (heart.id % 2 == 0L) LoveRose else LoveGold

    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Floating Love Heart",
        tint = color,
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .scale(heart.scale * (0.8f + progress.value * 0.4f))
            .alpha(alpha)
    )
}
