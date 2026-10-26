package com.example.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.DuoraHotPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessageOptionsSheet(
    message: ChatMessage,
    onDismissRequest: () -> Unit,
    onReply: (ChatMessage) -> Unit,
    onCopy: (ChatMessage) -> Unit,
    onForward: (ChatMessage) -> Unit,
    onToggleStar: (ChatMessage) -> Unit,
    onDelete: (ChatMessage) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Message?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this message for both of you?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(message)
                        onDismissRequest()
                    }
                ) {
                    Text("Delete", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            MessageOptionRow(
                icon = Icons.AutoMirrored.Filled.Reply,
                title = "Reply",
                tint = DuoraHotPink,
                onClick = {
                    onReply(message)
                    onDismissRequest()
                }
            )

            MessageOptionRow(
                icon = Icons.Default.ContentCopy,
                title = "Copy Text",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    onCopy(message)
                    onDismissRequest()
                }
            )

            MessageOptionRow(
                icon = if (message.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                title = if (message.isStarred) "Unstar Message" else "Star Message",
                tint = if (message.isStarred) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface,
                onClick = {
                    onToggleStar(message)
                    onDismissRequest()
                }
            )

            MessageOptionRow(
                icon = Icons.Default.Share,
                title = "Forward",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    onForward(message)
                    onDismissRequest()
                }
            )

            MessageOptionRow(
                icon = Icons.Default.Delete,
                title = "Delete Message",
                tint = Color(0xFFE91E63),
                onClick = {
                    showDeleteConfirm = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MessageOptionRow(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
