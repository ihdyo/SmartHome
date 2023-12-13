package com.ihdyo.smarthome

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

class SmartHome : Application() {
    override fun onCreate() {
        super.onCreate()

        // Dark Mode
        val appPreferences = AppPreferences(this)
        val isDarkModeEnabled = appPreferences.isDarkModeEnabled

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Dynamic Color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
