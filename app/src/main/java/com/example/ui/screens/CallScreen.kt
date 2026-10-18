package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.ui.components.CameraPreview
import com.example.ui.components.FloatingHeartsOverlay
import com.example.ui.components.MemoryBackground
import com.example.ui.components.PulsingHeartRing
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCardSurface
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveWhite
import com.example.viewmodel.LoveCallViewModel

@Composable
fun CallScreen(
    viewModel: LoveCallViewModel,
    callSession: CallSession
) {
    val profile by viewModel.profile.collectAsState()
    val activeWallpaper by viewModel.activeWallpaper.collectAsState()
    val floatingHearts by viewModel.floatingHearts.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = interactionSource, indication = null) {
                viewModel.sendFloatingHeart()
            }
    ) {
        // Background Layer: Either live Camera preview or romantic Memory Wallpaper
        if (callSession.type == CallType.VIDEO && callSession.status == CallStatus.CONNECTED) {
            CameraPreview(
                isFrontCamera = callSession.isFrontCamera,
                isVideoEnabled = callSession.isVideoEnabled,
                modifier = Modifier.fillMaxSize()
            )
            // Romantic subtle dark gradient over camera for top/bottom controls
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xAA0E0712),
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xDD0E0712)
                            )
                        )
                    )
            )
        } else {
            MemoryBackground(
                memory = activeWallpaper,
                dimAmount = if (callSession.status == CallStatus.CONNECTED) 0.65f else 0.5f
            ) {}
        }

        // Floating Hearts Layer
        FloatingHeartsOverlay(hearts = floatingHearts)

        // Main Foreground UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Call Info Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LoveCardSurface.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = LoveRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (callSession.type == CallType.VIDEO) "Secure Video Call" else "Secure Voice Call",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = LoveWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.partnerName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveWhite
                )

                Text(
                    text = "“${profile.partnerNickname}”",
                    fontSize = 16.sp,
                    color = LoveGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Call Status / Duration
                when (callSession.status) {
                    CallStatus.OUTGOING_RINGING -> {
                        Text(
                            text = "Calling ${profile.partnerName}... ❤️",
                            fontSize = 15.sp,
                            color = LoveBlush
                        )
                    }
                    CallStatus.INCOMING_RINGING -> {
                        Text(
                            text = "❤️ ${profile.partnerName} is calling you...",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveAccentGreen
                        )
                    }
                    CallStatus.CONNECTED -> {
                        val mins = callSession.durationSeconds / 60
                        val secs = callSession.durationSeconds % 60
                        val formatted = String.format("%02d:%02d", mins, secs)
                        Text(
                            text = formatted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveGold
                        )
                    }
                    else -> {}
                }
            }

            // Middle Area: Partner Avatar / Pulsing Rings or PiP
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (callSession.status == CallStatus.OUTGOING_RINGING || callSession.status == CallStatus.INCOMING_RINGING) {
                    PulsingHeartRing(baseSize = 130.dp) {
                        PartnerAvatarCircle(
                            avatarUri = profile.avatarUri,
                            sizeDp = 120
                        )
                    }
                } else if (callSession.type == CallType.VOICE) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PartnerAvatarCircle(
                            avatarUri = profile.avatarUri,
                            sizeDp = 140
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Tap anywhere to send Love Hearts ❤️",
                            fontSize = 12.sp,
                            color = LoveBlush.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    // Video Call Connected: Local PiP Preview in corner
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .size(width = 110.dp, height = 150.dp)
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(16.dp)),
                            border = androidx.compose.foundation.BorderStroke(2.dp, LoveRose)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF260824)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "My Feed",
                                    tint = LoveRose,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "You",
                                    fontSize = 11.sp,
                                    color = LoveWhite,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Controls Area
            when (callSession.status) {
                CallStatus.INCOMING_RINGING -> {
                    // Incoming Answer & Decline Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Button (Red)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { viewModel.declineCall() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LoveCrimson)
                                    .testTag("decline_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Decline Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Decline", color = LoveWhite, fontSize = 13.sp)
                        }

                        // Answer Button (Green)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { viewModel.acceptCall() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LoveAccentGreen)
                                    .testTag("answer_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Answer Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Answer ❤️", color = LoveWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                CallStatus.OUTGOING_RINGING, CallStatus.CONNECTED -> {
                    // In-Call Action Toolbar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Floating Heart Reaction Trigger Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = LoveRose.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { viewModel.sendFloatingHeart() }
                                .testTag("send_heart_reaction_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Heart Reaction",
                                    tint = LoveRose,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Send Love Reaction ❤️",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LoveWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Controls Row: Mute, Speaker, Camera, Flip, End Call
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = LoveCardSurface.copy(alpha = 0.9f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mute Button
                                CallControlButton(
                                    icon = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    label = if (callSession.isMuted) "Unmute" else "Mute",
                                    isActive = callSession.isMuted,
                                    activeColor = LoveRose,
                                    onClick = { viewModel.toggleMute() },
                                    testTag = "toggle_mute_button"
                                )

                                // Speaker Button
                                CallControlButton(
                                    icon = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    label = if (callSession.isSpeakerOn) "Speaker" else "Earpiece",
                                    isActive = callSession.isSpeakerOn,
                                    activeColor = LoveGold,
                                    onClick = { viewModel.toggleSpeaker() },
                                    testTag = "toggle_speaker_button"
                                )

                                if (callSession.type == CallType.VIDEO) {
                                    // Video Toggle Button
                                    CallControlButton(
                                        icon = if (callSession.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                        label = if (callSession.isVideoEnabled) "Cam On" else "Cam Off",
                                        isActive = !callSession.isVideoEnabled,
                                        activeColor = LoveRose,
                                        onClick = { viewModel.toggleVideo() },
                                        testTag = "toggle_video_button"
                                    )

                                    // Switch Camera Button (Front / Back)
                                    CallControlButton(
                                        icon = Icons.Default.Cameraswitch,
                                        label = if (callSession.isFrontCamera) "Front" else "Back",
                                        isActive = false,
                                        activeColor = LoveWhite,
                                        onClick = { viewModel.switchCamera() },
                                        testTag = "switch_camera_button"
                                    )
                                }

                                // End Call Button
                                IconButton(
                                    onClick = { viewModel.endCall() },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(LoveCrimson)
                                        .testTag("end_call_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallEnd,
                                        contentDescription = "End Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun PartnerAvatarCircle(
    avatarUri: String?,
    sizeDp: Int
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
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
                contentDescription = "Love Heart Avatar",
                tint = LoveRose,
                modifier = Modifier.size((sizeDp / 2).dp)
            )
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.35f) else Color(0x33FFFFFF))
                .border(
                    1.dp,
                    if (isActive) activeColor else Color(0x22FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else LoveWhite,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = LoveWhite.copy(alpha = 0.8f)
        )
    }
}
