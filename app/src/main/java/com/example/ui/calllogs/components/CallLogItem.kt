package com.example.ui.calllogs.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.calllogs.model.CallLogItemData
import com.example.ui.calllogs.model.CallLogType
import com.example.ui.theme.DuoraCallBg
import com.example.ui.theme.DuoraCallLogsBg
import com.example.ui.theme.DuoraError
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryText
import com.example.ui.theme.DuoraSuccess

/**
 * Reusable Call History Row matching Duora Call Logs reference design:
 * - Partner Avatar with pink border
 * - Colored call-type badge (Voice = green, Video = purple, Missed = red)
 * - Partner Name: "My Love 💕"
 * - Subtitle: Call type and duration
 * - Time: "10:45 AM"
 * - Three-dot overflow menu: Call Again, View Details, Delete
 */
@Composable
fun CallLogItem(
    callLog: CallLogItemData,
    onRowClick: () -> Unit,
    onCallAgainClick: () -> Unit,
    onViewDetailsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 500f),
        label = "CallItemScale"
    )

    val (iconBgColor, iconTint, callIcon) = when (callLog.type) {
        CallLogType.VOICE -> Triple(DuoraCallBg, DuoraSuccess, Icons.Default.Call)
        CallLogType.VIDEO -> Triple(DuoraCallLogsBg, Color(0xFF8B5CF6), Icons.Default.Videocam)
        CallLogType.MISSED -> Triple(Color(0xFFFFEAEA), DuoraError, Icons.AutoMirrored.Filled.CallMissed)
    }

    Surface(
        onClick = onRowClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF7ECF2)),
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
            .testTag("call_log_item_${callLog.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Partner Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, DuoraPrimaryPink, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_partner_demo),
                    contentDescription = "Partner Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Call Type Icon Pill
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = callIcon,
                    contentDescription = callLog.type.name,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Partner Name & Call Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = callLog.partnerName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoraPrimaryText
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = callLog.durationFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (callLog.type == CallLogType.MISSED) DuoraError else DuoraSecondaryText
                )
            }

            // Time & Three-Dot Overflow Menu
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = callLog.timeFormatted,
                    fontSize = 12.sp,
                    color = DuoraSecondaryText
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("call_item_menu_${callLog.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = DuoraSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Call Again") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (callLog.type == CallLogType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                                    contentDescription = null,
                                    tint = DuoraSuccess
                                )
                            },
                            onClick = {
                                showMenu = false
                                onCallAgainClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("View Details") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onViewDetailsClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", color = DuoraError) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = DuoraError
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
