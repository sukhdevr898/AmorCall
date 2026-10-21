package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoupleProfile
import com.example.ui.theme.DuoraDarkBg
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraRaspberry
import com.example.ui.theme.LoveGold
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String,
    val badge1: String,
    val badge2: String
)

@Composable
fun OnboardingScreen(
    currentProfile: CoupleProfile,
    onComplete: (partnerName: String, partnerNickname: String, partnerCode: String) -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Your private space",
            subtitle = "Only you and your paired partner. No strangers, no discovery feeds, no group chats—just a private, intimate sanctuary.",
            icon = Icons.Default.Lock,
            tag = "Two hearts. One private space.",
            badge1 = "Mutual Pairing Only",
            badge2 = "Zero Public Profile"
        ),
        OnboardingPageData(
            title = "Stay close",
            subtitle = "Real-time chat with truthful delivery, read states, live typing feedback, and 24-hour sweet partner love notes.",
            icon = Icons.Default.ChatBubble,
            tag = "Instant & Truthful Messaging",
            badge1 = "Delivered & Read Receipts",
            badge2 = "24-Hour Couple Notes"
        ),
        OnboardingPageData(
            title = "Hear and see each other",
            subtitle = "One-tap private audio and video calls, seamless background hotline, picture-in-picture multitasking, and floating heart reactions.",
            icon = Icons.Default.Videocam,
            tag = "Direct Audio & Video Hotline",
            badge1 = "Picture-in-Picture",
            badge2 = "Floating Heart Reactions"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DuoraDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wordmark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Duora",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = DuoraRaspberry,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Skip button on pages 0 and 1
                AnimatedVisibility(visible = pagerState.currentPage < 2) {
                    TextButton(
                        onClick = {
                            onComplete(
                                currentProfile.partnerName,
                                currentProfile.partnerNickname,
                                currentProfile.partnerPairCode
                            )
                        },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            color = DuoraPaleSky.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing Icon Container
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(
                                        DuoraRaspberry.copy(alpha = 0.45f),
                                        Color(0xFF23184A),
                                        DuoraDarkBg
                                    )
                                )
                            )
                            .border(2.dp, DuoraRaspberry.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = page.title,
                            tint = DuoraPaleSky,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Tagline chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DuoraRaspberry.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DuoraRaspberry.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = page.tag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DuoraPaleSky,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.subtitle,
                        fontSize = 15.sp,
                        color = DuoraPaleSky.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Feature highlights row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF262035),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382C4E))
                        ) {
                            Text(
                                text = "✓ ${page.badge1}",
                                fontSize = 11.sp,
                                color = LoveGold,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF262035),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382C4E))
                        ) {
                            Text(
                                text = "✓ ${page.badge2}",
                                fontSize = 11.sp,
                                color = LoveGold,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) DuoraRaspberry else DuoraPaleSky.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Action Button (Continue or Get started)
                if (pagerState.currentPage < 2) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("onboarding_continue_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DuoraRaspberry,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onComplete(
                                currentProfile.partnerName,
                                currentProfile.partnerNickname,
                                currentProfile.partnerPairCode
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("onboarding_get_started_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DuoraRaspberry,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
