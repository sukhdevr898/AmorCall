package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.example.calling.CallNotificationHelper
import com.example.model.CallStatus
import com.example.ui.screens.CallScreen
import com.example.ui.screens.DeveloperScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoriesScreen
import com.example.ui.screens.PairingScreen
import com.example.ui.theme.AmourCallTheme
import com.example.ui.theme.LoveDarkBg
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LoveCallViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LoveCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val profile by viewModel.profile.collectAsState()
            AmourCallTheme(theme = profile.appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = viewModel,
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

    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra(CallNotificationHelper.EXTRA_ACTION)
        when (action) {
            CallNotificationHelper.ACTION_ANSWER -> {
                viewModel.acceptCall()
            }
            CallNotificationHelper.ACTION_DECLINE -> {
                viewModel.declineCall()
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
        // Even if some permissions are not granted immediately, proceed so caller can experience the app
        pendingPermissionAction?.invoke()
        pendingPermissionAction = null
    }
}

@Composable
fun AppNavigation(
    viewModel: LoveCallViewModel,
    onRequestPermissions: (() -> Unit) -> Unit
) {
    val callSession by viewModel.callSession.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.profile.collectAsState()

    // Active full-screen call takes priority
    if (callSession.status != CallStatus.IDLE) {
        CallScreen(
            viewModel = viewModel,
            callSession = callSession
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
