package com.example.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DuoraCallBg
import com.example.ui.theme.DuoraCallLogsBg
import com.example.ui.theme.DuoraChatBg
import com.example.ui.theme.DuoraNotesBg
import com.example.ui.theme.DuoraPartnerBg
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraServerBg
import com.example.ui.theme.DuoraSuccess

/**
 * Responsive 3-column Feature Grid matching the Duora Home Screen design reference:
 * Row 1: Chat (pink), Call (green), Notes (yellow)
 * Row 2: Call Logs (purple), Partner (pink heart), Server (blue layers)
 */
@Composable
fun FeatureGrid(
    unreadChatCount: Int = 0,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onNotesClick: () -> Unit,
    onCallLogsClick: () -> Unit,
    onPartnerClick: () -> Unit,
    onServerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Chat, Call, Notes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                title = "Chat",
                icon = Icons.Default.ChatBubble,
                iconTint = DuoraPrimaryPink,
                iconBgColor = DuoraChatBg,
                badgeCount = unreadChatCount,
                testTag = "feature_card_chat",
                onClick = onChatClick,
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Call",
                icon = Icons.Default.Call,
                iconTint = DuoraSuccess,
                iconBgColor = DuoraCallBg,
                badgeCount = 0,
                testTag = "feature_card_call",
                onClick = onCallClick,
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Notes",
                icon = Icons.Default.Description,
                iconTint = Color(0xFFF59E0B),
                iconBgColor = DuoraNotesBg,
                badgeCount = 0,
                testTag = "feature_card_notes",
                onClick = onNotesClick,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Call Logs, Partner, Server
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                title = "Call Logs",
                icon = Icons.Default.History,
                iconTint = Color(0xFF8B5CF6),
                iconBgColor = DuoraCallLogsBg,
                badgeCount = 0,
                testTag = "feature_card_call_logs",
                onClick = onCallLogsClick,
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Partner",
                icon = Icons.Default.Favorite,
                iconTint = DuoraPrimaryPink,
                iconBgColor = DuoraPartnerBg,
                badgeCount = 0,
                testTag = "feature_card_partner",
                onClick = onPartnerClick,
                modifier = Modifier.weight(1f)
            )

            FeatureCard(
                title = "Server",
                icon = Icons.Default.Layers,
                iconTint = Color(0xFF3B82F6),
                iconBgColor = DuoraServerBg,
                badgeCount = 0,
                testTag = "feature_card_server",
                onClick = onServerClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
