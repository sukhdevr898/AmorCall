package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calling.CallNotificationHelper
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.network.ServerConnectionState
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraPinkGradientEnd
import com.example.ui.theme.DuoraPinkGradientStart
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.LoveAccentGreen
import com.example.ui.theme.LoveGold
import com.example.viewmodel.LoveCallViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatTab(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
    onBack: () -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isPartnerTyping by viewModel.isPartnerTyping.collectAsState()
    val serverState by viewModel.serverConnectionState.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendChatMessage("[Photo] $uri")
            scope.launch { listState.scrollToItem(0) }
            Toast.makeText(context, "Photo shared with ${profile.partnerName} ❤️", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.requestChatHistory()
        CallNotificationHelper.clearMessageNotification(context)
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val isConnected = serverState == ServerConnectionState.CONNECTED
    val isPartnerOnline = isConnected && profile.isPaired

    Scaffold(
        topBar = {
            // Pinned Chat Top Bar — matches Sheet 3 design
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, DuoraCardBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Avatar with green online dot
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)
                                    )
                                )
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.partnerName.take(1).ifBlank { "P" }.uppercase(),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraHotPink
                            )
                            if (isPartnerOnline) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(LoveAccentGreen)
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.partnerName.ifBlank { "My Love ❤️" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isPartnerOnline) "Online • Private Space" else "Private Space",
                                fontSize = 11.sp,
                                color = if (isPartnerOnline) LoveAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Call Actions & Overflow Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onRequestPermissions {
                                    viewModel.startOutgoingCall(CallType.VOICE)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DuoraSoftBlush)
                                .testTag("chat_voice_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice Call",
                                tint = DuoraHotPink,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                onRequestPermissions {
                                    viewModel.startOutgoingCall(CallType.VIDEO)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DuoraSoftBlush)
                                .testTag("chat_video_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = DuoraHotPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Send Sweet Heart ❤️") },
                                    onClick = {
                                        viewModel.sendChatMessage("❤️")
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Photo") },
                                    onClick = {
                                        showMenu = false
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear Chat View") },
                                    onClick = {
                                        showMenu = false
                                        Toast.makeText(context, "Messages stored securely", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Composer anchored at bottom with smooth imePadding
            ChatBottomComposer(
                partnerName = profile.partnerName.ifBlank { "Partner" },
                onSendMessage = { text ->
                    viewModel.sendChatMessage(text)
                    viewModel.setChatTyping(false)
                    scope.launch { listState.scrollToItem(0) }
                },
                onSendVoiceMessage = {
                    viewModel.sendChatMessage("[Voice Note 0:08]")
                    scope.launch { listState.scrollToItem(0) }
                    Toast.makeText(context, "Voice note sent ❤️", Toast.LENGTH_SHORT).show()
                },
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTypingStateChange = { isTyping ->
                    viewModel.setChatTyping(isTyping)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // Background with subtle romantic floating heart pattern
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            RomanticBackgroundPattern()

            if (chatMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Your Private Conversation",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Say something sweet to ${profile.partnerName.ifBlank { "your partner" }}.\nMessages are protected with end-to-end privacy for only you two.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "typing_indicator") {
                        AnimatedVisibility(
                            visible = isPartnerTyping,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder)
                                ) {
                                    Text(
                                        text = "${profile.partnerName.ifBlank { "Partner" }} is typing... ❤️",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DuoraHotPink,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(
                        items = chatMessages.asReversed(),
                        key = { it.id }
                    ) { message ->
                        ChatBubbleItem(
                            message = message,
                            partnerName = profile.partnerName,
                            isServerConnected = isConnected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RomanticBackgroundPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pinkDot = Color(0xFFFF2D6C).copy(alpha = 0.04f)
        val step = 48.dp.toPx()
        val width = size.width
        val height = size.height

        var y = 0f
        while (y < height) {
            var x = 0f
            while (x < width) {
                drawCircle(
                    color = pinkDot,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
                x += step
            }
            y += step
        }
    }
}

@Composable
private fun ChatBottomComposer(
    partnerName: String,
    onSendMessage: (String) -> Unit,
    onSendVoiceMessage: () -> Unit,
    onPickPhoto: () -> Unit,
    onTypingStateChange: (Boolean) -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    val isInputEmpty = messageInput.trim().isEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, DuoraCardBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Quick reply chips row — Sheet 3
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickPrompts = listOf("❤️", "😘", "Miss you ✨", "Call me 💕", "Love you endlessly 🌹", "Thinking of you 🥰")
                items(quickPrompts) { prompt ->
                    Surface(
                        onClick = { onSendMessage(prompt) },
                        shape = RoundedCornerShape(16.dp),
                        color = DuoraSoftBlush,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoraHotPink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rounded pill composer row with emoji, input, paperclip attachment, and circular send/mic button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill input box
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Smiley Icon
                        IconButton(
                            onClick = { messageInput += " ❤️" },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji",
                                tint = DuoraHotPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Message Text Field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (messageInput.isEmpty()) {
                                Text(
                                    text = "Type a message...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = messageInput,
                                onValueChange = {
                                    val wasEmpty = messageInput.isBlank()
                                    val isEmptyNow = it.isBlank()
                                    messageInput = it
                                    if (wasEmpty != isEmptyNow) {
                                        onTypingStateChange(!isEmptyNow)
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(DuoraHotPink),
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        val clean = messageInput.trim()
                                        if (clean.isNotBlank()) {
                                            onSendMessage(clean)
                                            messageInput = ""
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_input_field")
                            )
                        }

                        // Attachment Paperclip Icon
                        IconButton(
                            onClick = onPickPhoto,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Floating circular pink gradient send or voice mic button
                IconButton(
                    onClick = {
                        val clean = messageInput.trim()
                        if (clean.isNotBlank()) {
                            onSendMessage(clean)
                            messageInput = ""
                        } else {
                            onSendVoiceMessage()
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)
                            )
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = if (!isInputEmpty) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (!isInputEmpty) "Send Message" else "Voice Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    partnerName: String,
    isServerConnected: Boolean
) {
    val isMe = message.isOutgoing
    val context = LocalContext.current
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    val isVoiceMessage = message.content.startsWith("[Voice Note")
    val isPhotoMessage = message.content.startsWith("[Photo]")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            when {
                // 1. Voice Note Bubble with Audio Waveform
                isVoiceMessage -> {
                    VoiceMessageBubble(
                        isMe = isMe,
                        time = formattedTime,
                        isRead = message.isRead,
                        isServerConnected = isServerConnected
                    )
                }
                // 2. Photo Message Bubble with Heart Reaction Badge
                isPhotoMessage -> {
                    PhotoMessageBubble(
                        isMe = isMe,
                        photoUri = message.content.removePrefix("[Photo]").trim(),
                        time = formattedTime,
                        isRead = message.isRead,
                        isServerConnected = isServerConnected
                    )
                }
                // 3. Regular Text Bubble
                else -> {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isMe) 20.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 20.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) Color.Transparent else MaterialTheme.colorScheme.surface
                        ),
                        border = if (!isMe) androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder) else null,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Duora Message", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                            .then(
                                if (isMe) Modifier.background(
                                    brush = Brush.horizontalGradient(
                                        listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)
                                    ),
                                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                                ) else Modifier
                            )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                            Text(
                                text = message.content,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formattedTime,
                                    fontSize = 10.sp,
                                    color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isMe) {
                                    val stateIcon = when {
                                        message.isRead -> Icons.Default.DoneAll
                                        isServerConnected -> Icons.Default.Check
                                        else -> Icons.Default.Schedule
                                    }
                                    Icon(
                                        imageVector = stateIcon,
                                        contentDescription = if (message.isRead) "Read" else "Sent",
                                        tint = if (message.isRead) LoveGold else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceMessageBubble(
    isMe: Boolean,
    time: String,
    isRead: Boolean,
    isServerConnected: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp, 20.dp, if (isMe) 4.dp else 20.dp, if (isMe) 20.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        border = if (!isMe) androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder) else null,
        modifier = Modifier.then(
            if (isMe) Modifier.background(
                brush = Brush.horizontalGradient(listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)),
                shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
            ) else Modifier
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play / Pause Circle
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isMe) Color.White.copy(alpha = 0.25f) else DuoraSoftBlush)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Voice Note",
                        tint = if (isMe) Color.White else DuoraHotPink,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Audio Waveform Visualizer Graphic
                AudioWaveformBars(
                    isPlaying = isPlaying,
                    barColor = if (isMe) Color.White else DuoraHotPink,
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                )

                Text(
                    text = "0:08",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isMe) {
                    Icon(
                        imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isRead) LoveGold else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioWaveformBars(
    isPlaying: Boolean,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val barHeights = listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 1.0f, 0.6f, 0.75f, 0.45f, 0.85f, 0.6f, 0.35f, 0.7f, 0.9f, 0.5f)

    Canvas(modifier = modifier) {
        val totalBars = barHeights.size
        val barWidth = 3.dp.toPx()
        val spacing = (size.width - (totalBars * barWidth)) / (totalBars - 1)
        val maxHeight = size.height

        barHeights.forEachIndexed { index, ratio ->
            val h = maxHeight * ratio
            val x = index * (barWidth + spacing)
            val y = (maxHeight - h) / 2f
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun PhotoMessageBubble(
    isMe: Boolean,
    photoUri: String,
    time: String,
    isRead: Boolean,
    isServerConnected: Boolean
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, DuoraCardBorder),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.widthIn(max = 240.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DuoraSoftBlush)
            ) {
                if (photoUri.isNotBlank()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Photo from partner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = DuoraHotPink,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Heart reaction badge overlay
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("❤️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DuoraHotPink)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isRead) Icons.Default.DoneAll else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isRead) LoveGold else DuoraHotPink,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
