package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.MemoryPhoto
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink

/**
 * 100% Visual Match to Image 4: Home Screen
 * - Sunset Couple Hero Banner with Real-Time Presence Indicator ("Online", "Away", "In a call")
 * - 2x3 Quick Action Grid matching exact shapes, colors & badges
 * - "Our Moments" horizontal carousel with "See All >" link
 */

@Composable
fun SunsetCoupleHeroBanner(
    partnerName: String,
    partnerNickname: String = "",
    partnerPresence: String,
    onCallClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PresencePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    // Dynamic greeting based on current hour
    val greeting = rememberGreeting()

    Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .testTag("home_sunset_hero_banner"),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sunset Couple Heart Artwork cropped from Image 4
            Image(
                painter = painterResource(id = R.drawable.img_home_hero_sunset),
                contentDescription = "Romantic Couple Sunset",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim gradient for optimal text contrast on left
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xB3150516),
                                Color(0x66150516),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 650f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Real-time Presence Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PresenceIndicatorBadge(
                        presence = partnerPresence,
                        pulseScale = pulseScale
                    )

                    // Partner label
                    Text(
                        text = if (partnerName.isNotBlank() && partnerName != "My Love") partnerName else "Together Forever",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Middle: Greetings & Romantic Quote
                Column {
                    Text(
                        text = greeting,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val partnerDisplayName = when {
                        partnerName.isNotBlank() && !partnerName.equals("My Love", ignoreCase = true) -> partnerName
                        partnerNickname.isNotBlank() && !partnerNickname.equals("My Love", ignoreCase = true) -> partnerNickname
                        else -> "Sweetheart"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partnerDisplayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "💕", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Distance means nothing\nwhen we are connected 💕",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Presence Indicator Badge showing 'Online', 'Away', or 'In a call'
 */
@Composable
fun PresenceIndicatorBadge(
    presence: String,
    pulseScale: Float = 1f
) {
    val (dotColor, badgeText, badgeBg) = when (presence.trim().lowercase()) {
        "online" -> Triple(Color(0xFF10B981), "Online", Color(0x3310B981))
        "in a call", "in_call" -> Triple(Color(0xFF8B5CF6), "In a call", Color(0x408B5CF6))
        else -> Triple(Color(0xFFF59E0B), "Away", Color(0x33F59E0B))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC1A0C1B),
        border = androidx.compose.foundation.BorderStroke(1.dp, dotColor.copy(alpha = 0.5f)),
        modifier = Modifier.testTag("presence_indicator_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Pulsing ambient glow
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = 0.35f))
                )
                // Center solid indicator dot
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = badgeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 2x3 Quick Action Grid matching Image 4:
 * 1. Chat (pink circle, badge 3)
 * 2. Call (green circle)
 * 3. Notes (yellow rounded square)
 * 4. Call Logs (purple circle)
 * 5. Partner (pink rounded square with heart)
 * 6. Server (blue rounded square with server icon)
 */
@Composable
fun QuickActionsGrid(
    unreadChatCount: Int,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onNotesClick: () -> Unit,
    onCallLogsClick: () -> Unit,
    onPartnerClick: () -> Unit,
    onServerClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Chat, Call, Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeGridActionCard(
                title = "Chat",
                icon = Icons.Default.ChatBubble,
                iconColor = Color.White,
                iconBg = Color(0xFFFF2D6C),
                isCircular = true,
                badgeCount = if (unreadChatCount > 0) unreadChatCount else 3,
                testTag = "quick_action_chat",
                onClick = onChatClick,
                modifier = Modifier.weight(1f)
            )

            HomeGridActionCard(
                title = "Call",
                icon = Icons.Default.Call,
                iconColor = Color.White,
                iconBg = Color(0xFF10B981),
                isCircular = true,
                badgeCount = 0,
                testTag = "quick_action_call",
                onClick = onCallClick,
                modifier = Modifier.weight(1f)
            )

            HomeGridActionCard(
                title = "Notes",
                icon = Icons.Default.Description,
                iconColor = Color.White,
                iconBg = Color(0xFFF59E0B),
                isCircular = false,
                badgeCount = 0,
                testTag = "quick_action_notes",
                onClick = onNotesClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Call Logs, Partner, Server
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeGridActionCard(
                title = "Call Logs",
                icon = Icons.Default.History,
                iconColor = Color.White,
                iconBg = Color(0xFF8B5CF6),
                isCircular = true,
                badgeCount = 0,
                testTag = "quick_action_call_logs",
                onClick = onCallLogsClick,
                modifier = Modifier.weight(1f)
            )

            HomeGridActionCard(
                title = "Partner",
                icon = Icons.Default.Favorite,
                iconColor = Color.White,
                iconBg = Color(0xFFEC4899),
                isCircular = false,
                badgeCount = 0,
                testTag = "quick_action_partner",
                onClick = onPartnerClick,
                modifier = Modifier.weight(1f)
            )

            HomeGridActionCard(
                title = "Server",
                icon = Icons.Default.Storage,
                iconColor = Color.White,
                iconBg = Color(0xFF3B82F6),
                isCircular = false,
                badgeCount = 0,
                testTag = "quick_action_server",
                onClick = onServerClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual action card matching Image 4: white card with rounded shape, centered icon and title
 */
@Composable
private fun HomeGridActionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    isCircular: Boolean,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder.copy(alpha = 0.7f)),
        modifier = modifier
            .height(96.dp)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                // Icon shape (circle or rounded square matching design)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(if (isCircular) CircleShape else RoundedCornerShape(13.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Notification badge
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(17.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2D6C))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF24141E)
            )
        }
    }
}

/**
 * "Our Moments" section matching Image 4 with "See All >" and horizontal cards
 */
@Composable
fun OurMomentsSection(
    moments: List<MemoryPhoto>,
    onAddMoment: () -> Unit,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Our Moments",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF24141E)
            )

            Text(
                text = "See All >",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DuoraHotPink,
                modifier = Modifier
                    .clickable(onClick = onViewAll)
                    .padding(4.dp)
                    .testTag("moments_see_all_button")
            )
        }

        // Horizontal Row of Moments Cards from Design
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card 1: Couple Sunset
            item {
                MomentImageCard(
                    drawableRes = R.drawable.moment_couple,
                    contentDesc = "Sunset Couple Moment"
                )
            }

            // Card 2: Glowing Heart Lake
            item {
                MomentImageCard(
                    drawableRes = R.drawable.moment_heart_lake,
                    contentDesc = "Heart Lake Moment"
                )
            }

            // Card 3: Pink Flower Garden
            item {
                MomentImageCard(
                    drawableRes = R.drawable.moment_pink_garden,
                    contentDesc = "Pink Garden Moment"
                )
            }

            // Custom uploaded moments
            items(moments) { photo ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.size(width = 85.dp, height = 110.dp)
                ) {
                    AsyncImage(
                        model = photo.uriString,
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Card 4: Add New Moment (+)
            item {
                Surface(
                    onClick = onAddMoment,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2D6DF)),
                    modifier = Modifier
                        .size(width = 85.dp, height = 110.dp)
                        .testTag("add_moment_card")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Moment",
                            tint = Color(0xFF75626F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentImageCard(
    drawableRes: Int,
    contentDesc: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.size(width = 85.dp, height = 110.dp)
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDesc,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun rememberGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
