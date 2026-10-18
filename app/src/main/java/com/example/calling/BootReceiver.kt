package com.example.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.CoupleRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val repository = CoupleRepository(context)
            if (repository.profile.value.isOnboardingCompleted) {
                CallForegroundService.start(context)
            }
        }
    }
}
