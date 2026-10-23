package com.example.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    ENDED
}

enum class AppTheme {
    DARK,
    LIGHT,
    LOVE
}

data class CoupleProfile(
    val userName: String = "You",
    val partnerName: String = "My Love",
    val partnerNickname: String = "Sweetheart",
    val myPairCode: String = "",
    val partnerPairCode: String = "",
    val isPaired: Boolean = false,
    val relationshipStartDateEpochMs: Long = System.currentTimeMillis() - (520L * 24 * 60 * 60 * 1000L),
    val partnerStatus: String = "Thinking of you always ❤️",
    val avatarUri: String? = null,
    val serverUrl: String = "ws://10.0.2.2:3000",
    val isOnboardingCompleted: Boolean = false,
    val isNotificationsMuted: Boolean = false,
    val appTheme: AppTheme = AppTheme.LOVE
)

data class CallSession(
    val status: CallStatus = CallStatus.IDLE,
    val type: CallType = CallType.VOICE,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isPartnerRinging: Boolean = false,
    val statusMessage: String? = null
)

data class FloatingHeart(
    val id: Long,
    val startXFraction: Float,
    val scale: Float
)

data class MemoryPhoto(
    val id: String,
    val title: String,
    val note: String,
    val dateStr: String,
    val uriString: String? = null,
    val isSelectedWallpaper: Boolean = false,
    val gradientStartHex: Long = 0xFFE11D48,
    val gradientEndHex: Long = 0xFF4A052E
)

data class CallRecord(
    val id: String,
    val type: CallType,
    val timestamp: Long,
    val durationSeconds: Int,
    val isOutgoing: Boolean
)

data class CoupleNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val authorCode: String = "",
    val authorName: String = "",
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
    val isFromPartner: Boolean = false,
    val isShared: Boolean = true
) {
    val isExpired: Boolean
        get() = (System.currentTimeMillis() - createdAtEpochMs) > (24L * 60 * 60 * 1000L)

    val remainingMinutes: Long
        get() {
            val elapsed = System.currentTimeMillis() - createdAtEpochMs
            val remainingMs = (24L * 60 * 60 * 1000L) - elapsed
            return if (remainingMs > 0) remainingMs / (1000L * 60L) else 0L
        }

    val remainingFormatted: String
        get() {
            val mins = remainingMinutes
            val hours = mins / 60
            val remMins = mins % 60
            return when {
                hours > 0 -> "${hours}h ${remMins}m left"
                mins > 0 -> "${mins}m left"
                else -> "Expiring soon"
            }
        }
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fromCode: String = "",
    val toCode: String = "",
    val senderName: String = "",
    val content: String = "",
    val messageType: String = "text",
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val isRead: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val mediaUrl: String? = null
)

