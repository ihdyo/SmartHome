package com.ihdyo.smarthome

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.ihdyo.smarthome.data.preferences.AppPreferences
import java.util.Locale

@Suppress("DEPRECATION")
class SmartHome : Application() {
    override fun onCreate() {
        super.onCreate()

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

        // Language
        val selectedLanguage = appPreferences.selectedLanguage
        val resources = resources
        val configuration = resources.configuration
        val locale = Locale(selectedLanguage)
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // Dynamic Color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
