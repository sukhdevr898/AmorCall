package com.example.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MainActivity
import com.example.data.CoupleRepository
import com.example.model.CallType
import com.example.network.SignalingClient

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            CallNotificationHelper.ACTION_END_CALL -> {
                if (CallStateManager.onEndCallRequested != null) {
                    CallStateManager.onEndCallRequested?.invoke()
                } else {
                    val repo = CoupleRepository(context)
                    val partnerCode = repo.profile.value.partnerPairCode
                    if (partnerCode.isNotBlank()) {
                        SignalingClient.getInstance().sendCallEnd(partnerCode)
                    }
                    CallForegroundService.restoreHotline(context)
                }
            }

            CallNotificationHelper.ACTION_TOGGLE_MUTE -> {
                CallStateManager.onToggleMuteRequested?.invoke()
            }

            CallNotificationHelper.ACTION_TOGGLE_SPEAKER -> {
                CallStateManager.onToggleSpeakerRequested?.invoke()
            }

            CallNotificationHelper.ACTION_ANSWER -> {
                val callTypeStr = intent.getStringExtra(CallNotificationHelper.EXTRA_CALL_TYPE)
                val callType = if (callTypeStr.equals("VIDEO", ignoreCase = true)) CallType.VIDEO else CallType.VOICE

                if (CallStateManager.onAnswerCallRequested != null) {
                    CallStateManager.onAnswerCallRequested?.invoke(callType)
                } else {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(CallNotificationHelper.EXTRA_ACTION, CallNotificationHelper.ACTION_ANSWER)
                        putExtra(CallNotificationHelper.EXTRA_CALL_TYPE, callType.name)
                    }
                    context.startActivity(launchIntent)
                }
            }

            CallNotificationHelper.ACTION_DECLINE -> {
                if (CallStateManager.onDeclineCallRequested != null) {
                    CallStateManager.onDeclineCallRequested?.invoke()
                } else {
                    val repo = CoupleRepository(context)
                    val partnerCode = repo.profile.value.partnerPairCode
                    if (partnerCode.isNotBlank()) {
                        SignalingClient.getInstance().sendCallDecline(partnerCode)
                    }
                    CallNotificationHelper.cancelNotification(context)
                }
            }
        }
    }
}
