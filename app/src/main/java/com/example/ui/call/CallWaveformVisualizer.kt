package com.example.ui.call

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated Sound Wave Visualizer flanking the avatar on left and right,
 * as depicted in Duora Outgoing Audio Call (Sheet 1) and Ongoing Audio Call (Sheet 3).
 */
@Composable
fun SideWaveform(
    isLeft: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 6,
    maxHeight: Dp = 56.dp,
    barWidth: Dp = 3.5.dp,
    spacing: Dp = 4.dp
) {
    val transition = rememberInfiniteTransition(label = "SideWaveform")

    val anim1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave1"
    )

    val anim2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave2"
    )

    val anim3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(720, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Wave3"
    )

    val animFactors = listOf(anim1, anim2, anim3, anim2 * 0.9f, anim1 * 1.1f, anim3 * 0.7f)

    Canvas(
        modifier = modifier
            .width((barWidth + spacing) * barCount)
            .height(maxHeight)
    ) {
        val w = barWidth.toPx()
        val s = spacing.toPx()
        val totalH = size.height

        for (i in 0 until barCount) {
            val factorIdx = if (isLeft) (barCount - 1 - i) else i
            val progress = (animFactors.getOrNull(factorIdx % animFactors.size) ?: 0.5f).coerceIn(0.2f, 1.0f)
            val barH = totalH * progress
            val x = i * (w + s)
            val y = (totalH - barH) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        CallColors.PrimaryPink,
                        CallColors.SecondaryPink
                    )
                ),
                topLeft = Offset(x, y),
                size = Size(w, barH),
                cornerRadius = CornerRadius(w / 2f, w / 2f)
            )
        }
    }
}

/**
 * Avatar with symmetrical left and right audio wave pulses
 */
@Composable
fun AvatarWithSideWaveforms(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        SideWaveform(isLeft = true)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
        content()
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
        SideWaveform(isLeft = false)
    }
}
