package com.ihdyo.smarthome

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.ihdyo.smarthome.data.AppPreferences

class SmartHome : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(applicationContext)

        // Theme
        val appPreferences = AppPreferences(this)
        val themeMode = appPreferences.themeMode

        AppCompatDelegate.setDefaultNightMode(
            when (themeMode) {
                AppPreferences.ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppPreferences.ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                AppPreferences.ThemeMode.SYSTEM_DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        // Dynamic Color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }


}
