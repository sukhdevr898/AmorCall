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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.R
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.theme.LoveRose
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calling.CallNotificationHelper
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.network.ServerConnectionState
import com.example.ui.chat.ChatAttachmentSheet
import com.example.ui.chat.ChatAttachmentType
import com.example.ui.chat.ChatBubbleItem
import com.example.ui.chat.ChatMediaViewerDialog
import com.example.ui.chat.ChatMessageOptionsSheet
import com.example.ui.chat.ChatReplyPreview
import com.example.ui.chat.ChatVoiceRecorder
import com.example.ui.chat.ReplyContext
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPinkGradientEnd
import com.example.ui.theme.DuoraPinkGradientStart
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.LoveAccentGreen
import com.example.viewmodel.LoveCallViewModel
import kotlinx.coroutines.launch

enum class ChatSubScreen {
    LIST,
    PARTNER_CHAT,
    ADMIN_NOTICES
}

@Composable
fun ChatTab(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
    onBack: () -> Unit = {}
) {
    val isIndividualChatOpen by viewModel.isIndividualChatOpen.collectAsState()
    var currentSubScreen by remember { mutableStateOf(ChatSubScreen.LIST) }

    LaunchedEffect(isIndividualChatOpen) {
        if (isIndividualChatOpen) {
            currentSubScreen = ChatSubScreen.PARTNER_CHAT
        }
    }

    when (currentSubScreen) {
        ChatSubScreen.PARTNER_CHAT -> {
            IndividualChatView(
                viewModel = viewModel,
                onRequestPermissions = onRequestPermissions,
                onBack = {
                    viewModel.closeIndividualChat()
                    currentSubScreen = ChatSubScreen.LIST
                }
            )
        }
        ChatSubScreen.ADMIN_NOTICES -> {
            AdminNoticesChatView(
                viewModel = viewModel,
                onBack = { currentSubScreen = ChatSubScreen.LIST }
            )
        }
        ChatSubScreen.LIST -> {
            ChatListView(
                viewModel = viewModel,
                onRequestPermissions = onRequestPermissions,
                onOpenConversation = {
                    viewModel.openIndividualChat()
                    currentSubScreen = ChatSubScreen.PARTNER_CHAT
                },
                onOpenAdminNotices = {
                    currentSubScreen = ChatSubScreen.ADMIN_NOTICES
                },
                onBack = onBack
            )
        }
    }
}

@Composable
fun ChatListView(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
    onOpenConversation: () -> Unit,
    onOpenAdminNotices: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val profile by viewModel.profile.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isPartnerTyping by viewModel.isPartnerTyping.collectAsState()
    val serverState by viewModel.serverConnectionState.collectAsState()
    val unreadChatCount by viewModel.unreadChatCount.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val isConnected = serverState == ServerConnectionState.CONNECTED
    val isPartnerOnline = isConnected && profile.isPaired

    val lastMessage = chatMessages.lastOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Chats",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPartnerOnline) LoveAccentGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isPartnerOnline) LoveAccentGreen else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPartnerOnline) "Partner Online" else "Private Space",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPartnerOnline) LoveAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clean search bar for message filtering
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_search_input"),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search messages...",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Partner Conversation Item
        val partnerNameDisplay = profile.partnerNickname.ifBlank { profile.partnerName.ifBlank { "My Partner" } }
        val matchesSearch = searchQuery.isBlank() ||
                partnerNameDisplay.contains(searchQuery, ignoreCase = true) ||
                (lastMessage?.content?.contains(searchQuery, ignoreCase = true) == true)

        if (matchesSearch) {
            Surface(
                onClick = onOpenConversation,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DuoraCardBorder.copy(alpha = 0.5f)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("partner_conversation_item")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with online indicator
                    val partnerAvatarToDisplay = profile.partnerAvatarUrl
                        ?: viewModel.resolveAvatarThumbnailUrl(profile.partnerAvatarId)

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)))
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!partnerAvatarToDisplay.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(partnerAvatarToDisplay),
                                contentDescription = partnerNameDisplay,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = partnerNameDisplay.take(1).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoraHotPink
                            )
                        }

                        if (isPartnerOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(LoveAccentGreen)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Center Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(
                                    text = partnerNameDisplay,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                if (profile.partnerIsVerified || profile.partnerBlueTick) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_blue_tick),
                                        contentDescription = "Verified Partner",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Timestamp
                            if (lastMessage != null) {
                                Text(
                                    text = lastMessage.timestampFormatted,
                                    fontSize = 11.sp,
                                    color = if (unreadChatCount > 0) DuoraHotPink else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (unreadChatCount > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isPartnerTyping) {
                                    Text(
                                        text = "typing... ❤️",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DuoraHotPink
                                    )
                                } else if (lastMessage != null) {
                                    if (lastMessage.isOutgoing) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Delivered",
                                            tint = if (lastMessage.isRead) DuoraHotPink else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "You: ${lastMessage.content}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    } else {
                                        Text(
                                            text = lastMessage.content,
                                            fontSize = 13.sp,
                                            color = if (unreadChatCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (unreadChatCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Tap to open your love chat ❤️",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Unread Badge (ONLY if unreadChatCount > 0)
                            if (unreadChatCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .height(20.dp)
                                        .widthIn(min = 20.dp)
                                        .clip(CircleShape)
                                        .background(DuoraHotPink)
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadChatCount > 99) "99+" else unreadChatCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2nd Option: Official Duora Admin Messages (Read-only channel, user cannot reply/call)
            val adminNotices by viewModel.adminNotices.collectAsState()
            val latestNotice = adminNotices.firstOrNull()

            Surface(
                onClick = onOpenAdminNotices,
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("admin_notices_conversation_item")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5))
                            .border(1.5.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_admin_green_tick),
                            contentDescription = "Admin Official",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Duora Admin",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.ic_admin_green_tick),
                                    contentDescription = "Admin Verified",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (latestNotice != null) {
                                Text(
                                    text = "Official",
                                    fontSize = 10.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = latestNotice?.title ?: "Official notices & alerts • Read only",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )

                            if (adminNotices.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${adminNotices.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Search result empty
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No messages found matching \"$searchQuery\"",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun IndividualChatView(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit,
    onBack: () -> Unit
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

    // Chat Interactive State
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var isVoiceRecording by remember { mutableStateOf(false) }
    var replyContext by remember { mutableStateOf<ReplyContext?>(null) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
    var selectedMediaForViewer by remember { mutableStateOf<ChatMessage?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendChatMessage(
                text = "Photo shared ❤️",
                type = "image",
                mediaUrl = uri.toString(),
                replyToId = replyContext?.id,
                replyToSender = replyContext?.sender,
                replyToText = replyContext?.text,
                replyToType = replyContext?.type
            )
            replyContext = null
            scope.launch { listState.scrollToItem(0) }
            Toast.makeText(context, "Photo shared with ${profile.partnerName} ❤️", Toast.LENGTH_SHORT).show()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendChatMessage(
                text = "Video shared 🎥",
                type = "video",
                mediaUrl = uri.toString(),
                replyToId = replyContext?.id,
                replyToSender = replyContext?.sender,
                replyToText = replyContext?.text,
                replyToType = replyContext?.type
            )
            replyContext = null
            scope.launch { listState.scrollToItem(0) }
            Toast.makeText(context, "Video shared with ${profile.partnerName} ❤️", Toast.LENGTH_SHORT).show()
        }
    }

    // Mark messages as read when opening ChatTab and clear notification
    LaunchedEffect(Unit) {
        viewModel.markChatAsRead()
        viewModel.requestChatHistory()
        CallNotificationHelper.clearMessageNotification(context)
    }

    // Auto-scroll when sending or receiving only if close to bottom
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            if (listState.firstVisibleItemIndex <= 1) {
                listState.animateScrollToItem(0)
            }
        }
    }

    val isConnected = serverState == ServerConnectionState.CONNECTED
    val isPartnerOnline = isConnected && profile.isPaired

    Scaffold(
        topBar = {
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
                        val partnerAvatarToDisplay = profile.partnerAvatarUrl
                            ?: viewModel.resolveAvatarThumbnailUrl(profile.partnerAvatarId)

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)))
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!partnerAvatarToDisplay.isNullOrBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(partnerAvatarToDisplay),
                                    contentDescription = profile.partnerName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = profile.partnerName.take(1).ifBlank { "P" }.uppercase(),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoraHotPink
                                )
                            }
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
                                if (profile.partnerIsVerified || profile.partnerBlueTick) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_blue_tick),
                                        contentDescription = "Verified Partner",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                                    text = { Text("Mark All As Read") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.markChatAsRead()
                                        Toast.makeText(context, "All messages marked as read", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Floating Quoted Reply preview banner
                replyContext?.let { reply ->
                    ChatReplyPreview(
                        replyContext = reply,
                        onDismiss = { replyContext = null }
                    )
                }

                if (isVoiceRecording) {
                    ChatVoiceRecorder(
                        onCancel = { isVoiceRecording = false },
                        onSendVoiceNote = { durationSec ->
                            isVoiceRecording = false
                            viewModel.sendChatMessage(
                                text = "Voice Note (${durationSec}s)",
                                type = "voice",
                                voiceDurationSec = durationSec,
                                replyToId = replyContext?.id,
                                replyToSender = replyContext?.sender,
                                replyToText = replyContext?.text,
                                replyToType = replyContext?.type
                            )
                            replyContext = null
                            scope.launch { listState.scrollToItem(0) }
                            Toast.makeText(context, "Voice note sent ❤️", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    ChatComposerBar(
                        partnerName = profile.partnerName.ifBlank { "Partner" },
                        onSendMessage = { text ->
                            viewModel.sendChatMessage(
                                text = text,
                                replyToId = replyContext?.id,
                                replyToSender = replyContext?.sender,
                                replyToText = replyContext?.text,
                                replyToType = replyContext?.type
                            )
                            replyContext = null
                            viewModel.setChatTyping(false)
                            scope.launch { listState.scrollToItem(0) }
                        },
                        onStartVoiceRecording = {
                            isVoiceRecording = true
                        },
                        onOpenAttachmentSheet = {
                            showAttachmentSheet = true
                        },
                        onTypingStateChange = { isTyping ->
                            viewModel.setChatTyping(isTyping)
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ChatRomanticPattern()

            if (chatMessages.isEmpty()) {
                ChatEmptyState(partnerName = profile.partnerName)
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
                            isServerConnected = isConnected,
                            onLongClick = { msg ->
                                selectedMessageForOptions = msg
                            },
                            onMediaClick = { msg ->
                                selectedMediaForViewer = msg
                            },
                            onQuotedClick = { quoteId ->
                                val targetIndex = chatMessages.asReversed().indexOfFirst { it.id == quoteId }
                                if (targetIndex >= 0) {
                                    scope.launch { listState.animateScrollToItem(targetIndex) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Attachment Sheet
    if (showAttachmentSheet) {
        ChatAttachmentSheet(
            onDismissRequest = { showAttachmentSheet = false },
            onSelectAttachment = { type ->
                when (type) {
                    ChatAttachmentType.GALLERY -> {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    ChatAttachmentType.VIDEO -> {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                    ChatAttachmentType.VOICE_NOTE -> {
                        isVoiceRecording = true
                    }
                    ChatAttachmentType.LOCATION -> {
                        viewModel.sendChatMessage("📍 Our Favorite Romantic Spot")
                        Toast.makeText(context, "Location pin sent ❤️", Toast.LENGTH_SHORT).show()
                    }
                    ChatAttachmentType.STICKER -> {
                        viewModel.sendChatMessage("💖 You have my entire heart forever 🌹💍")
                    }
                    ChatAttachmentType.AUDIO -> {
                        viewModel.sendChatMessage("🎵 Playing our love song for you")
                    }
                    ChatAttachmentType.DOCUMENT -> {
                        viewModel.sendChatMessage("📄 Love_Letter_For_You.pdf", type = "file", fileName = "Love_Letter_For_You.pdf", fileSize = "1.2 MB")
                    }
                    ChatAttachmentType.CAMERA -> {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    ChatAttachmentType.CONTACT -> {
                        viewModel.sendChatMessage("👤 Contact: ${profile.partnerName} (${profile.partnerNickname})")
                    }
                }
            }
        )
    }

    // Message Long-Press Action Sheet
    selectedMessageForOptions?.let { msg ->
        ChatMessageOptionsSheet(
            message = msg,
            onDismissRequest = { selectedMessageForOptions = null },
            onReply = { messageToReply ->
                replyContext = ReplyContext(
                    id = messageToReply.id,
                    sender = if (messageToReply.isOutgoing) "You" else profile.partnerName.ifBlank { "Partner" },
                    text = messageToReply.content,
                    type = messageToReply.messageType
                )
            },
            onCopy = { messageToCopy ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Duora Message", messageToCopy.content)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            onForward = {
                Toast.makeText(context, "Message forwarded ❤️", Toast.LENGTH_SHORT).show()
            },
            onToggleStar = { messageToStar ->
                viewModel.toggleStarMessage(messageToStar.id)
            },
            onDelete = { messageToDelete ->
                viewModel.deleteChatMessage(messageToDelete.id)
                Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Media Viewer Dialog
    selectedMediaForViewer?.let { mediaMsg ->
        ChatMediaViewerDialog(
            mediaUrl = mediaMsg.mediaUrl ?: mediaMsg.content.removePrefix("[Photo]").trim(),
            mediaType = mediaMsg.messageType,
            caption = if (mediaMsg.content.startsWith("[Photo]")) null else mediaMsg.content,
            onDismissRequest = { selectedMediaForViewer = null }
        )
    }
}

@Composable
private fun ChatComposerBar(
    partnerName: String,
    onSendMessage: (String) -> Unit,
    onStartVoiceRecording: () -> Unit,
    onOpenAttachmentSheet: () -> Unit,
    onTypingStateChange: (Boolean) -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    val isInputEmpty = messageInput.trim().isEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
            // Quick reply chips row
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

            // Rounded pill composer row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (messageInput.isEmpty()) {
                                Text(
                                    text = "Type a sweet message...",
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

                        // Attachment button
                        IconButton(
                            onClick = onOpenAttachmentSheet,
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

                // Send or Voice Note Button
                IconButton(
                    onClick = {
                        val clean = messageInput.trim()
                        if (clean.isNotBlank()) {
                            onSendMessage(clean)
                            messageInput = ""
                        } else {
                            onStartVoiceRecording()
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd)))
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

@Composable
private fun ChatEmptyState(partnerName: String) {
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
                .background(Brush.linearGradient(listOf(DuoraPinkGradientStart, DuoraPinkGradientEnd))),
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
            text = "Say something sweet to ${partnerName.ifBlank { "your partner" }}.\nMessages are protected with end-to-end privacy for only you two.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ChatRomanticPattern() {
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
fun AdminNoticesChatView(
    viewModel: LoveCallViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val adminNotices by viewModel.adminNotices.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("admin_chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5))
                            .border(1.5.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_admin_green_tick),
                            contentDescription = "Admin Verified",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Duora Admin",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_admin_green_tick),
                                contentDescription = "Admin Verified",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Official Announcements • Read Only",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Read-Only Banner at bottom (No input, no calls)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Replies and calls are disabled in this official admin channel.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Informational header notice card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_admin_green_tick),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Direct administrative bulletins, security alerts, and service updates from the Duora Operations Team.",
                        fontSize = 12.sp,
                        color = Color(0xFF065F46),
                        lineHeight = 16.sp
                    )
                }
            }

            if (adminNotices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_admin_green_tick),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Official Notices Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Any administrative announcements will appear here.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(adminNotices, key = { it.id }) { notice ->
                        val isCritical = notice.isAlert
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCritical) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCritical) LoveRose.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_admin_green_tick),
                                            contentDescription = "Admin Notice",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isCritical) "SECURITY ALERT" else "OFFICIAL BULLETIN",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCritical) LoveRose else Color(0xFF10B981)
                                        )
                                    }

                                    val dateStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                        .format(java.util.Date(notice.timestamp))
                                    Text(
                                        text = dateStr,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = notice.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notice.message,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
