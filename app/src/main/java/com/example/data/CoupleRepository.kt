package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.model.CoupleNote
import com.example.model.CoupleProfile
import com.example.model.MemoryPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class CoupleRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("amour_couple_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<CoupleProfile> = _profile.asStateFlow()

    private val _memories = MutableStateFlow(loadMemories())
    val memories: StateFlow<List<MemoryPhoto>> = _memories.asStateFlow()

    private val _callHistory = MutableStateFlow(loadCallHistory())
    val callHistory: StateFlow<List<CallRecord>> = _callHistory.asStateFlow()

    private val _sharedNote = MutableStateFlow<CoupleNote?>(loadSharedNote())
    val sharedNote: StateFlow<CoupleNote?> = _sharedNote.asStateFlow()

    private val _notesList = MutableStateFlow<List<CoupleNote>>(loadNotesList())
    val notes: StateFlow<List<CoupleNote>> = _notesList.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(loadChatMessages())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private fun generateUniqueLoveCode(): String {
        val prefixes = listOf("AMOUR", "LOVE", "ROSE", "SOUL", "HEART", "CHERI")
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = java.security.SecureRandom()
        val randomSuffix = (1..4).map { chars[random.nextInt(chars.length)] }.joinToString("")
        val prefix = prefixes[random.nextInt(prefixes.size)]
        return "$prefix-$randomSuffix"
    }

    private fun loadProfile(): CoupleProfile {
        val userName = prefs.getString("user_name", "You") ?: "You"
        val partnerName = prefs.getString("partner_name", "My Love") ?: "My Love"
        val partnerNickname = prefs.getString("partner_nickname", "Sweetheart") ?: "Sweetheart"

        val existingMyCode = prefs.getString("my_pair_code", null)
        val myPairCode = if (existingMyCode.isNullOrBlank() || existingMyCode == "LOVE89" || existingMyCode == "ROSE99") {
            val newCode = generateUniqueLoveCode()
            prefs.edit().putString("my_pair_code", newCode).apply()
            newCode
        } else {
            existingMyCode
        }

        var partnerPairCode = prefs.getString("partner_pair_code", "") ?: ""
        // Purge dummy/stale placeholders and avoid accidental self-pairing
        if (partnerPairCode == "ROSE99" || partnerPairCode == "LOVE89" || partnerPairCode.equals(myPairCode, ignoreCase = true)) {
            partnerPairCode = ""
            prefs.edit().putString("partner_pair_code", "").putBoolean("is_paired", false).apply()
        }
        val isPaired = partnerPairCode.isNotBlank()
        val startDate = prefs.getLong(
            "start_date",
            System.currentTimeMillis() - (520L * 24 * 60 * 60 * 1000L)
        )
        val status = prefs.getString("partner_status", "Thinking of you always ❤️") ?: "Thinking of you always ❤️"
        val avatarUri = prefs.getString("avatar_uri", null)
        val serverUrl = prefs.getString("server_url", "ws://10.0.2.2:3000") ?: "ws://10.0.2.2:3000"
        val isOnboardingCompleted = prefs.getBoolean("is_onboarding_completed", false)
        val isNotificationsMuted = prefs.getBoolean("is_notifications_muted", false)
        val themeStr = prefs.getString("app_theme", com.example.model.AppTheme.LOVE.name) ?: com.example.model.AppTheme.LOVE.name
        val appTheme = try {
            com.example.model.AppTheme.valueOf(themeStr)
        } catch (_: Exception) {
            com.example.model.AppTheme.LOVE
        }

        return CoupleProfile(
            userName = userName,
            partnerName = partnerName,
            partnerNickname = partnerNickname,
            myPairCode = myPairCode,
            partnerPairCode = partnerPairCode,
            isPaired = isPaired,
            relationshipStartDateEpochMs = startDate,
            partnerStatus = status,
            avatarUri = avatarUri,
            serverUrl = serverUrl,
            isOnboardingCompleted = isOnboardingCompleted,
            isNotificationsMuted = isNotificationsMuted,
            appTheme = appTheme
        )
    }

    fun regenerateMyCode(): String {
        val newCode = generateUniqueLoveCode()
        val updated = _profile.value.copy(myPairCode = newCode)
        saveProfile(updated)
        return newCode
    }

    fun unlinkPartner() {
        val updated = _profile.value.copy(
            partnerPairCode = "",
            isPaired = false
        )
        saveProfile(updated)
    }

    fun setAppTheme(theme: com.example.model.AppTheme) {
        val updated = _profile.value.copy(appTheme = theme)
        saveProfile(updated)
    }

    fun updateUserName(name: String) {
        val updated = _profile.value.copy(userName = name.ifBlank { "You" })
        saveProfile(updated)
    }

    fun saveProfile(profile: CoupleProfile) {
        val cleanPartnerCode = if (profile.partnerPairCode.equals("ROSE99", ignoreCase = true) ||
            profile.partnerPairCode.equals(profile.myPairCode, ignoreCase = true)) {
            ""
        } else {
            profile.partnerPairCode.trim().uppercase()
        }
        val actualIsPaired = cleanPartnerCode.isNotBlank()

        prefs.edit()
            .putString("user_name", profile.userName)
            .putString("partner_name", profile.partnerName)
            .putString("partner_nickname", profile.partnerNickname)
            .putString("my_pair_code", profile.myPairCode)
            .putString("partner_pair_code", cleanPartnerCode)
            .putBoolean("is_paired", actualIsPaired)
            .putLong("start_date", profile.relationshipStartDateEpochMs)
            .putString("partner_status", profile.partnerStatus)
            .putString("avatar_uri", profile.avatarUri)
            .putString("server_url", profile.serverUrl)
            .putBoolean("is_onboarding_completed", profile.isOnboardingCompleted)
            .putBoolean("is_notifications_muted", profile.isNotificationsMuted)
            .putString("app_theme", profile.appTheme.name)
            .apply()
        _profile.value = profile.copy(
            partnerPairCode = cleanPartnerCode,
            isPaired = actualIsPaired
        )
    }

    fun setNotificationsMuted(muted: Boolean) {
        saveProfile(_profile.value.copy(isNotificationsMuted = muted))
    }

    fun completeOnboarding() {
        saveProfile(_profile.value.copy(isOnboardingCompleted = true))
    }

    private fun loadMemories(): List<MemoryPhoto> {
        val jsonStr = prefs.getString("memories_json", null)
        if (jsonStr.isNullOrBlank()) {
            return defaultMemories()
        }
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<MemoryPhoto>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MemoryPhoto(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        note = obj.getString("note"),
                        dateStr = obj.getString("dateStr"),
                        uriString = obj.optString("uriString").takeIf { it.isNotBlank() },
                        isSelectedWallpaper = obj.optBoolean("isSelectedWallpaper", false),
                        gradientStartHex = obj.optLong("gradientStartHex", 0xFFE11D48),
                        gradientEndHex = obj.optLong("gradientEndHex", 0xFF4A052E)
                    )
                )
            }
            if (list.isEmpty()) defaultMemories() else list
        } catch (_: Exception) {
            defaultMemories()
        }
    }

    private fun defaultMemories(): List<MemoryPhoto> {
        return listOf(
            MemoryPhoto(
                id = "mem_1",
                title = "Our First Coffee Date",
                note = "The moment our eyes met and my heart knew you were the one.",
                dateStr = "May 14, 2024",
                isSelectedWallpaper = true,
                gradientStartHex = 0xFFE11D48,
                gradientEndHex = 0xFF4A052E
            ),
            MemoryPhoto(
                id = "mem_2",
                title = "Sunset by the Shore",
                note = "Holding your warm hands while the sky turned to gold and crimson.",
                dateStr = "August 22, 2024",
                isSelectedWallpaper = false,
                gradientStartHex = 0xFF9F1239,
                gradientEndHex = 0xFF1E0A1A
            ),
            MemoryPhoto(
                id = "mem_3",
                title = "Midnight Stargazing",
                note = "Counting shooting stars and wishing for every tomorrow with you.",
                dateStr = "December 31, 2024",
                isSelectedWallpaper = false,
                gradientStartHex = 0xFFBE185D,
                gradientEndHex = 0xFF0E0712
            )
        )
    }

    fun addMemory(title: String, note: String, dateStr: String, uriString: String?) {
        val current = _memories.value.toMutableList()
        val newMemory = MemoryPhoto(
            id = UUID.randomUUID().toString(),
            title = title,
            note = note,
            dateStr = dateStr,
            uriString = uriString,
            isSelectedWallpaper = current.isEmpty(),
            gradientStartHex = 0xFFF43F5E,
            gradientEndHex = 0xFF2A0824
        )
        current.add(0, newMemory)
        saveMemories(current)
    }

    fun setWallpaper(memoryId: String) {
        val updated = _memories.value.map {
            it.copy(isSelectedWallpaper = it.id == memoryId)
        }
        saveMemories(updated)
    }

    fun deleteMemory(memoryId: String) {
        val updated = _memories.value.filter { it.id != memoryId }
        saveMemories(if (updated.isEmpty()) defaultMemories() else updated)
    }

    private fun saveMemories(list: List<MemoryPhoto>) {
        val array = JSONArray()
        list.forEach { mem ->
            val obj = JSONObject()
            obj.put("id", mem.id)
            obj.put("title", mem.title)
            obj.put("note", mem.note)
            obj.put("dateStr", mem.dateStr)
            obj.put("uriString", mem.uriString ?: "")
            obj.put("isSelectedWallpaper", mem.isSelectedWallpaper)
            obj.put("gradientStartHex", mem.gradientStartHex)
            obj.put("gradientEndHex", mem.gradientEndHex)
            array.put(obj)
        }
        prefs.edit().putString("memories_json", array.toString()).apply()
        _memories.value = list
    }

    private fun loadCallHistory(): List<CallRecord> {
        val jsonStr = prefs.getString("call_history_json", null) ?: return defaultCallHistory()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CallRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CallRecord(
                        id = obj.getString("id"),
                        type = CallType.valueOf(obj.getString("type")),
                        timestamp = obj.getLong("timestamp"),
                        durationSeconds = obj.getInt("durationSeconds"),
                        isOutgoing = obj.getBoolean("isOutgoing")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun defaultCallHistory(): List<CallRecord> = emptyList()

    fun clearCallHistory() {
        prefs.edit().remove("call_history_json").apply()
        _callHistory.value = emptyList()
    }

    fun recordCall(type: CallType, durationSeconds: Int, isOutgoing: Boolean) {
        val current = _callHistory.value.toMutableList()
        val record = CallRecord(
            id = UUID.randomUUID().toString(),
            type = type,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            isOutgoing = isOutgoing
        )
        current.add(0, record)
        val trimmed = if (current.size > 50) current.take(50) else current

        val array = JSONArray()
        trimmed.forEach { rec ->
            val obj = JSONObject()
            obj.put("id", rec.id)
            obj.put("type", rec.type.name)
            obj.put("timestamp", rec.timestamp)
            obj.put("durationSeconds", rec.durationSeconds)
            obj.put("isOutgoing", rec.isOutgoing)
            array.put(obj)
        }
        prefs.edit().putString("call_history_json", array.toString()).apply()
        _callHistory.value = trimmed
    }

    private fun loadSharedNote(): CoupleNote? {
        val jsonStr = prefs.getString("shared_note_json", null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            val note = CoupleNote(
                id = obj.getString("id"),
                content = obj.getString("content"),
                authorCode = obj.optString("authorCode"),
                authorName = obj.optString("authorName"),
                createdAtEpochMs = obj.getLong("createdAtEpochMs"),
                isFromPartner = obj.optBoolean("isFromPartner", false)
            )
            if (note.isExpired) {
                prefs.edit().remove("shared_note_json").apply()
                null
            } else {
                note
            }
        } catch (_: Exception) {
            null
        }
    }

    fun checkNoteExpiry() {
        val current = _sharedNote.value
        if (current != null && current.isExpired) {
            deleteSharedNote()
        }
    }

    fun saveSharedNote(note: CoupleNote) {
        if (note.isExpired) {
            deleteSharedNote()
            return
        }
        val obj = JSONObject().apply {
            put("id", note.id)
            put("content", note.content)
            put("authorCode", note.authorCode)
            put("authorName", note.authorName)
            put("createdAtEpochMs", note.createdAtEpochMs)
            put("isFromPartner", note.isFromPartner)
        }
        prefs.edit().putString("shared_note_json", obj.toString()).apply()
        _sharedNote.value = note
    }

    fun deleteSharedNote() {
        prefs.edit().remove("shared_note_json").apply()
        _sharedNote.value = null
    }

    private fun loadNotesList(): List<CoupleNote> {
        val jsonStr = prefs.getString("couple_notes_json", null)
        if (jsonStr.isNullOrBlank()) {
            return defaultNotesList()
        }
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CoupleNote>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CoupleNote(
                        id = obj.getString("id"),
                        title = obj.optString("title", ""),
                        content = obj.getString("content"),
                        authorCode = obj.optString("authorCode"),
                        authorName = obj.optString("authorName"),
                        createdAtEpochMs = obj.getLong("createdAtEpochMs"),
                        updatedAtEpochMs = obj.optLong("updatedAtEpochMs", obj.getLong("createdAtEpochMs")),
                        isFromPartner = obj.optBoolean("isFromPartner", false),
                        isShared = obj.optBoolean("isShared", true)
                    )
                )
            }
            if (list.isEmpty()) defaultNotesList() else list
        } catch (_: Exception) {
            defaultNotesList()
        }
    }

    private fun defaultNotesList(): List<CoupleNote> {
        return listOf(
            CoupleNote(
                id = "note_welcome",
                title = "Welcome to Our Private Sanctuary ❤️",
                content = "This is our exclusive space to write love notes, secret reminders, and sweet thoughts meant only for our eyes.",
                authorCode = _profile.value.myPairCode,
                authorName = _profile.value.userName,
                createdAtEpochMs = System.currentTimeMillis() - (2 * 60 * 60 * 1000L),
                updatedAtEpochMs = System.currentTimeMillis() - (2 * 60 * 60 * 1000L),
                isFromPartner = false,
                isShared = true
            )
        )
    }

    fun addOrUpdateNote(note: CoupleNote) {
        val current = _notesList.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            current[index] = note
        } else {
            current.add(0, note)
        }
        saveNotesList(current)
        if (note.isShared) {
            saveSharedNote(note)
        }
    }

    fun deleteNote(noteId: String) {
        val current = _notesList.value.filter { it.id != noteId }
        saveNotesList(current)
        if (_sharedNote.value?.id == noteId) {
            deleteSharedNote()
        }
    }

    private fun saveNotesList(list: List<CoupleNote>) {
        val array = JSONArray()
        list.forEach { note ->
            val obj = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("authorCode", note.authorCode)
                put("authorName", note.authorName)
                put("createdAtEpochMs", note.createdAtEpochMs)
                put("updatedAtEpochMs", note.updatedAtEpochMs)
                put("isFromPartner", note.isFromPartner)
                put("isShared", note.isShared)
            }
            array.put(obj)
        }
        prefs.edit().putString("couple_notes_json", array.toString()).apply()
        _notesList.value = list
    }

    private fun loadChatMessages(): List<ChatMessage> {
        val jsonStr = prefs.getString("chat_messages_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        fromCode = obj.optString("fromCode"),
                        toCode = obj.optString("toCode"),
                        senderName = obj.optString("senderName"),
                        content = obj.optString("content"),
                        messageType = obj.optString("messageType", "text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isOutgoing = obj.optBoolean("isOutgoing", true),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveChatMessage(msg: ChatMessage) {
        val current = _chatMessages.value.toMutableList()
        if (current.none { it.id == msg.id }) {
            current.add(msg)
            val trimmed = if (current.size > 200) current.takeLast(200) else current
            persistChatMessages(trimmed)
            _chatMessages.value = trimmed
        }
    }

    fun setChatMessages(list: List<ChatMessage>) {
        val trimmed = if (list.size > 200) list.takeLast(200) else list
        persistChatMessages(trimmed)
        _chatMessages.value = trimmed
    }

    fun clearChatMessages() {
        prefs.edit().remove("chat_messages_json").apply()
        _chatMessages.value = emptyList()
    }

    private fun persistChatMessages(list: List<ChatMessage>) {
        val arr = JSONArray()
        list.forEach { msg ->
            val obj = JSONObject().apply {
                put("id", msg.id)
                put("fromCode", msg.fromCode)
                put("toCode", msg.toCode)
                put("senderName", msg.senderName)
                put("content", msg.content)
                put("messageType", msg.messageType)
                put("timestamp", msg.timestamp)
                put("isOutgoing", msg.isOutgoing)
                put("isRead", msg.isRead)
            }
            arr.put(obj)
        }
        prefs.edit().putString("chat_messages_json", arr.toString()).apply()
    }
}
