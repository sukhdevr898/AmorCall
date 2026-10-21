package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import com.example.privacy.PrivacyIconManager
import com.example.privacy.PrivacyIconType
import com.example.ui.components.PrivacyIconDialog
import com.example.ui.theme.LoveAccentGreen
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LoveCardSurface
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveDarkBg
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveWhite
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

@Composable
fun DeveloperScreen(viewModel: LoveCallViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val context = LocalContext.current
    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    val isChecking by viewModel.isCheckingUpdate.collectAsState()
    val statusMessage by viewModel.updateStatusMessage.collectAsState()

    var showPrivacyIconDialog by remember { mutableStateOf(false) }
    val currentPrivacyIcon by PrivacyIconManager.currentIcon.collectAsState()

    if (showPrivacyIconDialog) {
        PrivacyIconDialog(
            currentIcon = currentPrivacyIcon,
            onDismiss = { showPrivacyIconDialog = false },
            onIconChanged = { showPrivacyIconDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LoveCardSurface)
                        .border(1.dp, LoveCardBorder, CircleShape)
                        .testTag("dev_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LoveWhite
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Developer Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, LoveRose.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(LoveCrimson, LoveRose, LoveGold)
                                )
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF200922)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Developer Avatar",
                            tint = LoveGold,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sukhdev Singh",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoveWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33F43F5E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "@sukhdevr898",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LoveGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Lead Android Developer & Creator of AmourCall",
                        fontSize = 13.sp,
                        color = LoveBlush,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Built with pure love and passion to keep two hearts effortlessly connected through crystal-clear voice and video calls.",
                        fontSize = 12.sp,
                        color = LoveTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Connect with Sukhdev",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LoveBlush,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // GitHub Link Card
            DeveloperLinkCard(
                title = "GitHub Profile",
                handle = "github.com/sukhdevr898",
                iconColor = LoveWhite,
                accentBorder = Color(0x66FFFFFF),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/sukhdevr898"))
                    context.startActivity(intent)
                },
                testTag = "dev_github_link"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Instagram Link Card
            DeveloperLinkCard(
                title = "Instagram Profile",
                handle = "instagram.com/sukhdevr898",
                iconColor = LoveRose,
                accentBorder = LoveRose.copy(alpha = 0.6f),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/sukhdevr898"))
                    context.startActivity(intent)
                },
                testTag = "dev_instagram_link"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App Updates & Version Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, LoveRose.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                                    text = "App Updates & Releases",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoveWhite
                                )
                                Text(
                                    text = "Current: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                    fontSize = 11.sp,
                                    color = LoveGold
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.checkForUpdates(isManual = true) },
                            enabled = !isChecking,
                            colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("check_updates_button")
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (statusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33FFFFFF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = statusMessage!!,
                                fontSize = 12.sp,
                                color = LoveWhite,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (updateInfo?.isAvailable == true) {
                        Button(
                            onClick = {
                                val targetUrl = updateInfo!!.browserUrl.ifBlank { updateInfo!!.downloadUrl }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dev_download_update_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download v${updateInfo!!.versionName} from GitHub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Direct link to GitHub Repo APK Directory
                    DeveloperLinkCard(
                        title = "GitHub APK Directory",
                        handle = "github.com/sukhdevr898/AmorCall/tree/main/APK",
                        iconColor = LoveGold,
                        accentBorder = LoveGold.copy(alpha = 0.5f),
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/sukhdevr898/AmorCall/tree/main/APK")
                            )
                            context.startActivity(intent)
                        },
                        testTag = "dev_apk_directory_link"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stealth Privacy App Icon Disguise Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, LoveRose.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(LoveRose.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Privacy Shield",
                                    tint = LoveRose,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Privacy App Icon",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoveWhite
                                )
                                Text(
                                    text = "Active: ${currentPrivacyIcon.launcherLabel} (${currentPrivacyIcon.title})",
                                    fontSize = 11.sp,
                                    color = LoveGold
                                )
                            }
                        }

                        Button(
                            onClick = { showPrivacyIconDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LoveRose),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dev_change_privacy_icon_button")
                        ) {
                            Text("Disguise", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Disguise the launcher icon and title as a Calculator, Notes, Clock, or Weather app to keep calls discreet.",
                        fontSize = 11.sp,
                        color = LoveTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Version & Dedication
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Love",
                            tint = LoveRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Duora v${BuildConfig.VERSION_NAME} • Two hearts. One private space.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LoveGold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Designed exclusively for personal couple communication.",
                        fontSize = 11.sp,
                        color = LoveTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DeveloperLinkCard(
    title: String,
    handle: String,
    iconColor: Color,
    accentBorder: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoveWhite
                    )
                    Text(
                        text = handle,
                        fontSize = 12.sp,
                        color = LoveGold
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Link",
                tint = LoveBlush,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
