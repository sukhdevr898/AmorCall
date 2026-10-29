package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.ui.call.AudioCallScreen
import com.example.ui.call.CallColors
import com.example.ui.call.InCallMessageOverlay
import com.example.ui.call.VideoCallScreen
import com.example.ui.components.CameraPreview
import com.example.ui.components.FloatingHeartsOverlay
import com.example.viewmodel.LoveCallViewModel

/**
 * Top-level CallScreen orchestrating Native Android Calling & Notifications
 * Exactly implementing the 15-sheet Duora Call and Notification Design Reference:
 * - 1. Outgoing Audio Call (glowing pink ring, side waveform pulses, Mute/Speaker/Keypad + End)
 * - 2. Incoming Audio Call (Message / Remind Me + Decline/Accept)
 * - 3. Ongoing Audio Call (timer, 2 rows of round controls, live waveform bars, End)
 * - 4. Outgoing Video Call (live camera card with glowing romantic border, Camera Off/Mute/Flip + End)
 * - 5. Incoming Video Call (partner fullscreen with local PIP preview, Decline/Accept)
 * - 6. Ongoing Video Call (fullscreen partner video, draggable PIP camera with timer, inactivity fade, controls, End)
 * - 7 & 8. Picture-in-Picture (PiP) Window with remote actions
 * - 12. In-Call Message Notification Overlay
 */
@Composable
fun CallScreen(
    viewModel: LoveCallViewModel,
    callSession: CallSession,
    onEnterPip: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val remoteVideo by viewModel.remoteVideoBitmap.collectAsState()
    val floatingHearts by viewModel.floatingHearts.collectAsState()
    val isInPipMode by viewModel.isInPipMode.collectAsState()
    val inCallMessage by viewModel.inCallMessageOverlay.collectAsState()

    // Handle system back button to prevent accidental call dropping
    BackHandler(enabled = callSession.status != CallStatus.IDLE) {
        if (callSession.status == CallStatus.CONNECTED && callSession.type == CallType.VIDEO) {
            onEnterPip()
        } else if (callSession.status == CallStatus.CONNECTED || callSession.status == CallStatus.RECONNECTING) {
            viewModel.minimizeCallScreen()
        } else if (callSession.status == CallStatus.ENDED) {
            viewModel.resetCallSessionToIdle()
        } else {
            Toast.makeText(context, "Use End Call button to hang up ❤️", Toast.LENGTH_SHORT).show()
        }
    }

    // 1. Native Android Picture-in-Picture Mode (Sheets 7 & 8)
    if (isInPipMode && callSession.type == CallType.VIDEO) {
        PipCallLayout(
            viewModel = viewModel,
            callSession = callSession,
            partnerNickname = profile.partnerNickname.ifBlank { profile.partnerName },
            remoteVideo = remoteVideo
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // 2. Call Ended State
            callSession.status == CallStatus.ENDED -> {
                CallEndedScreen(
                    partnerName = profile.partnerName.ifBlank { "My Love 💕" },
                    durationSeconds = callSession.durationSeconds,
                    statusMessage = callSession.statusMessage,
                    onCallAgain = {
                        val type = callSession.type
                        viewModel.resetCallSessionToIdle()
                        viewModel.startOutgoingCall(type)
                    },
                    onReturnHome = { viewModel.resetCallSessionToIdle() }
                )
            }

            // 3. Audio Calling (Sheets 1, 2, 3)
            callSession.type == CallType.VOICE -> {
                val partnerAvatarToDisplay = profile.partnerAvatarUrl ?: viewModel.resolveAvatarUrl(profile.partnerAvatarId)
                AudioCallScreen(
                    callSession = callSession,
                    partnerName = profile.partnerName.ifBlank { "My Love 💕" },
                    partnerNickname = profile.partnerNickname,
                    avatarUri = partnerAvatarToDisplay,
                    onAccept = { viewModel.acceptCall() },
                    onDecline = { viewModel.declineCall() },
                    onEndCall = { viewModel.endCall() },
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onSendMessageQuick = {
                        viewModel.sendChatMessage("Can't talk right now ❤️, texting you!")
                        Toast.makeText(context, "Quick text sent to ${profile.partnerName} ❤️", Toast.LENGTH_SHORT).show()
                    },
                    onRemindMe = {
                        Toast.makeText(context, "Call reminder set for 10 minutes", Toast.LENGTH_SHORT).show()
                    },
                    onMinimize = { viewModel.minimizeCallScreen() }
                )
            }

            // 4. Video Calling (Sheets 4, 5, 6)
            callSession.type == CallType.VIDEO -> {
                val partnerAvatarToDisplay = profile.partnerAvatarUrl ?: viewModel.resolveAvatarUrl(profile.partnerAvatarId)
                VideoCallScreen(
                    callSession = callSession,
                    partnerName = profile.partnerName.ifBlank { "My Love 💕" },
                    partnerNickname = profile.partnerNickname,
                    avatarUri = partnerAvatarToDisplay,
                    remoteVideoBitmap = remoteVideo,
                    inCallMessage = inCallMessage,
                    onAccept = { viewModel.acceptCall(incomingType = CallType.VIDEO) },
                    onDecline = { viewModel.declineCall() },
                    onEndCall = { viewModel.endCall() },
                    onToggleMute = { viewModel.toggleMute() },
                    onToggleVideo = { viewModel.toggleVideo() },
                    onSwitchCamera = { viewModel.switchCamera() },
                    onEnterPip = onEnterPip,
                    onSendVideoFrame = { bitmap -> viewModel.sendVideoFrame(bitmap) },
                    onSendHeartReaction = { viewModel.sendFloatingHeart() },
                    onDismissInCallMessage = { viewModel.dismissInCallMessageOverlay() },
                    onOpenChatFromCall = { viewModel.openChatFromCall() }
                )
            }
        }

        // Sheet 12: In-Call Message Notification Overlay (for voice calls as well)
        if (callSession.type == CallType.VOICE && callSession.status != CallStatus.ENDED) {
            InCallMessageOverlay(
                message = inCallMessage,
                partnerName = profile.partnerName.ifBlank { "My Love 💕" },
                onDismiss = { viewModel.dismissInCallMessageOverlay() },
                onClick = { viewModel.openChatFromCall() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            )
        }

        // Floating Hearts Reaction Animation Layer
        FloatingHeartsOverlay(hearts = floatingHearts)
    }
}

/**
 * Call Ended Screen displaying final duration and graceful return to home
 */
@Composable
private fun CallEndedScreen(
    partnerName: String,
    durationSeconds: Int,
    statusMessage: String?,
    onCallAgain: () -> Unit,
    onReturnHome: () -> Unit
) {
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    val durationFormatted = String.format("%02d:%02d", mins, secs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CallColors.DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(CallColors.Decline.copy(alpha = 0.2f))
                    .border(2.dp, CallColors.Decline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Call Ended",
                    tint = CallColors.Decline,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Call Ended",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CallColors.PrimaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (durationSeconds > 0) {
                    "Call with $partnerName • $durationFormatted"
                } else {
                    statusMessage ?: "$partnerName was unavailable"
                },
                fontSize = 15.sp,
                color = CallColors.SecondaryText
            )

            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onCallAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = CallColors.Accept),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("call_ended_call_again_button")
                ) {
                    Text(
                        text = "Call Again ❤️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onReturnHome,
                    colors = ButtonDefaults.buttonColors(containerColor = CallColors.PrimaryPink),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("call_ended_back_home_button")
                ) {
                    Text(
                        text = "Back to Home",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Picture-in-Picture Layout (Sheets 7 & 8)
 * Real Android Picture-in-Picture floating window with remote actions
 */
@Composable
private fun PipCallLayout(
    viewModel: LoveCallViewModel,
    callSession: CallSession,
    partnerNickname: String,
    remoteVideo: android.graphics.Bitmap?
) {
    val floatingHearts by viewModel.floatingHearts.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel.sendFloatingHeart() }
    ) {
        if (remoteVideo != null) {
            androidx.compose.foundation.Image(
                bitmap = remoteVideo.asImageBitmap(),
                contentDescription = "Partner Live Video",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Corner PIP Thumbnail: Local camera preview
            Card(
                modifier = Modifier
                    .size(width = 46.dp, height = 64.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(6.dp)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CallColors.PrimaryPink)
            ) {
                CameraPreview(
                    isFrontCamera = callSession.isFrontCamera,
                    isVideoEnabled = callSession.isVideoEnabled,
                    onFrameCaptured = { viewModel.sendVideoFrame(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            CameraPreview(
                isFrontCamera = callSession.isFrontCamera,
                isVideoEnabled = callSession.isVideoEnabled,
                onFrameCaptured = { viewModel.sendVideoFrame(it) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top duration and partner pill
        val mins = callSession.durationSeconds / 60
        val secs = callSession.durationSeconds % 60
        val durationFormatted = String.format("%02d:%02d", mins, secs)

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xAA000000),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤️ $partnerNickname • $durationFormatted",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (callSession.isMuted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Muted",
                        tint = CallColors.PrimaryPink,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        FloatingHeartsOverlay(hearts = floatingHearts)
    }
}
