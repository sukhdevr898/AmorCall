package com.example.ui.call

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.ChatMessage
import com.example.ui.components.CameraPreview
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Duora Video Call Screen implementing Sheets 4, 5, 6, and 12 from the Design Reference:
 * 4. Outgoing Video Call: Live camera card with glowing border, Camera Off/Mute/Flip controls, End
 * 5. Incoming Video Call: Fullscreen background, small PIP preview of self, Accept & Decline buttons
 * 6. Ongoing Video Call: Fullscreen remote video, draggable PIP camera with duration pill, controls fade, Mute/Camera/Flip/More, End
 * 12. In-Call Message Notification Overlay: Floating banner at top when message arrives
 */
@Composable
fun VideoCallScreen(
    callSession: CallSession,
    partnerName: String,
    partnerNickname: String,
    avatarUri: String?,
    remoteVideoBitmap: Bitmap?,
    inCallMessage: ChatMessage?,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEnterPip: () -> Unit,
    onSendVideoFrame: (Bitmap) -> Unit,
    onSendHeartReaction: () -> Unit,
    onDismissInCallMessage: () -> Unit,
    onOpenChatFromCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreOptions by remember { mutableStateOf(false) }
    var areControlsVisible by remember { mutableStateOf(true) }

    // Auto-fade controls after 4 seconds of inactivity when connected
    LaunchedEffect(callSession.status, areControlsVisible) {
        if (callSession.status == CallStatus.CONNECTED && areControlsVisible) {
            delay(4500)
            areControlsVisible = false
        }
    }

    // Draggable PIP coordinates for local camera
    var pipOffsetX by remember { mutableFloatStateOf(0f) }
    var pipOffsetY by remember { mutableFloatStateOf(0f) }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CallColors.DarkBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                areControlsVisible = !areControlsVisible
            }
    ) {
        // LAYER 1: Background Media
        when (callSession.status) {
            CallStatus.CONNECTED, CallStatus.RECONNECTING -> {
                if (remoteVideoBitmap != null) {
                    Image(
                        bitmap = remoteVideoBitmap.asImageBitmap(),
                        contentDescription = "Partner Live Video",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Waiting for video frame placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CallColors.DarkBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CallAvatarWithPulse(
                                avatarUri = avatarUri,
                                partnerName = partnerName,
                                baseSize = 120.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Connecting live video with $partnerName...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = CallColors.SecondaryText
                            )
                        }
                    }
                }

                // Subtle gradient overlay for readability of header & controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x99171322),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0xCC171322)
                                )
                            )
                        )
                )
            }

            CallStatus.OUTGOING_RINGING -> {
                // Outgoing Video Call (Sheet 4): Dark background with glowing preview card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CallColors.DarkBg)
                )
            }

            CallStatus.INCOMING_RINGING -> {
                // Incoming Video Call (Sheet 5): Partner background wallpaper / video
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CallColors.DarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    CallAvatarWithPulse(
                        avatarUri = avatarUri,
                        partnerName = partnerName,
                        baseSize = 150.dp
                    )
                }
            }

            else -> {}
        }

        // LAYER 2: Outgoing Call Center Video Card (Sheet 4)
        if (callSession.status == CallStatus.OUTGOING_RINGING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 110.dp, bottom = 180.dp, start = 28.dp, end = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CallColors.PrimaryPink.copy(alpha = 0.7f)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF20132B)),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CameraPreview(
                            isFrontCamera = callSession.isFrontCamera,
                            isVideoEnabled = callSession.isVideoEnabled,
                            onFrameCaptured = onSendVideoFrame,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // LAYER 3: Connected Draggable PIP Window (Sheet 6)
        if (callSession.status == CallStatus.CONNECTED || callSession.status == CallStatus.RECONNECTING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CallColors.PrimaryPink),
                    modifier = Modifier
                        .size(width = 115.dp, height = 160.dp)
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(pipOffsetX.roundToInt(), pipOffsetY.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                pipOffsetX += dragAmount.x
                                pipOffsetY += dragAmount.y
                            }
                        }
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF20132B)),
                        contentAlignment = Alignment.Center
                    ) {
                        CameraPreview(
                            isFrontCamera = callSession.isFrontCamera,
                            isVideoEnabled = callSession.isVideoEnabled,
                            onFrameCaptured = onSendVideoFrame,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Duration pill in top of PIP
                        val mins = callSession.durationSeconds / 60
                        val secs = callSession.durationSeconds % 60
                        val durationFormatted = String.format("%02d:%02d", mins, secs)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x99000000),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp)
                        ) {
                            Text(
                                text = durationFormatted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // LAYER 4: Incoming Video Small PIP of Self (Sheet 5)
        if (callSession.status == CallStatus.INCOMING_RINGING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 180.dp, end = 24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CallColors.PrimaryPink),
                    modifier = Modifier
                        .size(width = 100.dp, height = 140.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    CameraPreview(
                        isFrontCamera = callSession.isFrontCamera,
                        isVideoEnabled = callSession.isVideoEnabled,
                        onFrameCaptured = onSendVideoFrame,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // LAYER 5: Foreground UI Header & Controls (Fadable during ongoing call)
        AnimatedVisibility(
            visible = areControlsVisible || callSession.status != CallStatus.CONNECTED || callSession.status == CallStatus.RECONNECTING,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.fillMaxSize()
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
                // Top Info Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    // Header row with PiP button and Status Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (callSession.status == CallStatus.CONNECTED || callSession.status == CallStatus.RECONNECTING) {
                            Surface(
                                shape = CircleShape,
                                color = CallColors.StatusPillBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CallColors.StatusPillBorder),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { onEnterPip() }
                                    .testTag("video_pip_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "Picture in Picture",
                                        tint = CallColors.PrimaryPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }

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
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = if (callSession.status == CallStatus.RECONNECTING) Color(0xFFFF7043) else CallColors.PrimaryPink,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (callSession.status) {
                                        CallStatus.OUTGOING_RINGING -> "Calling..."
                                        CallStatus.INCOMING_RINGING -> "Incoming Video Call"
                                        CallStatus.CONNECTING -> "Connecting..."
                                        CallStatus.RECONNECTING -> "Reconnecting (${callSession.reconnectGraceSecondsRemaining}s)..."
                                        CallStatus.CONNECTED -> "Video Call"
                                        else -> "Video Call"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (callSession.status == CallStatus.RECONNECTING) Color(0xFFFFAB91) else CallColors.PrimaryText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = partnerName.ifBlank { "My Love 💕" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CallColors.PrimaryText
                    )

                    Text(
                        text = if (callSession.status == CallStatus.INCOMING_RINGING) "Incoming Video Call" else "Video Call",
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
                                text = "Partner connection lost • Reconnecting in ${callSession.reconnectGraceSecondsRemaining}s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF7043),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Bottom Controls
                when (callSession.status) {
                    CallStatus.OUTGOING_RINGING -> {
                        // Sheet 4: Controls (Camera Off, Mute, Flip) + Large End Button
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
                                    icon = if (callSession.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    label = if (callSession.isVideoEnabled) "Camera On" else "Camera Off",
                                    isActive = !callSession.isVideoEnabled,
                                    activeColor = CallColors.Decline,
                                    onClick = onToggleVideo,
                                    testTag = "video_camera_toggle_button"
                                )
                                CallRoundButton(
                                    icon = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    label = "Mute",
                                    isActive = callSession.isMuted,
                                    onClick = onToggleMute,
                                    testTag = "video_mute_toggle_button"
                                )
                                CallRoundButton(
                                    icon = Icons.Default.Cameraswitch,
                                    label = "Flip",
                                    onClick = onSwitchCamera,
                                    testTag = "video_flip_camera_button"
                                )
                            }

                            Spacer(modifier = Modifier.height(26.dp))

                            EndCallButton(onClick = onEndCall)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    CallStatus.INCOMING_RINGING -> {
                        // Sheet 5: Decline (Red) & Accept (Green)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp, vertical = 20.dp),
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
                    }

                    CallStatus.CONNECTED, CallStatus.RECONNECTING, CallStatus.CONNECTING -> {
                        // Sheet 6: Connected Controls (Mute, Camera, Flip, More) + Large End Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CallRoundButton(
                                    icon = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    label = "Mute",
                                    isActive = callSession.isMuted,
                                    onClick = onToggleMute,
                                    testTag = "connected_video_mute_button"
                                )
                                CallRoundButton(
                                    icon = if (callSession.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    label = "Camera",
                                    isActive = !callSession.isVideoEnabled,
                                    activeColor = CallColors.Decline,
                                    onClick = onToggleVideo,
                                    testTag = "connected_video_camera_button"
                                )
                                CallRoundButton(
                                    icon = Icons.Default.Cameraswitch,
                                    label = "Flip",
                                    onClick = onSwitchCamera,
                                    testTag = "connected_video_flip_button"
                                )
                                CallRoundButton(
                                    icon = Icons.Default.MoreHoriz,
                                    label = "More",
                                    onClick = { showMoreOptions = true },
                                    testTag = "connected_video_more_button"
                                )
                            }

                            Spacer(modifier = Modifier.height(26.dp))

                            EndCallButton(onClick = onEndCall)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    else -> {}
                }
            }
        }

        // LAYER 6: In-Call Message Notification Overlay (Sheet 12)
        InCallMessageOverlay(
            message = inCallMessage,
            partnerName = partnerName,
            onDismiss = onDismissInCallMessage,
            onClick = onOpenChatFromCall,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // More Options Bottom Sheet
        if (showMoreOptions) {
            CallMoreOptionsSheet(
                onDismissRequest = { showMoreOptions = false },
                onSendHeartReaction = onSendHeartReaction,
                onTogglePip = onEnterPip,
                isPipSupported = true
            )
        }
    }
}
