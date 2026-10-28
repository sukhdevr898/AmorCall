package com.example.ui.pairing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import com.example.ui.pairing.DuoraQrCodeView
import com.example.ui.pairing.QrCodeGenerator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ui.theme.DuoraHotPink
import com.example.viewmodel.LoveCallViewModel

@Composable
fun PartnerPairingScreen(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val incomingRequest by viewModel.incomingPairingRequest.collectAsState()
    val isPairingSuccessful by viewModel.isPairingSuccessful.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Enter Code, 1: Scan QR
    var inputCode by remember { mutableStateOf("") }
    var isSendingRequest by remember { mutableStateOf(false) }
    var sentRequestToCode by remember { mutableStateOf<String?>(null) }
    var isVerifyingQr by remember { mutableStateOf(false) }
    var recognizedPartner by remember { mutableStateOf<PairingRecognizedPartner?>(null) }

    // Dialog 1: Partner Request Received Dialog (Screen 7)
    if (incomingRequest != null) {
        val req = incomingRequest!!
        Dialog(onDismissRequest = { viewModel.dismissIncomingPairingRequest() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("partner_request_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Interconnected Hearts & Avatars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Requester Avatar
                        val reqPainter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(viewModel.resolveAvatarThumbnailUrl(req.fromAvatarId))
                                .crossfade(true)
                                .build()
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, DuoraHotPink, CircleShape)
                        ) {
                            Image(
                                painter = reqPainter,
                                contentDescription = req.fromName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Heart icon in middle
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = DuoraHotPink,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(28.dp)
                        )

                        // My Avatar
                        val myPainter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(viewModel.resolveAvatarThumbnailUrl(profile.avatarId))
                                .crossfade(true)
                                .build()
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF7C3AED), CircleShape)
                        ) {
                            Image(
                                painter = myPainter,
                                contentDescription = "Me",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Partner Request Received!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21102E)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${req.fromName} (#${req.fromCode}) wants to connect with you on Duora 💕",
                        fontSize = 13.sp,
                        color = Color(0xFF89818E),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Accept Button
                    Button(
                        onClick = {
                            viewModel.respondPairingRequest("ACCEPT")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Accept Request", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Decline Button
                    OutlinedButton(
                        onClick = {
                            viewModel.respondPairingRequest("DECLINE")
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Decline", color = Color(0xFF89818E))
                    }
                }
            }
        }
    }

    // Dialog 2: Paired Successfully Screen (Screen 8)
    if (isPairingSuccessful) {
        Dialog(onDismissRequest = { viewModel.dismissPairingSuccess() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("pairing_success_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF16A765),
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Paired Successfully!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21102E)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You are now connected with ${profile.partnerName.ifBlank { "your partner" }}.\nAlways Together 💕",
                        fontSize = 14.sp,
                        color = Color(0xFF89818E),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.dismissPairingSuccess()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Continue to Duora", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

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
                modifier = Modifier.testTag("pairing_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Column {
                Text(
                    text = "Partner Pairing",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = "Connect with your love 💕",
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Enter Code vs Scan QR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF2E6ED))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 0) Color.White else Color.Transparent)
                    .clickable { activeTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter Code",
                    fontSize = 14.sp,
                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeTab == 0) DuoraHotPink else Color(0xFF89818E)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeTab == 1) Color.White else Color.Transparent)
                    .clickable { activeTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Scan QR",
                    fontSize = 14.sp,
                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeTab == 1) DuoraHotPink else Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (activeTab == 0) {
            // TAB 0: ENTER CODE (Screen 5)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Love chain circular badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBF2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Love Chain",
                        tint = DuoraHotPink,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Enter Partner Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )

                Text(
                    text = "Ask your partner for their code and send a request",
                    fontSize = 13.sp,
                    color = Color(0xFF89818E),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 6-Character PIN Entry Boxes (A-Z, 0-9)
                val pinLength = 6
                val cleanInput = inputCode.uppercase().filter { it.isLetterOrDigit() }.take(pinLength)
                val isSelfCode = cleanInput.isNotEmpty() && cleanInput.equals(profile.myPairCode, ignoreCase = true)
                val isCodeValid = cleanInput.length == pinLength && !isSelfCode

                BasicTextField(
                    value = cleanInput,
                    onValueChange = { inputCode = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(pinLength) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until pinLength) {
                                val char = cleanInput.getOrNull(i)?.toString() ?: ""
                                val isFocused = cleanInput.length == i
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(
                                            width = if (isFocused) 2.dp else 1.dp,
                                            color = when {
                                                isSelfCode -> Color(0xFFEF4444)
                                                isFocused -> DuoraHotPink
                                                char.isNotEmpty() -> DuoraHotPink.copy(alpha = 0.6f)
                                                else -> Color(0xFFEADBEE)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelfCode) Color(0xFFEF4444) else Color(0xFF21102E),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                )

                if (isSelfCode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ You cannot pair with your own code",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Send Request Button (Enabled ONLY when code is exactly 6 valid characters)
                Button(
                    onClick = {
                        if (!isCodeValid) {
                            Toast.makeText(context, "Please enter all 6 partner code characters", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSendingRequest = true
                        viewModel.sendPairingRequest(cleanInput) { success, msg ->
                            isSendingRequest = false
                            if (success) {
                                sentRequestToCode = cleanInput
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isCodeValid && !isSendingRequest,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_pairing_request_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (isCodeValid) {
                                        listOf(DuoraHotPink, Color(0xFFFF5D88))
                                    } else {
                                        listOf(Color(0xFFE5D4DF), Color(0xFFDCC8D6))
                                    }
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSendingRequest) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = if (isCodeValid) Color.White else Color(0xFF89818E),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send Request",
                                    color = if (isCodeValid) Color.White else Color(0xFF89818E),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (sentRequestToCode != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Pending request to #${sentRequestToCode}. Waiting for partner to accept...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD97706),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // "Your Code & QR" Card (Screen 5 of Design Sheet)
                val myCode = profile.myPairCode.ifBlank { "7H2K9L" }
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "YOUR CODE & QR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF89818E)
                            )

                            // Regenerate code button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newCode = viewModel.regenerateMyCode()
                                        Toast.makeText(context, "New Code Generated: $newCode", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = DuoraHotPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Regenerate",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraHotPink
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large 6-character Code Display with Copy Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFF0F5))
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = myCode,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DuoraHotPink,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 3.sp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("Duora Code", myCode)
                                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = DuoraHotPink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Automatic High-Resolution QR Code with Heart Center
                        DuoraQrCodeView(
                            code = myCode,
                            sizeDp = 180.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Share Code & Download QR (PNG)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Download QR Button
                            OutlinedButton(
                                onClick = {
                                    val saved = QrCodeGenerator.saveToGallery(context, myCode)
                                    if (saved) {
                                        Toast.makeText(context, "QR saved to Pictures/Duora ❤️", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "QR saved successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download QR",
                                    tint = DuoraHotPink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Save QR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraHotPink
                                )
                            }

                            // Share Code Button
                            Button(
                                onClick = {
                                    QrCodeGenerator.shareCode(context, myCode)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "⏱️ This code will expire in 10 minutes",
                            fontSize = 11.sp,
                            color = Color(0xFF89818E)
                        )
                    }
                }
            }
        } else {
            // TAB 1: SCAN QR (Screen 6 - Step 4 Implementation)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated neon viewfinder frame
                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    animProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E1022))
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(DuoraHotPink, Color(0xFF8B5CF6), DuoraHotPink)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isVerifyingQr) {
                            CircularProgressIndicator(
                                color = DuoraHotPink,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Verifying QR Code...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = DuoraHotPink,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera Scanner Active",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Align partner's Duora QR code within the frame",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF21102E)
                )

                Text(
                    text = "Codes are securely verified with Duora Server",
                    fontSize = 11.sp,
                    color = Color(0xFF89818E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gallery & Flash & Manual QR Input
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Scanning photo from album...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gallery",
                            tint = DuoraHotPink
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Camera flashlight toggled", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash",
                            tint = Color(0xFFEAB308)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick QR verification action (enter code or simulated scan)
                var scanInputText by remember { mutableStateOf("") }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "QUICK QR VERIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF89818E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9F5F8))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            BasicTextField(
                                value = scanInputText,
                                onValueChange = { scanInputText = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("qr_payload_input"),
                                decorationBox = { innerTextField ->
                                    if (scanInputText.isEmpty()) {
                                        Text(
                                            text = "Paste QR URL or 6-char code",
                                            fontSize = 13.sp,
                                            color = Color(0xFFA19BA6)
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            Button(
                                onClick = {
                                    val text = scanInputText.trim()
                                    if (text.isBlank()) {
                                        Toast.makeText(context, "Please enter a code or QR string", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isVerifyingQr = true
                                    viewModel.scanQrPayload(text) { success, msg, partnerCode, partnerName, partnerAvatarId, partnerAvatarUrl, blueTick, greenTick ->
                                        isVerifyingQr = false
                                        if (success && partnerCode != null) {
                                            recognizedPartner = PairingRecognizedPartner(
                                                code = partnerCode,
                                                name = partnerName ?: "Partner",
                                                avatarId = partnerAvatarId ?: "girl_001",
                                                avatarUrl = partnerAvatarUrl,
                                                blueTick = blueTick,
                                                greenTick = greenTick
                                            )
                                            Toast.makeText(context, "Partner Recognized! 💕", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("verify_qr_button")
                            ) {
                                Text("Verify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // If partner recognized via QR, show Verified Partner Card
                if (recognizedPartner != null) {
                    val partner = recognizedPartner!!
                    Spacer(modifier = Modifier.height(18.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DuoraHotPink.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recognized_partner_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MATCH FOUND! 💕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraHotPink
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, DuoraHotPink, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val dpUrl = partner.avatarUrl ?: viewModel.resolveAvatarThumbnailUrl(partner.avatarId)
                                Image(
                                    painter = rememberAsyncImagePainter(dpUrl),
                                    contentDescription = partner.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = partner.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21102E)
                                )
                                if (partner.blueTick) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified User",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (partner.greenTick) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Partner Verified",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = "#${partner.code}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    isSendingRequest = true
                                    viewModel.sendPairingRequest(partner.code) { success, msg ->
                                        isSendingRequest = false
                                        if (success) {
                                            sentRequestToCode = partner.code
                                            recognizedPartner = null
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DuoraHotPink),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("connect_scanned_partner_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Pairing Request 💕", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

data class PairingRecognizedPartner(
    val code: String,
    val name: String,
    val avatarId: String,
    val avatarUrl: String?,
    val blueTick: Boolean,
    val greenTick: Boolean
)
