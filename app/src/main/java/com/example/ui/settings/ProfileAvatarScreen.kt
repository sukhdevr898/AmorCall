package com.example.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ui.theme.DuoraHotPink
import com.example.viewmodel.LoveCallViewModel
import com.example.viewmodel.SettingsSubScreen

@Composable
fun ProfileAvatarScreen(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit,
    onOpenAvatarSelection: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val avatars by viewModel.avatarCatalog.collectAsState()

    var displayName by remember { mutableStateOf(profile.userName) }
    var statusMessage by remember { mutableStateOf(profile.partnerStatus) }
    var selectedAvatarId by remember { mutableStateOf(profile.avatarId) }
    var customPhotoUri by remember { mutableStateOf<Uri?>(profile.avatarUri?.let { Uri.parse(it) }) }
    var activeCategory by remember { mutableStateOf(if (profile.avatarId.startsWith("girl")) "GIRL" else "BOY") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customPhotoUri = uri
            Toast.makeText(context, "Photo selected! Tap Save to apply.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAvatars(activeCategory)
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
                modifier = Modifier.testTag("profile_avatar_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF21102E)
                )
            }
            Column {
                Text(
                    text = "Profile & Avatar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21102E)
                )
                Text(
                    text = "Make it yours 💕",
                    fontSize = 12.sp,
                    color = Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Profile Avatar Circle Preview
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(110.dp)
        ) {
            val previewUrl = customPhotoUri ?: profile.avatarUrl ?: viewModel.resolveAvatarUrl(selectedAvatarId)
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(previewUrl)
                    .crossfade(true)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Selected Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(3.dp, DuoraHotPink, CircleShape)
            )

            // Edit badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DuoraHotPink)
                    .clickable {
                        onOpenAvatarSelection(activeCategory)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name & Status Form Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Display Name",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF21102E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraHotPink,
                        unfocusedBorderColor = Color(0xFFEADBEE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("display_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Status Message",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF21102E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = statusMessage,
                    onValueChange = { statusMessage = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraHotPink,
                        unfocusedBorderColor = Color(0xFFEADBEE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("status_message_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Boy / Girl Category Switch
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
                    .background(if (activeCategory == "BOY") Color.White else Color.Transparent)
                    .clickable {
                        activeCategory = "BOY"
                        viewModel.loadAvatars("BOY")
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Boy Avatars",
                    fontSize = 13.sp,
                    fontWeight = if (activeCategory == "BOY") FontWeight.Bold else FontWeight.Medium,
                    color = if (activeCategory == "BOY") DuoraHotPink else Color(0xFF89818E)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeCategory == "GIRL") Color.White else Color.Transparent)
                    .clickable {
                        activeCategory = "GIRL"
                        viewModel.loadAvatars("GIRL")
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Girl Avatars",
                    fontSize = 13.sp,
                    fontWeight = if (activeCategory == "GIRL") FontWeight.Bold else FontWeight.Medium,
                    color = if (activeCategory == "GIRL") DuoraHotPink else Color(0xFF89818E)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Grid Preview (6 avatars) + "View All"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Choose from Library",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF21102E)
            )
            Text(
                text = "View All >",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DuoraHotPink,
                modifier = Modifier.clickable {
                    onOpenAvatarSelection(activeCategory)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Row of 5 avatars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            avatars.take(5).forEach { avatar ->
                val isSelected = selectedAvatarId == avatar.id
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(avatar.thumbnailUrl ?: avatar.imageUrl)
                        .crossfade(true)
                        .build()
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) DuoraHotPink else Color(0xFFF2E6ED),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            selectedAvatarId = avatar.id
                            customPhotoUri = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painter,
                        contentDescription = avatar.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(DuoraHotPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Upload custom photo card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBF2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = DuoraHotPink,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Upload Photo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF21102E)
                    )
                    Text(
                        text = "Use your own custom picture from gallery",
                        fontSize = 12.sp,
                        color = Color(0xFF89818E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Save Changes Button
        Button(
            onClick = {
                viewModel.updateRelationshipProfile(
                    userName = displayName,
                    partnerName = profile.partnerName,
                    partnerNickname = profile.partnerNickname
                )
                viewModel.updateSelectedAvatar(selectedAvatarId) { success ->
                    if (customPhotoUri != null) {
                        viewModel.updateAvatarUri(customPhotoUri.toString())
                        try {
                            val stream = context.contentResolver.openInputStream(customPhotoUri!!)
                            val bytes = stream?.readBytes()
                            stream?.close()
                            if (bytes != null) {
                                val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                viewModel.uploadDirectDp(b64, "image/jpeg") { _, _ -> }
                            }
                        } catch (_: Exception) {}
                    }
                    Toast.makeText(context, "Profile updated ❤️", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp)
                .testTag("save_profile_button")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(DuoraHotPink, Color(0xFFFF5D88))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Changes",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}
