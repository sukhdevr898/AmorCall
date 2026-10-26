package com.example.calling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.model.CallType

object CallNotificationHelper {
    const val CHANNEL_ID = "amour_call_notifications"
    const val SERVICE_CHANNEL_ID = "amour_foreground_service"
    const val MESSAGE_CHANNEL_ID = "duora_message_notifications"
    const val NOTIFICATION_ID = 1008
    const val SERVICE_NOTIFICATION_ID = 1009
    const val MESSAGE_NOTIFICATION_ID = 1015

    const val EXTRA_ACTION = "extra_call_action"
    const val EXTRA_CALL_TYPE = "extra_call_type"

    const val ACTION_ANSWER = "com.example.ACTION_ANSWER"
    const val ACTION_DECLINE = "com.example.ACTION_DECLINE"
    const val ACTION_OPEN = "com.example.ACTION_OPEN"
    const val ACTION_OPEN_CHAT = "com.example.ACTION_OPEN_CHAT"
    const val ACTION_END_CALL = "com.example.ACTION_END_CALL"
    const val ACTION_TOGGLE_MUTE = "com.example.ACTION_TOGGLE_MUTE"
    const val ACTION_TOGGLE_SPEAKER = "com.example.ACTION_TOGGLE_SPEAKER"
    const val ACTION_REPLY_MESSAGE = "com.example.ACTION_REPLY_MESSAGE"
    const val KEY_TEXT_REPLY = "key_duora_text_reply"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // High Priority Incoming Call Channel
            val callChannel = NotificationChannel(
                CHANNEL_ID,
                "Duora Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority incoming audio and video calls from your partner"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 150, 200, 600)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(callChannel)

            // Ongoing Foreground Service Channel (Hotline & In-Progress Calls)
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Duora Active Call & Hotline",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active voice/video call session and 24/7 background hotline"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // High Priority Real-time Incoming Message Channel
            val messageChannel = NotificationChannel(
                MESSAGE_CHANNEL_ID,
                "Duora Partner Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time incoming chat messages from your partner"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 80, 150)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(messageChannel)
        }
    }

    fun buildForegroundServiceNotification(context: Context, partnerName: String): Notification {
        initChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ACTION, ACTION_OPEN)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            10,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val partnerText = if (partnerName.isNotBlank()) "Connected to $partnerName • " else ""

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle("Duora Hotline Active")
            .setContentText("${partnerText}Ready for incoming calls ❤️")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun buildOngoingCallNotification(
        context: Context,
        partnerName: String,
        callType: CallType,
        durationSeconds: Int,
        isMuted: Boolean,
        isSpeakerOn: Boolean
    ): Notification {
        initChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ACTION, ACTION_OPEN)
            putExtra(EXTRA_CALL_TYPE, callType.name)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            20,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // End Call Broadcast Intent
        val endCallIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_END_CALL
        }
        val endCallPendingIntent = PendingIntent.getBroadcast(
            context,
            21,
            endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle Mute Broadcast Intent
        val toggleMuteIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_TOGGLE_MUTE
        }
        val toggleMutePendingIntent = PendingIntent.getBroadcast(
            context,
            22,
            toggleMuteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle Speaker Broadcast Intent
        val toggleSpeakerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_TOGGLE_SPEAKER
        }
        val toggleSpeakerPendingIntent = PendingIntent.getBroadcast(
            context,
            23,
            toggleSpeakerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        val durationFormatted = String.format("%02d:%02d", mins, secs)

        val typeLabel = if (callType == CallType.VIDEO) "📹 Video Call" else "📞 Voice Call"
        val statusText = if (durationSeconds > 0) "$typeLabel • $durationFormatted" else "$typeLabel • In Call"

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(if (callType == CallType.VIDEO) android.R.drawable.presence_video_online else android.R.drawable.stat_sys_phone_call)
            .setContentTitle("❤️ In Call with $partnerName")
            .setContentText(statusText)
            .setSubText(durationFormatted)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Call",
                endCallPendingIntent
            )
            .addAction(
                if (isMuted) android.R.drawable.stat_notify_call_mute else android.R.drawable.ic_btn_speak_now,
                if (isMuted) "Unmute" else "Mute",
                toggleMutePendingIntent
            )
            .addAction(
                if (isSpeakerOn) android.R.drawable.stat_sys_speakerphone else android.R.drawable.ic_lock_silent_mode_off,
                if (isSpeakerOn) "Earpiece" else "Speaker",
                toggleSpeakerPendingIntent
            )
            .build()
    }

    fun showIncomingCallNotification(
        context: Context,
        callerName: String,
        callType: CallType,
        isMuted: Boolean = false
    ) {
        initChannels(context)

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ACTION, ACTION_OPEN)
            putExtra(EXTRA_CALL_TYPE, callType.name)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ACTION, ACTION_ANSWER)
            putExtra(EXTRA_CALL_TYPE, callType.name)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = ACTION_DECLINE
            putExtra(EXTRA_CALL_TYPE, callType.name)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callTypeLabel = if (callType == CallType.VIDEO) "Incoming Video Call 📹" else "Incoming Voice Call 📞"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(if (callType == CallType.VIDEO) android.R.drawable.presence_video_online else android.R.drawable.stat_sys_phone_call)
            .setContentTitle("❤️ $callerName is Calling...")
            .setContentText("$callTypeLabel • Duora")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setSilent(isMuted)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Answer ❤️",
                answerPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Decline",
                declinePendingIntent
            )

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        messageType: String = "text",
        mediaUrl: String? = null
    ) {
        initChannels(context)

        val partnerDisplayName = if (senderName.isNotBlank()) senderName else "My Love 💕"

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ACTION, ACTION_OPEN_CHAT)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            120,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android RemoteInput Quick Reply (Sheet 14: Quick Reply from Notification)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply to $partnerDisplayName...")
            .build()

        val replyIntent = Intent(context, MessageReplyReceiver::class.java).apply {
            action = ACTION_REPLY_MESSAGE
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            121,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply ❤️",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        val openAction = NotificationCompat.Action.Builder(
            android.R.drawable.sym_action_chat,
            "Open Chat",
            pendingIntent
        ).build()

        val displayContent = when (messageType) {
            "image" -> "📷 Sent a photo ❤️"
            "video" -> "🎥 Sent a video"
            "voice" -> "🎙️ Voice note"
            else -> messageText
        }

        val builder = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle("Duora • $partnerDisplayName")
            .setContentText(displayContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(replyAction)
            .addAction(openAction)

        // Sheet 15: Media thumbnail if image
        if (messageType == "image" && !mediaUrl.isNullOrBlank()) {
            try {
                val uri = android.net.Uri.parse(mediaUrl)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        builder.setLargeIcon(bitmap)
                        builder.setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .setSummaryText(displayContent)
                        )
                    }
                }
            } catch (_: Exception) {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(displayContent))
            }
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(displayContent))
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, builder.build())
    }

    fun showReplyConfirmationNotification(
        context: Context,
        partnerName: String,
        repliedText: String
    ) {
        initChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ACTION, ACTION_OPEN_CHAT)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            122,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle("Duora • $partnerName")
            .setContentText("You: $repliedText")
            .setStyle(NotificationCompat.BigTextStyle().bigText("You replied: $repliedText ❤️"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification)
    }

    fun clearMessageNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(MESSAGE_NOTIFICATION_ID)
    }
}
