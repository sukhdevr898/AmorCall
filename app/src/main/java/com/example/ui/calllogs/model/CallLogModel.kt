package com.example.ui.calllogs.model

import com.example.model.CallRecord
import com.example.model.CallType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CallLogType {
    VOICE,
    VIDEO,
    MISSED
}

enum class CallLogFilter {
    ALL,
    VOICE,
    VIDEO,
    MISSED
}

data class CallLogItemData(
    val id: String,
    val partnerName: String,
    val type: CallLogType,
    val durationFormatted: String,
    val timeFormatted: String,
    val dateGroup: String, // "Today", "Yesterday", or "Earlier"
    val isOutgoing: Boolean,
    val timestamp: Long
)

/**
 * Maps a raw CallRecord to a presentation-ready CallLogItemData
 */
fun CallRecord.toCallLogItem(partnerName: String = "My Love 💕"): CallLogItemData {
    val callType = when {
        durationSeconds <= 0 -> CallLogType.MISSED
        type == CallType.VIDEO -> CallLogType.VIDEO
        else -> CallLogType.VOICE
    }

    val durationText = when {
        durationSeconds <= 0 -> "Missed Call"
        durationSeconds < 60 -> "0:${durationSeconds.toString().padStart(2, '0')}"
        else -> {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            "${mins}:${secs.toString().padStart(2, '0')}"
        }
    }

    val subtitle = when (callType) {
        CallLogType.MISSED -> "Missed Call"
        CallLogType.VIDEO -> "Video Call • $durationText"
        CallLogType.VOICE -> "Voice Call • $durationText"
    }

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(timestamp))

    val dateGroup = computeDateGroup(timestamp)

    return CallLogItemData(
        id = id,
        partnerName = if (partnerName.isNotBlank()) partnerName else "My Love 💕",
        type = callType,
        durationFormatted = subtitle,
        timeFormatted = formattedTime,
        dateGroup = dateGroup,
        isOutgoing = isOutgoing,
        timestamp = timestamp
    )
}

private fun computeDateGroup(timestamp: Long): String {
    val now = Calendar.getInstance()
    val callDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == callDate.get(Calendar.DAY_OF_YEAR) -> "Today"

        now.get(Calendar.YEAR) == callDate.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - callDate.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"

        else -> SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
