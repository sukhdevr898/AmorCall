package com.example.calling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.CallType

object CallNotificationHelper {
    const val CHANNEL_ID = "amour_call_notifications"
    const val SERVICE_CHANNEL_ID = "amour_foreground_service"
    const val NOTIFICATION_ID = 1008
    const val SERVICE_NOTIFICATION_ID = 1009

    const val EXTRA_ACTION = "extra_call_action"
    const val ACTION_ANSWER = "action_answer_call"
    const val ACTION_DECLINE = "action_decline_call"
    const val ACTION_OPEN = "action_open_call"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // High Priority Incoming Call Channel
            val callChannel = NotificationChannel(
                CHANNEL_ID,
                "AmourCall Love Calling",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority incoming audio and video calls from your partner"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 150, 200, 600)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(callChannel)

            // Ongoing Foreground Service Channel (Maintains background hotline connection)
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "AmourCall Hotline Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains connection to receive partner calls in the background"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun buildForegroundServiceNotification(context: Context, partnerName: String): Notification {
        initChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            10,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle("AmourCall Hotline Active")
            .setContentText("Connected to $partnerName • Ready for incoming calls ❤️")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_OPEN)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_ANSWER)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_DECLINE)
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val callTypeLabel = if (callType == CallType.VIDEO) "Incoming Video Call 📹" else "Incoming Voice Call 📞"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call)
            .setContentTitle("❤️ $callerName is Calling...")
            .setContentText("$callTypeLabel • AmourCall")
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
}
