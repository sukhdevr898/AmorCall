package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MemoryPhoto
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraPinkGradientEnd
import com.example.ui.theme.DuoraPinkGradientStart
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveGold

/**
 * 100% Visual Match to Sheet 2 Home Screen:
 * - Sunset Couple Hero Banner with Romantic Greeting
 * - 2x3 Quick Action Grid (Chat, Call, Notes, Call Logs, Partner, Server)
 * - "Our Moments ❤️" Horizontal Gallery
 */

@Composable
fun SunsetCoupleHeroBanner(
    partnerName: String,
    isPaired: Boolean,
    isOnline: Boolean,
    onCallClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_sunset_hero_banner"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF5282),
                            Color(0xFFFF3366),
                            Color(0xFFFF6B95)
                        )
                    )
                )
        ) {
            // Romantic sunset silhouette decorative artwork on canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                val w = size.width
                val h = size.height

                // Sunset golden sun
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(w * 0.78f, h * 0.38f),
                        radius = 80.dp.toPx()
                    ),
                    center = Offset(w * 0.78f, h * 0.38f),
                    radius = 80.dp.toPx()
                )

                // Gentle hill silhouette
                val hillColor = Color(0x332B0F2A)
                drawOval(
                    color = hillColor,
                    topLeft = Offset(w * 0.45f, h * 0.65f),
                    size = androidx.compose.ui.geometry.Size(w * 0.65f, h * 0.6f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Tag: Live Connection Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) LoveAccentGreen else Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPaired) "Always Connected 💕" else "Connect Hearts ❤️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Romantic greeting
                Text(
                    text = "Good Morning My Love ❤️",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Distance means nothing when we are connected",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom strip with partner avatar and quick action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (partnerName.isNotBlank()) "With $partnerName 🥰" else "Private Couple Space",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Surface(
                        onClick = onCallClick,
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        modifier = Modifier.testTag("hero_quick_call_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = DuoraHotPink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Call Love",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraHotPink
                            )
                        }
                    }
                }
            }
        }
    }
}

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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Quick Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Row 1: Chat, Call, Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionItem(
                title = "Chat",
                subtitle = "Real-time msg",
                icon = Icons.Default.ChatBubble,
                iconBg = Color(0xFFFF2D6C),
                badgeCount = unreadChatCount,
                testTag = "quick_action_chat",
                onClick = onChatClick,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                title = "Call",
                subtitle = "Voice & video",
                icon = Icons.Default.Call,
                iconBg = Color(0xFF10B981),
                badgeCount = 0,
                testTag = "quick_action_call",
                onClick = onCallClick,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                title = "Notes",
                subtitle = "Shared words",
                icon = Icons.Default.Description,
                iconBg = Color(0xFFF97316),
                badgeCount = 0,
                testTag = "quick_action_notes",
                onClick = onNotesClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Call Logs, Partner, Server
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionItem(
                title = "Call Logs",
                subtitle = "Recent history",
                icon = Icons.Default.History,
                iconBg = Color(0xFF8B5CF6),
                badgeCount = 0,
                testTag = "quick_action_call_logs",
                onClick = onCallLogsClick,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                title = "Partner",
                subtitle = "Profile & code",
                icon = Icons.Default.Favorite,
                iconBg = Color(0xFFEC4899),
                badgeCount = 0,
                testTag = "quick_action_partner",
                onClick = onPartnerClick,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                title = "Server",
                subtitle = "Health & sync",
                icon = Icons.Default.Cloud,
                iconBg = Color(0xFF3B82F6),
                badgeCount = 0,
                testTag = "quick_action_server",
                onClick = onServerClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconBg,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(DuoraHotPink),
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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OurMomentsSection(
    moments: List<MemoryPhoto>,
    onAddMoment: () -> Unit,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Our Moments",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("❤️", fontSize = 14.sp)
            }

            Surface(
                onClick = onAddMoment,
                shape = RoundedCornerShape(12.dp),
                color = DuoraSoftBlush,
                border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = DuoraHotPink,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Add",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoraHotPink
                    )
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Default Romantic Couple Moments if list is empty
            if (moments.isEmpty()) {
                val samples = listOf(
                    "Beach Sunset" to "Oct 14, 2024",
                    "First Coffee Date" to "Nov 02, 2024",
                    "Stargazing Night" to "Dec 25, 2024"
                )
                items(samples) { sample ->
                    SampleMomentCard(title = sample.first, date = sample.second, onClick = onViewAll)
                }
            } else {
                items(moments) { photo ->
                    RealMomentCard(photo = photo, onClick = onViewAll)
                }
            }
        }
    }
}

@Composable
private fun SampleMomentCard(
    title: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF85A1), Color(0xFFFF5282))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = date,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RealMomentCard(
    photo: MemoryPhoto,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DuoraSoftBlush)
            ) {
                AsyncImage(
                    model = photo.uriString,
                    contentDescription = photo.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = photo.title.ifBlank { "Our Memory" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = photo.dateStr,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
