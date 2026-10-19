package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveDarkBg
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    // Pulsing heartbeat animation for ambient ring
    val infiniteTransition = rememberInfiniteTransition(label = "SplashRings")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RingAlpha"
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(650, easing = FastOutSlowInEasing))
        scale.animateTo(1.08f, animationSpec = tween(850, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        delay(800)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF2C072A),
                        Color(0xFF19061E),
                        LoveDarkBg
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp)
        ) {
            // Heart Emblem with Pulsing Glow Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Outer Pulsing Ambient Ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(2.dp, LoveRose.copy(alpha = ringAlpha), CircleShape)
                )

                // Middle Soft Glow
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(LoveRose.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )

                // Inner Heart Circle
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(LoveRose, LoveCrimson, Color(0xFF4A052E))
                            )
                        )
                        .border(2.dp, LoveGold.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "AmourCall Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Name & Version Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Duora",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = LoveWhite
                )
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LoveRose.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "v2.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoveGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Two hearts. One private space.",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LoveGold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Real-Time Calling • PiP Mode • Private Hotline",
                fontSize = 12.sp,
                color = LoveTextMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Security Indicator
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x33000000),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secured",
                        tint = LoveAccentGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Private & Direct Connection",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoveWhite.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
