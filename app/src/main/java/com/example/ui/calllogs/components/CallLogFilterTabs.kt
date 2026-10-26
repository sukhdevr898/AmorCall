package com.example.ui.calllogs.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.calllogs.model.CallLogFilter
import com.example.ui.theme.DuoraPrimaryPink
import com.example.ui.theme.DuoraSecondaryText

/**
 * Filter Tabs row matching Duora Call Logs reference design:
 * Tabs: All, Voice, Video, Missed
 */
@Composable
fun CallLogFilterTabs(
    selectedFilter: CallLogFilter,
    onFilterSelected: (CallLogFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallLogFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
                CallLogFilter.ALL -> "All"
                CallLogFilter.VOICE -> "Voice"
                CallLogFilter.VIDEO -> "Video"
                CallLogFilter.MISSED -> "Missed"
            }

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) DuoraPrimaryPink else Color.White,
                animationSpec = tween(200),
                label = "FilterBgColor"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else DuoraSecondaryText,
                animationSpec = tween(200),
                label = "FilterTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .border(
                        1.dp,
                        if (isSelected) DuoraPrimaryPink else Color(0xFFF1E4EC),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 8.dp)
                    .testTag("call_filter_tab_${filter.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}
