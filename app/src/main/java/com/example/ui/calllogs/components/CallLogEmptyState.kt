package com.example.ui.calllogs.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraPrimaryText
import com.example.ui.theme.DuoraSecondaryPink
import com.example.ui.theme.DuoraSecondaryText

/**
 * Empty State matching Mockup 3 in the design reference:
 * - Romantic cloud illustration with phone receiver and hearts
 * - "No Call Logs Yet"
 * - "Your call history will appear here when you start calling 💕"
 * - "Start a Call" gradient button
 */
@Composable
fun CallLogEmptyState(
    isFiltered: Boolean = false,
    onStartCallClick: () -> Unit,
    onClearFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Cloud & Phone Romantic Art
        Box(
            modifier = Modifier
                .size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Cloud Circles
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEEF5))
            )
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFDDEB))
            )

            // Phone icon in center
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(DuoraPrimaryPink, DuoraSecondaryPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Floating decorative hearts
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = DuoraPrimaryPink.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 20.dp)
                    .size(20.dp)
            )

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = DuoraSecondaryPink.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 22.dp, start = 18.dp)
                    .size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isFiltered) "No Matching Calls" else "No Call Logs Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DuoraPrimaryText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFiltered) {
                "No calls found for this filter or search term."
            } else {
                "Your call history will appear here\nwhen you start calling 💕"
            },
            fontSize = 13.sp,
            color = DuoraSecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isFiltered) {
            Button(
                onClick = onClearFilterClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, DuoraPrimaryPink),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(48.dp)
                    .testTag("clear_call_filter_button")
            ) {
                Text(
                    text = "Clear Filter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoraPrimaryPink
                )
            }
        } else {
            Button(
                onClick = onStartCallClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(48.dp)
                    .background(
                        Brush.horizontalGradient(listOf(DuoraPrimaryPink, DuoraSecondaryPink)),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("start_call_empty_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Start a Call",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
