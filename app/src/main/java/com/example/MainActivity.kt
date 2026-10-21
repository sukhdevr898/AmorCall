package com.example

import android.Manifest
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.calling.CallActionReceiver
import com.example.calling.CallNotificationHelper
import com.example.calling.CallStateManager
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.ui.screens.CallScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoriesScreen
import com.example.ui.screens.PairingScreen
import com.example.ui.theme.AmourCallTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LoveCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.privacy.PrivacyIconManager.init(this)

        CallStateManager.onEnterPipRequested = { enterPipMode() }
        handleIntent(intent)

        setContent {
            val profile by viewModel.profile.collectAsState()
            val callSession by viewModel.callSession.collectAsState()

            LaunchedEffect(callSession.status, callSession.type, callSession.isMuted) {
                updatePipParams()
            }

            AmourCallTheme(theme = profile.appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        onEnterPip = { enterPipMode() },
                        onRequestPermissions = { onGranted ->
                            requestRequiredPermissions(onGranted)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.cleanupStaleCallOnResume()
        updatePipParams()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        viewModel.setIsInPipMode(isInPictureInPictureMode)
        if (!isInPictureInPictureMode) {
            updatePipParams()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val session = viewModel.callSession.value
        if (session.status == CallStatus.CONNECTED && session.type == CallType.VIDEO) {
            enterPipMode()
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val session = viewModel.callSession.value
                val actions = createPipActions(session.isMuted)
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(9, 16))
                    .setActions(actions)
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to enter Picture-in-Picture mode", e)
            }
        }
    }

    fun updatePipParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val session = viewModel.callSession.value
                val isVideoConnected = session.status == CallStatus.CONNECTED && session.type == CallType.VIDEO
                val actions = createPipActions(session.isMuted)
                val builder = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(9, 16))
                    .setActions(actions)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(isVideoConnected)
                }
                setPictureInPictureParams(builder.build())
            } catch (_: Exception) {}
        }
    }

    private fun createPipActions(isMuted: Boolean): List<RemoteAction> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()

        val actions = mutableListOf<RemoteAction>()

        val muteIntent = PendingIntent.getBroadcast(
            this,
            101,
            Intent(this, CallActionReceiver::class.java).apply {
                action = CallNotificationHelper.ACTION_TOGGLE_MUTE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val muteIcon = Icon.createWithResource(
            this,
            if (isMuted) android.R.drawable.stat_notify_call_mute else android.R.drawable.ic_btn_speak_now
        )
        val muteLabel = if (isMuted) "Unmute" else "Mute"
        actions.add(RemoteAction(muteIcon, muteLabel, muteLabel, muteIntent))

        val endCallIntent = PendingIntent.getBroadcast(
            this,
            102,
            Intent(this, CallActionReceiver::class.java).apply {
                action = CallNotificationHelper.ACTION_END_CALL
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val endIcon = Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel)
        actions.add(RemoteAction(endIcon, "End", "End Call", endCallIntent))

        return actions
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra(CallNotificationHelper.EXTRA_ACTION)
        val callTypeStr = intent?.getStringExtra(CallNotificationHelper.EXTRA_CALL_TYPE)
        val callType = if (callTypeStr.equals("VIDEO", ignoreCase = true)) CallType.VIDEO else CallType.VOICE

        when (action) {
            CallNotificationHelper.ACTION_ANSWER -> {
                viewModel.acceptCall(incomingType = callType)
            }
            CallNotificationHelper.ACTION_DECLINE -> {
                viewModel.declineCall()
            }
            CallNotificationHelper.ACTION_END_CALL -> {
                viewModel.endCall()
            }
            CallNotificationHelper.ACTION_TOGGLE_MUTE -> {
                viewModel.toggleMute()
            }
            CallNotificationHelper.ACTION_TOGGLE_SPEAKER -> {
                viewModel.toggleSpeaker()
            }
        }
    }

    private var pendingPermissionAction: (() -> Unit)? = null

    private fun requestRequiredPermissions(onGranted: () -> Unit) {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onGranted()
        } else {
            pendingPermissionAction = onGranted
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        pendingPermissionAction?.invoke()
        pendingPermissionAction = null
    }
}

@Composable
fun AppNavigation(
    viewModel: LoveCallViewModel,
    onEnterPip: () -> Unit,
    onRequestPermissions: (() -> Unit) -> Unit
) {
    val callSession by viewModel.callSession.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.profile.collectAsState()

    // Active full-screen call takes priority
    if (callSession.status != CallStatus.IDLE) {
        CallScreen(
            viewModel = viewModel,
            callSession = callSession,
            onEnterPip = onEnterPip
        )
    } else {
        when (currentScreen) {
            AppScreen.SPLASH -> {
                com.example.ui.screens.SplashScreen(
                    onFinish = { viewModel.onSplashFinished() }
                )
            }
            AppScreen.ONBOARDING -> {
                com.example.ui.screens.OnboardingScreen(
                    currentProfile = profile,
                    onComplete = { partnerName, partnerNickname, partnerCode ->
                        viewModel.completeOnboarding(partnerName, partnerNickname, partnerCode)
                    }
                )
            }
            AppScreen.DASHBOARD -> {
                HomeScreen(
                    viewModel = viewModel,
                    onRequestPermissions = onRequestPermissions
                )
            }
            AppScreen.MEMORIES -> {
                MemoriesScreen(viewModel = viewModel)
            }
            AppScreen.PAIRING -> {
                PairingScreen(viewModel = viewModel)
            }
            AppScreen.DEVELOPER -> {
                DeveloperScreen(viewModel = viewModel)
            }
        }
    }
}
