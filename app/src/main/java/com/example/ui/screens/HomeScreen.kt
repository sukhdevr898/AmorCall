package com.example.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CallType
import com.example.model.MemoryPhoto
import com.example.ui.components.MemoryBackground
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LoveCardSurface
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveWhite
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

@Composable
fun HomeScreen(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val activeWallpaper by viewModel.activeWallpaper.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()

    val daysTogether = ((System.currentTimeMillis() - profile.relationshipStartDateEpochMs) /
            (1000L * 60 * 60 * 24)).coerceAtLeast(1)

    MemoryBackground(memory = activeWallpaper, dimAmount = 0.72f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AmourCall",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love Icon",
                            tint = LoveRose,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    val serverState by viewModel.serverConnectionState.collectAsState()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.PAIRING) }
                    ) {
                        val pairStatusText = if (profile.isPaired && profile.partnerPairCode.isNotBlank()) {
                            "#${profile.myPairCode} ⇄ #${profile.partnerPairCode}"
                        } else {
                            "#${profile.myPairCode} • Not Linked"
                        }
                        Text(
                            text = pairStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (profile.isPaired) LoveGold else LoveRose
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (profile.isPaired && serverState == com.example.network.ServerConnectionState.CONNECTED)
                                        LoveAccentGreen else LoveRose
                                )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleMuteNotifications() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LoveCardSurface.copy(alpha = 0.85f))
                            .border(1.dp, LoveCardBorder, CircleShape)
                            .testTag("toggle_notification_mute_button")
                    ) {
                        Icon(
                            imageVector = if (profile.isNotificationsMuted)
                                Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                            contentDescription = if (profile.isNotificationsMuted) "Unmute Alerts" else "Mute Alerts",
                            tint = if (profile.isNotificationsMuted) LoveRose else LoveGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MEMORIES) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LoveCardSurface.copy(alpha = 0.85f))
                            .border(1.dp, LoveCardBorder, CircleShape)
                            .testTag("nav_memories_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Memories",
                            tint = LoveBlush,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.PAIRING) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LoveCardSurface.copy(alpha = 0.85f))
                            .border(1.dp, LoveCardBorder, CircleShape)
                            .testTag("nav_pairing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Pairing",
                            tint = LoveGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DEVELOPER) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LoveCardSurface.copy(alpha = 0.85f))
                            .border(1.dp, LoveCardBorder, CircleShape)
                            .testTag("nav_developer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Developer Info",
                            tint = LoveWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Partner Profile Hero
                item {
                    PartnerHeroCard(
                        partnerName = profile.partnerName,
                        partnerNickname = profile.partnerNickname,
                        partnerStatus = profile.partnerStatus,
                        pairCode = profile.partnerPairCode,
                        daysTogether = daysTogether,
                        avatarUri = profile.avatarUri
                    )
                }

                if (!profile.isPaired || profile.partnerPairCode.isBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(AppScreen.PAIRING) }
                                .testTag("unpaired_alert_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x33F43F5E)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, LoveRose.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(LoveRose.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "Link Partner",
                                        tint = LoveGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Link Partner's Code to Call ❤️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = LoveWhite
                                    )
                                    Text(
                                        text = "Your Code is #${profile.myPairCode}. Tap here to share and link devices.",
                                        fontSize = 11.sp,
                                        color = LoveBlush
                                    )
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(AppScreen.PAIRING) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Quick Call Actions (Voice & Video)
                item {
                    Text(
                        text = "Quick One-Tap Call",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LoveBlush,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Voice Call Button
                        Button(
                            onClick = {
                                onRequestPermissions {
                                    viewModel.startOutgoingCall(CallType.VOICE)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("one_tap_voice_call_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(LoveCrimson, LoveRose)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Voice Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Voice Call",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Direct Hotline",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }

                        // Video Call Button
                        Button(
                            onClick = {
                                onRequestPermissions {
                                    viewModel.startOutgoingCall(CallType.VIDEO)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .testTag("one_tap_video_call_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFFBE185D), LoveRose)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Video Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Video Call",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "See My Love",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Memories Horizontal Carousel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Our Best Memories",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LoveBlush
                        )
                        Text(
                            text = "View All (${memories.size})",
                            fontSize = 12.sp,
                            color = LoveRose,
                            modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.MEMORIES) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(memories) { memory ->
                            MemoryThumbnailCard(
                                memory = memory,
                                isCurrentWallpaper = memory.isSelectedWallpaper,
                                onClick = { viewModel.setWallpaper(memory.id) }
                            )
                        }
                    }
                }

                // Call Logs Summary with Clear Call Logs feature
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Moments",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LoveBlush
                        )
                        if (callHistory.isNotEmpty()) {
                            Text(
                                text = "Clear Logs",
                                fontSize = 12.sp,
                                color = LoveRose,
                                modifier = Modifier
                                    .clickable { viewModel.clearCallHistory() }
                                    .testTag("clear_call_logs_button")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (callHistory.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LoveCardSurface.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Love",
                                    tint = LoveRose.copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No call logs yet",
                                    color = LoveWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "One-tap voice or video call above to connect directly with ${profile.partnerName} ❤️",
                                    color = LoveTextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            callHistory.take(6).forEach { record ->
                                CallRecordRow(record = record, partnerName = profile.partnerName)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PartnerHeroCard(
    partnerName: String,
    partnerNickname: String,
    partnerStatus: String,
    pairCode: String,
    daysTogether: Long,
    avatarUri: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = LoveCardSurface.copy(alpha = 0.85f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, LoveCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with glowing ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(3.dp, LoveRose, CircleShape)
                    .background(Color(0xFF3B0B2E)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUri.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Partner Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Avatar",
                        tint = LoveRose,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$partnerName ❤️",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LoveWhite
            )

            Text(
                text = "“$partnerNickname”",
                fontSize = 14.sp,
                color = LoveGold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x3310B981),
                border = androidx.compose.foundation.BorderStroke(1.dp, LoveAccentGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LoveAccentGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = partnerStatus,
                        fontSize = 11.sp,
                        color = LoveWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Days Together Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LoveCrimson.copy(alpha = 0.2f))
                    .border(1.dp, LoveCrimson.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💖", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Days in Love:",
                        fontSize = 13.sp,
                        color = LoveBlush
                    )
                }
                Text(
                    text = "$daysTogether Days",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveGold
                )
            }
        }
    }
}

@Composable
private fun MemoryThumbnailCard(
    memory: MemoryPhoto,
    isCurrentWallpaper: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isCurrentWallpaper) 2.dp else 1.dp,
            if (isCurrentWallpaper) LoveGold else LoveCardBorder
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!memory.uriString.isNullOrBlank()) {
                AsyncImage(
                    model = memory.uriString,
                    contentDescription = memory.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(memory.gradientStartHex),
                                    Color(memory.gradientEndHex)
                                )
                            )
                        )
                )
            }

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC0E0712))
                        )
                    )
            )

            if (isCurrentWallpaper) {
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                    color = LoveGold,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Active Wallpaper",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = memory.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = memory.dateStr,
                    fontSize = 9.sp,
                    color = LoveBlush
                )
            }
        }
    }
}

@Composable
private fun CallRecordRow(record: com.example.model.CallRecord, partnerName: String) {
    val durationText = if (record.durationSeconds > 0) {
        val mins = record.durationSeconds / 60
        val secs = record.durationSeconds % 60
        "${mins}m ${secs}s"
    } else {
        "Missed"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LoveCardSurface.copy(alpha = 0.7f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (record.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call Icon",
                    tint = if (record.type == CallType.VIDEO) LoveRose else LoveAccentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (record.isOutgoing) "Outgoing to $partnerName" else "Incoming from $partnerName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = LoveWhite
                    )
                    Text(
                        text = if (record.type == CallType.VIDEO) "Video Call" else "Voice Call",
                        fontSize = 11.sp,
                        color = LoveTextMuted
                    )
                }
            }

            Text(
                text = durationText,
                fontSize = 12.sp,
                color = if (record.durationSeconds > 0) LoveGold else LoveRose,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
