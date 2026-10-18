package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calling.CallAudioHaptics
import com.example.calling.CallForegroundService
import com.example.calling.CallNotificationHelper
import com.example.data.CoupleRepository
import com.example.model.CallRecord
import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.CoupleProfile
import com.example.model.FloatingHeart
import com.example.model.MemoryPhoto
import com.example.network.ServerConnectionState
import com.example.network.SignalingClient
import com.example.network.SignalingEvent
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
import kotlin.random.Random

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    DASHBOARD,
    MEMORIES,
    PAIRING,
    DEVELOPER
}

class LoveCallViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CoupleRepository(application)
    private val audioHaptics = CallAudioHaptics(application)
    private val signalingClient = SignalingClient()

    val profile: StateFlow<CoupleProfile> = repository.profile
    val memories: StateFlow<List<MemoryPhoto>> = repository.memories
    val callHistory: StateFlow<List<CallRecord>> = repository.callHistory
    val serverConnectionState: StateFlow<ServerConnectionState> = signalingClient.connectionState

    val activeWallpaper: StateFlow<MemoryPhoto?> = repository.memories
        .map { list -> list.firstOrNull { it.isSelectedWallpaper } ?: list.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _callSession = MutableStateFlow(CallSession())
    val callSession: StateFlow<CallSession> = _callSession.asStateFlow()

    private val _floatingHearts = MutableStateFlow<List<FloatingHeart>>(emptyList())
    val floatingHearts: StateFlow<List<FloatingHeart>> = _floatingHearts.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private var callTimerJob: Job? = null
    private var simulatedAnswerJob: Job? = null

    init {
        CallNotificationHelper.initChannels(application)

        // Connect to signaling server
        val prof = profile.value
        signalingClient.connect(prof.serverUrl, prof.myPairCode, prof.partnerPairCode)

        // Start Foreground Service if onboarding is already completed
        if (prof.isOnboardingCompleted) {
            CallForegroundService.start(application)
        }

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
                        triggerIncomingCall(type)
                    }
                    is SignalingEvent.CallAccepted -> {
                        acceptCall(isOutgoingInitiated = true)
                    }
                    is SignalingEvent.CallDeclined -> {
                        declineCall(notifyPartner = false)
                    }
                    is SignalingEvent.CallEnded -> {
                        endCall(notifyPartner = false)
                    }
                    is SignalingEvent.CallUnavailable -> {
                        val status = "Partner currently offline"
                        repository.saveProfile(profile.value.copy(partnerStatus = status))
                    }
                    is SignalingEvent.PartnerStatus -> {
                        val status = if (event.isOnline) "Partner Online ❤️" else "Thinking of you always ❤️"
                        repository.saveProfile(profile.value.copy(partnerStatus = status))
                    }
                    is SignalingEvent.HeartReaction -> {
                        spawnFloatingHeart()
                    }
                }
            }
        }
    }

    fun onSplashFinished() {
        if (profile.value.isOnboardingCompleted) {
            _currentScreen.value = AppScreen.DASHBOARD
        } else {
            _currentScreen.value = AppScreen.ONBOARDING
        }
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

    fun reconnectServer(newServerUrl: String) {
        val updated = profile.value.copy(serverUrl = newServerUrl.trim())
        repository.saveProfile(updated)
        signalingClient.connect(updated.serverUrl, updated.myPairCode, updated.partnerPairCode)
    }

    fun startOutgoingCall(type: CallType) {
        if (!profile.value.isPaired || profile.value.partnerPairCode.isBlank()) {
            _currentScreen.value = AppScreen.PAIRING
            return
        }

        _callSession.value = CallSession(
            status = CallStatus.OUTGOING_RINGING,
            type = type,
            durationSeconds = 0,
            isSpeakerOn = type == CallType.VIDEO
        )
        audioHaptics.setSpeakerphone(type == CallType.VIDEO)
        audioHaptics.startOutgoingRinging(viewModelScope)

        // Send signal to partner via WebSocket
        signalingClient.sendCallInitiate(
            to = profile.value.partnerPairCode,
            callerName = profile.value.partnerNickname,
            callType = type.name
        )

        // Ringing timeout (40 seconds)
        simulatedAnswerJob?.cancel()
        simulatedAnswerJob = viewModelScope.launch {
            delay(40000)
            if (_callSession.value.status == CallStatus.OUTGOING_RINGING) {
                endCall(notifyPartner = false)
            }
        }
    }

    fun startSelfTestCall(type: CallType) {
        _callSession.value = CallSession(
            status = CallStatus.CONNECTED,
            type = type,
            durationSeconds = 0,
            isSpeakerOn = type == CallType.VIDEO
        )
        audioHaptics.setSpeakerphone(type == CallType.VIDEO)
        audioHaptics.playConnectedChime()

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _callSession.value = _callSession.value.copy(
                    durationSeconds = _callSession.value.durationSeconds + 1
                )
            }
        }
    }

    fun triggerIncomingCall(type: CallType) {
        val partner = profile.value.partnerName
        _callSession.value = CallSession(
            status = CallStatus.INCOMING_RINGING,
            type = type,
            durationSeconds = 0,
            isSpeakerOn = type == CallType.VIDEO
        )
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

    fun acceptCall(isOutgoingInitiated: Boolean = false) {
        simulatedAnswerJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playConnectedChime()

        val currentType = _callSession.value.type
        _callSession.value = _callSession.value.copy(
            status = CallStatus.CONNECTED,
            durationSeconds = 0,
            isSpeakerOn = currentType == CallType.VIDEO
        )
        audioHaptics.setSpeakerphone(currentType == CallType.VIDEO)

        if (!isOutgoingInitiated) {
            signalingClient.sendCallAccept(profile.value.partnerPairCode)
        }

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _callSession.value = _callSession.value.copy(
                    durationSeconds = _callSession.value.durationSeconds + 1
                )
            }
        }
    }

    fun declineCall(notifyPartner: Boolean = true) {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.stopAll()

        if (notifyPartner) {
            signalingClient.sendCallDecline(profile.value.partnerPairCode)
        }

        repository.recordCall(
            type = _callSession.value.type,
            durationSeconds = 0,
            isOutgoing = false
        )

        _callSession.value = CallSession(status = CallStatus.IDLE)
    }

    fun endCall(notifyPartner: Boolean = true) {
        simulatedAnswerJob?.cancel()
        callTimerJob?.cancel()
        CallNotificationHelper.cancelNotification(getApplication())
        audioHaptics.playEndCallTone()
        audioHaptics.stopAll()

        if (notifyPartner) {
            signalingClient.sendCallEnd(profile.value.partnerPairCode)
        }

        val duration = _callSession.value.durationSeconds
        val type = _callSession.value.type
        val wasConnected = _callSession.value.status == CallStatus.CONNECTED

        if (wasConnected && duration > 0) {
            repository.recordCall(
                type = type,
                durationSeconds = duration,
                isOutgoing = true
            )
        }

        _callSession.value = CallSession(status = CallStatus.IDLE)
    }

    fun toggleMute() {
        val next = !_callSession.value.isMuted
        _callSession.value = _callSession.value.copy(isMuted = next)
        audioHaptics.setMicrophoneMute(next)
    }

    fun toggleSpeaker() {
        val next = !_callSession.value.isSpeakerOn
        _callSession.value = _callSession.value.copy(isSpeakerOn = next)
        audioHaptics.setSpeakerphone(next)
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

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        simulatedAnswerJob?.cancel()
        audioHaptics.release()
        signalingClient.disconnect()
    }
}
