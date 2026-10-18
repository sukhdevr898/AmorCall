package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoupleProfile
import com.example.ui.theme.LoveBlush
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LoveCardSurface
import com.example.ui.theme.LoveCrimson
import com.example.ui.theme.LoveDarkBg
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LoveRose
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveWhite

@Composable
fun OnboardingScreen(
    currentProfile: CoupleProfile,
    onComplete: (partnerName: String, partnerNickname: String, partnerCode: String) -> Unit
) {
    var partnerName by remember { mutableStateOf(currentProfile.partnerName) }
    var partnerNickname by remember { mutableStateOf(currentProfile.partnerNickname) }
    var partnerPairCode by remember { mutableStateOf(currentProfile.partnerPairCode) }

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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(LoveRose, LoveCrimson, Color(0xFF380824))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Welcome Heart",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to AmourCall ❤️",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveWhite
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Set up your personal couple hotline in 30 seconds",
                    fontSize = 13.sp,
                    color = LoveTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Setup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LoveCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, LoveCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // My Code Display
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x22F43F5E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Love Code:",
                                    fontSize = 12.sp,
                                    color = LoveBlush
                                )
                                Text(
                                    text = "#${currentProfile.myPairCode}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoveGold
                                )
                            }
                        }

                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Partner's Name") },
                            placeholder = { Text("e.g. My Love, Priya, Sophia") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LoveWhite,
                                unfocusedTextColor = LoveWhite,
                                focusedBorderColor = LoveRose,
                                unfocusedBorderColor = LoveCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_partner_name")
                        )

                        OutlinedTextField(
                            value = partnerNickname,
                            onValueChange = { partnerNickname = it },
                            label = { Text("Pet Name / Nickname") },
                            placeholder = { Text("e.g. Sweetheart, Babu, Darling") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LoveWhite,
                                unfocusedTextColor = LoveWhite,
                                focusedBorderColor = LoveRose,
                                unfocusedBorderColor = LoveCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_partner_nickname")
                        )

                        OutlinedTextField(
                            value = partnerPairCode,
                            onValueChange = { partnerPairCode = it.uppercase() },
                            label = { Text("Partner's Love Pairing Code") },
                            placeholder = { Text("e.g. ROSE99") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = LoveWhite,
                                unfocusedTextColor = LoveWhite,
                                focusedBorderColor = LoveRose,
                                unfocusedBorderColor = LoveCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_partner_code")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val finalName = partnerName.ifBlank { "My Love" }
                        val finalNickname = partnerNickname.ifBlank { "Sweetheart" }
                        val finalCode = partnerPairCode.ifBlank { "ROSE99" }
                        onComplete(finalName, finalNickname, finalCode)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_submit_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoveRose)
                ) {
                    Text(
                        text = "Enter AmourCall ❤️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 Private & secure • You can update details anytime in Settings",
                    fontSize = 11.sp,
                    color = LoveTextMuted
                )
            }
        }
    }
}
