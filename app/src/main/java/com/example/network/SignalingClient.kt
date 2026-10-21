package com.example.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.example.model.ChatMessage

enum class ServerConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

sealed class SignalingEvent {
    data class IncomingCall(val from: String, val callerName: String, val callType: String) : SignalingEvent()
    data class CallRinging(val from: String) : SignalingEvent()
    data class CallAccepted(val from: String) : SignalingEvent()
    data class CallDeclined(val from: String) : SignalingEvent()
    data class CallEnded(val from: String) : SignalingEvent()
    data class CallUnavailable(val reason: String) : SignalingEvent()
    data class PartnerStatus(val isOnline: Boolean) : SignalingEvent()
    data class AudioChunk(val from: String, val audioData: String) : SignalingEvent()
    data class VideoFrame(val from: String, val frameData: String) : SignalingEvent()
    object HeartReaction : SignalingEvent()
    data class PartnerNoteReceived(
        val from: String,
        val noteId: String,
        val content: String,
        val authorName: String,
        val createdAt: Long
    ) : SignalingEvent()
    data class PartnerNoteDeleted(val from: String, val noteId: String) : SignalingEvent()
    data class ChatMessageReceived(val message: ChatMessage) : SignalingEvent()
    data class ChatTyping(val from: String, val isTyping: Boolean) : SignalingEvent()
    data class ChatHistoryLoaded(val messages: List<ChatMessage>) : SignalingEvent()
}

class SignalingClient private constructor() {
    companion object {
        @Volatile
        private var instance: SignalingClient? = null

        fun getInstance(): SignalingClient {
            return instance ?: synchronized(this) {
                instance ?: SignalingClient().also { instance = it }
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var reconnectJob: Job? = null

    private val _connectionState = MutableStateFlow(ServerConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ServerConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<SignalingEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SignalingEvent> = _events.asSharedFlow()

    private var currentServerUrl: String = ""
    private var currentMyCode: String = ""
    private var currentPartnerCode: String = ""
    private var isExplicitDisconnect = false

    fun connect(serverUrl: String, myCode: String, partnerCode: String) {
        if (myCode.isBlank()) return

        currentServerUrl = serverUrl
        currentMyCode = myCode.uppercase().trim()
        currentPartnerCode = partnerCode.uppercase().trim()
        isExplicitDisconnect = false

        if (webSocket != null && _connectionState.value == ServerConnectionState.CONNECTED) {
            // Already connected, send updated registration if needed
            val regMsg = JSONObject().apply {
                put("type", "register")
                put("pairCode", currentMyCode)
                put("partnerPairCode", currentPartnerCode)
            }
            send(regMsg)
            return
        }

        reconnectJob?.cancel()
        performConnect()
    }

    private fun performConnect() {
        try {
            webSocket?.close(1000, "Reconnecting")
            webSocket = null
        } catch (_: Exception) {}

        val cleanUrl = if (!currentServerUrl.startsWith("ws://") && !currentServerUrl.startsWith("wss://")) {
            "ws://$currentServerUrl"
        } else {
            currentServerUrl
        }

        _connectionState.value = ServerConnectionState.CONNECTING

        val request = try {
            Request.Builder().url(cleanUrl).build()
        } catch (e: Exception) {
            Log.e("SignalingClient", "Invalid URL: $currentServerUrl", e)
            _connectionState.value = ServerConnectionState.DISCONNECTED
            scheduleReconnect()
            return
        }

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ServerConnectionState.CONNECTED
                Log.d("SignalingClient", "WebSocket connected. Registering as #$currentMyCode")

                // Register with server
                val regMsg = JSONObject().apply {
                    put("type", "register")
                    put("pairCode", currentMyCode)
                    put("partnerPairCode", currentPartnerCode)
                }
                webSocket.send(regMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ServerConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w("SignalingClient", "WebSocket connection issue: ${t.message}")
                _connectionState.value = ServerConnectionState.DISCONNECTED
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ServerConnectionState.DISCONNECTED
                if (!isExplicitDisconnect) {
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (isExplicitDisconnect || currentMyCode.isBlank()) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(3000)
            if (_connectionState.value != ServerConnectionState.CONNECTED && !isExplicitDisconnect) {
                Log.d("SignalingClient", "Attempting automatic reconnection to $currentServerUrl...")
                performConnect()
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            when (json.optString("type")) {
                "call_incoming" -> {
                    val from = json.optString("from")
                    val callerName = json.optString("callerName", "My Love")
                    val callType = json.optString("callType", "VOICE")
                    scope.launch {
                        _events.emit(SignalingEvent.IncomingCall(from, callerName, callType))
                    }
                }
                "call_ringing" -> {
                    scope.launch {
                        _events.emit(SignalingEvent.CallRinging(json.optString("from")))
                    }
                }
                "call_accepted" -> {
                    scope.launch {
                        _events.emit(SignalingEvent.CallAccepted(json.optString("from")))
                    }
                }
                "call_declined" -> {
                    scope.launch {
                        _events.emit(SignalingEvent.CallDeclined(json.optString("from")))
                    }
                }
                "call_ended" -> {
                    scope.launch {
                        _events.emit(SignalingEvent.CallEnded(json.optString("from")))
                    }
                }
                "call_unavailable" -> {
                    val reason = json.optString("reason", "Partner is currently offline")
                    scope.launch {
                        _events.emit(SignalingEvent.CallUnavailable(reason))
                    }
                }
                "partner_status" -> {
                    val isOnline = json.optBoolean("isOnline", false)
                    scope.launch {
                        _events.emit(SignalingEvent.PartnerStatus(isOnline))
                    }
                }
                "audio_chunk" -> {
                    val from = json.optString("from")
                    val audioData = json.optString("audioData", "")
                    if (audioData.isNotBlank()) {
                        scope.launch {
                            _events.emit(SignalingEvent.AudioChunk(from, audioData))
                        }
                    }
                }
                "video_frame" -> {
                    val from = json.optString("from")
                    val frameData = json.optString("frameData", "")
                    if (frameData.isNotBlank()) {
                        scope.launch {
                            _events.emit(SignalingEvent.VideoFrame(from, frameData))
                        }
                    }
                }
                "heart_reaction" -> {
                    scope.launch {
                        _events.emit(SignalingEvent.HeartReaction)
                    }
                }
                "couple_note" -> {
                    val from = json.optString("from")
                    val noteId = json.optString("noteId")
                    val content = json.optString("content")
                    val authorName = json.optString("authorName", "Partner")
                    val createdAt = json.optLong("createdAt", System.currentTimeMillis())
                    if (content.isNotBlank()) {
                        scope.launch {
                            _events.emit(
                                SignalingEvent.PartnerNoteReceived(
                                    from = from,
                                    noteId = noteId,
                                    content = content,
                                    authorName = authorName,
                                    createdAt = createdAt
                                )
                            )
                        }
                    }
                }
                "delete_note" -> {
                    val from = json.optString("from")
                    val noteId = json.optString("noteId")
                    scope.launch {
                        _events.emit(SignalingEvent.PartnerNoteDeleted(from, noteId))
                    }
                }
                "chat_message" -> {
                    val id = json.optString("id", java.util.UUID.randomUUID().toString())
                    val from = json.optString("from")
                    val to = json.optString("to")
                    val senderName = json.optString("senderName", "Partner")
                    val content = json.optString("content")
                    val messageType = json.optString("messageType", "text")
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())
                    val chatMsg = ChatMessage(
                        id = id,
                        fromCode = from,
                        toCode = to,
                        senderName = senderName,
                        content = content,
                        messageType = messageType,
                        timestamp = timestamp,
                        isOutgoing = false,
                        isRead = false
                    )
                    scope.launch {
                        _events.emit(SignalingEvent.ChatMessageReceived(chatMsg))
                    }
                }
                "chat_typing" -> {
                    val from = json.optString("from")
                    val isTyping = json.optBoolean("isTyping", false)
                    scope.launch {
                        _events.emit(SignalingEvent.ChatTyping(from, isTyping))
                    }
                }
                "chat_history" -> {
                    val array = json.optJSONArray("messages")
                    val list = mutableListOf<ChatMessage>()
                    if (array != null) {
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val from = item.optString("fromCode")
                            val isOut = from.equals(currentMyCode, ignoreCase = true)
                            list.add(
                                ChatMessage(
                                    id = item.optString("id"),
                                    fromCode = from,
                                    toCode = item.optString("toCode"),
                                    senderName = item.optString("senderName"),
                                    content = item.optString("content"),
                                    messageType = item.optString("messageType", "text"),
                                    timestamp = item.optLong("timestamp"),
                                    isOutgoing = isOut,
                                    isRead = item.optBoolean("isRead", false)
                                )
                            )
                        }
                    }
                    scope.launch {
                        _events.emit(SignalingEvent.ChatHistoryLoaded(list))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SignalingClient", "Failed to parse message: $text", e)
        }
    }

    fun checkPartnerOnline(partnerCode: String) {
        val payload = JSONObject().apply {
            put("type", "check_partner")
            put("partnerCode", partnerCode.uppercase().trim())
        }
        send(payload)
    }

    fun sendCallInitiate(to: String, callerName: String, callType: String) {
        val payload = JSONObject().apply {
            put("type", "call_initiate")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("callerName", callerName)
            put("callType", callType)
        }
        send(payload)
    }

    fun sendCallRinging(to: String) {
        val payload = JSONObject().apply {
            put("type", "call_ringing")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
        }
        send(payload)
    }

    fun sendCallAccept(to: String) {
        val payload = JSONObject().apply {
            put("type", "call_accept")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
        }
        send(payload)
    }

    fun sendCallDecline(to: String) {
        val payload = JSONObject().apply {
            put("type", "call_decline")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
        }
        send(payload)
    }

    fun sendCallEnd(to: String) {
        val payload = JSONObject().apply {
            put("type", "call_end")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
        }
        send(payload)
    }

    fun sendAudioChunk(to: String, audioBase64: String) {
        if (webSocket == null || audioBase64.isBlank()) return
        val payload = JSONObject().apply {
            put("type", "audio_chunk")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("audioData", audioBase64)
        }
        send(payload)
    }

    fun sendVideoFrame(to: String, frameBase64: String) {
        if (webSocket == null || frameBase64.isBlank()) return
        val payload = JSONObject().apply {
            put("type", "video_frame")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("frameData", frameBase64)
        }
        send(payload)
    }

    fun sendHeartReaction(to: String) {
        val payload = JSONObject().apply {
            put("type", "heart_reaction")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
        }
        send(payload)
    }

    fun sendCoupleNote(to: String, noteId: String, content: String, authorName: String, createdAt: Long) {
        val payload = JSONObject().apply {
            put("type", "couple_note")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("noteId", noteId)
            put("content", content)
            put("authorName", authorName)
            put("createdAt", createdAt)
        }
        send(payload)
    }

    fun sendDeleteNote(to: String, noteId: String) {
        val payload = JSONObject().apply {
            put("type", "delete_note")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("noteId", noteId)
        }
        send(payload)
    }

    fun sendChatMessage(to: String, id: String, content: String, senderName: String, timestamp: Long) {
        val payload = JSONObject().apply {
            put("type", "chat_message")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("id", id)
            put("content", content)
            put("senderName", senderName)
            put("timestamp", timestamp)
        }
        send(payload)
    }

    fun sendChatTyping(to: String, isTyping: Boolean) {
        val payload = JSONObject().apply {
            put("type", "chat_typing")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
            put("isTyping", isTyping)
        }
        send(payload)
    }

    fun requestChatHistory(partnerCode: String) {
        val payload = JSONObject().apply {
            put("type", "chat_history_request")
            put("from", currentMyCode)
            put("to", partnerCode.uppercase().trim())
        }
        send(payload)
    }

    fun sendChatRead(to: String) {
        val payload = JSONObject().apply {
            put("type", "chat_read")
            put("from", currentMyCode)
            put("to", to.uppercase().trim())
        }
        send(payload)
    }

    private fun send(json: JSONObject) {
        try {
            webSocket?.send(json.toString())
        } catch (_: Exception) {}
    }

    fun disconnect() {
        isExplicitDisconnect = true
        reconnectJob?.cancel()
        try {
            webSocket?.close(1000, "App closed")
            webSocket = null
        } catch (_: Exception) {}
        _connectionState.value = ServerConnectionState.DISCONNECTED
    }
}
