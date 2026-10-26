package com.example.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DuoraError
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryText
import com.example.ui.theme.DuoraSuccess

/**
 * Profile & Menu Sheet matching Mockup 3 in the design reference:
 * - Close button (X)
 * - Circular avatar with online green status badge
 * - "My Love 💗" and "Online"
 * - Menu Items:
 *   1. Partner Pairing (pink heart/link icon)
 *   2. Theme (palette icon)
 *   3. Server Settings (layers/gear icon)
 *   4. Profile (person icon)
 *   5. Privacy & Security (shield icon)
 *   6. Help & Support (question icon)
 *   7. Logout (logout icon in red)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuSheet(
    partnerName: String = "My Love",
    isOnline: Boolean = true,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onPartnerPairingClick: () -> Unit,
    onThemeClick: () -> Unit,
    onServerSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPrivacySecurityClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("profile_menu_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row with Close Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DuoraSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Circular Avatar with Online indicator
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_partner_demo),
                    contentDescription = "Partner Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, DuoraPrimaryPink, CircleShape)
                )

                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(DuoraSuccess)
                            .border(2.5.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Partner Name & Online text
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (partnerName.isNotBlank()) partnerName else "My Love",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoraPrimaryText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "💗", fontSize = 16.sp)
            }

            Text(
                text = if (isOnline) "Online" else "Away",
                fontSize = 12.sp,
                color = if (isOnline) DuoraSuccess else DuoraSecondaryText,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Menu Items List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ProfileMenuRow(
                        title = "Partner Pairing",
                        icon = Icons.Default.Favorite,
                        iconTint = DuoraPrimaryPink,
                        onClick = {
                            onDismissRequest()
                            onPartnerPairingClick()
                        }
                    )
                }

                item {
                    ProfileMenuRow(
                        title = "Theme",
                        icon = Icons.Default.Palette,
                        iconTint = Color(0xFFF59E0B),
                        onClick = {
                            onDismissRequest()
                            onThemeClick()
                        }
                    )
                }

                item {
                    ProfileMenuRow(
                        title = "Server Settings",
                        icon = Icons.Default.Layers,
                        iconTint = Color(0xFF3B82F6),
                        onClick = {
                            onDismissRequest()
                            onServerSettingsClick()
                        }
                    )
                }

                item {
                    ProfileMenuRow(
                        title = "Profile",
                        icon = Icons.Default.Person,
                        iconTint = Color(0xFF8B5CF6),
                        onClick = {
                            onDismissRequest()
                            onProfileClick()
                        }
                    )
                }

                item {
                    ProfileMenuRow(
                        title = "Privacy & Security",
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF10B981),
                        onClick = {
                            onDismissRequest()
                            onPrivacySecurityClick()
                        }
                    )
                }

                item {
                    ProfileMenuRow(
                        title = "Help & Support",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconTint = Color(0xFF0284C7),
                        onClick = {
                            onDismissRequest()
                            onHelpSupportClick()
                        }
                    )
                }

                item {
                    ProfileMenuRow(
                        title = "Logout",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconTint = DuoraError,
                        titleColor = DuoraError,
                        onClick = {
                            onDismissRequest()
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    titleColor: Color = DuoraPrimaryText,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFAFAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1EAEF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = DuoraSecondaryText.copy(alpha = 0.6f),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
