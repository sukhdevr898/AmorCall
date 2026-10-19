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

    val context = androidx.compose.ui.platform.LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    text = "Welcome to Duora",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveWhite
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Two hearts. One private space.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = LoveGold,
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
                        // My Unique Love Code Display
                        Text(
                            text = "STEP 1: YOUR SPECIAL LOVE CODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveBlush
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x33F43F5E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LoveRose.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${currentProfile.myPairCode}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = LoveGold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        androidx.compose.material3.TextButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("AmourCall Code", currentProfile.myPairCode)
                                                clipboard.setPrimaryClip(clip)
                                                android.widget.Toast.makeText(context, "Code copied! Share with your love ❤️", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Copy", fontSize = 12.sp, color = LoveWhite)
                                        }
                                        androidx.compose.material3.TextButton(
                                            onClick = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "Hey my love! Connect with me on AmourCall ❤️ My Love Code is: #${currentProfile.myPairCode}")
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Share Love Code"))
                                            },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Share", fontSize = 12.sp, color = LoveGold)
                                        }
                                    }
                                }
                                Text(
                                    text = "Send this code to your partner so they can link with you.",
                                    fontSize = 11.sp,
                                    color = LoveTextMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "STEP 2: PARTNER DETAILS & CODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoveBlush,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Partner's Name") },
                            placeholder = { Text("e.g. My Love, Sophia, Rahul") },
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
                            placeholder = { Text("e.g. Sweetheart, Jaanu, Darling") },
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
                            onValueChange = {
                                partnerPairCode = it.uppercase()
                                errorMessage = null
                            },
                            label = { Text("Partner's Love Code (Optional)") },
                            placeholder = { Text("Enter their code (e.g. AMOUR-7K9W)") },
                            singleLine = true,
                            supportingText = {
                                Text(
                                    text = "Leave blank if you haven't received partner's code yet. You can link anytime!",
                                    fontSize = 11.sp,
                                    color = LoveTextMuted
                                )
                            },
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

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = LoveRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                        val finalCode = partnerPairCode.trim().uppercase()
                        if (finalCode.isNotBlank() && finalCode.equals(currentProfile.myPairCode, ignoreCase = true)) {
                            errorMessage = "You cannot enter your own code! Please enter your partner's code."
                            return@Button
                        }
                        val finalName = partnerName.ifBlank { "My Love" }
                        val finalNickname = partnerNickname.ifBlank { "Sweetheart" }
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
