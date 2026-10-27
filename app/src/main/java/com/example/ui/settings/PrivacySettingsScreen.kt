package com.example.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privacy.PrivacyIconManager
import com.example.privacy.PrivacyIconType
import com.example.ui.theme.DuoraHotPink
import com.example.viewmodel.LoveCallViewModel

@Composable
fun PrivacySettingsScreen(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val currentIcon by PrivacyIconManager.currentIcon.collectAsState()

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
                modifier = Modifier.testTag("privacy_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Column {
                Text(
                    text = "Privacy & App Icon",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = "Disguise app & protect private chats",
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Disguise App Icon
        Text(
            text = "DISGUISE APP ICON",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF89818E),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppIconChoiceCard(
                icon = Icons.Default.Favorite,
                title = "Duora",
                tint = DuoraHotPink,
                isSelected = currentIcon == PrivacyIconType.DEFAULT,
                modifier = Modifier.weight(1f),
                onClick = {
                    PrivacyIconManager.setPrivacyIcon(context, PrivacyIconType.DEFAULT)
                    Toast.makeText(context, "Launcher icon set to Duora", Toast.LENGTH_SHORT).show()
                }
            )

            AppIconChoiceCard(
                icon = Icons.Default.Calculate,
                title = "Calculator",
                tint = Color(0xFF0284C7),
                isSelected = currentIcon == PrivacyIconType.CALCULATOR,
                modifier = Modifier.weight(1f),
                onClick = {
                    PrivacyIconManager.setPrivacyIcon(context, PrivacyIconType.CALCULATOR)
                    Toast.makeText(context, "Launcher icon disguised as Calculator", Toast.LENGTH_SHORT).show()
                }
            )

            AppIconChoiceCard(
                icon = Icons.Default.Description,
                title = "Notes",
                tint = Color(0xFFEAB308),
                isSelected = currentIcon == PrivacyIconType.NOTES,
                modifier = Modifier.weight(1f),
                onClick = {
                    PrivacyIconManager.setPrivacyIcon(context, PrivacyIconType.NOTES)
                    Toast.makeText(context, "Launcher icon disguised as Notes", Toast.LENGTH_SHORT).show()
                }
            )

            AppIconChoiceCard(
                icon = Icons.Default.Cloud,
                title = "Weather",
                tint = Color(0xFF10B981),
                isSelected = currentIcon == PrivacyIconType.WEATHER,
                modifier = Modifier.weight(1f),
                onClick = {
                    PrivacyIconManager.setPrivacyIcon(context, PrivacyIconType.WEATHER)
                    Toast.makeText(context, "Launcher icon disguised as Weather", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Section: Privacy Controls
        Text(
            text = "SECURITY CONTROLS",
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
                PrivacyToggleRow(
                    icon = Icons.Default.VisibilityOff,
                    title = "Hide From Recent Apps",
                    subtitle = "Blurs app snapshot in multitasking carousel",
                    checked = profile.isHideFromRecentApps,
                    onCheckedChange = { checked ->
                        viewModel.updatePrivacySettings(
                            hideFromRecent = checked,
                            appLock = profile.isAppLockEnabled,
                            blurNotif = profile.isBlurNotifications
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                PrivacyToggleRow(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = "Require device screen lock or biometric",
                    checked = profile.isAppLockEnabled,
                    onCheckedChange = { checked ->
                        viewModel.updatePrivacySettings(
                            hideFromRecent = profile.isHideFromRecentApps,
                            appLock = checked,
                            blurNotif = profile.isBlurNotifications
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF7F2F6), thickness = 1.dp)

                PrivacyToggleRow(
                    icon = Icons.Default.Visibility,
                    title = "Blur Notifications",
                    subtitle = "Hide sender and chat preview on lock screen",
                    checked = profile.isBlurNotifications,
                    onCheckedChange = { checked ->
                        viewModel.updatePrivacySettings(
                            hideFromRecent = profile.isHideFromRecentApps,
                            appLock = profile.isAppLockEnabled,
                            blurNotif = checked
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun AppIconChoiceCard(
    icon: ImageVector,
    title: String,
    tint: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) DuoraHotPink else Color(0xFFF0E8ED),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DuoraHotPink else Color(0xFF21102E)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(DuoraHotPink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
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
