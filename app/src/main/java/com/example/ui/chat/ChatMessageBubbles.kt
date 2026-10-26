package com.example.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.ui.theme.DuoraCardBorder
import com.example.ui.theme.DuoraHotPink
import com.example.ui.theme.DuoraPinkGradientEnd
import com.example.ui.theme.DuoraPinkGradientStart
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.LoveGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    partnerName: String,
    isServerConnected: Boolean,
    onLongClick: (ChatMessage) -> Unit,
    onMediaClick: (ChatMessage) -> Unit,
    onQuotedClick: (quoteId: String) -> Unit
) {
    val isMe = message.isOutgoing
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    val isVoice = message.messageType == "voice" || message.voiceDurationSeconds > 0 || message.content.startsWith("[Voice Note")
    val isImage = message.messageType == "image" || !message.mediaUrl.isNullOrBlank() || message.content.startsWith("[Photo]")
    val isVideo = message.messageType == "video"
    val isFile = message.messageType == "file" || !message.fileName.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
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
                        onClick = {
                            if (isImage || isVideo) onMediaClick(message)
                        },
                        onLongClick = { onLongClick(message) }
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
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Quoted Reply preview inside message bubble
                    if (!message.replyToText.isNullOrBlank() || !message.replyToSender.isNullOrBlank()) {
                        QuotedReplyInsideBubble(
                            sender = message.replyToSender ?: partnerName,
                            text = message.replyToText ?: "",
                            isMe = isMe,
                            onClick = {
                                message.replyToId?.let { onQuotedClick(it) }
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Deleted Message
                    if (message.isDeleted) {
                        Text(
                            text = "This message was deleted",
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else when {
                        // 1. Voice Note Bubble
                        isVoice -> {
                            VoiceBubbleContent(
                                message = message,
                                isMe = isMe
                            )
                        }

                        // 2. Image Bubble
                        isImage -> {
                            ImageBubbleContent(
                                message = message,
                                isMe = isMe,
                                onClick = { onMediaClick(message) }
                            )
                        }

                        // 3. Video Bubble
                        isVideo -> {
                            VideoBubbleContent(
                                message = message,
                                isMe = isMe,
                                onClick = { onMediaClick(message) }
                            )
                        }

                        // 4. File / Document Bubble
                        isFile -> {
                            FileBubbleContent(
                                message = message,
                                isMe = isMe
                            )
                        }

                        // 5. Standard Text
                        else -> {
                            Text(
                                text = message.content,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Meta: Timestamp + Delivery ticks + Star
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.isStarred) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Starred",
                                tint = if (isMe) LoveGold else Color(0xFFFFB300),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (isMe) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isMe) {
                            DeliveryTickIndicator(
                                status = message.deliveryStatus,
                                isRead = message.isRead,
                                isServerConnected = isServerConnected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotedReplyInsideBubble(
    sender: String,
    text: String,
    isMe: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isMe) Color.White.copy(alpha = 0.2f) else DuoraSoftBlush.copy(alpha = 0.8f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (isMe) Color.White else DuoraHotPink)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = sender,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isMe) Color.White else DuoraHotPink
                )
                Text(
                    text = text,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeliveryTickIndicator(
    status: String,
    isRead: Boolean,
    isServerConnected: Boolean
) {
    when {
        isRead || status.equals("READ", ignoreCase = true) -> {
            // Read = Pink/Gold colored double check
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = LoveGold,
                modifier = Modifier.size(14.dp)
            )
        }
        status.equals("DELIVERED", ignoreCase = true) -> {
            // Delivered = Double check
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(14.dp)
            )
        }
        status.equals("SENT", ignoreCase = true) || isServerConnected -> {
            // Sent = Single check
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(14.dp)
            )
        }
        else -> {
            // Pending = Clock
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Pending",
                tint = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun VoiceBubbleContent(
    message: ChatMessage,
    isMe: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    val durationSec = if (message.voiceDurationSeconds > 0) message.voiceDurationSeconds else 8
    val durationText = remember(durationSec) {
        String.format("%d:%02d", durationSec / 60, durationSec % 60)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
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

        AudioWaveformBars(
            isPlaying = isPlaying,
            barColor = if (isMe) Color.White else DuoraHotPink,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )

        Text(
            text = durationText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
        )

        // Speed Toggle (1x, 1.5x, 2x)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isMe) Color.White.copy(alpha = 0.25f) else DuoraSoftBlush,
            modifier = Modifier.clickable {
                playbackSpeed = when (playbackSpeed) {
                    1.0f -> 1.5f
                    1.5f -> 2.0f
                    else -> 1.0f
                }
            }
        ) {
            Text(
                text = "${playbackSpeed}x",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMe) Color.White else DuoraHotPink,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun ImageBubbleContent(
    message: ChatMessage,
    isMe: Boolean,
    onClick: () -> Unit
) {
    val photoUri = message.mediaUrl ?: message.content.removePrefix("[Photo]").trim()
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DuoraSoftBlush)
                .clickable(onClick = onClick)
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
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Heart reaction badge
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

        if (message.content.isNotBlank() && !message.content.startsWith("[Photo]")) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.content,
                fontSize = 13.sp,
                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VideoBubbleContent(
    message: ChatMessage,
    isMe: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!message.mediaUrl.isNullOrBlank()) {
            AsyncImage(
                model = message.mediaUrl,
                contentDescription = "Video preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(DuoraHotPink),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play Video",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("0:24", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FileBubbleContent(
    message: ChatMessage,
    isMe: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isMe) Color.White.copy(alpha = 0.2f) else DuoraSoftBlush,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isMe) Color.White else DuoraHotPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = if (isMe) DuoraHotPink else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = message.fileName ?: message.content,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message.fileSize ?: "2.4 MB",
                    fontSize = 11.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
