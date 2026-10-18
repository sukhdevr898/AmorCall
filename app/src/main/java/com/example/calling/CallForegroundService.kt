package com.example.calling

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.data.CoupleRepository
import com.example.model.CallType
import com.example.network.SignalingClient
import com.example.network.SignalingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CallForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: CoupleRepository
    private val signalingClient = SignalingClient()

    override fun onCreate() {
        super.onCreate()
        repository = CoupleRepository(this)

        CallNotificationHelper.initChannels(this)
        val profile = repository.profile.value
        val notification = CallNotificationHelper.buildForegroundServiceNotification(
            this,
            profile.partnerName
        )

        try {
            startForeground(CallNotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        } catch (_: Exception) {}

        // Keep WebSocket active in background
        signalingClient.connect(profile.serverUrl, profile.myPairCode, profile.partnerPairCode)

        serviceScope.launch {
            signalingClient.events.collect { event ->
                if (event is SignalingEvent.IncomingCall) {
                    val currentProf = repository.profile.value
                    val type = if (event.callType.equals("VIDEO", ignoreCase = true)) {
                        CallType.VIDEO
                    } else {
                        CallType.VOICE
                    }
                    CallNotificationHelper.showIncomingCallNotification(
                        applicationContext,
                        callerName = currentProf.partnerName,
                        callType = type,
                        isMuted = currentProf.isNotificationsMuted
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val profile = repository.profile.value
        val notification = CallNotificationHelper.buildForegroundServiceNotification(
            this,
            profile.partnerName
        )
        try {
            startForeground(CallNotificationHelper.SERVICE_NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        signalingClient.disconnect()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            try {
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }
}
