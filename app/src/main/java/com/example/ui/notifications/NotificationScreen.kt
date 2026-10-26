package com.example.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoraCallBg
import com.example.ui.theme.DuoraCallLogsBg
import com.example.ui.theme.DuoraChatBg
import com.example.ui.theme.DuoraNotesBg
import com.example.ui.theme.DuoraPartnerBg
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryText

data class DuoraNotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color,
    val isUnread: Boolean = false
)

/**
 * Notification Panel ModalBottomSheet matching Mockup 2 in the design reference:
 * - Title "Notifications"
 * - Action "Mark all read"
 * - List of notifications:
 *   1. New Message ("Your partner sent a message", 2m ago)
 *   2. Partner Online ("Your partner is now online", 15m ago)
 *   3. New Memory ("You added a new moment", 1h ago)
 *   4. App Update ("New features are available", 2h ago)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPanelSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onItemClick: (DuoraNotificationItem) -> Unit = {}
) {
    val notificationItems = remember {
        mutableStateListOf(
            DuoraNotificationItem(
                id = "1",
                title = "New Message",
                description = "Your partner sent a message",
                timeAgo = "2m ago",
                icon = Icons.Default.ChatBubble,
                iconBgColor = DuoraChatBg,
                iconTint = DuoraPrimaryPink,
                isUnread = true
            ),
            DuoraNotificationItem(
                id = "2",
                title = "Partner Online",
                description = "Your partner is now online",
                timeAgo = "15m ago",
                icon = Icons.Default.Favorite,
                iconBgColor = DuoraPartnerBg,
                iconTint = DuoraPrimaryPink,
                isUnread = false
            ),
            DuoraNotificationItem(
                id = "3",
                title = "New Memory",
                description = "You added a new moment",
                timeAgo = "1h ago",
                icon = Icons.Default.PhotoLibrary,
                iconBgColor = DuoraNotesBg,
                iconTint = Color(0xFFF59E0B),
                isUnread = false
            ),
            DuoraNotificationItem(
                id = "4",
                title = "App Update",
                description = "New features are available",
                timeAgo = "2h ago",
                icon = Icons.Default.SystemUpdate,
                iconBgColor = DuoraCallLogsBg,
                iconTint = Color(0xFF8B5CF6),
                isUnread = false
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("notification_panel_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: "Notifications" and "Mark all read"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DuoraPrimaryText,
                    modifier = Modifier.testTag("notifications_title")
                )

                Text(
                    text = "Mark all read",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DuoraPrimaryPink,
                    modifier = Modifier
                        .clickable {
                            for (i in notificationItems.indices) {
                                notificationItems[i] = notificationItems[i].copy(isUnread = false)
                            }
                        }
                        .padding(4.dp)
                        .testTag("mark_all_read_button")
                )
            }

            // Notification List Items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notificationItems) { item ->
                    NotificationRowItem(
                        item = item,
                        onClick = {
                            onItemClick(item)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRowItem(
    item: DuoraNotificationItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (item.isUnread) Color(0xFFFFF6FA) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isUnread) Color(0xFFFFDDEB) else Color(0xFFF3E8EE)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored icon box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Description
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoraPrimaryText
                    )

                    Text(
                        text = item.timeAgo,
                        fontSize = 11.sp,
                        color = DuoraSecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = DuoraSecondaryText,
                    maxLines = 1
                )
            }

            // Unread Dot Indicator
            if (item.isUnread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(DuoraPrimaryPink)
                )
            }
        }
    }
}
