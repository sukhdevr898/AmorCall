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
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class CoupleRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("amour_couple_prefs", Context.MODE_PRIVATE)
    private val callDb = com.example.data.db.CallHistoryDatabase.getInstance(context)
    private val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

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

    private val _adminNotices = MutableStateFlow(loadAdminNotices())
    val adminNotices: StateFlow<List<com.example.model.AdminNotice>> = _adminNotices.asStateFlow()

    private fun generateUniqueLoveCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = java.security.SecureRandom()
        return (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun loadProfile(): CoupleProfile {
        val userName = prefs.getString("user_name", "You") ?: "You"
        val fullName = prefs.getString("full_name", userName) ?: userName
        val username = prefs.getString("user_username", "duora_user") ?: "duora_user"
        val partnerName = prefs.getString("partner_name", "My Love") ?: "My Love"
        val partnerUsername = prefs.getString("partner_username", "") ?: ""
        val partnerNickname = prefs.getString("partner_nickname", "Sweetheart") ?: "Sweetheart"
        val statusMsg = prefs.getString("status_msg", "Always together ❤️") ?: "Always together ❤️"

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
        val avatarId = prefs.getString("avatar_id", "boy_001") ?: "boy_001"
        val partnerAvatarId = prefs.getString("partner_avatar_id", "girl_001") ?: "girl_001"
        val accentColorHex = prefs.getLong("accent_color_hex", 0xFFFF2D8DL)
        val serverUrl = "ws://amorcall.onrender.com/"
        val isOnboardingCompleted = prefs.getBoolean("is_onboarding_completed", true)
        val isNotificationsMuted = prefs.getBoolean("is_notifications_muted", false)
        val themeStr = prefs.getString("app_theme", com.example.model.AppTheme.LOVE.name) ?: com.example.model.AppTheme.LOVE.name
        val appTheme = try {
            com.example.model.AppTheme.valueOf(themeStr)
        } catch (_: Exception) {
            com.example.model.AppTheme.LOVE
        }
        val isHideRecent = prefs.getBoolean("hide_from_recent", false)
        val isAppLock = prefs.getBoolean("app_lock_enabled", false)
        val isBlurNotif = prefs.getBoolean("blur_notifications", false)
        val isMsgNotif = prefs.getBoolean("msg_notif_enabled", true)
        val isCallNotif = prefs.getBoolean("call_notif_enabled", true)
        val isInAppNotif = prefs.getBoolean("in_app_notif_enabled", true)
        val soundPref = prefs.getString("sound_pref", "Default") ?: "Default"
        val vibPref = prefs.getString("vib_pref", "Default") ?: "Default"
        val ringPref = prefs.getString("ring_pref", "Default") ?: "Default"
        val blueTick = prefs.getBoolean("blue_tick", false)
        val greenTick = prefs.getBoolean("green_tick", false)
        val isVerified = prefs.getBoolean("is_verified", false) || blueTick
        val partnerBlueTick = prefs.getBoolean("partner_blue_tick", false)
        val partnerGreenTick = prefs.getBoolean("partner_green_tick", false)
        val partnerIsVerified = prefs.getBoolean("partner_is_verified", false) || partnerBlueTick
        val avatarUrl = prefs.getString("avatar_url", null)
        val partnerAvatarUrl = prefs.getString("partner_avatar_url", null)
        val isBlocked = prefs.getBoolean("is_blocked", false)
        val appLockPin = prefs.getString("app_lock_pin", "") ?: ""
        val securityQuestion = prefs.getString("security_question", "What is your favorite color?") ?: "What is your favorite color?"
        val securityAnswer = prefs.getString("security_answer", "") ?: ""

        return CoupleProfile(
            userName = userName,
            fullName = fullName,
            username = username,
            partnerName = partnerName,
            partnerUsername = partnerUsername,
            partnerNickname = partnerNickname,
            myPairCode = myPairCode,
            partnerPairCode = partnerPairCode,
            isPaired = isPaired,
            relationshipStartDateEpochMs = startDate,
            partnerStatus = status,
            statusMsg = statusMsg,
            avatarUri = avatarUri,
            avatarId = avatarId,
            partnerAvatarId = partnerAvatarId,
            accentColorHex = accentColorHex,
            serverUrl = serverUrl,
            isOnboardingCompleted = isOnboardingCompleted,
            isNotificationsMuted = isNotificationsMuted,
            appTheme = appTheme,
            isHideFromRecentApps = isHideRecent,
            isAppLockEnabled = isAppLock,
            isBlurNotifications = isBlurNotif,
            isMessageNotificationsEnabled = isMsgNotif,
            isCallNotificationsEnabled = isCallNotif,
            isInAppNotificationsEnabled = isInAppNotif,
            notificationSound = soundPref,
            notificationVibration = vibPref,
            notificationRingtone = ringPref,
            blueTick = blueTick,
            greenTick = greenTick,
            isVerified = isVerified,
            partnerBlueTick = partnerBlueTick,
            partnerGreenTick = partnerGreenTick,
            partnerIsVerified = partnerIsVerified,
            avatarUrl = avatarUrl,
            partnerAvatarUrl = partnerAvatarUrl,
            isBlocked = isBlocked,
            appLockPin = appLockPin,
            securityQuestion = securityQuestion,
            securityAnswer = securityAnswer
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

    fun updateAvatarId(newAvatarId: String) {
        val updated = _profile.value.copy(avatarId = newAvatarId)
        saveProfile(updated)
    }

    fun updatePartnerAvatarId(newAvatarId: String) {
        val updated = _profile.value.copy(partnerAvatarId = newAvatarId)
        saveProfile(updated)
    }

    fun updateAccentColor(colorHex: Long) {
        val updated = _profile.value.copy(accentColorHex = colorHex)
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
            .putString("full_name", profile.fullName)
            .putString("user_username", profile.username)
            .putString("partner_name", profile.partnerName)
            .putString("partner_username", profile.partnerUsername)
            .putString("partner_nickname", profile.partnerNickname)
            .putString("status_msg", profile.statusMsg)
            .putString("my_pair_code", profile.myPairCode)
            .putString("partner_pair_code", cleanPartnerCode)
            .putBoolean("is_paired", actualIsPaired)
            .putLong("start_date", profile.relationshipStartDateEpochMs)
            .putString("partner_status", profile.partnerStatus)
            .putString("avatar_uri", profile.avatarUri)
            .putString("avatar_id", profile.avatarId)
            .putString("partner_avatar_id", profile.partnerAvatarId)
            .putLong("accent_color_hex", profile.accentColorHex)
            .putString("server_url", profile.serverUrl)
            .putBoolean("is_onboarding_completed", profile.isOnboardingCompleted)
            .putBoolean("is_notifications_muted", profile.isNotificationsMuted)
            .putString("app_theme", profile.appTheme.name)
            .putBoolean("hide_from_recent", profile.isHideFromRecentApps)
            .putBoolean("app_lock_enabled", profile.isAppLockEnabled)
            .putBoolean("blur_notifications", profile.isBlurNotifications)
            .putBoolean("msg_notif_enabled", profile.isMessageNotificationsEnabled)
            .putBoolean("call_notif_enabled", profile.isCallNotificationsEnabled)
            .putBoolean("in_app_notif_enabled", profile.isInAppNotificationsEnabled)
            .putString("sound_pref", profile.notificationSound)
            .putString("vib_pref", profile.notificationVibration)
            .putString("ring_pref", profile.notificationRingtone)
            .putBoolean("blue_tick", profile.blueTick)
            .putBoolean("green_tick", profile.greenTick)
            .putBoolean("is_verified", profile.isVerified)
            .putBoolean("partner_blue_tick", profile.partnerBlueTick)
            .putBoolean("partner_green_tick", profile.partnerGreenTick)
            .putBoolean("partner_is_verified", profile.partnerIsVerified)
            .putString("avatar_url", profile.avatarUrl)
            .putString("partner_avatar_url", profile.partnerAvatarUrl)
            .putBoolean("is_blocked", profile.isBlocked)
            .putString("app_lock_pin", profile.appLockPin)
            .putString("security_question", profile.securityQuestion)
            .putString("security_answer", profile.securityAnswer)
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
        return try {
            val dbCalls = callDb.queryCallsSync()
            if (dbCalls.isNotEmpty()) {
                dbCalls
            } else {
                val jsonStr = prefs.getString("call_history_json", null) ?: return defaultCallHistory()
                val array = JSONArray(jsonStr)
                val list = mutableListOf<CallRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val rec = CallRecord(
                        id = obj.getString("id"),
                        type = CallType.valueOf(obj.getString("type")),
                        timestamp = obj.getLong("timestamp"),
                        durationSeconds = obj.getInt("durationSeconds"),
                        isOutgoing = obj.getBoolean("isOutgoing")
                    )
                    list.add(rec)
                    kotlinx.coroutines.runBlocking {
                        callDb.insertCall(rec)
                    }
                }
                list
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun defaultCallHistory(): List<CallRecord> = emptyList()

    fun clearCallHistory() {
        prefs.edit().remove("call_history_json").apply()
        _callHistory.value = emptyList()
        coroutineScope.launch {
            callDb.clearHistory()
        }
    }

    fun deleteCallRecord(id: String) {
        val current = _callHistory.value.filter { it.id != id }
        _callHistory.value = current
        coroutineScope.launch {
            callDb.deleteCall(id)
        }
    }

    fun recordCall(type: CallType, durationSeconds: Int, isOutgoing: Boolean) {
        val record = CallRecord(
            id = UUID.randomUUID().toString(),
            type = type,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            isOutgoing = isOutgoing
        )
        val current = _callHistory.value.toMutableList()
        current.add(0, record)
        val trimmed = if (current.size > 100) current.take(100) else current
        _callHistory.value = trimmed

        val p = _profile.value
        coroutineScope.launch {
            callDb.insertCall(
                record = record,
                partnerCode = p.partnerPairCode,
                partnerName = p.partnerName,
                partnerAvatar = p.partnerAvatarUrl ?: p.partnerAvatarId
            )
        }
    }

    private fun loadAdminNotices(): List<com.example.model.AdminNotice> {
        val jsonStr = prefs.getString("admin_notices_json", null) ?: return defaultAdminNotices()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<com.example.model.AdminNotice>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    com.example.model.AdminNotice(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        message = obj.getString("message"),
                        timestamp = obj.getLong("timestamp"),
                        isRead = obj.optBoolean("isRead", false),
                        isAlert = obj.optBoolean("isAlert", false)
                    )
                )
            }
            if (list.isEmpty()) defaultAdminNotices() else list
        } catch (_: Exception) {
            defaultAdminNotices()
        }
    }

    private fun defaultAdminNotices(): List<com.example.model.AdminNotice> {
        return listOf(
            com.example.model.AdminNotice(
                id = "admin_welcome",
                title = "Welcome to Duora Official",
                message = "Welcome to Duora! Your private love sanctuary is end-to-end connected and fully encrypted. Official updates and server announcements will appear here.",
                timestamp = System.currentTimeMillis() - 3600000L,
                isRead = true,
                isAlert = false
            )
        )
    }

    fun saveAdminNotice(notice: com.example.model.AdminNotice) {
        val current = _adminNotices.value.toMutableList()
        val index = current.indexOfFirst { it.id == notice.id }
        if (index >= 0) {
            current[index] = notice
        } else {
            current.add(0, notice)
        }
        val trimmed = if (current.size > 50) current.take(50) else current
        persistAdminNotices(trimmed)
        _adminNotices.value = trimmed
    }

    fun markAdminNoticeRead(id: String) {
        val current = _adminNotices.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        persistAdminNotices(current)
        _adminNotices.value = current
    }

    fun clearAdminNotices() {
        prefs.edit().remove("admin_notices_json").apply()
        _adminNotices.value = defaultAdminNotices()
    }

    private fun persistAdminNotices(list: List<com.example.model.AdminNotice>) {
        val arr = JSONArray()
        list.forEach { notice ->
            val obj = JSONObject().apply {
                put("id", notice.id)
                put("title", notice.title)
                put("message", notice.message)
                put("timestamp", notice.timestamp)
                put("isRead", notice.isRead)
                put("isAlert", notice.isAlert)
            }
            arr.put(obj)
        }
        prefs.edit().putString("admin_notices_json", arr.toString()).apply()
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
        val jsonStr = prefs.getString("chat_messages_json", null) ?: return defaultChatMessages()
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
                        isRead = obj.optBoolean("isRead", false),
                        deliveryStatus = obj.optString("deliveryStatus", if (obj.optBoolean("isOutgoing", true)) "READ" else "DELIVERED"),
                        voiceDurationSeconds = obj.optInt("voiceDurationSeconds", 0),
                        mediaUrl = if (obj.has("mediaUrl")) obj.getString("mediaUrl") else null,
                        fileName = if (obj.has("fileName")) obj.getString("fileName") else null,
                        fileSize = if (obj.has("fileSize")) obj.getString("fileSize") else null,
                        replyToId = if (obj.has("replyToId")) obj.getString("replyToId") else null,
                        replyToSender = if (obj.has("replyToSender")) obj.getString("replyToSender") else null,
                        replyToText = if (obj.has("replyToText")) obj.getString("replyToText") else null,
                        replyToType = if (obj.has("replyToType")) obj.getString("replyToType") else null,
                        isStarred = obj.optBoolean("isStarred", false),
                        isDeleted = obj.optBoolean("isDeleted", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            defaultChatMessages()
        }
    }

    private fun defaultChatMessages(): List<ChatMessage> {
        val now = System.currentTimeMillis()
        val partner = prefs.getString("partner_name", "My Love") ?: "My Love"
        return listOf(
            ChatMessage(
                id = "default_1",
                senderName = partner,
                content = "Good Morning baby 💕",
                timestamp = now - 20 * 60 * 1000L,
                isOutgoing = false,
                isRead = true,
                deliveryStatus = "READ"
            ),
            ChatMessage(
                id = "default_2",
                senderName = "Me",
                content = "Good Morning my love 💖",
                timestamp = now - 19 * 60 * 1000L,
                isOutgoing = true,
                isRead = true,
                deliveryStatus = "READ"
            ),
            ChatMessage(
                id = "default_3",
                senderName = partner,
                content = "Kya kar rahe ho? 😊",
                timestamp = now - 18 * 60 * 1000L,
                isOutgoing = false,
                isRead = true,
                deliveryStatus = "READ"
            ),
            ChatMessage(
                id = "default_4",
                senderName = "Me",
                content = "Voice message",
                messageType = "voice",
                voiceDurationSeconds = 12,
                timestamp = now - 17 * 60 * 1000L,
                isOutgoing = true,
                isRead = true,
                deliveryStatus = "READ"
            ),
            ChatMessage(
                id = "default_5",
                senderName = "Me",
                content = "Bas tumhe yaad kar raha tha ❤️",
                replyToId = "default_3",
                replyToSender = partner,
                replyToText = "Kya kar rahe ho? 😊",
                timestamp = now - 16 * 60 * 1000L,
                isOutgoing = true,
                isRead = true,
                deliveryStatus = "READ"
            ),
            ChatMessage(
                id = "default_6",
                senderName = partner,
                content = "Miss you ❤️",
                timestamp = now - 15 * 60 * 1000L,
                isOutgoing = false,
                isRead = true,
                deliveryStatus = "READ"
            ),
            ChatMessage(
                id = "default_7",
                senderName = "Me",
                content = "So beautiful 😍💖",
                messageType = "image",
                mediaUrl = "android.resource://" + context.packageName + "/" + com.example.R.drawable.img_home_hero_sunset_clean,
                timestamp = now - 14 * 60 * 1000L,
                isOutgoing = true,
                isRead = true,
                deliveryStatus = "READ"
            ),
            // 3 Unread messages matching the reference design badge '3'
            ChatMessage(
                id = "default_8",
                senderName = partner,
                content = "Baby, suno na? ❤️",
                timestamp = now - 5 * 60 * 1000L,
                isOutgoing = false,
                isRead = false,
                deliveryStatus = "DELIVERED"
            ),
            ChatMessage(
                id = "default_9",
                senderName = partner,
                content = "Free hoke call karo na jaldi se 💕",
                timestamp = now - 3 * 60 * 1000L,
                isOutgoing = false,
                isRead = false,
                deliveryStatus = "DELIVERED"
            ),
            ChatMessage(
                id = "default_10",
                senderName = partner,
                content = "Waiting for your sweet voice 🌹",
                timestamp = now - 1 * 60 * 1000L,
                isOutgoing = false,
                isRead = false,
                deliveryStatus = "DELIVERED"
            )
        )
    }

    fun saveChatMessage(msg: ChatMessage) {
        val current = _chatMessages.value.toMutableList()
        val index = current.indexOfFirst { it.id == msg.id }
        if (index >= 0) {
            current[index] = msg
        } else {
            current.add(msg)
        }
        val trimmed = if (current.size > 200) current.takeLast(200) else current
        persistChatMessages(trimmed)
        _chatMessages.value = trimmed
    }

    fun markMessagesAsRead() {
        val current = _chatMessages.value
        val hasUnread = current.any { !it.isOutgoing && !it.isRead }
        if (!hasUnread) return
        val updated = current.map {
            if (!it.isOutgoing && !it.isRead) {
                it.copy(isRead = true, deliveryStatus = "READ")
            } else {
                it
            }
        }
        persistChatMessages(updated)
        _chatMessages.value = updated
    }

    fun deleteMessage(id: String) {
        val current = _chatMessages.value.map {
            if (it.id == id) {
                it.copy(isDeleted = true, content = "This message was deleted")
            } else {
                it
            }
        }
        persistChatMessages(current)
        _chatMessages.value = current
    }

    fun toggleStarMessage(id: String) {
        val current = _chatMessages.value.map {
            if (it.id == id) {
                it.copy(isStarred = !it.isStarred)
            } else {
                it
            }
        }
        persistChatMessages(current)
        _chatMessages.value = current
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
                put("deliveryStatus", msg.deliveryStatus)
                put("voiceDurationSeconds", msg.voiceDurationSeconds)
                if (msg.mediaUrl != null) put("mediaUrl", msg.mediaUrl)
                if (msg.fileName != null) put("fileName", msg.fileName)
                if (msg.fileSize != null) put("fileSize", msg.fileSize)
                if (msg.replyToId != null) put("replyToId", msg.replyToId)
                if (msg.replyToSender != null) put("replyToSender", msg.replyToSender)
                if (msg.replyToText != null) put("replyToText", msg.replyToText)
                if (msg.replyToType != null) put("replyToType", msg.replyToType)
                put("isStarred", msg.isStarred)
                put("isDeleted", msg.isDeleted)
            }
            arr.put(obj)
        }
        prefs.edit().putString("chat_messages_json", arr.toString()).apply()
    }
}
