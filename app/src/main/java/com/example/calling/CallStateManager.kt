package com.example.calling

import com.example.model.CallType

/**
 * Global call action bus coordinating Background Services, Notifications,
 * Picture-in-Picture remote actions, and ViewModel lifecycle events.
 */
object CallStateManager {
    var onEndCallRequested: (() -> Unit)? = null
    var onToggleMuteRequested: (() -> Unit)? = null
    var onToggleSpeakerRequested: (() -> Unit)? = null
    var onAnswerCallRequested: ((callType: CallType) -> Unit)? = null
    var onDeclineCallRequested: (() -> Unit)? = null
    var onEnterPipRequested: (() -> Unit)? = null

    @Volatile
    var isCallActive: Boolean = false

    @Volatile
    var currentCallType: CallType = CallType.VOICE

    @Volatile
    var isMuted: Boolean = false

    @Volatile
    var isSpeakerOn: Boolean = false

    @Volatile
    var currentPartnerName: String = ""

    @Volatile
    var durationSeconds: Int = 0
}
