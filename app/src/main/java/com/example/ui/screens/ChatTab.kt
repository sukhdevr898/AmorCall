package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calling.CallNotificationHelper
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.network.ServerConnectionState
import com.example.ui.theme.DuoraPaleSky
import com.example.ui.theme.DuoraRaspberry
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

    LaunchedEffect(Unit) {
        viewModel.requestChatHistory()
        CallNotificationHelper.clearMessageNotification(context)
    }

    // Scroll to latest (which is index 0 in reverseLayout) when messages change
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val isConnected = serverState == ServerConnectionState.CONNECTED
    val isPartnerOnline = isConnected && profile.isPaired

    Scaffold(
        topBar = {
            // Pinned Chat Top Bar - fixed to top of screen, never pushed off by IME keyboard
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(DuoraPaleSky.copy(alpha = 0.5f))
                                .border(1.5.dp, DuoraRaspberry, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.partnerName.take(1).ifBlank { "P" }.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraRaspberry
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = profile.partnerName.ifBlank { "Partner" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isPartnerOnline) LoveAccentGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isPartnerOnline) "Online" else "Private Space",
                                    fontSize = 11.sp,
                                    color = if (isPartnerOnline) LoveAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Call quick actions from Chat
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                onRequestPermissions {
                                    viewModel.startOutgoingCall(CallType.VOICE)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                .testTag("chat_voice_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Voice Call",
                                tint = DuoraRaspberry,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                onRequestPermissions {
                                    viewModel.startOutgoingCall(CallType.VIDEO)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                .testTag("chat_video_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = DuoraRaspberry,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Isolated Chat Composer - typing never recomposes the messages list or tab
            ChatBottomComposer(
                partnerName = profile.partnerName.ifBlank { "Partner" },
                onSendMessage = { text ->
                    viewModel.sendChatMessage(text)
                    viewModel.setChatTyping(false)
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                },
                onTypingStateChange = { isTyping ->
                    viewModel.setChatTyping(isTyping)
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DuoraPaleSky.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = DuoraRaspberry,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Your Private Conversation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Say something sweet to ${profile.partnerName.ifBlank { "your partner" }}.\nMessages are stored securely for only you two.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // reverseLayout = true ensures recent messages stay pinned at the bottom above keyboard smoothly
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Typing indicator is at the bottom (index 0 in reverse)
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
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${profile.partnerName.ifBlank { "Partner" }} is typing... ❤️",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DuoraRaspberry,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Reversed items: most recent is first in reverseLayout
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
private fun ChatBottomComposer(
    partnerName: String,
    onSendMessage: (String) -> Unit,
    onTypingStateChange: (Boolean) -> Unit
) {
    var messageInput by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Quick Emotion / Heart Prompts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickPrompts = listOf("❤️", "😘", "Miss you ✨", "Call me 💕")
                quickPrompts.forEach { prompt ->
                    Surface(
                        onClick = {
                            onSendMessage(prompt)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = DuoraPaleSky.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, DuoraRaspberry.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = DuoraRaspberry,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Text Field and Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = {
                        val wasBlank = messageInput.isBlank()
                        val isBlankNow = it.isBlank()
                        messageInput = it
                        if (wasBlank != isBlankNow) {
                            onTypingStateChange(!isBlankNow)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Message $partnerName...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DuoraRaspberry,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
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
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val clean = messageInput.trim()
                        if (clean.isNotBlank()) {
                            onSendMessage(clean)
                            messageInput = ""
                        }
                    },
                    enabled = messageInput.isNotBlank(),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (messageInput.isNotBlank()) DuoraRaspberry else MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (messageInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMe) DuoraRaspberry else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (!isMe) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Duora Message", message.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                    Text(
                        text = message.content,
                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (isMe) DuoraPaleSky.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isMe) {
                            // Truthful delivery state icon
                            val stateIcon = when {
                                message.isRead -> Icons.Default.DoneAll
                                isServerConnected -> Icons.Default.Check
                                else -> Icons.Default.Schedule
                            }
                            val stateTint = when {
                                message.isRead -> LoveGold
                                else -> DuoraPaleSky.copy(alpha = 0.85f)
                            }
                            Icon(
                                imageVector = stateIcon,
                                contentDescription = if (message.isRead) "Read" else "Sent",
                                tint = stateTint,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
