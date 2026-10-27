package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.privacy.PrivacyIconManager
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraRaspberry
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

@Composable
fun SettingsTab(
    viewModel: LoveCallViewModel,
    onOpenThemeDialog: () -> Unit,
    onOpenServerDialog: () -> Unit,
    onOpenPrivacyIconDialog: () -> Unit,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val serverState by viewModel.serverConnectionState.collectAsState()
    val currentPrivacyIcon by PrivacyIconManager.currentIcon.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
                Toast.makeText(context, "Profile updated ❤️", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAboutDialog) {
        AboutDuoraDialog(
            viewModel = viewModel,
            onDismiss = { showAboutDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Dedicated Settings Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = DuoraRaspberry,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Preferences, privacy & connection",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

        // 1. Profile & Relationship Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_profile_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(DuoraHotPink.copy(alpha = 0.15f))
                                    .border(1.5.dp, DuoraHotPink, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.userName.take(1).uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraHotPink
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile.userName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@${profile.userName.lowercase().replace(" ", "_").ifBlank { "duora_love" }}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (profile.isPaired) "Paired with ${profile.partnerName} ❤️" else "Unpaired",
                                    fontSize = 11.sp,
                                    color = if (profile.isPaired) LoveAccentGreen else DuoraHotPink,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DuoraSoftBlush)
                                .border(1.dp, DuoraCardBorder, CircleShape)
                                .testTag("settings_edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = DuoraRaspberry,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Your Code", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("#${profile.myPairCode}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LoveGold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Partner Code", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (profile.partnerPairCode.isNotBlank()) "#${profile.partnerPairCode}" else "None",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.partnerPairCode.isNotBlank()) LoveGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Personalization & Security
        item {
            Text(
                text = "Preferences & Privacy",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        // App Theme Row
        item {
            SettingsActionRow(
                icon = Icons.Default.Palette,
                iconTint = DuoraRaspberry,
                title = "App Theme",
                subtitle = "Current: ${profile.appTheme.name.lowercase().replaceFirstChar { it.uppercase() }}",
                onClick = onOpenThemeDialog,
                testTag = "settings_row_theme"
            )
        }

        // Privacy App Icon Disguise Row
        item {
            SettingsActionRow(
                icon = Icons.Default.Shield,
                iconTint = DuoraRaspberry,
                title = "Privacy & App Icon Disguise",
                subtitle = "Active: ${currentPrivacyIcon.launcherLabel} • Stealth disguise",
                onClick = onOpenPrivacyIconDialog,
                testTag = "settings_row_privacy_icon"
            )
        }

        // Notifications & Mute Alerts Row
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (profile.isNotificationsMuted) LoveRose.copy(alpha = 0.2f) else DuoraRaspberry.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (profile.isNotificationsMuted) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (profile.isNotificationsMuted) LoveRose else DuoraRaspberry,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Notification Alerts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (profile.isNotificationsMuted) "Muted • No sound during calls" else "Active • Incoming call notifications on",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = !profile.isNotificationsMuted,
                        onCheckedChange = { viewModel.toggleMuteNotifications() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DuoraRaspberry,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("settings_notifications_switch")
                    )
                }
            }
        }

        // Section: Network & Infrastructure
        item {
            Text(
                text = "Connection & Diagnostics",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        // Server Settings Row
        item {
            SettingsActionRow(
                icon = Icons.Default.Dns,
                iconTint = LoveGold,
                title = "Server Connection Health",
                subtitle = "ws://amorcall.onrender.com/ • Tap to test connection",
                onClick = onOpenServerDialog,
                testTag = "settings_row_server"
            )
        }

        // Pairing Screen Shortcut Row
        item {
            SettingsActionRow(
                icon = Icons.Default.Link,
                iconTint = DuoraRaspberry,
                title = "Pairing Management",
                subtitle = if (profile.isPaired) "Mutually paired (#${profile.partnerPairCode})" else "Scan QR or enter partner code",
                onClick = { viewModel.navigateTo(AppScreen.PAIRING) },
                testTag = "settings_row_pairing"
            )
        }


        // Developer Info Row
        item {
            SettingsActionRow(
                icon = Icons.Default.Person,
                iconTint = DuoraRaspberry,
                title = "Developer Profile",
                subtitle = "Sukhdev Singh (@sukhdevr898) • Creator",
                onClick = { viewModel.navigateTo(AppScreen.DEVELOPER) },
                testTag = "settings_row_developer"
            )
        }

        // About Duora Row
        item {
            SettingsActionRow(
                icon = Icons.Default.Info,
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "About Duora",
                subtitle = "v${BuildConfig.VERSION_NAME} • Diagnostics & Updates",
                onClick = { showAboutDialog = true },
                testTag = "settings_row_about"
            )
        }

        // Logout Row matching Sheet 3
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                onClick = { viewModel.signOut() },
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFF0F3),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB3C6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_logout_button")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFE53955),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53955)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }
}
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
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
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileEditDialog(
    currentUserName: String,
    currentPartnerName: String,
    currentPartnerNickname: String,
    myCode: String,
    partnerCode: String,
    onDismiss: () -> Unit,
    onSave: (userName: String, partnerName: String, partnerNickname: String) -> Unit
) {
    var userNameInput by remember { mutableStateOf(currentUserName) }
    var partnerNameInput by remember { mutableStateOf(currentPartnerName) }
    var partnerNicknameInput by remember { mutableStateOf(currentPartnerNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = DuoraRaspberry,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profile & Relationship",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = userNameInput,
                    onValueChange = { userNameInput = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraRaspberry,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_username_input")
                )

                OutlinedTextField(
                    value = partnerNameInput,
                    onValueChange = { partnerNameInput = it },
                    label = { Text("Partner Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraRaspberry,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_partnername_input")
                )

                OutlinedTextField(
                    value = partnerNicknameInput,
                    onValueChange = { partnerNicknameInput = it },
                    label = { Text("Partner Nickname") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraRaspberry,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("profile_nickname_input")
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Mutual Pairing Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (partnerCode.isNotBlank()) "Linked: #$myCode ↔ #$partnerCode" else "Your code: #$myCode (Unpaired)",
                            fontSize = 11.sp,
                            color = LoveGold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(userNameInput.trim(), partnerNameInput.trim(), partnerNicknameInput.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = DuoraRaspberry),
                modifier = Modifier.testTag("profile_save_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AboutDuoraDialog(
    viewModel: LoveCallViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = DuoraRaspberry,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About Duora",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Duora — Two hearts. One private space.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A private communication sanctuary built exclusively for two mutually paired people. Features one-tap voice & video calls, background hotline, real-time chat, picture-in-picture, and privacy app icon disguises.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
                Text(
                    text = "Version: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LoveGold
                )

                // Redacted diagnostics export
                Button(
                    onClick = {
                        val diag = viewModel.getRedactedDiagnostics()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Duora Diagnostics", diag))
                        Toast.makeText(context, "Diagnostics copied (Sensitive data redacted)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("copy_diagnostics_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = DuoraRaspberry,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy Redacted Diagnostics",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DuoraRaspberry)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
