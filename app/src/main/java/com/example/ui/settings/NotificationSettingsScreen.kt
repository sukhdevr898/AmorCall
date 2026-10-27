package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoraHotPink
import com.example.viewmodel.LoveCallViewModel

@Composable
fun NotificationSettingsScreen(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val profile by viewModel.profile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6FA))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("notif_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Column {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = "Manage your message & call alerts",
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Toggles
        Text(
            text = "ALERTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF89818E),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column {
                NotifToggleRow(
                    icon = Icons.Default.ChatBubble,
                    title = "Message Notifications",
                    subtitle = "Get notified when partner sends a chat",
                    checked = profile.isMessageNotificationsEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateNotificationSettings(
                            msg = checked,
                            call = profile.isCallNotificationsEnabled,
                            inApp = profile.isInAppNotificationsEnabled,
                            sound = profile.notificationSound,
                            vib = profile.notificationVibration,
                            ring = profile.notificationRingtone
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                NotifToggleRow(
                    icon = Icons.Default.Call,
                    title = "Call Notifications",
                    subtitle = "Ring when partner calls you",
                    checked = profile.isCallNotificationsEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateNotificationSettings(
                            msg = profile.isMessageNotificationsEnabled,
                            call = checked,
                            inApp = profile.isInAppNotificationsEnabled,
                            sound = profile.notificationSound,
                            vib = profile.notificationVibration,
                            ring = profile.notificationRingtone
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                NotifToggleRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "In-App Banners",
                    subtitle = "Show top floating notification when in app",
                    checked = profile.isInAppNotificationsEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updateNotificationSettings(
                            msg = profile.isMessageNotificationsEnabled,
                            call = profile.isCallNotificationsEnabled,
                            inApp = checked,
                            sound = profile.notificationSound,
                            vib = profile.notificationVibration,
                            ring = profile.notificationRingtone
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Sounds & Vibrations
        Text(
            text = "SOUND & VIBRATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF89818E),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column {
                NotifSelectorRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Notification Sound",
                    value = profile.notificationSound,
                    onClick = {
                        val next = if (profile.notificationSound == "Default") "Romantic Bell" else "Default"
                        viewModel.updateNotificationSettings(
                            msg = profile.isMessageNotificationsEnabled,
                            call = profile.isCallNotificationsEnabled,
                            inApp = profile.isInAppNotificationsEnabled,
                            sound = next,
                            vib = profile.notificationVibration,
                            ring = profile.notificationRingtone
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                NotifSelectorRow(
                    icon = Icons.Default.Vibration,
                    title = "Vibration Pattern",
                    value = profile.notificationVibration,
                    onClick = {
                        val next = if (profile.notificationVibration == "Default") "Heartbeat" else "Default"
                        viewModel.updateNotificationSettings(
                            msg = profile.isMessageNotificationsEnabled,
                            call = profile.isCallNotificationsEnabled,
                            inApp = profile.isInAppNotificationsEnabled,
                            sound = profile.notificationSound,
                            vib = next,
                            ring = profile.notificationRingtone
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                NotifSelectorRow(
                    icon = Icons.Default.MusicNote,
                    title = "Call Ringtone",
                    value = profile.notificationRingtone,
                    onClick = {
                        val next = if (profile.notificationRingtone == "Default") "Amour Sweet" else "Default"
                        viewModel.updateNotificationSettings(
                            msg = profile.isMessageNotificationsEnabled,
                            call = profile.isCallNotificationsEnabled,
                            inApp = profile.isInAppNotificationsEnabled,
                            sound = profile.notificationSound,
                            vib = profile.notificationVibration,
                            ring = next
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DuoraHotPink,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DuoraHotPink,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD4CBD8)
            )
        )
    }
}

@Composable
private fun NotifSelectorRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF21102E)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = DuoraHotPink,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFCCC5CE),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
