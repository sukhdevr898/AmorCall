package com.example.ui.call

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Standard Circular Control Button matching Duora design reference:
 * Glassmorphic circle with icon and text label below.
 */
@Composable
fun CallRoundButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    activeColor: Color = CallColors.PrimaryPink,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "ButtonScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 64.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.35f) else CallColors.ControlBg)
                .border(
                    1.5.dp,
                    if (isActive) activeColor else CallColors.ControlBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else CallColors.PrimaryText,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) activeColor else CallColors.SecondaryText
        )
    }
}

/**
 * Large Centered End Call Button:
 * Prominent red button #F04452 with call end icon.
 */
@Composable
fun EndCallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "EndScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(68.dp)
            .clip(CircleShape)
            .background(CallColors.Decline)
            .border(2.dp, Color(0x33FFFFFF), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("end_call_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}

/**
 * Incoming Call Action Button (Accept or Decline)
 */
@Composable
fun IncomingActionButton(
    isAccept: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = if (isAccept) "Accept" else "Decline"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        label = "ActionScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isAccept) CallColors.Accept else CallColors.Decline)
                .border(2.5.dp, Color(0x44FFFFFF), CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .testTag(if (isAccept) "accept_call_button" else "decline_call_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAccept) Icons.Default.Call else Icons.Default.CallEnd,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = CallColors.PrimaryText
        )
    }
}

/**
 * In-Call DTMF Dialpad Dialog
 */
@Composable
fun CallKeypadDialog(
    onDismissRequest: () -> Unit,
    onDigitPressed: (Char) -> Unit
) {
    var enteredDigits by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CallColors.DarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CallColors.PrimaryPink.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keypad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CallColors.PrimaryText
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CallColors.SecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Digits Display
                Text(
                    text = enteredDigits.ifBlank { "Touch dialpad keys" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enteredDigits.isNotBlank()) CallColors.PrimaryPink else CallColors.SecondaryText.copy(alpha = 0.6f),
                    modifier = Modifier.height(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Keypad Grid: 1-9, *, 0, #
                val keypadKeys = listOf(
                    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                    listOf("*" to "", "0" to "+", "#" to "")
                )

                keypadKeys.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (digit, letters) ->
                            KeypadKey(
                                digit = digit,
                                letters = letters,
                                onClick = {
                                    val ch = digit.first()
                                    enteredDigits += ch
                                    onDigitPressed(ch)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(Color(0x22FFFFFF))
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = digit,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CallColors.PrimaryText
            )
            if (letters.isNotBlank()) {
                Text(
                    text = letters,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = CallColors.SecondaryText
                )
            }
        }
    }
}

/**
 * More Options Sheet for Video & Audio Calls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallMoreOptionsSheet(
    onDismissRequest: () -> Unit,
    onSendHeartReaction: () -> Unit,
    onTogglePip: () -> Unit,
    isPipSupported: Boolean = true
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CallColors.DarkBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Call Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CallColors.PrimaryText
            )
            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                onClick = {
                    onDismissRequest()
                    onSendHeartReaction()
                },
                color = Color(0x22FFFFFF),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "❤️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Send Romantic Hearts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CallColors.PrimaryText
                        )
                        Text(
                            text = "Floating hearts reaction on partner screen",
                            fontSize = 12.sp,
                            color = CallColors.SecondaryText
                        )
                    }
                }
            }

            if (isPipSupported) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = {
                        onDismissRequest()
                        onTogglePip()
                    },
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📱", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Picture-in-Picture (PiP)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CallColors.PrimaryText
                            )
                            Text(
                                text = "Keep video call visible while multitasking",
                                fontSize = 12.sp,
                                color = CallColors.SecondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Floating Mini Call Pill shown at top of the app when user navigates Duora during an active call
 */
@Composable
fun MiniCallPill(
    partnerName: String,
    durationSeconds: Int,
    isReconnecting: Boolean,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onExpand: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    Surface(
        onClick = onExpand,
        color = Color(0xF01C1226),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isReconnecting) Color(0xFFFFB300) else CallColors.PrimaryPink
        ),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("mini_call_pill")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CallColors.PrimaryPink.copy(alpha = 0.25f))
                        .border(1.5.dp, CallColors.PrimaryPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = partnerName.take(1).ifBlank { "❤️" }.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = partnerName.ifBlank { "My Love 💕" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = if (isReconnecting) "Reconnecting..." else "In Call • $timeFormatted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isReconnecting) Color(0xFFFFB300) else Color(0xFF16C784)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mute Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("mini_call_mute_button")
                ) {
                    Text(
                        text = if (isMuted) "🔇" else "🎙️",
                        fontSize = 14.sp
                    )
                }

                // End Call Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CallColors.Decline)
                        .testTag("mini_call_end_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
