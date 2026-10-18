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

data class CoupleProfile(
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
    val isNotificationsMuted: Boolean = false
)

data class CallSession(
    val status: CallStatus = CallStatus.IDLE,
    val type: CallType = CallType.VOICE,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true
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
