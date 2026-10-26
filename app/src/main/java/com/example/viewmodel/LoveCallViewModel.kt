package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calling.AudioStreamManager
import com.example.calling.CallAudioHaptics
import com.example.calling.CallForegroundService
import com.example.calling.CallNotificationHelper
import com.example.data.CoupleRepository
import com.example.model.AppTheme
import com.example.model.CallRecord
import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.model.CoupleNote
import com.example.model.CoupleProfile
import com.example.model.FloatingHeart
import com.example.model.MemoryPhoto
import com.example.network.ServerConnectionState
import com.example.network.SignalingClient
import com.example.network.SignalingEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import com.example.data.firebase.FirebaseCoupleRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class AppScreen {
    SPLASH,
    AUTH,
    ONBOARDING,
    DASHBOARD,
    MEMORIES,
    PAIRING,
    DEVELOPER
}

class LoveCallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CoupleRepository(application)
    private val firebaseRepo = FirebaseCoupleRepository.getInstance(application)
    private val audioHaptics = CallAudioHaptics(application)
    private val signalingClient = SignalingClient.getInstance()
    private val audioStreamManager = AudioStreamManager(application)
    private var activeCallSignalId: String? = null
    private var lastNotifiedMessageId: String? = null

    val profile: StateFlow<CoupleProfile> = repository.profile
    val memories: StateFlow<List<MemoryPhoto>> = repository.memories
    val callHistory: StateFlow<List<CallRecord>> = repository.callHistory
    val sharedNote: StateFlow<CoupleNote?> = repository.sharedNote
    val notes: StateFlow<List<CoupleNote>> = repository.notes
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val unreadChatCount: StateFlow<Int> = chatMessages.map { list ->
        list.count { !it.isOutgoing && !it.isRead && !it.isDeleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val serverConnectionState: StateFlow<ServerConnectionState> = signalingClient.connectionState

    private val _partnerPresence = MutableStateFlow<String>("Online")
    val partnerPresence: StateFlow<String> = _partnerPresence.asStateFlow()
    private var partnerPresenceJob: Job? = null

    private val _isScreenLoading = MutableStateFlow(false)
    val isScreenLoading: StateFlow<Boolean> = _isScreenLoading.asStateFlow()

    fun navigateToScreenWithLoading(screen: AppScreen) {
        viewModelScope.launch {
            _isScreenLoading.value = true
            delay(280)
            _currentScreen.value = screen
            delay(120)
            _isScreenLoading.value = false
        }
    }

    fun observePartnerPresence(partnerCode: String) {
        partnerPresenceJob?.cancel()
        val clean = partnerCode.trim().uppercase()
        if (clean.isBlank()) {
            _partnerPresence.value = "Away"
            return
        }
        partnerPresenceJob = viewModelScope.launch {
            firebaseRepo.observePartnerPresence(clean).collect { presence ->
                _partnerPresence.value = presence ?: "Away"
            }
        }
    }

    fun setMyPresence(presence: String) {
        viewModelScope.launch {
            try {
                firebaseRepo.updateUserPresence(presence)
            } catch (_: Exception) {}
        }
    }

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    private val _isChatOpen = MutableStateFlow(false)
    val isChatOpen: StateFlow<Boolean> = _isChatOpen.asStateFlow()

    val activeWallpaper: StateFlow<MemoryPhoto?> = repository.memories
        .map { list -> list.firstOrNull { it.isSelectedWallpaper } ?: list.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _callSession = MutableStateFlow(CallSession())
    val callSession: StateFlow<CallSession> = _callSession.asStateFlow()

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    fun setIsInPipMode(inPip: Boolean) {
        _isInPipMode.value = inPip
    }

    private val _remoteVideoBitmap = MutableStateFlow<Bitmap?>(null)
    val remoteVideoBitmap: StateFlow<Bitmap?> = _remoteVideoBitmap.asStateFlow()

    private val _floatingHearts = MutableStateFlow<List<FloatingHeart>>(emptyList())
    val floatingHearts: StateFlow<List<FloatingHeart>> = _floatingHearts.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _targetBottomTab = MutableStateFlow<String?>(null)
    val targetBottomTab: StateFlow<String?> = _targetBottomTab.asStateFlow()

    private val _inCallMessageOverlay = MutableStateFlow<ChatMessage?>(null)
    val inCallMessageOverlay: StateFlow<ChatMessage?> = _inCallMessageOverlay.asStateFlow()

    private val _inAppMessageNotification = MutableStateFlow<ChatMessage?>(null)
    val inAppMessageNotification: StateFlow<ChatMessage?> = _inAppMessageNotification.asStateFlow()

    private val _isCallScreenMinimized = MutableStateFlow(false)
    val isCallScreenMinimized: StateFlow<Boolean> = _isCallScreenMinimized.asStateFlow()

    fun minimizeCallScreen() {
        _isCallScreenMinimized.value = true
    }

    fun expandCallScreen() {
        _isCallScreenMinimized.value = false
    }

    private val _isIndividualChatOpen = MutableStateFlow(false)
    val isIndividualChatOpen: StateFlow<Boolean> = _isIndividualChatOpen.asStateFlow()

    fun openIndividualChat() {
        _isIndividualChatOpen.value = true
        _isChatOpen.value = true
        _inAppMessageNotification.value = null
        markChatAsRead()
    }

    fun closeIndividualChat() {
        _isIndividualChatOpen.value = false
        _isChatOpen.value = false
    }

    fun dismissInAppMessageNotification() {
        _inAppMessageNotification.value = null
    }

    fun openChatFromInAppNotification() {
        _inAppMessageNotification.value = null
        _currentScreen.value = AppScreen.DASHBOARD
        _targetBottomTab.value = "CHAT"
        openIndividualChat()
    }

    fun dismissInCallMessageOverlay() {
        _inCallMessageOverlay.value = null
    }

    fun openChatFromCall() {
        _inCallMessageOverlay.value = null
        minimizeCallScreen()
        openChatTab()
    }

    fun openChatTab() {
        _currentScreen.value = AppScreen.DASHBOARD
        _targetBottomTab.value = "CHAT"
        openIndividualChat()
    }

    fun clearTargetBottomTab() {
        _targetBottomTab.value = null
    }

    private val updateManager = com.example.network.AppUpdateManager.getInstance()
    val appUpdateInfo: StateFlow<com.example.network.AppUpdateInfo?> = updateManager.updateInfo
    val isCheckingUpdate: StateFlow<Boolean> = updateManager.isChecking
    val updateStatusMessage: StateFlow<String?> = updateManager.statusMessage

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            updateManager.checkForUpdates(isManual)
        }
    }

    fun dismissUpdateDialog() {
        updateManager.dismissUpdateDialog()
    }

    fun clearUpdateStatusMessage() {
        updateManager.clearStatusMessage()
    }

    private var callTimerJob: Job? = null
    private var simulatedAnswerJob: Job? = null
    private var callHeartbeatJob: Job? = null
    private var reconnectGraceJob: Job? = null
    private var isCallRecordedForCurrentSession = false

    init {
        CallNotificationHelper.initChannels(application)

        // Connect global CallStateManager callbacks for notifications & PiP remote actions
        com.example.calling.CallStateManager.onEndCallRequested = { endCall() }
        com.example.calling.CallStateManager.onToggleMuteRequested = { toggleMute() }
        com.example.calling.CallStateManager.onToggleSpeakerRequested = { toggleSpeaker() }
        com.example.calling.CallStateManager.onAnswerCallRequested = { type -> acceptCall(incomingType = type) }
        com.example.calling.CallStateManager.onDeclineCallRequested = { declineCall() }

        // Connect to signaling server
        val prof = profile.value
        signalingClient.connect(prof.serverUrl, prof.myPairCode, prof.partnerPairCode)

        // Start Foreground Service if onboarding is already completed
        if (prof.isOnboardingCompleted) {
            CallForegroundService.start(application)
        }

        // Check for app updates from GitHub repository
        checkForUpdates(isManual = false)

        // Observe real-time signaling events
        viewModelScope.launch {
            signalingClient.events.collect { event ->
                when (event) {
                    is SignalingEvent.IncomingCall -> {
                        val type = if (event.callType.equals("VIDEO", ignoreCase = true)) {
                            CallType.VIDEO
                        } else {
                            CallType.VOICE
                        }
                        triggerIncomingCall(type, incomingCallId = event.callId)
                    }
                    is SignalingEvent.CallRinging -> {
                        if (_callSession.value.status == CallStatus.OUTGOING_RINGING) {
                            _callSession.value = _callSession.value.copy(
                                isPartnerRinging = true,
                                statusMessage = "Ringing..."
                            )
                        }
                    }
                    is SignalingEvent.CallAccepted -> {
                        acceptCall(isOutgoingInitiated = true, incomingCallId = event.callId)
                    }
                    is SignalingEvent.CallDeclined -> {
                        handlePartnerDeclinedCall()
                    }
                    is SignalingEvent.CallEnded -> {
                        handlePartnerEndedCall(reason = event.reason, customMessage = event.statusMessage)
                    }
                    is SignalingEvent.CallReconnecting -> {
                        handleCallReconnecting(event.callId, event.gracePeriodSeconds)
                    }
                    is SignalingEvent.CallReconnected -> {
                        handleCallReconnected(event.callId)
                    }
                    is SignalingEvent.CallUnavailable -> {
                        handlePartnerUnavailable()
                    }
                    is SignalingEvent.PartnerStatus -> {
                        val status = if (event.isOnline) "Partner Online ❤️" else "Thinking of you always ❤️"
                        repository.saveProfile(profile.value.copy(partnerStatus = status))
                    }
                    is SignalingEvent.AudioChunk -> {
                        audioStreamManager.playAudioChunk(event.audioData)
                    }
                    is SignalingEvent.VideoFrame -> {
                        try {
                            val bytes = Base64.decode(event.frameData, Base64.NO_WRAP)
                            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (decoded != null) {
                                _remoteVideoBitmap.value = decoded
                            }
                        } catch (_: Exception) {}
                    }
                    is SignalingEvent.HeartReaction -> {
                        spawnFloatingHeart()
                    }
                    is SignalingEvent.PartnerNoteReceived -> {
                        val note = CoupleNote(
                            id = event.noteId,
                            content = event.content,
                            authorCode = event.from,
                            authorName = event.authorName,
                            createdAtEpochMs = event.createdAt,
                            isFromPartner = true
                        )
                        repository.saveSharedNote(note)
                    }
                    is SignalingEvent.PartnerNoteDeleted -> {
                        repository.deleteSharedNote()
                    }
                    is SignalingEvent.ChatMessageReceived -> {
                        repository.saveChatMessage(event.message)
                        if (!event.message.isOutgoing) {
                            val partner = profile.value.partnerName.ifBlank { "Partner" }
                            if (_callSession.value.status != CallStatus.IDLE) {
                                _inCallMessageOverlay.value = event.message
                                viewModelScope.launch {
                                    delay(5000)
                                    if (_inCallMessageOverlay.value?.id == event.message.id) {
                                        _inCallMessageOverlay.value = null
                                    }
                                }
                            }
                            if (!_isChatOpen.value) {
                                // Phase 7: Trigger floating in-app notification banner
                                _inAppMessageNotification.value = event.message
                                viewModelScope.launch {
                                    delay(4500)
                                    if (_inAppMessageNotification.value?.id == event.message.id) {
                                        _inAppMessageNotification.value = null
                                    }
                                }
                                com.example.calling.CallNotificationHelper.showMessageNotification(
                                    context = getApplication(),
                                    senderName = partner,
                                    messageText = event.message.content,
                                    messageType = event.message.messageType,
                                    mediaUrl = event.message.mediaUrl
                                )
                            }
                        }
                    }
                    is SignalingEvent.ChatTyping -> {
                        _isPartnerTyping.value = event.isTyping
                    }
                    is SignalingEvent.ChatHistoryLoaded -> {
                        repository.setChatMessages(event.messages)
                    }
                }
            }
        }

        // Observe real-time Firestore messages, notes, and call signals for the couple
        viewModelScope.launch {
            profile.collect { prof ->
                if (prof.isPaired && prof.partnerPairCode.isNotBlank()) {
                    val coupleId = firebaseRepo.getCoupleId(prof.myPairCode, prof.partnerPairCode)

                    // Realtime Messages from Firestore
                    launch {
                        firebaseRepo.observeMessages(coupleId).collect { msgs ->
                            if (msgs.isNotEmpty()) {
                                repository.setChatMessages(msgs)
                                val latest = msgs.firstOrNull()
                                if (latest != null && !latest.isOutgoing && latest.id != lastNotifiedMessageId) {
                                    lastNotifiedMessageId = latest.id
                                    if (_callSession.value.status != CallStatus.IDLE) {
                                        _inCallMessageOverlay.value = latest
                                        viewModelScope.launch {
                                            delay(5000)
                                            if (_inCallMessageOverlay.value?.id == latest.id) {
                                                _inCallMessageOverlay.value = null
                                            }
                                        }
                                    }
                                    if (!_isChatOpen.value) {
                                        CallNotificationHelper.showMessageNotification(
                                            context = getApplication(),
                                            senderName = prof.partnerName.ifBlank { "My Love" },
                                            messageText = latest.content,
                                            messageType = latest.messageType,
                                            mediaUrl = latest.mediaUrl
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Realtime Notes from Firestore
                    launch {
                        firebaseRepo.observeNotes(coupleId).collect { notesList ->
                            for (n in notesList) {
                                repository.addOrUpdateNote(n)
                            }
                        }
                    }

                    // Realtime Call Signals & Alerts from Firestore
                    launch {
                        firebaseRepo.observeCallSignals(coupleId).collect { signal ->
                            if (signal != null && signal.recipientId == prof.myPairCode && signal.status == "RINGING") {
                                if (_callSession.value.status == CallStatus.IDLE) {
                                    val type = if (signal.callType.equals("VIDEO", ignoreCase = true)) CallType.VIDEO else CallType.VOICE
                                    triggerIncomingCall(type)
                                }
                            } else if (signal != null && signal.status in listOf("REJECTED", "ENDED")) {
                                if (_callSession.value.status != CallStatus.IDLE) {
                                    handlePartnerEndedCall()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun syncCallStateToService() {
        val session = _callSession.value
        val isConnected = session.status == CallStatus.CONNECTED
        val isOutgoing = session.status == CallStatus.OUTGOING_RINGING
        val isActive = isConnected || isOutgoing

        com.example.calling.CallStateManager.isCallActive = isActive
        com.example.calling.CallStateManager.currentCallType = session.type
        com.example.calling.CallStateManager.isMuted = session.isMuted
        com.example.calling.CallStateManager.isSpeakerOn = session.isSpeakerOn
        com.example.calling.CallStateManager.currentPartnerName = profile.value.partnerName
        com.example.calling.CallStateManager.durationSeconds = session.durationSeconds

        if (isActive) {
            CallForegroundService.updateCallProgress(
                getApplication(),
                isCallActive = true,
                partnerName = profile.value.partnerName,
                callType = session.type,
                durationSeconds = session.durationSeconds,
                isMuted = session.isMuted,
                isSpeakerOn = session.isSpeakerOn
            )
        } else {
            CallForegroundService.restoreHotline(getApplication())
        }
    }

    fun onSplashFinished() {
        try {
            val app = getApplication<Application>()
            if (com.google.firebase.FirebaseApp.getApps(app).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(app)
            }
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                _currentScreen.value = AppScreen.DASHBOARD
                setMyPresence("Online")
                observePartnerPresence(profile.value.partnerPairCode)
            } else {
                _currentScreen.value = AppScreen.AUTH
            }
        } catch (e: Exception) {
            android.util.Log.e("LoveCallViewModel", "Auth check error on splash finish", e)
            _currentScreen.value = AppScreen.AUTH
        }
    }

    fun onAuthSuccess(userId: String, email: String, displayName: String) {
        android.util.Log.i("DuoraAuth", "==================================================")
        android.util.Log.i("DuoraAuth", "onAuthSuccess START: userId=$userId, email=$email, displayName=$displayName")

        try {
            val current = profile.value
            val updated = current.copy(
                userName = displayName.ifBlank { current.userName },
                isOnboardingCompleted = true
            )
            android.util.Log.d("DuoraAuth", "Step 1: Saving updated profile to local repository: $updated")
            repository.saveProfile(updated)
            android.util.Log.d("DuoraAuth", "Step 1 SUCCESS: Local profile saved successfully.")
        } catch (e: Throwable) {
            android.util.Log.e("DuoraAuth", "Step 1 ERROR: Failed to save profile to repository", e)
        }

        viewModelScope.launch {
            try {
                android.util.Log.d("DuoraAuth", "Step 2: Syncing profile with Firebase repository...")
                val current = profile.value
                firebaseRepo.syncUserProfile(
                    displayName = current.userName,
                    partnerCode = current.myPairCode,
                    pairedUserId = null,
                    pairedPartnerCode = current.partnerPairCode.ifBlank { null },
                    isOnline = true
                )
                setMyPresence("Online")
                android.util.Log.d("DuoraAuth", "Step 2 SUCCESS: Firebase user profile synced.")
            } catch (e: Throwable) {
                android.util.Log.e("DuoraAuth", "Step 2 ERROR: Firebase profile sync failed", e)
            }
        }

        try {
            android.util.Log.d("DuoraAuth", "Step 3: Setting up partner presence observer for code=${profile.value.partnerPairCode}")
            observePartnerPresence(profile.value.partnerPairCode)
            android.util.Log.d("DuoraAuth", "Step 3 SUCCESS: Partner presence listener attached.")
        } catch (e: Throwable) {
            android.util.Log.e("DuoraAuth", "Step 3 ERROR: Failed to observe partner presence", e)
        }

        try {
            android.util.Log.d("DuoraAuth", "Step 4: Triggering navigation to DASHBOARD destination (previous=${_currentScreen.value})")
            _currentScreen.value = AppScreen.DASHBOARD
            android.util.Log.i("DuoraAuth", "Step 4 SUCCESS: currentScreen successfully set to AppScreen.DASHBOARD")
            android.util.Log.i("DuoraAuth", "==================================================")
        } catch (e: Throwable) {
            android.util.Log.e("DuoraAuth", "Step 4 CRITICAL ERROR: Navigation trigger failed", e)
        }
    }

    fun signOut() {
        setMyPresence("Away")
        Firebase.auth.signOut()
        _currentScreen.value = AppScreen.AUTH
    }

    fun completeOnboarding(partnerName: String, partnerNickname: String, partnerCode: String) {
        val cleanPartnerCode = partnerCode.trim().uppercase()
        val finalPartnerCode = if (cleanPartnerCode.equals("ROSE99", ignoreCase = true) ||
            cleanPartnerCode.equals(profile.value.myPairCode, ignoreCase = true)) {
            ""
        } else {
            cleanPartnerCode
        }
        val updated = profile.value.copy(
            partnerName = partnerName.ifBlank { "My Love" },
            partnerNickname = partnerNickname.ifBlank { "Sweetheart" },
            partnerPairCode = finalPartnerCode,
            isPaired = finalPartnerCode.isNotBlank(),
            isOnboardingCompleted = true
        )
        repository.saveProfile(updated)
        signalingClient.connect(updated.serverUrl, updated.myPairCode, updated.partnerPairCode)
        CallForegroundService.start(getApplication())
        _currentScreen.value = AppScreen.DASHBOARD
    }

    fun replayOnboarding() {
        _currentScreen.value = AppScreen.ONBOARDING
    }

    fun updateRelationshipProfile(userName: String, partnerName: String, partnerNickname: String) {
        val updated = profile.value.copy(
            userName = userName.ifBlank { "You" },
            partnerName = partnerName.ifBlank { "My Love" },
            partnerNickname = partnerNickname.ifBlank { "Sweetheart" }
        )
        repository.saveProfile(updated)
    }

    fun getRedactedDiagnostics(): String {
        val prof = profile.value
        val isConnected = serverConnectionState.value == ServerConnectionState.CONNECTED
        return """
            === Duora Diagnostics ===
            App: Duora v${com.example.BuildConfig.VERSION_NAME} (${com.example.BuildConfig.VERSION_CODE})
            Platform: Android Native (Kotlin + Jetpack Compose)
            WebSocket Server: [REDACTED_URL]
            Socket Connection: ${if (isConnected) "CONNECTED" else "DISCONNECTED"}
            Mutual Pairing: ${if (prof.isPaired) "PAIRED (#${prof.myPairCode.take(2)}*** -> #${prof.partnerPairCode.take(2)}***)" else "UNPAIRED"}
            Cached Messages: ${chatMessages.value.size}
            Active 24h Note: ${if (sharedNote.value != null) "ACTIVE" else "NONE"}
            Call State: ${_callSession.value.status}
            Theme: ${prof.appTheme.name}
            Notifications Muted: ${prof.isNotificationsMuted}
            Sensitive Data (Tokens, Passwords, Codes, Messages, SDP/ICE): REDACTED
        """.trimIndent()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleMuteNotifications() {
        val next = !profile.value.isNotificationsMuted
        repository.setNotificationsMuted(next)
    }

    fun clearCallHistory() {
        repository.clearCallHistory()
    }

    fun deleteCallRecord(id: String) {
        repository.deleteCallRecord(id)
    }

    fun reconnectServer(newServerUrl: String = "ws://amorcall.onrender.com/") {
        signalingClient.connect("ws://amorcall.onrender.com/", profile.value.myPairCode, profile.value.partnerPairCode)
    }

    fun initiateCall(type: CallType) {
        startOutgoingCall(type)
    }

    fun startOutgoingCall(type: CallType) {
        if (!profile.value.isPaired || profile.value.partnerPairCode.isBlank()) {
            _currentScreen.value = AppScreen.PAIRING
            return
        }

        val partner = profile.value.partnerName
        val callId = "call_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        isCallRecordedForCurrentSession = false
        _isCallScreenMinimized.value = false

        _callSession.value = CallSession(
            callId = callId,
            status = CallStatus.OUTGOING_RINGING,
            type = type,
            durationSeconds = 0,
            isSpeakerOn = type == CallType.VIDEO,
            isPartnerRinging = false,
            statusMessage = "Calling $partner..."
        )
        setMyPresence("In a call")
        audioHaptics.setSpeakerphone(type == CallType.VIDEO)
        audioHaptics.startOutgoingRinging(viewModelScope)
        syncCallStateToService()

        // Send signal to partner via WebSocket & Firestore
        signalingClient.sendCallInitiate(
            to = profile.value.partnerPairCode,
            callerName = profile.value.userName.ifBlank { "My Love" },
            callType = type.name,
            callId = callId
        )
        if (profile.value.isPaired && profile.value.partnerPairCode.isNotBlank()) {
            val coupleId = firebaseRepo.getCoupleId(profile.value.myPairCode, profile.value.partnerPairCode)
            viewModelScope.launch {
                activeCallSignalId = firebaseRepo.sendCallSignal(coupleId, profile.value.partnerPairCode, type)
            }
        }

        // Ringing timeout (40 seconds)
        simulatedAnswerJob?.cancel()
        simulatedAnswerJob = viewModelScope.launch {
            delay(40000)
            if (_callSession.value.status == CallStatus.OUTGOING_RINGING) {
                _callSession.value = _callSession.value.copy(
                    status = CallStatus.ENDED,
                    statusMessage = "$partner did not answer"
                )
                syncCallStateToService()
                delay(2500)
                resetCallSessionToIdle()
            }
        }
    }

    fun startSelfTestCall(type: CallType) {
        isCallRecordedForCurrentSession = false
        _isCallScreenMinimized.value = false
        _callSession.value = CallSession(
            callId = "self_test_${System.currentTimeMillis()}",
            status = CallStatus.CONNECTED,
            type = type,
            durationSeconds = 0,
            isSpeakerOn = type == CallType.VIDEO,
            isPartnerRinging = true,
            statusMessage = "Self Test Connected"
        )
        audioHaptics.setSpeakerphone(type == CallType.VIDEO)
        audioHaptics.playConnectedChime()
        syncCallStateToService()

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_callSession.value.status == CallStatus.CONNECTED) {
                    _callSession.value = _callSession.value.copy(
                        durationSeconds = _callSession.value.durationSeconds + 1
                    )
                    syncCallStateToService()
                }
            }
        }
    }

    fun triggerIncomingCall(type: CallType, incomingCallId: String = "") {
        val partner = profile.value.partnerName
        val callId = if (incomingCallId.isNotBlank()) incomingCallId else "call_${System.currentTimeMillis()}"
        isCallRecordedForCurrentSession = false
        _isCallScreenMinimized.value = false

        _callSession.value = CallSession(
            callId = callId,
            status = CallStatus.INCOMING_RINGING,
            type = type,
            durationSeconds = 0,
            isSpeakerOn = type == CallType.VIDEO,
            isPartnerRinging = true,
            statusMessage = "Incoming Call"
        )
        // Acknowledge back that destination device has received call and is ringing
        if (profile.value.partnerPairCode.isNotBlank()) {
            signalingClient.sendCallRinging(profile.value.partnerPairCode, callId)
        }

        if (!profile.value.isNotificationsMuted) {
            audioHaptics.startIncomingRinging(viewModelScope)
        }
        CallNotificationHelper.showIncomingCallNotification(
            getApplication(),
            partner,
            type,
            isMuted = profile.value.isNotificationsMuted
        )
    }

    fun acceptCall(isOutgoingInitiated: Boolean = false, incomingType: CallType? = null, incomingCallId: String = "") {
        simulatedAnswerJob?.cancel()
        reconnectGraceJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playConnectedChime()

        val currentType = incomingType ?: _callSession.value.type
        val speakerDefault = currentType == CallType.VIDEO
        val callId = if (incomingCallId.isNotBlank()) incomingCallId else (_callSession.value.callId.ifBlank { "call_${System.currentTimeMillis()}" })
        isCallRecordedForCurrentSession = false
        _isCallScreenMinimized.value = false

        _callSession.value = _callSession.value.copy(
            callId = callId,
            status = CallStatus.CONNECTED,
            type = currentType,
            durationSeconds = 0,
            isSpeakerOn = speakerDefault,
            isPartnerRinging = true,
            statusMessage = "Connected",
            isReconnecting = false
        )
        audioHaptics.setSpeakerphone(speakerDefault)
        setMyPresence("In a call")

        // Start real VoIP audio streaming
        audioStreamManager.start(profile.value.partnerPairCode, initialSpeakerOn = speakerDefault)

        if (!isOutgoingInitiated) {
            signalingClient.sendCallAccept(profile.value.partnerPairCode, callId)
        }

        activeCallSignalId?.let { sigId ->
            val coupleId = firebaseRepo.getCoupleId(profile.value.myPairCode, profile.value.partnerPairCode)
            viewModelScope.launch {
                firebaseRepo.updateCallSignalStatus(coupleId, sigId, "ACCEPTED")
            }
        }

        syncCallStateToService()
        startCallHeartbeats()

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_callSession.value.status == CallStatus.CONNECTED) {
                    _callSession.value = _callSession.value.copy(
                        durationSeconds = _callSession.value.durationSeconds + 1
                    )
                    syncCallStateToService()
                }
            }
        }
    }

    private fun startCallHeartbeats() {
        callHeartbeatJob?.cancel()
        callHeartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                val session = _callSession.value
                if (session.status == CallStatus.CONNECTED || session.status == CallStatus.RECONNECTING) {
                    signalingClient.sendCallHeartbeat(session.callId, profile.value.partnerPairCode)
                    activeCallSignalId?.let { sigId ->
                        val coupleId = firebaseRepo.getCoupleId(profile.value.myPairCode, profile.value.partnerPairCode)
                        firebaseRepo.updateCallHeartbeat(coupleId, sigId)
                    }
                } else {
                    break
                }
            }
        }
    }

    fun handleCallReconnecting(callId: String, gracePeriodSeconds: Int = 15) {
        val current = _callSession.value
        if (current.status == CallStatus.CONNECTED) {
            _callSession.value = current.copy(
                status = CallStatus.RECONNECTING,
                isReconnecting = true,
                statusMessage = "Reconnecting with ${profile.value.partnerName}...",
                reconnectGraceSecondsRemaining = gracePeriodSeconds
            )
            syncCallStateToService()

            reconnectGraceJob?.cancel()
            reconnectGraceJob = viewModelScope.launch {
                for (sec in gracePeriodSeconds downTo 0) {
                    delay(1000)
                    if (_callSession.value.status == CallStatus.RECONNECTING) {
                        _callSession.value = _callSession.value.copy(
                            reconnectGraceSecondsRemaining = sec,
                            statusMessage = "Reconnecting (${sec}s)..."
                        )
                    } else {
                        break
                    }
                }
                if (_callSession.value.status == CallStatus.RECONNECTING) {
                    handleCallTimeout("Connection lost with partner")
                }
            }
        }
    }

    fun handleCallReconnected(callId: String) {
        reconnectGraceJob?.cancel()
        if (_callSession.value.status == CallStatus.RECONNECTING) {
            _callSession.value = _callSession.value.copy(
                status = CallStatus.CONNECTED,
                isReconnecting = false,
                statusMessage = "Connected"
            )
            syncCallStateToService()
        }
    }

    private fun handleCallTimeout(reason: String) {
        callHeartbeatJob?.cancel()
        reconnectGraceJob?.cancel()
        callTimerJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playEndCallTone()
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null

        val duration = _callSession.value.durationSeconds
        val type = _callSession.value.type

        if (!isCallRecordedForCurrentSession && duration > 0) {
            isCallRecordedForCurrentSession = true
            repository.recordCall(type = type, durationSeconds = duration, isOutgoing = true)
        }

        _callSession.value = _callSession.value.copy(
            status = CallStatus.ENDED,
            isReconnecting = false,
            statusMessage = reason
        )
        setMyPresence("Online")
        syncCallStateToService()
    }

    fun declineCall(notifyPartner: Boolean = true) {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        callHeartbeatJob?.cancel()
        reconnectGraceJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null

        val currentSession = _callSession.value
        if (notifyPartner && profile.value.partnerPairCode.isNotBlank()) {
            signalingClient.sendCallDecline(profile.value.partnerPairCode, currentSession.callId)
        }

        activeCallSignalId?.let { sigId ->
            val coupleId = firebaseRepo.getCoupleId(profile.value.myPairCode, profile.value.partnerPairCode)
            viewModelScope.launch {
                firebaseRepo.updateCallSignalStatus(coupleId, sigId, "REJECTED")
            }
            activeCallSignalId = null
        }

        if (!isCallRecordedForCurrentSession) {
            isCallRecordedForCurrentSession = true
            repository.recordCall(
                type = currentSession.type,
                durationSeconds = 0,
                isOutgoing = false
            )
        }

        _callSession.value = CallSession(status = CallStatus.IDLE)
        syncCallStateToService()
    }

    fun handlePartnerDeclinedCall() {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        callHeartbeatJob?.cancel()
        reconnectGraceJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playEndCallTone()
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null

        val partner = profile.value.partnerName
        val declineMessage = "$partner call declined by partner"

        if (!isCallRecordedForCurrentSession) {
            isCallRecordedForCurrentSession = true
            repository.recordCall(
                type = _callSession.value.type,
                durationSeconds = 0,
                isOutgoing = true
            )
        }

        _callSession.value = _callSession.value.copy(
            status = CallStatus.ENDED,
            statusMessage = declineMessage
        )

        viewModelScope.launch {
            delay(3000)
            if (_callSession.value.status == CallStatus.ENDED) {
                _callSession.value = CallSession(status = CallStatus.IDLE)
            }
        }
    }

    fun handlePartnerEndedCall(reason: String = "NORMAL", customMessage: String = "") {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        callHeartbeatJob?.cancel()
        reconnectGraceJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playEndCallTone()
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null

        val partner = profile.value.partnerName
        val duration = _callSession.value.durationSeconds
        val type = _callSession.value.type

        if (!isCallRecordedForCurrentSession && duration > 0) {
            isCallRecordedForCurrentSession = true
            repository.recordCall(
                type = type,
                durationSeconds = duration,
                isOutgoing = false
            )
        }

        val displayMsg = when {
            customMessage.isNotBlank() -> customMessage
            reason == "DISCONNECTED_TIMEOUT" -> "Connection lost with partner"
            reason == "HEARTBEAT_TIMEOUT" -> "Connection lost with partner"
            else -> "$partner ended the call"
        }

        _callSession.value = _callSession.value.copy(
            status = CallStatus.ENDED,
            statusMessage = displayMsg
        )

        viewModelScope.launch {
            delay(2500)
            if (_callSession.value.status == CallStatus.ENDED) {
                _callSession.value = CallSession(status = CallStatus.IDLE)
            }
        }
    }

    fun handlePartnerUnavailable() {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        callHeartbeatJob?.cancel()
        reconnectGraceJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playEndCallTone()
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null

        val partner = profile.value.partnerName
        val status = "$partner is currently offline"
        repository.saveProfile(profile.value.copy(partnerStatus = status))

        _callSession.value = _callSession.value.copy(
            status = CallStatus.ENDED,
            statusMessage = status
        )

        viewModelScope.launch {
            delay(3000)
            if (_callSession.value.status == CallStatus.ENDED) {
                _callSession.value = CallSession(status = CallStatus.IDLE)
            }
        }
    }

    fun endCall(notifyPartner: Boolean = true) {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        callHeartbeatJob?.cancel()
        reconnectGraceJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playEndCallTone()
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null

        val currentSession = _callSession.value
        if (notifyPartner && profile.value.partnerPairCode.isNotBlank()) {
            signalingClient.sendCallEnd(profile.value.partnerPairCode, currentSession.callId)
        }

        activeCallSignalId?.let { sigId ->
            val coupleId = firebaseRepo.getCoupleId(profile.value.myPairCode, profile.value.partnerPairCode)
            viewModelScope.launch {
                firebaseRepo.updateCallSignalStatus(coupleId, sigId, "ENDED")
            }
            activeCallSignalId = null
        }

        val duration = currentSession.durationSeconds
        val type = currentSession.type
        val wasConnected = currentSession.status == CallStatus.CONNECTED || currentSession.status == CallStatus.RECONNECTING

        if (!isCallRecordedForCurrentSession && wasConnected && duration > 0) {
            isCallRecordedForCurrentSession = true
            repository.recordCall(
                type = type,
                durationSeconds = duration,
                isOutgoing = true
            )
        }

        _callSession.value = currentSession.copy(
            status = CallStatus.ENDED,
            isReconnecting = false,
            statusMessage = "Call Ended"
        )
        setMyPresence("Online")
        syncCallStateToService()
        viewModelScope.launch {
            delay(1500)
            if (_callSession.value.status == CallStatus.ENDED) {
                _callSession.value = CallSession(status = CallStatus.IDLE)
                syncCallStateToService()
            }
        }
    }

    fun resetCallSessionToIdle() {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.stopAll()
        audioStreamManager.stop()
        _remoteVideoBitmap.value = null
        _callSession.value = CallSession(status = CallStatus.IDLE)
        syncCallStateToService()
        _currentScreen.value = AppScreen.DASHBOARD
    }

    fun cleanupStaleCallOnResume() {
        val status = _callSession.value.status
        if (status == CallStatus.ENDED) {
            _callSession.value = CallSession(status = CallStatus.IDLE)
            syncCallStateToService()
        }
        repository.checkNoteExpiry()
    }

    fun toggleMute() {
        val next = !_callSession.value.isMuted
        _callSession.value = _callSession.value.copy(isMuted = next)
        audioHaptics.setMicrophoneMute(next)
        audioStreamManager.setMuted(next)
        syncCallStateToService()
    }

    fun toggleSpeaker() {
        val next = !_callSession.value.isSpeakerOn
        _callSession.value = _callSession.value.copy(isSpeakerOn = next)
        audioHaptics.setSpeakerphone(next)
        audioStreamManager.setSpeakerphone(next)
        syncCallStateToService()
    }

    fun toggleVideo() {
        _callSession.value = _callSession.value.copy(
            isVideoEnabled = !_callSession.value.isVideoEnabled
        )
    }

    fun switchCamera() {
        _callSession.value = _callSession.value.copy(
            isFrontCamera = !_callSession.value.isFrontCamera
        )
    }

    fun switchCallType() {
        val currentType = _callSession.value.type
        val nextType = if (currentType == CallType.VOICE) CallType.VIDEO else CallType.VOICE
        val speakerDefault = nextType == CallType.VIDEO
        _callSession.value = _callSession.value.copy(
            type = nextType,
            isSpeakerOn = speakerDefault
        )
        audioHaptics.setSpeakerphone(speakerDefault)
        syncCallStateToService()
    }

    fun sendVideoFrame(bitmap: Bitmap) {
        if (_callSession.value.status != CallStatus.CONNECTED || _callSession.value.type != CallType.VIDEO) return
        val partnerCode = profile.value.partnerPairCode
        if (partnerCode.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Downsample bitmap for fast real-time transmission over WebSocket
                val scaled = if (bitmap.width > 360 || bitmap.height > 480) {
                    val scaleFactor = 360f / maxOf(bitmap.width, bitmap.height)
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scaleFactor).toInt(),
                        (bitmap.height * scaleFactor).toInt(),
                        true
                    )
                } else {
                    bitmap
                }
                val stream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 45, stream)
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                signalingClient.sendVideoFrame(partnerCode, base64)
            } catch (_: Exception) {}
        }
    }

    fun setAppTheme(theme: AppTheme) {
        repository.setAppTheme(theme)
    }

    fun updateUserName(name: String) {
        repository.updateUserName(name)
    }

    fun sendFloatingHeart() {
        spawnFloatingHeart()
        signalingClient.sendHeartReaction(profile.value.partnerPairCode)
    }

    private fun spawnFloatingHeart() {
        val newHeart = FloatingHeart(
            id = System.nanoTime(),
            startXFraction = Random.nextFloat() * 0.7f + 0.15f,
            scale = Random.nextFloat() * 0.5f + 0.8f
        )
        val current = _floatingHearts.value.toMutableList()
        current.add(newHeart)
        _floatingHearts.value = current

        viewModelScope.launch {
            delay(2800)
            _floatingHearts.value = _floatingHearts.value.filter { it.id != newHeart.id }
        }
    }

    fun regenerateMyCode(): String {
        val newCode = repository.regenerateMyCode()
        val prof = profile.value
        signalingClient.connect(prof.serverUrl, newCode, prof.partnerPairCode)
        return newCode
    }

    fun updateProfile(partnerName: String, partnerNickname: String, partnerCode: String) {
        val cleanPartnerCode = partnerCode.trim().uppercase()
        val finalPartnerCode = if (cleanPartnerCode.equals("ROSE99", ignoreCase = true) ||
            cleanPartnerCode.equals(profile.value.myPairCode, ignoreCase = true)) {
            ""
        } else {
            cleanPartnerCode
        }
        val updated = profile.value.copy(
            partnerName = partnerName.ifBlank { "My Love" },
            partnerNickname = partnerNickname.ifBlank { "Sweetheart" },
            partnerPairCode = finalPartnerCode,
            isPaired = finalPartnerCode.isNotBlank()
        )
        repository.saveProfile(updated)
        signalingClient.connect(updated.serverUrl, updated.myPairCode, updated.partnerPairCode)
    }

    fun unlinkPartner() {
        repository.unlinkPartner()
        val prof = profile.value
        signalingClient.connect(prof.serverUrl, prof.myPairCode, "")
    }

    fun checkPartnerStatus() {
        if (profile.value.partnerPairCode.isNotBlank()) {
            signalingClient.checkPartnerOnline(profile.value.partnerPairCode)
        }
    }

    fun addMemoryPhoto(title: String, note: String, dateStr: String, uriString: String?) {
        repository.addMemory(title, note, dateStr, uriString)
    }

    fun setWallpaper(memoryId: String) {
        repository.setWallpaper(memoryId)
    }

    fun deleteMemory(memoryId: String) {
        repository.deleteMemory(memoryId)
    }

    fun postSharedNote(content: String) {
        val cleanContent = content.trim()
        if (cleanContent.isBlank()) return
        val prof = profile.value
        val note = CoupleNote(
            id = java.util.UUID.randomUUID().toString(),
            content = cleanContent,
            authorCode = prof.myPairCode,
            authorName = prof.userName,
            createdAtEpochMs = System.currentTimeMillis(),
            isFromPartner = false
        )
        repository.saveSharedNote(note)
        if (prof.isPaired && prof.partnerPairCode.isNotBlank()) {
            signalingClient.sendCoupleNote(
                to = prof.partnerPairCode,
                noteId = note.id,
                content = note.content,
                authorName = note.authorName,
                createdAt = note.createdAtEpochMs
            )
        }
    }

    fun deleteSharedNote() {
        val current = repository.sharedNote.value
        val prof = profile.value
        repository.deleteSharedNote()
        if (current != null && prof.isPaired && prof.partnerPairCode.isNotBlank()) {
            signalingClient.sendDeleteNote(
                to = prof.partnerPairCode,
                noteId = current.id
            )
        }
    }

    fun saveNote(title: String, content: String, isShared: Boolean = true, existingId: String? = null) {
        val cleanContent = content.trim()
        if (cleanContent.isBlank()) return
        val prof = profile.value
        val note = CoupleNote(
            id = existingId ?: java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            content = cleanContent,
            authorCode = prof.myPairCode,
            authorName = prof.userName,
            createdAtEpochMs = System.currentTimeMillis(),
            updatedAtEpochMs = System.currentTimeMillis(),
            isFromPartner = false,
            isShared = isShared
        )
        repository.addOrUpdateNote(note)
        if (isShared && prof.isPaired && prof.partnerPairCode.isNotBlank()) {
            val coupleId = firebaseRepo.getCoupleId(prof.myPairCode, prof.partnerPairCode)
            viewModelScope.launch {
                firebaseRepo.saveNote(coupleId, note.title, note.content, isShared = isShared, existingId = note.id)
            }
            signalingClient.sendCoupleNote(
                to = prof.partnerPairCode,
                noteId = note.id,
                content = note.content,
                authorName = note.authorName,
                createdAt = note.createdAtEpochMs
            )
        }
    }

    fun deleteNote(noteId: String) {
        val prof = profile.value
        repository.deleteNote(noteId)
        if (prof.isPaired && prof.partnerPairCode.isNotBlank()) {
            val coupleId = firebaseRepo.getCoupleId(prof.myPairCode, prof.partnerPairCode)
            viewModelScope.launch {
                firebaseRepo.deleteNote(coupleId, noteId)
            }
            signalingClient.sendDeleteNote(
                to = prof.partnerPairCode,
                noteId = noteId
            )
        }
    }

    fun checkNoteExpiry() {
        repository.checkNoteExpiry()
    }

    fun markChatAsRead() {
        repository.markMessagesAsRead()
        val prof = profile.value
        val partnerCode = prof.partnerPairCode.trim()
        if (partnerCode.isNotBlank()) {
            val coupleId = firebaseRepo.getCoupleId(prof.myPairCode, partnerCode)
            viewModelScope.launch {
                firebaseRepo.markMessagesAsRead(coupleId)
            }
        }
    }

    fun sendChatMessage(
        text: String,
        type: String = "text",
        mediaUrl: String? = null,
        voiceDurationSec: Int = 0,
        fileName: String? = null,
        fileSize: String? = null,
        replyToId: String? = null,
        replyToSender: String? = null,
        replyToText: String? = null,
        replyToType: String? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank() && mediaUrl.isNullOrBlank() && type != "voice") return
        val prof = profile.value
        val partnerCode = prof.partnerPairCode.trim()
        val msgId = java.util.UUID.randomUUID().toString()
        val msg = ChatMessage(
            id = msgId,
            fromCode = prof.myPairCode,
            toCode = partnerCode,
            senderName = prof.userName.ifBlank { "Me" },
            content = trimmed.ifBlank {
                when (type) {
                    "voice" -> "Voice Note"
                    "image" -> "Photo"
                    "video" -> "Video"
                    "file" -> fileName ?: "Document"
                    else -> "Message"
                }
            },
            messageType = type,
            timestamp = System.currentTimeMillis(),
            isOutgoing = true,
            isRead = false,
            deliveryStatus = "SENT",
            voiceDurationSeconds = voiceDurationSec,
            mediaUrl = mediaUrl,
            fileName = fileName,
            fileSize = fileSize,
            replyToId = replyToId,
            replyToSender = replyToSender,
            replyToText = replyToText,
            replyToType = replyToType
        )
        repository.saveChatMessage(msg)
        if (partnerCode.isNotBlank()) {
            val coupleId = firebaseRepo.getCoupleId(prof.myPairCode, partnerCode)
            viewModelScope.launch {
                firebaseRepo.sendMessage(
                    coupleId = coupleId,
                    text = msg.content,
                    type = type,
                    mediaUrl = mediaUrl ?: "",
                    voiceDurationSec = voiceDurationSec,
                    fileName = fileName,
                    fileSize = fileSize,
                    replyToId = replyToId,
                    replyToSender = replyToSender,
                    replyToText = replyToText,
                    replyToType = replyToType,
                    clientMessageId = msgId
                )
            }
            signalingClient.sendChatMessage(
                to = partnerCode,
                id = msg.id,
                content = msg.content,
                senderName = prof.partnerName,
                timestamp = msg.timestamp
            )
        }
    }

    fun deleteChatMessage(messageId: String) {
        repository.deleteMessage(messageId)
        val prof = profile.value
        val partnerCode = prof.partnerPairCode.trim()
        if (partnerCode.isNotBlank()) {
            val coupleId = firebaseRepo.getCoupleId(prof.myPairCode, partnerCode)
            viewModelScope.launch {
                firebaseRepo.deleteMessage(coupleId, messageId)
            }
        }
    }

    fun toggleStarMessage(messageId: String) {
        repository.toggleStarMessage(messageId)
    }

    private var lastSentTypingState: Boolean? = null
    fun setChatTyping(isTyping: Boolean) {
        if (lastSentTypingState == isTyping) return
        lastSentTypingState = isTyping
        val partnerCode = profile.value.partnerPairCode.trim()
        if (partnerCode.isNotBlank()) {
            signalingClient.sendChatTyping(partnerCode, isTyping)
        }
    }

    fun requestChatHistory() {
        val partnerCode = profile.value.partnerPairCode.trim()
        if (partnerCode.isNotBlank()) {
            signalingClient.requestChatHistory(partnerCode)
        }
    }

    fun clearChat() {
        repository.clearChatMessages()
    }

    fun openChat() {
        _isChatOpen.value = true
        requestChatHistory()
    }

    fun closeChat() {
        _isChatOpen.value = false
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        simulatedAnswerJob?.cancel()
        audioHaptics.release()
        audioStreamManager.stop()
        // DO NOT disconnect singleton signalingClient here so CallForegroundService stays connected 24/7!
    }
}
