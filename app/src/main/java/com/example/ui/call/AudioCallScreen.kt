package com.example.ui.call

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.IconButton

/**
 * Duora Audio Call Screen implementing Sheets 1, 2, and 3 from the Design Reference:
 * 1. Outgoing Audio Call: "Calling...", avatar in glowing pink ring + side wave bars, Mute/Speaker/Keypad + End
 * 2. Incoming Audio Call: "Incoming Audio Call", Message / Remind Me row, Decline / Accept buttons
 * 3. Ongoing Audio Call: Elapsed duration timer, 2 rows of round controls (Mute, Speaker, Keypad / Add Call, Hold, Record), End
 */
@Composable
fun AudioCallScreen(
    callSession: CallSession,
    partnerName: String,
    partnerNickname: String,
    avatarUri: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSendMessageQuick: () -> Unit = {},
    onRemindMe: () -> Unit = {},
    onMinimize: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showKeypad by remember { mutableStateOf(false) }
    var isOnHold by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CallColors.DarkBg)
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
            // Top Section: Minimize button, Status pill, and Partner details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Top header row with Minimize button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (callSession.status == CallStatus.CONNECTED || callSession.status == CallStatus.RECONNECTING) {
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CallColors.StatusPillBg)
                                .testTag("audio_call_minimize_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize Call",
                                tint = CallColors.PrimaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (callSession.status == CallStatus.RECONNECTING) Color(0x33FF5722) else CallColors.StatusPillBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (callSession.status == CallStatus.RECONNECTING) Color(0xFFFF5722) else CallColors.StatusPillBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = if (callSession.status == CallStatus.RECONNECTING) Color(0xFFFF7043) else CallColors.PrimaryPink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (callSession.status) {
                                    CallStatus.OUTGOING_RINGING -> "Calling..."
                                    CallStatus.INCOMING_RINGING -> "Incoming Audio Call"
                                    CallStatus.CONNECTING -> "Connecting..."
                                    CallStatus.RECONNECTING -> "Reconnecting (${callSession.reconnectGraceSecondsRemaining}s)..."
                                    CallStatus.CONNECTED -> {
                                        val mins = callSession.durationSeconds / 60
                                        val secs = callSession.durationSeconds % 60
                                        String.format("%02d:%02d", mins, secs)
                                    }
                                    else -> "Audio Call"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (callSession.status == CallStatus.RECONNECTING) Color(0xFFFFAB91) else CallColors.PrimaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = partnerName.ifBlank { "My Love 💕" },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = CallColors.PrimaryText
                )

                Text(
                    text = "Audio Call",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CallColors.SecondaryText
                )

                if (callSession.status == CallStatus.RECONNECTING) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33FF5722),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5722))
                    ) {
                        Text(
                            text = "Connection interrupted • Retrying (${callSession.reconnectGraceSecondsRemaining}s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7043),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                if (isOnHold) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x33FFB300),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Text(
                            text = "Call on Hold",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Middle Section: Partner Avatar with Glowing Ring and Side Waveforms
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (callSession.status) {
                    CallStatus.OUTGOING_RINGING -> {
                        // Sheet 1: Outgoing call with horizontal side waveforms
                        AvatarWithSideWaveforms {
                            CallAvatarWithPulse(
                                avatarUri = avatarUri,
                                partnerName = partnerName,
                                baseSize = 130.dp
                            )
                        }
                    }
                    CallStatus.INCOMING_RINGING -> {
                        // Sheet 2: Incoming call with prominent pulsing ring
                        CallAvatarWithPulse(
                            avatarUri = avatarUri,
                            partnerName = partnerName,
                            baseSize = 140.dp
                        )
                    }
                    CallStatus.CONNECTED -> {
                        // Sheet 3: Ongoing call with active side waveforms
                        AvatarWithSideWaveforms {
                            CallAvatarWithPulse(
                                avatarUri = avatarUri,
                                partnerName = partnerName,
                                baseSize = 130.dp
                            )
                        }
                    }
                    else -> {
                        CallAvatarWithPulse(
                            avatarUri = avatarUri,
                            partnerName = partnerName,
                            baseSize = 130.dp
                        )
                    }
                }
            }

            // Bottom Section: Call Controls matching the respective Sheet
            when (callSession.status) {
                CallStatus.OUTGOING_RINGING -> {
                    // Sheet 1: Outgoing Controls
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CallRoundButton(
                                icon = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                label = "Mute",
                                isActive = callSession.isMuted,
                                onClick = onToggleMute,
                                testTag = "audio_mute_button"
                            )
                            CallRoundButton(
                                icon = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                label = "Speaker",
                                isActive = callSession.isSpeakerOn,
                                onClick = onToggleSpeaker,
                                testTag = "audio_speaker_button"
                            )
                            CallRoundButton(
                                icon = Icons.Default.Dialpad,
                                label = "Keypad",
                                onClick = { showKeypad = true },
                                testTag = "audio_keypad_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        EndCallButton(onClick = onEndCall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                CallStatus.INCOMING_RINGING -> {
                    // Sheet 2: Incoming Controls with Message/Remind Me & Decline/Accept
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Mid-row: Quick Message & Remind Me
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CallRoundButton(
                                icon = Icons.Default.ChatBubble,
                                label = "Message",
                                onClick = onSendMessageQuick,
                                testTag = "incoming_message_button"
                            )
                            CallRoundButton(
                                icon = Icons.Default.AccessTime,
                                label = "Remind Me",
                                onClick = onRemindMe,
                                testTag = "incoming_remind_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // Bottom row: Decline (Red) on left, Accept (Green) on right
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IncomingActionButton(
                                isAccept = false,
                                label = "Decline",
                                onClick = onDecline
                            )
                            IncomingActionButton(
                                isAccept = true,
                                label = "Accept",
                                onClick = onAccept
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                CallStatus.CONNECTED, CallStatus.RECONNECTING, CallStatus.CONNECTING -> {
                    // Sheet 3: Connected Controls with 2 Rows of Controls + Large End Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: Mute, Speaker, Keypad
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CallRoundButton(
                                icon = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                label = "Mute",
                                isActive = callSession.isMuted,
                                onClick = onToggleMute,
                                testTag = "connected_mute_button"
                            )
                            CallRoundButton(
                                icon = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                label = "Speaker",
                                isActive = callSession.isSpeakerOn,
                                onClick = onToggleSpeaker,
                                testTag = "connected_speaker_button"
                            )
                            CallRoundButton(
                                icon = Icons.Default.Dialpad,
                                label = "Keypad",
                                onClick = { showKeypad = true },
                                testTag = "connected_keypad_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Row 2: Add Call, Hold, Record
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CallRoundButton(
                                icon = Icons.Default.PersonAdd,
                                label = "Add Call",
                                onClick = { onSendMessageQuick() },
                                testTag = "connected_add_call_button"
                            )
                            CallRoundButton(
                                icon = Icons.Default.Pause,
                                label = if (isOnHold) "Unhold" else "Hold",
                                isActive = isOnHold,
                                activeColor = Color(0xFFFFB300),
                                onClick = { isOnHold = !isOnHold },
                                testTag = "connected_hold_button"
                            )
                            CallRoundButton(
                                icon = Icons.Default.FiberManualRecord,
                                label = if (isRecording) "Recording" else "Record",
                                isActive = isRecording,
                                activeColor = CallColors.Decline,
                                onClick = { isRecording = !isRecording },
                                testTag = "connected_record_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        EndCallButton(onClick = onEndCall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                else -> {}
            }
        }

        // DTMF Keypad Dialog
        if (showKeypad) {
            CallKeypadDialog(
                onDismissRequest = { showKeypad = false },
                onDigitPressed = {}
            )
        }
    }
}

/**
 * Avatar with Neon Glowing Ring and Pulsing Animation
 */
@Composable
fun CallAvatarWithPulse(
    avatarUri: String?,
    partnerName: String,
    baseSize: androidx.compose.ui.unit.Dp
) {
    val transition = rememberInfiniteTransition(label = "AvatarPulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(baseSize + 30.dp)
    ) {
        // Outer glow circle
        Box(
            modifier = Modifier
                .size(baseSize + 18.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(CallColors.GlowPink.copy(alpha = 0.25f))
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(baseSize + 6.dp)
                .clip(CircleShape)
                .border(2.5.dp, CallColors.PrimaryPink, CircleShape)
        )

        // Avatar Image or Heart Icon fallback
        Box(
            modifier = Modifier
                .size(baseSize)
                .clip(CircleShape)
                .background(Color(0xFF261338)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUri.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = partnerName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = CallColors.PrimaryPink,
                    modifier = Modifier.size(baseSize / 2)
                )
            }
        }
    }
}
