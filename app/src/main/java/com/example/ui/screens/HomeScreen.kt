package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Videocam
import com.example.BuildConfig
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppTheme
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.network.ServerConnectionState
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveWhite
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

@Composable
fun HomeScreen(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val serverState by viewModel.serverConnectionState.collectAsState()

    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPermissionsModal by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateInfo) {
        if (updateInfo?.isAvailable == true) {
            showUpdateDialog = true
        }
    }

    fun checkMissingPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(Unit) {
        showPermissionsModal = checkMissingPermissions()
    }

    // Permission Modal Dialog
    if (showPermissionsModal) {
        AlertDialog(
            onDismissRequest = { showPermissionsModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LoveRose,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Permissions Required",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Text(
                    text = "Duora requires Microphone, Camera, and Notification permissions to provide real-time voice and video calls and receive incoming calls while the app is in the background.\n\nPlease allow these permissions to ensure full hotline functionality.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestPermissions {
                            showPermissionsModal = checkMissingPermissions()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                    modifier = Modifier.testTag("modal_grant_permissions_button")
                ) {
                    Text("Grant Permissions", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Text("App Settings", color = LoveGold)
                    }
                    TextButton(
                        onClick = { showPermissionsModal = false }
                    ) {
                        Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Theme Selector Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Select App Theme",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ThemeOptionRow(
                        title = "Love Theme (Romantic Dark)",
                        subtitle = "Deep wine, romantic rose & gold accents",
                        icon = Icons.Default.Favorite,
                        iconTint = LoveRose,
                        isSelected = profile.appTheme == AppTheme.LOVE,
                        onClick = {
                            viewModel.setAppTheme(AppTheme.LOVE)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = "Dark Theme (Slate AMOLED)",
                        subtitle = "Sleek pitch dark for battery saving",
                        icon = Icons.Default.DarkMode,
                        iconTint = Color(0xFF60A5FA),
                        isSelected = profile.appTheme == AppTheme.DARK,
                        onClick = {
                            viewModel.setAppTheme(AppTheme.DARK)
                            showThemeDialog = false
                        }
                    )
                    ThemeOptionRow(
                        title = "Light Theme (Clean White)",
                        subtitle = "Bright, high-contrast, modern clean look",
                        icon = Icons.Default.LightMode,
                        iconTint = Color(0xFFF59E0B),
                        isSelected = profile.appTheme == AppTheme.LIGHT,
                        onClick = {
                            viewModel.setAppTheme(AppTheme.LIGHT)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Done", color = LoveRose)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // App Update Available Dialog
    if (showUpdateDialog && updateInfo != null && updateInfo!!.isAvailable) {
        val info = updateInfo!!
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Update Available",
                        tint = LoveRose,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "New Update Available! 🚀",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "A new version of Duora is available on GitHub.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LoveRose.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current: v${BuildConfig.VERSION_NAME}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "➔  New: v${info.versionName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoveRose
                            )
                        }
                    }

                    if (info.releaseNotes.isNotBlank()) {
                        Text(
                            text = "What's New:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = info.releaseNotes,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetUrl = info.browserUrl.ifBlank { info.downloadUrl }
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(browserIntent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                    modifier = Modifier.testTag("download_update_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar: Branding, Connection, Theme, Mute, Pairing, Dev
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title & connection status
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Duora",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Duora Hearts",
                            tint = LoveRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Two hearts. One private space.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Real-time server connection indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.PAIRING) }
                    ) {
                        val isConnected = serverState == ServerConnectionState.CONNECTED
                        val statusLabel = if (isConnected) {
                            if (profile.isPaired) "Hotline Online" else "Server Connected"
                        } else {
                            "Connecting..."
                        }
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) LoveAccentGreen else LoveRose)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConnected) LoveAccentGreen else LoveRose
                        )
                    }
                }

                // Important Top Action Icons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Theme Switcher Button
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("theme_selector_button")
                    ) {
                        val themeIcon = when (profile.appTheme) {
                            AppTheme.LOVE -> Icons.Default.Favorite
                            AppTheme.DARK -> Icons.Default.DarkMode
                            AppTheme.LIGHT -> Icons.Default.LightMode
                        }
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = "Change Theme",
                            tint = LoveRose,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Notification Mute Toggle
                    IconButton(
                        onClick = { viewModel.toggleMuteNotifications() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("toggle_notification_mute_button")
                    ) {
                        Icon(
                            imageVector = if (profile.isNotificationsMuted)
                                Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                            contentDescription = if (profile.isNotificationsMuted) "Unmute Alerts" else "Mute Alerts",
                            tint = if (profile.isNotificationsMuted) LoveRose else LoveGold,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Partner Pairing Settings
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.PAIRING) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("nav_pairing_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Pairing Settings",
                            tint = LoveGold,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Developer Screen Info
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DEVELOPER) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("nav_developer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Developer Info",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 0. Update Alert Banner (if update available)
                if (updateInfo?.isAvailable == true) {
                    item {
                        Surface(
                            onClick = { showUpdateDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = LoveRose.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, LoveRose.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("update_available_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(LoveRose.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = null,
                                            tint = LoveRose,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Duora v${updateInfo!!.versionName} Available! 🎉",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tap to review update & download APK",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LoveRose
                                ) {
                                    Text(
                                        text = "Update",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. Couple Connection Card (User Name & Partner Name)
                item {
                    CoupleHeroCard(
                        userName = profile.userName,
                        userCode = profile.myPairCode,
                        partnerName = profile.partnerName,
                        partnerNickname = profile.partnerNickname,
                        partnerCode = profile.partnerPairCode,
                        partnerStatus = profile.partnerStatus,
                        isPaired = profile.isPaired && profile.partnerPairCode.isNotBlank(),
                        isServerConnected = serverState == ServerConnectionState.CONNECTED,
                        onLinkClick = { viewModel.navigateTo(AppScreen.PAIRING) }
                    )
                }

                // 2. Calling Features: One-Tap Voice & Video Calls
                item {
                    Text(
                        text = "Direct Hotline Calling",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            shape = RoundedCornerShape(18.dp)
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
                                            text = "Real VoIP Audio",
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
                            shape = RoundedCornerShape(18.dp)
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
                                            text = "Live Camera Feed",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Recent Calls Section with Clear Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Calls",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (callHistory.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.clearCallHistory() }
                                    .testTag("clear_call_logs_button")
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear All",
                                    tint = LoveRose,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Clear Logs",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LoveRose
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (callHistory.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "No Calls",
                                    tint = LoveRose.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No call logs yet",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap Voice Call or Video Call above to connect instantly with ${profile.partnerName} ❤️",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            callHistory.take(8).forEach { record ->
                                CallRecordRow(
                                    record = record,
                                    partnerName = profile.partnerName
                                )
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
private fun CoupleHeroCard(
    userName: String,
    userCode: String,
    partnerName: String,
    partnerNickname: String,
    partnerCode: String,
    partnerStatus: String,
    isPaired: Boolean,
    isServerConnected: Boolean,
    onLinkClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Two Partners side by side connected by glowing Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // User Side
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(LoveCrimson.copy(alpha = 0.2f))
                            .border(2.dp, LoveRose, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveRose
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = userName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "#$userCode",
                        fontSize = 11.sp,
                        color = LoveGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Center Connection Heart Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isPaired) LoveRose.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Connection Heart",
                            tint = if (isPaired) LoveRose else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPaired) (if (isServerConnected) "Connected" else "Linked") else "Not Linked",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaired) LoveAccentGreen else LoveRose
                    )
                }

                // Partner Side
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(LoveGold.copy(alpha = 0.2f))
                            .border(2.dp, if (isPaired) LoveGold else Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = partnerName.take(1).uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaired) LoveGold else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = partnerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = if (isPaired) "#$partnerCode" else "No code",
                        fontSize = 11.sp,
                        color = if (isPaired) LoveGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Partner status line or link button
            if (isPaired) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LoveAccentGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveAccentGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onLinkClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unpaired_link_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LoveRose),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Link Partner Code Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun CallRecordRow(record: CallRecord, partnerName: String) {
    val durationText = if (record.durationSeconds > 0) {
        val mins = record.durationSeconds / 60
        val secs = record.durationSeconds % 60
        "${mins}m ${secs}s"
    } else {
        "Missed"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (record.durationSeconds > 0) LoveAccentGreen.copy(alpha = 0.2f) else LoveRose.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (record.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call Icon",
                        tint = if (record.durationSeconds > 0) LoveAccentGreen else LoveRose,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (record.isOutgoing) "Outgoing to $partnerName" else "Incoming from $partnerName",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (record.type == CallType.VIDEO) "Video Call" else "Voice Call",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = durationText,
                fontSize = 12.sp,
                color = if (record.durationSeconds > 0) LoveGold else LoveRose,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) LoveRose.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) LoveRose else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Selected",
                    tint = LoveRose,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
