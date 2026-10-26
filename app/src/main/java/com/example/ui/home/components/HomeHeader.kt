package com.example.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSoftBlush
import com.example.ui.theme.DuoraSuccess

/**
 * Top Header matching Duora Home Screen design reference:
 * - Official Duora "Always Together" Logo on the left
 * - Notification bell with unread badge on the right
 * - Circular profile avatar with online green status dot
 */
@Composable
fun HomeHeader(
    unreadNotificationCount: Int = 1,
    isPartnerOnline: Boolean = true,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("home_top_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Duora Official Logo on Left
        Box(
            modifier = Modifier
                .height(36.dp)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_duora_logo_header),
                contentDescription = "Duora Always Together",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(34.dp)
                    .testTag("home_header_logo")
            )
        }

        // Notification Bell + Avatar on Right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notification Bell with Badge
            Box(
                modifier = Modifier.testTag("header_notification_button")
            ) {
                Surface(
                    onClick = onNotificationClick,
                    shape = CircleShape,
                    color = DuoraSoftBlush,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFDCE8)),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = DuoraPrimaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Unread dot badge
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DuoraPrimaryPink)
                            .border(1.5.dp, Color.White, CircleShape)
                            .testTag("notification_badge_dot")
                    )
                }
            }

            // Circular Profile Avatar with Online Status
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick)
                    .testTag("header_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_partner_demo),
                    contentDescription = "Partner Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, DuoraPrimaryPink, CircleShape)
                )

                // Online indicator green dot on bottom right
                if (isPartnerOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 1.dp, end = 1.dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(DuoraSuccess)
                            .border(2.dp, Color.White, CircleShape)
                            .testTag("avatar_online_status_dot")
                    )
                }
            }
        }
    }
}
