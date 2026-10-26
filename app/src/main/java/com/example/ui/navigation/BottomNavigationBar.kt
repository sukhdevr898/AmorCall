package com.example.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraSecondaryText

enum class DuoraNavDestination {
    HOME,
    CHAT,
    CALL,
    NOTES,
    SETTINGS
}

/**
 * Bottom Navigation Bar matching Duora Home Screen design reference:
 * - 5 Destinations: Home, Chat, Call, Notes, Settings
 * - Active tab highlighted in #FF2D8D with active indicator line underneath
 * - Unread message count badge on Chat
 * - Smooth scale and tint animation on destination change
 */
@Composable
fun DuoraBottomNavigationBar(
    currentDestination: DuoraNavDestination,
    unreadChatCount: Int = 0,
    onDestinationSelected: (DuoraNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF6E7EE)),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("duora_bottom_navigation_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                destination = DuoraNavDestination.HOME,
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentDestination == DuoraNavDestination.HOME,
                badgeCount = 0,
                onClick = { onDestinationSelected(DuoraNavDestination.HOME) }
            )

            // 2. Chat
            BottomNavItem(
                destination = DuoraNavDestination.CHAT,
                label = "Chat",
                icon = Icons.Default.ChatBubble,
                isSelected = currentDestination == DuoraNavDestination.CHAT,
                badgeCount = unreadChatCount,
                onClick = { onDestinationSelected(DuoraNavDestination.CHAT) }
            )

            // 3. Call
            BottomNavItem(
                destination = DuoraNavDestination.CALL,
                label = "Call",
                icon = Icons.Default.Call,
                isSelected = currentDestination == DuoraNavDestination.CALL,
                badgeCount = 0,
                onClick = { onDestinationSelected(DuoraNavDestination.CALL) }
            )

            // 4. Notes
            BottomNavItem(
                destination = DuoraNavDestination.NOTES,
                label = "Notes",
                icon = Icons.Default.Description,
                isSelected = currentDestination == DuoraNavDestination.NOTES,
                badgeCount = 0,
                onClick = { onDestinationSelected(DuoraNavDestination.NOTES) }
            )

            // 5. Settings
            BottomNavItem(
                destination = DuoraNavDestination.SETTINGS,
                label = "Settings",
                icon = Icons.Default.Settings,
                isSelected = currentDestination == DuoraNavDestination.SETTINGS,
                badgeCount = 0,
                onClick = { onDestinationSelected(DuoraNavDestination.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: DuoraNavDestination,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) DuoraPrimaryPink else DuoraSecondaryText,
        animationSpec = tween(200),
        label = "NavColor"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "NavScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .scale(animatedScale)
            .testTag("nav_item_${destination.name.lowercase()}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-4).dp)
                        .height(16.dp)
                        .widthIn(min = 16.dp)
                        .clip(CircleShape)
                        .background(DuoraPrimaryPink)
                        .border(1.5.dp, Color.White, CircleShape)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = animatedColor
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Active Indicator Line underneath
        Box(
            modifier = Modifier
                .width(if (isSelected) 24.dp else 0.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (isSelected) DuoraPrimaryPink else Color.Transparent)
        )
    }
}
