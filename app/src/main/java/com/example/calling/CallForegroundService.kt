package com.example.calling

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
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
    private val signalingClient = SignalingClient.getInstance()
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        private const val TAG = "CallForegroundService"

        const val ACTION_UPDATE_CALL_PROGRESS = "com.example.ACTION_UPDATE_CALL_PROGRESS"
        const val ACTION_RESTORE_HOTLINE = "com.example.ACTION_RESTORE_HOTLINE"

        const val EXTRA_CALL_ACTIVE = "extra_call_active"
        const val EXTRA_PARTNER_NAME = "extra_partner_name"
        const val EXTRA_CALL_TYPE = "extra_call_type"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_IS_MUTED = "extra_is_muted"
        const val EXTRA_IS_SPEAKER = "extra_is_speaker"

        fun start(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Could not start CallForegroundService: ${t.message}")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            try {
                context.stopService(intent)
            } catch (_: Throwable) {}
        }


        fun updateCallProgress(
            context: Context,
            isCallActive: Boolean,
            partnerName: String,
            callType: CallType,
            durationSeconds: Int,
            isMuted: Boolean,
            isSpeakerOn: Boolean
        ) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_UPDATE_CALL_PROGRESS
                putExtra(EXTRA_CALL_ACTIVE, isCallActive)
                putExtra(EXTRA_PARTNER_NAME, partnerName)
                putExtra(EXTRA_CALL_TYPE, callType.name)
                putExtra(EXTRA_DURATION, durationSeconds)
                putExtra(EXTRA_IS_MUTED, isMuted)
                putExtra(EXTRA_IS_SPEAKER, isSpeakerOn)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun restoreHotline(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_RESTORE_HOTLINE
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = CoupleRepository(this)
        CallNotificationHelper.initChannels(this)

        val profile = repository.profile.value
        val notification = CallNotificationHelper.buildForegroundServiceNotification(
            this,
            profile.partnerName
        )
        startServiceWithNotification(notification, isCallActive = false, callType = CallType.VOICE)

        // Keep WebSocket active in background 24/7
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
        val action = intent?.action
        val profile = repository.profile.value

        when (action) {
            ACTION_UPDATE_CALL_PROGRESS -> {
                val isCallActive = intent.getBooleanExtra(EXTRA_CALL_ACTIVE, false)
                val partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME) ?: profile.partnerName
                val typeStr = intent.getStringExtra(EXTRA_CALL_TYPE)
                val callType = if (typeStr.equals("VIDEO", ignoreCase = true)) CallType.VIDEO else CallType.VOICE
                val duration = intent.getIntExtra(EXTRA_DURATION, 0)
                val isMuted = intent.getBooleanExtra(EXTRA_IS_MUTED, false)
                val isSpeaker = intent.getBooleanExtra(EXTRA_IS_SPEAKER, false)

                if (isCallActive) {
                    acquireCallWakeLock()
                    val notification = CallNotificationHelper.buildOngoingCallNotification(
                        this,
                        partnerName = partnerName,
                        callType = callType,
                        durationSeconds = duration,
                        isMuted = isMuted,
                        isSpeakerOn = isSpeaker
                    )
                    startServiceWithNotification(notification, isCallActive = true, callType = callType)
                } else {
                    releaseCallWakeLock()
                    val notification = CallNotificationHelper.buildForegroundServiceNotification(this, partnerName)
                    startServiceWithNotification(notification, isCallActive = false, callType = CallType.VOICE)
                }
            }

            ACTION_RESTORE_HOTLINE -> {
                releaseCallWakeLock()
                val notification = CallNotificationHelper.buildForegroundServiceNotification(this, profile.partnerName)
                startServiceWithNotification(notification, isCallActive = false, callType = CallType.VOICE)
            }

            else -> {
                val notification = CallNotificationHelper.buildForegroundServiceNotification(
                    this,
                    profile.partnerName
                )
                startServiceWithNotification(notification, isCallActive = false, callType = CallType.VOICE)
                signalingClient.connect(profile.serverUrl, profile.myPairCode, profile.partnerPairCode)
            }
        }

        return START_STICKY
    }

    private fun startServiceWithNotification(
        notification: android.app.Notification,
        isCallActive: Boolean,
        callType: CallType
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var serviceType = if (isCallActive) {
                    var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                        }
                        if (callType == CallType.VIDEO && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                        }
                    }
                    type
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                }
                startForeground(CallNotificationHelper.SERVICE_NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(CallNotificationHelper.SERVICE_NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Failed startForeground: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(CallNotificationHelper.SERVICE_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(CallNotificationHelper.SERVICE_NOTIFICATION_ID, notification)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Fatal startForeground error, stopping self gracefully", t)
                stopSelf()
            }
        }
    }

    private fun acquireCallWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AmourCall:ActiveCallWakeLock")?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(45 * 60 * 1000L) // 45 min safety guard
                Log.d(TAG, "Call WakeLock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }

    private fun releaseCallWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "Call WakeLock released")
            }
        } catch (_: Exception) {}
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            val restartIntent = Intent(applicationContext, CallForegroundService::class.java).also {
                it.setPackage(packageName)
            }
            val pendingIntent = PendingIntent.getService(
                this, 101, restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1500,
                pendingIntent
            )
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCallWakeLock()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
