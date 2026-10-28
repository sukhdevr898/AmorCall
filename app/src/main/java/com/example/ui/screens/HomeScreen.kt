package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.material.icons.filled.Notifications
import com.example.ui.home.components.HomeHeader
import com.example.ui.home.components.HeroBanner
import com.example.ui.home.components.FeatureGrid
import com.example.ui.home.components.MomentsGallery
import com.example.ui.navigation.DuoraBottomNavigationBar
import com.example.ui.navigation.DuoraNavDestination
import com.example.ui.notifications.NotificationPanelSheet
import com.example.ui.profile.ProfileMenuSheet
import com.example.ui.components.SunsetCoupleHeroBanner
import com.example.ui.components.QuickActionsGrid
import com.example.ui.components.OurMomentsSection
import com.example.ui.components.ServerHealthDialog
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraRaspberry
import com.example.BuildConfig
import com.example.model.CoupleNote
import com.example.privacy.PrivacyIconManager
import com.example.privacy.PrivacyIconType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.CallStatus
import com.example.ui.call.MiniCallPill
import com.example.ui.chat.InAppMessageBanner
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

enum class HomeBottomTab {
    HOME,
    NOTES,
    CHAT,
    CALL_LOGS,
    SETTINGS
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val serverState by viewModel.serverConnectionState.collectAsState()
    val sharedNote by viewModel.sharedNote.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val memories by viewModel.memories.collectAsState()

    val callSession by viewModel.callSession.collectAsState()
    val isCallScreenMinimized by viewModel.isCallScreenMinimized.collectAsState()
    val inAppNotification by viewModel.inAppMessageNotification.collectAsState()
    val isIndividualChatOpen by viewModel.isIndividualChatOpen.collectAsState()
    val unreadChatCount by viewModel.unreadChatCount.collectAsState()

    var currentBottomTab by remember { mutableStateOf(HomeBottomTab.HOME) }
    BackHandler(enabled = currentBottomTab != HomeBottomTab.HOME) {
        if (currentBottomTab == HomeBottomTab.CHAT && isIndividualChatOpen) {
            viewModel.closeIndividualChat()
        } else {
            currentBottomTab = HomeBottomTab.HOME
        }
    }

    val targetTab by viewModel.targetBottomTab.collectAsState()
    LaunchedEffect(targetTab) {
        if (targetTab == "CHAT") {
            currentBottomTab = HomeBottomTab.CHAT
            viewModel.clearTargetBottomTab()
        }
    }
    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val updateDownloadProgress by viewModel.updateDownloadProgress.collectAsState()
    val updateInstallError by viewModel.updateInstallError.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showServerSettingsDialog by remember { mutableStateOf(false) }
    var showServerHealthDialog by remember { mutableStateOf(false) }
    var showNoteInputDialog by remember { mutableStateOf(false) }
    var showPermissionsModal by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showNotificationPanel by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    val currentPrivacyIcon by PrivacyIconManager.currentIcon.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addMemoryPhoto(
                title = "Our Memory ❤️",
                note = "",
                dateStr = "Today",
                uriString = uri.toString()
            )
            Toast.makeText(context, "Memory added to Our Moments ❤️", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(updateInfo) {
        if (updateInfo?.isAvailable == true) {
            showUpdateDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkNoteExpiry()
        viewModel.fetchServerDp()
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


    // Server settings are permanently set to ws://amorcall.onrender.com/ (no manual change allowed)

    // Server Health Diagnostic Dialog — Sheet 4
    if (showServerHealthDialog) {
        ServerHealthDialog(
            serverState = serverState,
            onReconnect = { viewModel.reconnectServer(profile.serverUrl) },
            onDismiss = { showServerHealthDialog = false }
        )
    }

    // Share Note with Partner Dialog
    if (showNoteInputDialog) {
        var noteDraft by remember { mutableStateOf("") }
        val quickPhrases = listOf(
            "Missing you so much ❤️",
            "Call me when you're free 💕",
            "Thinking of you always 🥰",
            "Have a wonderful day sweetheart ☀️",
            "I love you endlessly 🌹",
            "Can't wait to hear your voice ✨"
        )
        AlertDialog(
            onDismissRequest = { showNoteInputDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Love Note",
                        tint = LoveRose,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Note with ${profile.partnerName}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This note will be displayed on both your and ${profile.partnerName}'s home screen for 24 hours (unless deleted).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { if (it.length <= 160) noteDraft = it },
                        placeholder = { Text("Write something sweet for ${profile.partnerName}...") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LoveRose,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quick Love Prompts:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LoveGold
                        )
                        Text(
                            text = "${noteDraft.length}/160",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        quickPhrases.take(4).forEach { phrase ->
                            Surface(
                                onClick = { noteDraft = phrase },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = phrase,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteDraft.isNotBlank()) {
                            viewModel.postSharedNote(noteDraft)
                            showNoteInputDialog = false
                        }
                    },
                    enabled = noteDraft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                    modifier = Modifier.testTag("dialog_send_note_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share (24h)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteInputDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // App Update Available Dialog (Step 3: Direct In-App Download & Auto Install)
    val currentUpdate = updateInfo
    if (showUpdateDialog && currentUpdate != null && currentUpdate.isAvailable) {
        val info = currentUpdate
        AlertDialog(
            onDismissRequest = {
                if (!isDownloadingUpdate) {
                    showUpdateDialog = false
                    viewModel.dismissUpdateDialog()
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LoveRose.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Update Available",
                            tint = LoveRose,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "New Update Available! 🚀",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Duora v${info.versionName}",
                            fontSize = 12.sp,
                            color = LoveRose,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "A new upgrade is ready. Download and install directly inside the app without opening a browser.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = LoveRose.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, LoveRose.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Installed: v${BuildConfig.VERSION_NAME}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "➔  Latest: v${info.versionName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoveRose
                            )
                        }
                    }

                    if (info.releaseNotes.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Release Highlights:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = info.releaseNotes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (isDownloadingUpdate) {
                        val progress = updateDownloadProgress ?: 0.1f
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Downloading APK from GitHub...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoveRose
                                )
                            }
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                color = LoveRose,
                                trackColor = LoveRose.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    if (updateInstallError != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ${updateInstallError}",
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanVer = info.versionName.trim().removePrefix("v").removePrefix("V")
                        val targetUrl = info.downloadUrl
                            .replace("{version}", cleanVer)
                            .replace("{VERSION}", cleanVer)
                            .ifBlank { "https://raw.githubusercontent.com/sukhdevr898/AmorCall/refs/heads/main/APK/Duora_${cleanVer}.apk" }
                        viewModel.downloadAndInstallUpdate(context, info.versionName, targetUrl)
                    },
                    enabled = !isDownloadingUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("download_update_button")
                ) {
                    if (isDownloadingUpdate) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Downloading...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download & Install", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isDownloadingUpdate) {
                    TextButton(onClick = {
                        showUpdateDialog = false
                        viewModel.dismissUpdateDialog()
                    }) {
                        Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showProfileDialog) {
        ProfileEditDialog(
            currentUserName = profile.userName,
            currentPartnerName = profile.partnerName,
            currentPartnerNickname = profile.partnerNickname,
            myCode = profile.myPairCode,
            partnerCode = profile.partnerPairCode,
            onDismiss = { showProfileDialog = false },
            onSave = { uName, pName, pNick ->
                viewModel.updateRelationshipProfile(uName, pName, pNick)
                showProfileDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutDuoraDialog(
            viewModel = viewModel,
            onDismiss = { showAboutDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentBottomTab,
                    transitionSpec = {
                        if (targetState == HomeBottomTab.CHAT || initialState == HomeBottomTab.CHAT) {
                            (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f, animationSpec = tween(300)))
                                .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.98f, animationSpec = tween(200)))
                        } else {
                            fadeIn(animationSpec = tween(220))
                                .togetherWith(fadeOut(animationSpec = tween(180)))
                        }
                    },
                    label = "TabSharedTransition"
                ) { targetTab ->
                    when (targetTab) {
                        HomeBottomTab.HOME -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                        ) {
                            // Top Bar: Official Duora Logo, Notification Bell & Circular Profile Avatar
                            HomeHeader(
                                unreadNotificationCount = 1,
                                isPartnerOnline = profile.partnerStatus.equals("online", ignoreCase = true),
                                onNotificationClick = { showNotificationPanel = true },
                                onProfileClick = { showProfileMenu = true }
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                // 0. Update Alert Banner (if update available)
                val bannerUpdate = updateInfo
                if (bannerUpdate != null && bannerUpdate.isAvailable) {
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
                                            text = "Duora v${bannerUpdate.versionName} Available! 🎉",
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

                // 1. Romantic Hero Banner
                item {
                    HeroBanner(
                        partnerName = profile.partnerName,
                        onBannerClick = {
                            onRequestPermissions {
                                viewModel.startOutgoingCall(CallType.VOICE)
                            }
                        }
                    )
                }

                // 2. Responsive 3-column Feature Grid (Chat, Call, Notes, Call Logs, Partner, Server)
                item {
                    FeatureGrid(
                        unreadChatCount = unreadChatCount,
                        onChatClick = { currentBottomTab = HomeBottomTab.CHAT },
                        onCallClick = {
                            onRequestPermissions {
                                viewModel.startOutgoingCall(CallType.VOICE)
                            }
                        },
                        onNotesClick = { currentBottomTab = HomeBottomTab.NOTES },
                        onCallLogsClick = { currentBottomTab = HomeBottomTab.CALL_LOGS },
                        onPartnerClick = { viewModel.navigateTo(AppScreen.PAIRING) },
                        onServerClick = { showServerHealthDialog = true }
                    )
                }

                // 3. Couple Connection Summary Card
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
                        userBlueTick = profile.blueTick,
                        userGreenTick = profile.greenTick,
                        userAvatarUrl = profile.avatarUrl ?: viewModel.resolveAvatarThumbnailUrl(profile.avatarId),
                        partnerBlueTick = profile.partnerBlueTick,
                        partnerGreenTick = profile.partnerGreenTick,
                        partnerAvatarUrl = profile.partnerAvatarUrl ?: viewModel.resolveAvatarThumbnailUrl(profile.partnerAvatarId),
                        onLinkClick = { viewModel.navigateTo(AppScreen.PAIRING) }
                    )
                }

                // 1.25. Couple 24-Hour Shared Note Section
                item {
                    val activeNote = sharedNote
                    if (activeNote != null && !activeNote.isExpired) {
                        // Active Shared Note Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_shared_note_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(LoveRose, LoveGold))
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(LoveRose.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = LoveRose,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (activeNote.isFromPartner)
                                                    "${profile.partnerName}'s Note ❤️" else "Your Note to ${profile.partnerName}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "⏰ 24h Note • ${activeNote.remainingFormatted}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = LoveGold
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteSharedNote() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_shared_note_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Note",
                                            tint = LoveRose.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = LoveRose.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"${activeNote.content}\"",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    androidx.compose.material3.TextButton(
                                        onClick = { showNoteInputDialog = true },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("edit_shared_note_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = LoveGold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Update Note", fontSize = 11.sp, color = LoveGold, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty note state - Invite sharing
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_shared_note_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(LoveRose.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = LoveRose,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Partner 24-Hour Note",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Share a love note for 24 hours ❤️",
                                                fontSize = 11.sp,
                                                color = LoveGold
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showNoteInputDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("leave_note_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Write Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick one-tap preset notes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val quick1 = "Missing you ❤️"
                                    val quick2 = "Call me soon 💕"
                                    Surface(
                                        onClick = { viewModel.postSharedNote(quick1) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = quick1,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Surface(
                                        onClick = { viewModel.postSharedNote(quick2) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = quick2,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
                        HomeBottomTab.NOTES -> {
                            NotesTab(
                                viewModel = viewModel,
                                onBack = { currentBottomTab = HomeBottomTab.HOME }
                            )
                        }
                        HomeBottomTab.CHAT -> {
                            ChatTab(
                                viewModel = viewModel,
                                onRequestPermissions = onRequestPermissions,
                                onBack = { currentBottomTab = HomeBottomTab.HOME }
                            )
                        }
                        HomeBottomTab.CALL_LOGS -> {
                            CallLogsTab(
                                viewModel = viewModel,
                                onRequestPermissions = onRequestPermissions,
                                onBack = { currentBottomTab = HomeBottomTab.HOME }
                            )
                        }
                        HomeBottomTab.SETTINGS -> {
                            com.example.ui.settings.SettingsScreen(
                                viewModel = viewModel,
                                onBackToHome = { currentBottomTab = HomeBottomTab.HOME }
                            )
                        }
                    }
                }
            }

// Bottom Navigation Bar matching design reference
val settingsSubScreen by viewModel.settingsSubScreen.collectAsState()
val currentNavDestination = when (currentBottomTab) {
    HomeBottomTab.HOME -> DuoraNavDestination.HOME
    HomeBottomTab.CHAT -> DuoraNavDestination.CHAT
    HomeBottomTab.CALL_LOGS -> DuoraNavDestination.CALL
    HomeBottomTab.NOTES -> DuoraNavDestination.NOTES
    HomeBottomTab.SETTINGS -> DuoraNavDestination.SETTINGS
}

val shouldShowBottomBar = !(currentBottomTab == HomeBottomTab.CHAT && isIndividualChatOpen) &&
    !(currentBottomTab == HomeBottomTab.SETTINGS && settingsSubScreen != com.example.viewmodel.SettingsSubScreen.MAIN)
if (shouldShowBottomBar) {
    DuoraBottomNavigationBar(
        currentDestination = currentNavDestination,
        unreadChatCount = unreadChatCount,
        onDestinationSelected = { destination ->
            currentBottomTab = when (destination) {
                DuoraNavDestination.HOME -> HomeBottomTab.HOME
                DuoraNavDestination.CHAT -> HomeBottomTab.CHAT
                DuoraNavDestination.CALL -> HomeBottomTab.CALL_LOGS
                DuoraNavDestination.NOTES -> HomeBottomTab.NOTES
                DuoraNavDestination.SETTINGS -> HomeBottomTab.SETTINGS
            }
        }
    )
}
        }

        // In-App Floating Message Notification Banner
        InAppMessageBanner(
            message = inAppNotification,
            onOpenChat = {
                viewModel.openChatFromInAppNotification()
                currentBottomTab = HomeBottomTab.CHAT
            },
            onDismiss = {
                viewModel.dismissInAppMessageNotification()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // In-App Floating Mini Call Pill
        if (isCallScreenMinimized && (callSession.status == CallStatus.CONNECTED || callSession.status == CallStatus.RECONNECTING)) {
            MiniCallPill(
                partnerName = profile.partnerName.ifBlank { "My Love 💕" },
                durationSeconds = callSession.durationSeconds,
                isReconnecting = callSession.status == CallStatus.RECONNECTING,
                isMuted = callSession.isMuted,
                isSpeakerOn = callSession.isSpeakerOn,
                onExpand = { viewModel.expandCallScreen() },
                onToggleMute = { viewModel.toggleMute() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onEndCall = { viewModel.endCall() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp)
            )
        }
    }

    // Notification Panel Sheet from Design Reference
    if (showNotificationPanel) {
        NotificationPanelSheet(
            onDismissRequest = { showNotificationPanel = false }
        )
    }

    // Profile & Settings Menu Sheet from Design Reference
    if (showProfileMenu) {
        ProfileMenuSheet(
            partnerName = profile.partnerName,
            isOnline = profile.partnerStatus.equals("online", ignoreCase = true),
            onDismissRequest = { showProfileMenu = false },
            onPartnerPairingClick = { viewModel.navigateTo(AppScreen.PAIRING) },
            onThemeClick = { showThemeDialog = true },
            onServerSettingsClick = { showServerHealthDialog = true },
            onProfileClick = { showProfileDialog = true },
            onPrivacySecurityClick = {
                showProfileMenu = false
                currentBottomTab = HomeBottomTab.SETTINGS
                viewModel.navigateSettingsTo(com.example.viewmodel.SettingsSubScreen.PRIVACY_APP_ICON)
            },
            onHelpSupportClick = { showAboutDialog = true },
            onLogoutClick = { viewModel.signOut() }
        )
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
    userBlueTick: Boolean = false,
    userGreenTick: Boolean = false,
    userAvatarUrl: String? = null,
    partnerBlueTick: Boolean = false,
    partnerGreenTick: Boolean = false,
    partnerAvatarUrl: String? = null,
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
                        if (!userAvatarUrl.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(userAvatarUrl),
                                contentDescription = userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoveRose
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = userName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (userBlueTick) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified User",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (userGreenTick) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Partner Verified",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
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
                        if (!partnerAvatarUrl.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(partnerAvatarUrl),
                                contentDescription = partnerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = partnerName.take(1).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaired) LoveGold else Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = partnerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (partnerBlueTick) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified User",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (partnerGreenTick) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Partner Verified",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
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

@Composable
private fun DuoraBottomNavTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    testTag: String,
    badgeCount: Int = 0
) {
    val pillBg by animateColorAsState(
        targetValue = if (selected) DuoraSoftBlush else Color.Transparent,
        animationSpec = tween(220),
        label = "pillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) DuoraHotPink else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "contentColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(pillBg)
            .border(
                1.dp,
                if (selected) DuoraCardBorder else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = if (selected) 12.dp else 10.dp, vertical = 7.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(DuoraHotPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

