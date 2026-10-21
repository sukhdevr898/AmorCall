package com.example.privacy

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PrivacyIconType(
    val id: String,
    val title: String,
    val subtitle: String,
    val launcherLabel: String,
    val aliasClassName: String,
    val iconResId: Int
) {
    DEFAULT(
        id = "default",
        title = "Duora (Original)",
        subtitle = "Romantic rose icon with Duora title",
        launcherLabel = "Duora",
        aliasClassName = "com.example.MainActivityDefault",
        iconResId = R.mipmap.ic_launcher
    ),
    CALCULATOR(
        id = "calculator",
        title = "Calculator",
        subtitle = "Stealth calculator icon with Calculator title",
        launcherLabel = "Calculator",
        aliasClassName = "com.example.MainActivityCalculator",
        iconResId = R.mipmap.ic_launcher_calculator
    ),
    NOTES(
        id = "notes",
        title = "Notes",
        subtitle = "Disguised yellow notepad icon with Notes title",
        launcherLabel = "Notes",
        aliasClassName = "com.example.MainActivityNotes",
        iconResId = R.mipmap.ic_launcher_notes
    ),
    CLOCK(
        id = "clock",
        title = "Clock",
        subtitle = "Minimalist dial icon with Clock title",
        launcherLabel = "Clock",
        aliasClassName = "com.example.MainActivityClock",
        iconResId = R.mipmap.ic_launcher_clock
    ),
    WEATHER(
        id = "weather",
        title = "Weather",
        subtitle = "Sunny day icon with Weather title",
        launcherLabel = "Weather",
        aliasClassName = "com.example.MainActivityWeather",
        iconResId = R.mipmap.ic_launcher_weather
    );

    companion object {
        fun fromId(id: String): PrivacyIconType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}

object PrivacyIconManager {
    private const val TAG = "PrivacyIconManager"
    private const val PREFS_NAME = "privacy_icon_prefs"
    private const val KEY_SELECTED_ICON = "selected_privacy_icon"

    private val _currentIcon = MutableStateFlow(PrivacyIconType.DEFAULT)
    val currentIcon: StateFlow<PrivacyIconType> = _currentIcon.asStateFlow()

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedId = prefs.getString(KEY_SELECTED_ICON, PrivacyIconType.DEFAULT.id)
                ?: PrivacyIconType.DEFAULT.id
            _currentIcon.value = PrivacyIconType.fromId(savedId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load saved privacy icon: ${e.message}")
        }
    }

    /**
     * Dynamically switches the launcher icon and label using PackageManager activity-alias.
     * Uses DONT_KILL_APP to prevent unexpected process crashes during runtime.
     */
    fun setPrivacyIcon(context: Context, target: PrivacyIconType): Boolean {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            for (type in PrivacyIconType.entries) {
                val component = ComponentName(packageName, type.aliasClassName)
                val newState = if (type == target) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }

            // Save choice
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELECTED_ICON, target.id)
                .apply()

            _currentIcon.value = target
            Log.i(TAG, "Successfully changed app disguise icon to: ${target.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch app privacy icon", e)
            false
        }
    }
}
