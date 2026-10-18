package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LoveCardSurface
import com.example.ui.theme.LoveDarkBg
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveWhite
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

@Composable
fun PairingScreen(viewModel: LoveCallViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    var partnerName by remember(profile.partnerName) { mutableStateOf(profile.partnerName) }
    var partnerNickname by remember(profile.partnerNickname) { mutableStateOf(profile.partnerNickname) }
    var partnerPairCode by remember(profile.partnerPairCode) { mutableStateOf(profile.partnerPairCode) }

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
                        .testTag("pairing_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = LoveWhite
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Couple Connection & Pairing",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoveWhite
                    )
                    Text(
                        text = "Secure private peer pairing code",
                        fontSize = 12.sp,
                        color = LoveTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pairing Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LoveAccentGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Connected",
                                tint = LoveAccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Secure Pair Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = LoveWhite
                            )
                            Text(
                                text = "End-to-end encrypted direct couple lifeline",
                                fontSize = 12.sp,
                                color = LoveTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // My Pair Code Display
                    Text(
                        text = "YOUR DEVICE LOVE CODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LoveBlush
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x33F43F5E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${profile.myPairCode}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = LoveGold
                            )
                            Text(
                                text = "Share with Partner",
                                fontSize = 11.sp,
                                color = LoveWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Partner Customization Form
            Text(
                text = "Partner Information",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LoveBlush,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = partnerName,
                        onValueChange = { partnerName = it },
                        label = { Text("Partner Name") },
                        placeholder = { Text("My Love") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LoveWhite,
                            unfocusedTextColor = LoveWhite,
                            focusedBorderColor = LoveRose,
                            unfocusedBorderColor = LoveCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_name_input")
                    )

                    OutlinedTextField(
                        value = partnerNickname,
                        onValueChange = { partnerNickname = it },
                        label = { Text("Pet Name / Nickname") },
                        placeholder = { Text("Sweetheart / Babu") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LoveWhite,
                            unfocusedTextColor = LoveWhite,
                            focusedBorderColor = LoveRose,
                            unfocusedBorderColor = LoveCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_nickname_input")
                    )

                    OutlinedTextField(
                        value = partnerPairCode,
                        onValueChange = { partnerPairCode = it.uppercase() },
                        label = { Text("Partner's Love Code") },
                        placeholder = { Text("ROSE99") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LoveWhite,
                            unfocusedTextColor = LoveWhite,
                            focusedBorderColor = LoveRose,
                            unfocusedBorderColor = LoveCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_code_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(partnerName, partnerNickname, partnerPairCode)
                            Toast.makeText(context, "Pairing details saved ❤️", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_pairing_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LoveRose)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Link Device",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Self-Hosted Server Configuration Card
            val serverState by viewModel.serverConnectionState.collectAsState()
            var serverUrlInput by remember(profile.serverUrl) { mutableStateOf(profile.serverUrl) }

            Text(
                text = "Self-Hosted Node.js Server",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LoveBlush,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Server Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = LoveWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (serverState) {
                                com.example.network.ServerConnectionState.CONNECTED -> Color(0x3310B981)
                                com.example.network.ServerConnectionState.CONNECTING -> Color(0x33FBBF24)
                                com.example.network.ServerConnectionState.DISCONNECTED -> Color(0x33F43F5E)
                            }
                        ) {
                            Text(
                                text = when (serverState) {
                                    com.example.network.ServerConnectionState.CONNECTED -> "● Connected"
                                    com.example.network.ServerConnectionState.CONNECTING -> "Connecting..."
                                    com.example.network.ServerConnectionState.DISCONNECTED -> "○ Offline / Direct"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (serverState) {
                                    com.example.network.ServerConnectionState.CONNECTED -> LoveAccentGreen
                                    com.example.network.ServerConnectionState.CONNECTING -> LoveGold
                                    com.example.network.ServerConnectionState.DISCONNECTED -> LoveRose
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("WebSocket Server URL") },
                        placeholder = { Text("ws://10.0.2.2:3000 or wss://...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LoveWhite,
                            unfocusedTextColor = LoveWhite,
                            focusedBorderColor = LoveRose,
                            unfocusedBorderColor = LoveCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input")
                    )

                    Button(
                        onClick = {
                            viewModel.reconnectServer(serverUrlInput)
                            Toast.makeText(context, "Connecting to server...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("connect_server_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LoveCardBorder)
                    ) {
                        Text(
                            text = "Save & Connect Server",
                            fontSize = 13.sp,
                            color = LoveWhite
                        )
                    }

                    Text(
                        text = "The app connects directly to your Node.js server located in the /server directory for real-time couple signaling.",
                        fontSize = 11.sp,
                        color = LoveTextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    tint = LoveGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Encrypted direct peer channel. Zero tracking or ads.",
                    fontSize = 11.sp,
                    color = LoveTextMuted
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
