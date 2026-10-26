package com.example.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.data.CoupleRepository
import com.example.model.ChatMessage
import com.example.network.SignalingClient

/**
 * BroadcastReceiver handling Android RemoteInput Quick Replies
 * from the Notification Shade (Sheet 14: Quick Reply from Notification).
 */
class MessageReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val replyText = remoteInput?.getCharSequence(CallNotificationHelper.KEY_TEXT_REPLY)?.toString()?.trim()

        if (!replyText.isNullOrBlank()) {
            val repository = CoupleRepository(context)
            val profile = repository.profile.value

            val chatMessage = ChatMessage(
                fromCode = profile.myPairCode,
                toCode = profile.partnerPairCode,
                senderName = profile.userName,
                content = replyText,
                messageType = "text",
                timestamp = System.currentTimeMillis(),
                isOutgoing = true,
                deliveryStatus = "SENT"
            )

            // Save to local message repository
            repository.saveChatMessage(chatMessage)

            // Emit to partner via real-time WebSocket signaling
            if (profile.partnerPairCode.isNotBlank()) {
                SignalingClient.getInstance().sendChatMessage(
                    to = profile.partnerPairCode,
                    id = chatMessage.id,
                    content = chatMessage.content,
                    senderName = chatMessage.senderName,
                    timestamp = chatMessage.timestamp
                )
            }

            // Update notification to display confirmation of reply
            CallNotificationHelper.showReplyConfirmationNotification(
                context = context,
                partnerName = profile.partnerName.ifBlank { "My Love 💕" },
                repliedText = replyText
            )
        }
    }
}
