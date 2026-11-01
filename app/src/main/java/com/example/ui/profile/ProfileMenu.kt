package com.example.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.model.CoupleProfile
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuSheet(
    profile: CoupleProfile,
    avatarUrl: String?,
    partnerAvatarUrl: String?,
    isPartnerOnline: Boolean,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onEditProfileClick: () -> Unit,
    onPartnerPairingClick: () -> Unit,
    onUnpairClick: () -> Unit
) {
    val context = LocalContext.current
    var showUnpairDialog by remember { mutableStateOf(false) }

    if (showUnpairDialog) {
        AlertDialog(
            onDismissRequest = { showUnpairDialog = false },
            title = {
                Text(
                    text = "Unpair Partner?",
                    fontWeight = FontWeight.Bold,
                    color = DuoraPrimaryText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to unpair from ${profile.partnerName}? Both users will be unlinked, and you can re-pair anytime using your pair codes.",
                    fontSize = 14.sp,
                    color = DuoraSecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnpairDialog = false
                        onDismissRequest()
                        onUnpairClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Unpair", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnpairDialog = false }) {
                    Text("Cancel", color = DuoraSecondaryText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
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

            // User's Real DP with Online indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(3.dp, DuoraHotPink, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = "My Profile DP",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Name + Verification Ticks
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val displayName = profile.fullName.takeIf { it.isNotBlank() && !it.equals("You", ignoreCase = true) }
                    ?: profile.userName.ifBlank { "You" }
                Text(
                    text = displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoraPrimaryText
                )
                if (profile.greenTick) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_admin_green_tick),
                        contentDescription = "Admin Verified",
                        modifier = Modifier.size(20.dp)
                    )
                } else if (profile.blueTick || profile.isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_blue_tick),
                        contentDescription = "Verified User",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Username handle & Status Msg
            Text(
                text = "@${profile.username.ifBlank { "duora_user" }}",
                fontSize = 13.sp,
                color = DuoraSecondaryText,
                fontWeight = FontWeight.Medium
            )

            if (profile.statusMsg.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.statusMsg,
                    fontSize = 12.sp,
                    color = DuoraPrimaryPink,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // My Pair Code Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MY PAIR CODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoraHotPink,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "#${profile.myPairCode}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = DuoraPrimaryText
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Pair Code", profile.myPairCode))
                            Toast.makeText(context, "Pair code copied! 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = DuoraHotPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Partner Connection Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECEFF1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (profile.isPaired && profile.partnerPairCode.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, DuoraHotPink, CircleShape)
                        ) {
                            val partnerPainter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(partnerAvatarUrl)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = partnerPainter,
                                contentDescription = profile.partnerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val pName = profile.partnerName.takeIf { it.isNotBlank() && !it.equals("You", ignoreCase = true) }
                                    ?: "Partner"
                                Text(
                                    text = pName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraPrimaryText
                                )
                                if (profile.partnerGreenTick) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_admin_green_tick),
                                        contentDescription = "Admin Verified",
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (profile.partnerBlueTick || profile.partnerIsVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_blue_tick),
                                        contentDescription = "Verified Partner",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isPartnerOnline) "● Partner Online ❤️" else "○ Partner Offline",
                                fontSize = 12.sp,
                                color = if (isPartnerOnline) Color(0xFF10B981) else DuoraSecondaryText
                            )
                        }

                        // Unpair icon button
                        IconButton(
                            onClick = { showUnpairDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = "Unpair Partner",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Not Paired Yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraPrimaryText
                            )
                            Text(
                                text = "Connect with your partner to start",
                                fontSize = 12.sp,
                                color = DuoraSecondaryText
                            )
                        }
                        Button(
                            onClick = {
                                onDismissRequest()
                                onPartnerPairingClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pair", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismissRequest()
                        onEditProfileClick()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DuoraHotPink
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile", color = DuoraHotPink, fontSize = 13.sp)
                }

                if (profile.isPaired) {
                    Button(
                        onClick = { showUnpairDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF1F2)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("💔 Unpair", color = Color(0xFFE11D48), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = {
                            onDismissRequest()
                            onPartnerPairingClick()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Pair Now", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
