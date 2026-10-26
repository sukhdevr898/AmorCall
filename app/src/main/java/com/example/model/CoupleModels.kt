package com.example.model

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ENDED,
    DECLINED,
    MISSED,
    FAILED
}

enum class AppTheme {
    DARK,
    LIGHT,
    LOVE
}

enum class PresenceStatus(val label: String) {
    ONLINE("Online"),
    AWAY("Away"),
    IN_CALL("In a call")
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
    val serverUrl: String = "ws://amorcall.onrender.com/",
    val isOnboardingCompleted: Boolean = false,
    val isNotificationsMuted: Boolean = false,
    val appTheme: AppTheme = AppTheme.LOVE
)

data class CallSession(
    val callId: String = "",
    val status: CallStatus = CallStatus.IDLE,
    val type: CallType = CallType.VOICE,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isPartnerRinging: Boolean = false,
    val statusMessage: String? = null,
    val isSignalingConnected: Boolean = true,
    val isMediaConnected: Boolean = true,
    val isReconnecting: Boolean = false,
    val reconnectGraceSecondsRemaining: Int = 15
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
    val messageType: String = "text", // "text", "image", "video", "voice", "audio", "file"
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = true,
    val isRead: Boolean = false,
    val deliveryStatus: String = if (isOutgoing) "READ" else "DELIVERED", // "PENDING", "SENT", "DELIVERED", "READ", "FAILED"
    val voiceDurationSeconds: Int = 0,
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val replyToId: String? = null,
    val replyToSender: String? = null,
    val replyToText: String? = null,
    val replyToType: String? = null,
    val isStarred: Boolean = false,
    val isDeleted: Boolean = false
) {
    val timestampFormatted: String
        get() {
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}

