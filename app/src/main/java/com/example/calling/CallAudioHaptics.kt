package com.example.calling

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CallAudioHaptics(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var toneGenerator: ToneGenerator? = null
    private var ringJob: Job? = null
    private var vibrateJob: Job? = null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 75)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun startOutgoingRinging(scope: CoroutineScope) {
        stopAll()
        ringJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1500)
                } catch (_: Exception) {}
                delay(3500)
            }
        }
    }

    fun startIncomingRinging(scope: CoroutineScope) {
        stopAll()
        ringJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 1000)
                } catch (_: Exception) {}
                delay(2000)
            }
        }

        vibrateJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                playHeartbeatVibration()
                delay(1800)
            }
        }
    }

    private fun playHeartbeatVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 180, 120, 220)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 180, 120, 220), -1)
            }
        } catch (_: Exception) {}
    }

    fun playConnectedChime() {
        stopAll()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    fun playEndCallTone() {
        stopAll()
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        } catch (_: Exception) {}
    }

    fun setSpeakerphone(enabled: Boolean) {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = enabled
        } catch (_: Exception) {}
    }

    fun setMicrophoneMute(muted: Boolean) {
        try {
            audioManager.isMicrophoneMute = muted
        } catch (_: Exception) {}
    }

    fun stopAll() {
        ringJob?.cancel()
        ringJob = null
        vibrateJob?.cancel()
        vibrateJob = null
        try {
            toneGenerator?.stopTone()
            vibrator?.cancel()
        } catch (_: Exception) {}
    }

    fun release() {
        stopAll()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
