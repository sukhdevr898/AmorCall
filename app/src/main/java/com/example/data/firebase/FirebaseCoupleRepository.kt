package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.CallType
import com.example.model.ChatMessage
import com.example.model.CoupleNote
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class FirebaseCallSignal(
    val id: String = "",
    val coupleId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val recipientId: String = "",
    val callType: String = "VOICE", // VOICE or VIDEO
    val status: String = "RINGING", // RINGING, ACCEPTED, RECONNECTING, REJECTED, ENDED
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val lastHeartbeatEpochMs: Long = System.currentTimeMillis()
)

data class FirebasePairRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromCode: String = "",
    val fromName: String = "",
    val toCode: String = "",
    val status: String = "PENDING"
)

/**
 * Firebase Firestore repository for Duora.
 * Replaces previous Supabase database layer with real-time Firestore listeners,
 * secure couple isolation, real-time messaging, shared notes, and incoming call signaling.
 */
class FirebaseCoupleRepository(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseCoupleRepo"

        @Volatile
        private var instance: FirebaseCoupleRepository? = null

        fun getInstance(context: Context): FirebaseCoupleRepository {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val appContext = context.applicationContext ?: context
                    try {
                        if (com.google.firebase.FirebaseApp.getApps(appContext).isEmpty()) {
                            com.google.firebase.FirebaseApp.initializeApp(appContext)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to initialize FirebaseApp", e)
                    }

                    val firestore = try {
                        val dbId = appContext.getString(R.string.firestore_database_id)
                        FirebaseFirestore.getInstance(dbId)
                    } catch (_: Exception) {
                        FirebaseFirestore.getInstance()
                    }

                    val authInstance = try {
                        FirebaseAuth.getInstance()
                    } catch (_: Exception) {
                        Firebase.auth
                    }

                    FirebaseCoupleRepository(appContext, firestore, authInstance).also {
                        instance = it
                    }
                }
            }
        }
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    val currentUserDisplayName: String?
        get() = auth.currentUser?.displayName

    private fun requireUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before accessing Firestore.")
    }

    // ----------------------------------------------------
    // User Profile & Presence
    // ----------------------------------------------------

    suspend fun syncUserProfile(
        displayName: String,
        partnerCode: String,
        pairedUserId: String? = null,
        pairedPartnerCode: String? = null,
        avatarUrl: String = "",
        statusMessage: String = "Thinking of you always ❤️",
        isOnline: Boolean = true
    ) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid)
        val data = hashMapOf<String, Any>(
            "userId" to uid,
            "email" to (auth.currentUser?.email ?: ""),
            "displayName" to displayName.ifBlank { "My Love" },
            "partnerCode" to partnerCode,
            "avatarUrl" to avatarUrl,
            "statusMessage" to statusMessage,
            "online" to isOnline,
            "presence" to (if (isOnline) "Online" else "Away"),
            "lastSeenEpochMs" to System.currentTimeMillis(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (pairedUserId != null) data["pairedUserId"] = pairedUserId
        if (pairedPartnerCode != null) data["pairedPartnerCode"] = pairedPartnerCode

        try {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(data).await()
            } else {
                docRef.update(data).await()
            }
            Log.d(TAG, "Synced user profile for $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user profile: ${e.message}", e)
        }
    }

    suspend fun updateUserPresence(presence: String) {
        val uid = currentUserId ?: return
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "presence" to presence,
            "online" to (presence != "Away"),
            "lastSeenEpochMs" to System.currentTimeMillis(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
            Log.d(TAG, "User presence updated to: $presence")
        } catch (e: Exception) {
            Log.w(TAG, "Presence update error: ${e.message}")
        }
    }

    fun observePartnerPresence(partnerCode: String): Flow<String?> = callbackFlow {
        val cleanCode = partnerCode.trim().uppercase()
        if (cleanCode.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = db.collection("users")
            .whereEqualTo("partnerCode", cleanCode)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing partner presence: ${error.message}")
                    return@addSnapshotListener
                }
                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    val presence = doc.getString("presence")
                        ?: if (doc.getBoolean("online") == true) "Online" else "Away"
                    trySend(presence)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    // ----------------------------------------------------
    // Couple Linking
    // ----------------------------------------------------

    fun getCoupleId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    suspend fun createOrUpdateCouple(
        coupleId: String,
        memberUids: List<String>,
        coupleNickname: String = "Our Love Space"
    ) {
        val docRef = db.collection("couples").document(coupleId)
        val payload = hashMapOf<String, Any>(
            "coupleId" to coupleId,
            "memberUids" to memberUids,
            "coupleNickname" to coupleNickname,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            val doc = docRef.get().await()
            if (!doc.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create/update couple $coupleId: ${e.message}", e)
        }
    }

    // ----------------------------------------------------
    // Realtime Messages
    // ----------------------------------------------------

    fun observeMessages(coupleId: String): Flow<List<ChatMessage>> {
        if (coupleId.isBlank() || currentUserId == null) return emptyFlow()
        val messagesRef = db.collection("couples").document(coupleId).collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)

        return messagesRef.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val text = doc.getString("text") ?: ""
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val isRead = doc.getBoolean("isRead") ?: false
                    val voiceDuration = (doc.getLong("voiceDurationSec") ?: 0L).toInt()
                    val mediaUrl = doc.getString("mediaUrl") ?: ""
                    val fileName = doc.getString("fileName")
                    val fileSize = doc.getString("fileSize")
                    val isFromMe = senderId == currentUserId
                    val type = doc.getString("type")?.lowercase() ?: if (voiceDuration > 0) "voice" else if (mediaUrl.isNotBlank()) "image" else "text"
                    val status = doc.getString("deliveryStatus") ?: if (isRead) "READ" else if (isFromMe) "SENT" else "DELIVERED"
                    val isStarred = doc.getBoolean("isStarred") ?: false
                    val isDeleted = doc.getBoolean("isDeleted") ?: false

                    ChatMessage(
                        id = id,
                        fromCode = senderId.take(6),
                        toCode = "",
                        senderName = if (isFromMe) "You" else senderName.ifBlank { "Partner" },
                        content = text,
                        messageType = type,
                        timestamp = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        isOutgoing = isFromMe,
                        isRead = isRead,
                        deliveryStatus = status,
                        voiceDurationSeconds = voiceDuration,
                        mediaUrl = mediaUrl.ifBlank { null },
                        fileName = fileName,
                        fileSize = fileSize,
                        replyToId = doc.getString("replyToId"),
                        replyToSender = doc.getString("replyToSender"),
                        replyToText = doc.getString("replyToText"),
                        replyToType = doc.getString("replyToType"),
                        isStarred = isStarred,
                        isDeleted = isDeleted
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error mapping message ${doc.id}: ${e.message}")
                    null
                }
            }
        }
    }

    suspend fun sendMessage(
        coupleId: String,
        text: String,
        type: String = "text",
        mediaUrl: String = "",
        voiceDurationSec: Int = 0,
        fileName: String? = null,
        fileSize: String? = null,
        replyToId: String? = null,
        replyToSender: String? = null,
        replyToText: String? = null,
        replyToType: String? = null,
        clientMessageId: String? = null
    ): Boolean {
        val uid = currentUserId ?: return false
        val messageId = clientMessageId ?: UUID.randomUUID().toString()
        val docRef = db.collection("couples").document(coupleId).collection("messages").document(messageId)

        val payload = hashMapOf<String, Any>(
            "id" to messageId,
            "coupleId" to coupleId,
            "senderId" to uid,
            "senderName" to (currentUserDisplayName ?: "My Love"),
            "text" to text,
            "type" to type,
            "mediaUrl" to mediaUrl,
            "voiceDurationSec" to voiceDurationSec,
            "isRead" to false,
            "deliveryStatus" to "SENT",
            "isStarred" to false,
            "isDeleted" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (fileName != null) payload["fileName"] = fileName
        if (fileSize != null) payload["fileSize"] = fileSize
        if (replyToId != null) payload["replyToId"] = replyToId
        if (replyToSender != null) payload["replyToSender"] = replyToSender
        if (replyToText != null) payload["replyToText"] = replyToText
        if (replyToType != null) payload["replyToType"] = replyToType

        return try {
            docRef.set(payload).await()
            Log.d(TAG, "Message sent to couple $coupleId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message: ${e.message}", e)
            false
        }
    }

    suspend fun markMessagesAsRead(coupleId: String) {
        val uid = currentUserId ?: return
        if (coupleId.isBlank()) return
        try {
            val unreadDocs = db.collection("couples").document(coupleId).collection("messages")
                .whereNotEqualTo("senderId", uid)
                .whereEqualTo("isRead", false)
                .limit(50)
                .get()
                .await()

            if (!unreadDocs.isEmpty) {
                val batch = db.batch()
                for (doc in unreadDocs.documents) {
                    batch.update(doc.reference, mapOf(
                        "isRead" to true,
                        "deliveryStatus" to "READ",
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                }
                batch.commit().await()
                Log.d(TAG, "Marked ${unreadDocs.size()} messages as READ in couple $coupleId")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to mark messages as read: ${e.message}")
        }
    }

    suspend fun deleteMessage(coupleId: String, messageId: String): Boolean {
        if (coupleId.isBlank() || messageId.isBlank()) return false
        return try {
            val docRef = db.collection("couples").document(coupleId).collection("messages").document(messageId)
            docRef.update(mapOf(
                "isDeleted" to true,
                "text" to "This message was deleted",
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete message: ${e.message}")
            false
        }
    }

    // ----------------------------------------------------
    // Realtime Notes
    // ----------------------------------------------------

    fun observeNotes(coupleId: String): Flow<List<CoupleNote>> {
        if (coupleId.isBlank() || currentUserId == null) return emptyFlow()
        val notesRef = db.collection("couples").document(coupleId).collection("notes")
            .orderBy("updatedAt", Query.Direction.DESCENDING)

        return notesRef.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val title = doc.getString("title") ?: ""
                    val content = doc.getString("content") ?: ""
                    val authorId = doc.getString("authorId") ?: ""
                    val authorName = doc.getString("authorName") ?: ""
                    val isShared = doc.getBoolean("isShared") ?: true
                    val isFromPartner = authorId != currentUserId

                    CoupleNote(
                        id = id,
                        title = title,
                        content = content,
                        authorCode = authorId.take(6),
                        authorName = authorName,
                        createdAtEpochMs = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        updatedAtEpochMs = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        isFromPartner = isFromPartner,
                        isShared = isShared
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error mapping note ${doc.id}: ${e.message}")
                    null
                }
            }
        }
    }

    suspend fun saveNote(
        coupleId: String,
        title: String,
        content: String,
        category: String = "PLANS",
        isShared: Boolean = true,
        existingId: String? = null
    ): Boolean {
        val uid = currentUserId ?: return false
        val noteId = existingId ?: UUID.randomUUID().toString()
        val docRef = db.collection("couples").document(coupleId).collection("notes").document(noteId)

        val payload = hashMapOf<String, Any>(
            "id" to noteId,
            "coupleId" to coupleId,
            "authorId" to uid,
            "authorName" to (currentUserDisplayName ?: "My Love"),
            "title" to title,
            "content" to content,
            "category" to category,
            "isShared" to isShared,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            if (existingId == null) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save note: ${e.message}", e)
            false
        }
    }

    suspend fun deleteNote(coupleId: String, noteId: String): Boolean {
        return try {
            db.collection("couples").document(coupleId).collection("notes").document(noteId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete note: ${e.message}", e)
            false
        }
    }

    // ----------------------------------------------------
    // Realtime Incoming Call Signaling & Alerts
    // ----------------------------------------------------

    fun observeCallSignals(coupleId: String): Flow<FirebaseCallSignal?> = callbackFlow {
        if (coupleId.isBlank() || currentUserId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val signalsRef = db.collection("couples").document(coupleId).collection("call_signals")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)

        val registration = signalsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen call signals failed: ${error.message}")
                return@addSnapshotListener
            }
            val doc = snapshot?.documents?.firstOrNull()
            if (doc != null && doc.exists()) {
                val signal = FirebaseCallSignal(
                    id = doc.getString("id") ?: doc.id,
                    coupleId = doc.getString("coupleId") ?: coupleId,
                    callerId = doc.getString("callerId") ?: "",
                    callerName = doc.getString("callerName") ?: "",
                    recipientId = doc.getString("recipientId") ?: "",
                    callType = doc.getString("callType") ?: "VOICE",
                    status = doc.getString("status") ?: "RINGING",
                    createdAtEpochMs = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                    lastHeartbeatEpochMs = doc.getTimestamp("lastHeartbeat")?.toDate()?.time ?: System.currentTimeMillis()
                )
                trySend(signal)
            } else {
                trySend(null)
            }
        }

        awaitClose { registration.remove() }
    }

    suspend fun sendCallSignal(
        coupleId: String,
        recipientId: String,
        callType: CallType
    ): String? {
        val uid = currentUserId ?: return null
        val signalId = UUID.randomUUID().toString()
        val docRef = db.collection("couples").document(coupleId).collection("call_signals").document(signalId)

        val payload = hashMapOf<String, Any>(
            "id" to signalId,
            "coupleId" to coupleId,
            "callerId" to uid,
            "callerName" to (currentUserDisplayName ?: "My Love"),
            "recipientId" to recipientId,
            "callType" to if (callType == CallType.VIDEO) "VIDEO" else "VOICE",
            "status" to "RINGING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "lastHeartbeat" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Log.d(TAG, "Sent call signal $signalId (${callType.name}) to $recipientId")
            signalId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send call signal: ${e.message}", e)
            null
        }
    }

    suspend fun updateCallHeartbeat(
        coupleId: String,
        signalId: String
    ) {
        if (coupleId.isBlank() || signalId.isBlank()) return
        val docRef = db.collection("couples").document(coupleId).collection("call_signals").document(signalId)
        try {
            docRef.update("lastHeartbeat", FieldValue.serverTimestamp()).await()
        } catch (_: Exception) {}
    }

    suspend fun updateCallSignalStatus(
        coupleId: String,
        signalId: String,
        status: String // ACCEPTED, RECONNECTING, REJECTED, ENDED
    ) {
        if (coupleId.isBlank() || signalId.isBlank()) return
        val docRef = db.collection("couples").document(coupleId).collection("call_signals").document(signalId)
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Log.d(TAG, "Updated call signal $signalId to $status")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update call signal status: ${e.message}")
        }
    }

    // ----------------------------------------------------
    // Partner Pairing Requests
    // ----------------------------------------------------

    fun observeReceivedPairRequests(myCode: String): Flow<List<FirebasePairRequest>> = callbackFlow {
        if (myCode.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val requestsRef = db.collection("pair_requests")
            .whereEqualTo("toCode", myCode)
            .whereEqualTo("status", "PENDING")

        val registration = requestsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing pair requests: ${error.message}")
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                FirebasePairRequest(
                    id = doc.getString("id") ?: doc.id,
                    fromUserId = doc.getString("fromUserId") ?: "",
                    fromCode = doc.getString("fromCode") ?: "",
                    fromName = doc.getString("fromName") ?: "Love",
                    toCode = doc.getString("toCode") ?: myCode,
                    status = doc.getString("status") ?: "PENDING"
                )
            } ?: emptyList()
            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    suspend fun sendPairRequest(
        myCode: String,
        partnerTargetCode: String,
        myName: String
    ): Boolean {
        val uid = currentUserId ?: return false
        val requestId = UUID.randomUUID().toString()
        val docRef = db.collection("pair_requests").document(requestId)

        val payload = hashMapOf<String, Any>(
            "id" to requestId,
            "fromUserId" to uid,
            "fromCode" to myCode,
            "fromName" to myName,
            "toCode" to partnerTargetCode.trim().uppercase(),
            "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Log.d(TAG, "Sent pair request from $myCode to $partnerTargetCode")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send pair request: ${e.message}", e)
            false
        }
    }

    suspend fun respondToPairRequest(requestId: String, accept: Boolean) {
        val docRef = db.collection("pair_requests").document(requestId)
        try {
            docRef.update(
                mapOf(
                    "status" to if (accept) "ACCEPTED" else "DECLINED",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to respond to pair request: ${e.message}", e)
        }
    }
}
