package com.example.calling

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import com.example.network.SignalingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class AudioStreamManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var recordingJob: Job? = null

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Volatile
    private var isMuted = false

    @Volatile
    private var isStreaming = false

    companion object {
        private const val TAG = "AudioStreamManager"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    fun start(targetPartnerCode: String, initialSpeakerOn: Boolean = false) {
        if (isStreaming) return
        isStreaming = true

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            setSpeakerphone(initialSpeakerOn)

            initAudioTrack()
            initAudioRecord()

            // Start Recording Coroutine
            recordingJob = scope.launch {
                val record = audioRecord ?: return@launch
                val bufferSize = max(AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, ENCODING), 1024)
                val buffer = ByteArray(bufferSize)

                try {
                    record.startRecording()
                    Log.d(TAG, "Audio recording started successfully at ${SAMPLE_RATE}Hz")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start AudioRecord", e)
                    return@launch
                }

                val signaling = SignalingClient.getInstance()
                while (isActive && isStreaming) {
                    val readBytes = record.read(buffer, 0, buffer.size)
                    if (readBytes > 0 && !isMuted) {
                        val base64 = Base64.encodeToString(buffer, 0, readBytes, Base64.NO_WRAP)
                        signaling.sendAudioChunk(targetPartnerCode, base64)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioStreamManager", e)
        }
    }

    private fun initAudioTrack() {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, ENCODING)
            val bufferSize = max(minBufSize * 2, 4096)

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_OUT)
                .setEncoding(ENCODING)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            Log.d(TAG, "AudioTrack initialized and playing")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
        }
    }

    private fun initAudioRecord() {
        try {
            val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_IN, ENCODING)
            val bufferSize = max(minBuf * 2, 2048)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                CHANNEL_IN,
                ENCODING,
                bufferSize
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioRecord", e)
        }
    }

    fun playAudioChunk(base64Data: String) {
        if (!isStreaming) return
        try {
            val bytes = Base64.decode(base64Data, Base64.NO_WRAP)
            audioTrack?.write(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio chunk", e)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setSpeakerphone(speakerOn: Boolean) {
        try {
            audioManager.isSpeakerphoneOn = speakerOn
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle speakerphone", e)
        }
    }

    fun stop() {
        isStreaming = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    stop()
                }
                release()
            }
            audioRecord = null
        } catch (_: Exception) {}

        try {
            audioTrack?.apply {
                if (state == AudioTrack.STATE_INITIALIZED) {
                    stop()
                }
                release()
            }
            audioTrack = null
        } catch (_: Exception) {}

        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
        } catch (_: Exception) {}
    }
}
