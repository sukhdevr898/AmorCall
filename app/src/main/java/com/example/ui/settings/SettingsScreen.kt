package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ui.pairing.PartnerPairingScreen
import com.example.ui.theme.DuoraHotPink
import com.example.viewmodel.LoveCallViewModel
import com.example.viewmodel.SettingsSubScreen

@Composable
fun SettingsScreen(
    viewModel: LoveCallViewModel,
    onBackToHome: () -> Unit
) {
    val subScreen by viewModel.settingsSubScreen.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirmLogout = {
                showLogoutDialog = false
                viewModel.logOut()
            }
        )
    }

    AnimatedContent(
        targetState = subScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "SettingsSubScreenTransition"
    ) { screen ->
        when (screen) {
            SettingsSubScreen.MAIN -> {
                SettingsMainView(
                    viewModel = viewModel,
                    onBackToHome = onBackToHome,
                    onNavigate = { viewModel.navigateSettingsTo(it) },
                    onOpenLogout = { showLogoutDialog = true }
                )
            }
            SettingsSubScreen.PROFILE_AVATAR -> {
                ProfileAvatarScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateSettingsBack() },
                    onOpenAvatarSelection = { category ->
                        if (category == "GIRL") {
                            viewModel.navigateSettingsTo(SettingsSubScreen.AVATAR_SELECTION_GIRLS)
                        } else {
                            viewModel.navigateSettingsTo(SettingsSubScreen.AVATAR_SELECTION_BOYS)
                        }
                    }
                )
            }
            SettingsSubScreen.AVATAR_SELECTION_BOYS -> {
                AvatarSelectionScreen(
                    viewModel = viewModel,
                    initialCategory = "BOY",
                    onBack = { viewModel.navigateSettingsTo(SettingsSubScreen.PROFILE_AVATAR) }
                )
            }
            SettingsSubScreen.AVATAR_SELECTION_GIRLS -> {
                AvatarSelectionScreen(
                    viewModel = viewModel,
                    initialCategory = "GIRL",
                    onBack = { viewModel.navigateSettingsTo(SettingsSubScreen.PROFILE_AVATAR) }
                )
            }
            SettingsSubScreen.PAIRING_ENTER_CODE, SettingsSubScreen.PAIRING_SCAN_QR -> {
                PartnerPairingScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
            SettingsSubScreen.THEME -> {
                ThemeSettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
            SettingsSubScreen.PRIVACY_APP_ICON -> {
                PrivacySettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
            SettingsSubScreen.NOTIFICATIONS -> {
                NotificationSettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
            SettingsSubScreen.SERVER_HEALTH -> {
                ServerHealthScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
            SettingsSubScreen.ABOUT -> {
                AboutScreen(
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
            SettingsSubScreen.DEVELOPER -> {
                DeveloperProfileScreen(
                    onBack = { viewModel.navigateSettingsBack() }
                )
            }
        }
    }
}

@Composable
private fun SettingsMainView(
    viewModel: LoveCallViewModel,
    onBackToHome: () -> Unit,
    onNavigate: (SettingsSubScreen) -> Unit,
    onOpenLogout: () -> Unit
) {
    BackHandler(onBack = onBackToHome)
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6FA))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToHome,
                modifier = Modifier.testTag("settings_back_to_home")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Column {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = "Customize your Duora experience 💕",
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Profile Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { onNavigate(SettingsSubScreen.PROFILE_AVATAR) }
                .testTag("profile_card_row")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarUrl = viewModel.resolveAvatarThumbnailUrl(profile.avatarId)
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(profile.avatarUri ?: avatarUrl)
                        .crossfade(true)
                        .build()
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, DuoraHotPink, CircleShape)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Profile Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.userName.ifBlank { "You" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21102E)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A765))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = profile.partnerStatus.ifBlank { "Always Together 💕" },
                            fontSize = 12.sp,
                            color = Color(0xFF89818E)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFCCC5CE),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 9 Settings Items List
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column {
                SettingsMenuRow(
                    icon = Icons.Default.Link,
                    iconBg = Color(0xFFFFEBF2),
                    iconTint = DuoraHotPink,
                    title = "Partner Pairing",
                    subtitle = "Connect with your love",
                    onClick = { onNavigate(SettingsSubScreen.PAIRING_ENTER_CODE) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.AccountCircle,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF7C3AED),
                    title = "Profiles",
                    subtitle = "Edit profile & avatar",
                    onClick = { onNavigate(SettingsSubScreen.PROFILE_AVATAR) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.Palette,
                    iconBg = Color(0xFFFFF0E6),
                    iconTint = Color(0xFFEA580C),
                    title = "App Theme",
                    subtitle = "Light / Dark / Auto",
                    onClick = { onNavigate(SettingsSubScreen.THEME) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.Shield,
                    iconBg = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0284C7),
                    title = "Privacy & App Icon",
                    subtitle = "Hide app icon & privacy",
                    onClick = { onNavigate(SettingsSubScreen.PRIVACY_APP_ICON) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.NotificationsActive,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFD97706),
                    title = "Notifications",
                    subtitle = "Message, call & alert settings",
                    onClick = { onNavigate(SettingsSubScreen.NOTIFICATIONS) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.Dns,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF16A765),
                    title = "Server Health",
                    subtitle = "Check connection status",
                    onClick = { onNavigate(SettingsSubScreen.SERVER_HEALTH) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.Info,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF8B5CF6),
                    title = "About Duora",
                    subtitle = "Version, terms & support",
                    onClick = { onNavigate(SettingsSubScreen.ABOUT) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.Default.Code,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFE53945),
                    title = "Developer Profile",
                    subtitle = "About developer",
                    onClick = { onNavigate(SettingsSubScreen.DEVELOPER) }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                SettingsMenuRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconBg = Color(0xFFFFF0F5),
                    iconTint = DuoraHotPink,
                    title = "Log Out",
                    subtitle = "Sign out from Duora",
                    onClick = onOpenLogout
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
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

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCCC5CE),
            modifier = Modifier.size(20.dp)
        )
    }
}
