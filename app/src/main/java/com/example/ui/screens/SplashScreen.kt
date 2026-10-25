package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraSecondaryPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Production-Quality Animated Splash Screen for Duora Android
 *
 * Implements the 7-Layer visual hierarchy:
 * Layer 1 — Background: Romantic pastel sunset backdrop with couple composition
 * Layer 2 — Ambient Effects: Floating pink petals with sinusoidal drift + soft ambient glow
 * Layer 3 — Heart Logo: 3D glossy intertwined-heart logo with smooth entrance scale & subtle pulse
 * Layer 4 — Wordmark: "Duora" brand wordmark with heart 'o'
 * Layer 5 — Tagline: "Always Together"
 * Layer 6 — Romantic couple illustration near bottom (part of the composition)
 * Layer 7 — "Bringing you closer..." loading message and rounded gradient progress bar
 */
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val bgAlpha = remember { Animatable(0f) }
    val heartScale = remember { Animatable(0.7f) }
    val heartAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashAmbientMotion")

    // Gentle repeating pulse for heart logo after appearing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeartPulse"
    )

    // Soft ambient glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    // Text breathing alpha
    val textPulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TextPulse"
    )

    // Petal drift animation phase
    val petalPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PetalDrift"
    )

    LaunchedEffect(Unit) {
        // Layer 1: Background fade in
        launch { bgAlpha.animateTo(1f, tween(500)) }

        // Layer 3: Heart Logo enters with gentle scale and fade
        delay(150)
        launch { heartAlpha.animateTo(1f, tween(650)) }
        launch { heartScale.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }

        // Layer 4: Duora Wordmark enters
        delay(350)
        launch { wordmarkAlpha.animateTo(1f, tween(500)) }

        // Layer 5: Tagline enters
        delay(200)
        launch { taglineAlpha.animateTo(1f, tween(500)) }

        // Layer 7: Progress fills smoothly over 2000ms
        delay(150)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )

        delay(200)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen_root")
    ) {
        // Layer 1: Background — Full-screen romantic pastel pink and lavender backdrop
        Image(
            painter = painterResource(id = R.drawable.bg_splash),
            contentDescription = "Duora Romantic Sunset Backdrop",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(bgAlpha.value)
        )

        // Layer 2: Ambient Effects — Floating Petals with sinusoidal drift & rotation
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(bgAlpha.value)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 6 graceful floating petals
            val petalConfigs = listOf(
                Triple(0.15f, 0.2f, 18f),
                Triple(0.82f, 0.35f, 22f),
                Triple(0.28f, 0.65f, 15f),
                Triple(0.72f, 0.75f, 19f),
                Triple(0.48f, 0.15f, 14f),
                Triple(0.88f, 0.55f, 16f)
            )

            for ((i, config) in petalConfigs.withIndex()) {
                val startX = config.first * canvasWidth
                val speedMultiplier = 0.8f + (i * 0.12f)
                val currentY = ((config.second + petalPhase * speedMultiplier) % 1.15f) * canvasHeight - 20f
                val swayX = startX + (sin((petalPhase * 2 * Math.PI.toFloat()) + i) * 24f)
                val petalSize = config.third
                val rotationAngle = (petalPhase * 360f * (if (i % 2 == 0) 1 else -1) + i * 45f) % 360f

                rotate(rotationAngle, pivot = Offset(swayX, currentY)) {
                    val path = Path().apply {
                        moveTo(swayX, currentY - petalSize)
                        cubicTo(
                            swayX + petalSize * 0.8f, currentY - petalSize * 0.4f,
                            swayX + petalSize * 0.6f, currentY + petalSize * 0.8f,
                            swayX, currentY + petalSize
                        )
                        cubicTo(
                            swayX - petalSize * 0.6f, currentY + petalSize * 0.8f,
                            swayX - petalSize * 0.8f, currentY - petalSize * 0.4f,
                            swayX, currentY - petalSize
                        )
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF5D88).copy(alpha = 0.55f),
                                Color(0xFFFF2D8D).copy(alpha = 0.35f),
                                Color(0xFFFFB4D0).copy(alpha = 0.10f)
                            ),
                            center = Offset(swayX, currentY),
                            radius = petalSize * 1.2f
                        )
                    )
                }
            }
        }

        // Layer 3, 4, 5: Branding Stack (Centered in upper-middle area)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 70.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Layer 3: Intertwined 3D Heart Logo with Entrance Scale & Ambient Pulse
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Subtle ambient glow behind heart logo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x66FF5D88).copy(alpha = glowAlpha),
                                    Color(0x33FF2D8D).copy(alpha = glowAlpha * 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.logo_heart),
                    contentDescription = "Duora Intertwined Hearts Emblem",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(heartAlpha.value)
                        .scale(heartScale.value * pulseScale)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Layer 4: Duora Wordmark with heart 'o'
            Image(
                painter = painterResource(id = R.drawable.logo_wordmark),
                contentDescription = "Duora Wordmark",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(44.dp)
                    .alpha(wordmarkAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Layer 5: Tagline — "Always Together"
            Image(
                painter = painterResource(id = R.drawable.logo_tagline),
                contentDescription = "Always Together",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(18.dp)
                    .alpha(taglineAlpha.value)
            )
        }

        // Layer 7: Loading Message & Rounded Progress Indicator near bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 44.dp, start = 48.dp, end = 48.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bringing you closer...",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = textPulse),
                modifier = Modifier
                    .alpha(bgAlpha.value)
                    .testTag("splash_bringing_closer_text")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rounded Progress Pill matching design reference
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.35f))
                    .testTag("splash_progress_bar_track")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.value.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    DuoraPrimaryPink,
                                    DuoraSecondaryPink,
                                    Color(0xFFFF8EB4)
                                )
                            )
                        )
                )
            }
        }
    }
}
