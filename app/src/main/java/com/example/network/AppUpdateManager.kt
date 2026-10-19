package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val browserUrl: String,
    val releaseNotes: String,
    val isAvailable: Boolean
)

class AppUpdateManager private constructor() {
    companion object {
        private const val TAG = "AppUpdateManager"
        private const val GITHUB_VERSION_URL_MAIN =
            "https://raw.githubusercontent.com/sukhdevr898/AmorCall/main/APK/version.json"
        private const val GITHUB_VERSION_URL_MASTER =
            "https://raw.githubusercontent.com/sukhdevr898/AmorCall/master/APK/version.json"

        @Volatile
        private var instance: AppUpdateManager? = null

        fun getInstance(): AppUpdateManager {
            return instance ?: synchronized(this) {
                instance ?: AppUpdateManager().also { instance = it }
            }
        }

        fun isSemverNewer(remote: String, current: String): Boolean {
            try {
                val cleanRemote = remote.trim().removePrefix("v").removePrefix("V")
                val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")

                val rParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
                val cParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

                val maxLen = maxOf(rParts.size, cParts.size)
                for (i in 0 until maxLen) {
                    val r = rParts.getOrElse(i) { 0 }
                    val c = cParts.getOrElse(i) { 0 }
                    if (r > c) return true
                    if (r < c) return false
                }
            } catch (_: Exception) {}
            return false
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    suspend fun checkForUpdates(isManual: Boolean = false): AppUpdateInfo? = withContext(Dispatchers.IO) {
        _isChecking.value = true
        _statusMessage.value = if (isManual) "Checking for updates on GitHub..." else null

        val urls = listOf(GITHUB_VERSION_URL_MAIN, GITHUB_VERSION_URL_MASTER)
        var responseJsonStr: String? = null

        for (url in urls) {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        responseJsonStr = body
                        break
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch version from $url: ${e.message}")
            }
        }

        if (responseJsonStr == null) {
            _isChecking.value = false
            if (isManual) {
                _statusMessage.value = "Unable to connect to GitHub. Check connection."
            }
            return@withContext null
        }

        try {
            val json = JSONObject(responseJsonStr)
            val remoteVersionCode = json.optInt("versionCode", 0)
            val remoteVersionName = json.optString("versionName", "")
            val downloadUrl = json.optString(
                "downloadUrl",
                "https://github.com/sukhdevr898/AmorCall/raw/main/APK/Duora-2.0.apk"
            )
            val browserUrl = json.optString(
                "browserUrl",
                "https://github.com/sukhdevr898/AmorCall/blob/main/APK/Duora-2.0.apk"
            )
            val releaseNotes = json.optString("releaseNotes", "New update available with bug fixes and improvements.")

            val currentVersionCode = BuildConfig.VERSION_CODE
            val currentVersionName = BuildConfig.VERSION_NAME

            val isNewer = remoteVersionCode > currentVersionCode ||
                isSemverNewer(remoteVersionName, currentVersionName)

            val info = AppUpdateInfo(
                versionCode = remoteVersionCode,
                versionName = remoteVersionName,
                downloadUrl = downloadUrl,
                browserUrl = browserUrl,
                releaseNotes = releaseNotes,
                isAvailable = isNewer
            )

            _updateInfo.value = info
            _isChecking.value = false

            if (isNewer) {
                _statusMessage.value = "Update v$remoteVersionName is available!"
            } else if (isManual) {
                _statusMessage.value = "You are on the latest version (v$currentVersionName)."
            }

            return@withContext info
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse version JSON", e)
            _isChecking.value = false
            if (isManual) {
                _statusMessage.value = "Error checking update details."
            }
            return@withContext null
        }
    }

    fun dismissUpdateDialog() {
        // Keeps updateInfo but user dismissed current pop-up
        _statusMessage.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
