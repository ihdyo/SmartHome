package com.ihdyo.smarthome

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.ihdyo.smarthome.data.AppPreferences
import com.ihdyo.smarthome.utils.Const.DEFAULT
import com.ihdyo.smarthome.utils.Const.LOCALE_ENGLISH
import com.ihdyo.smarthome.utils.Const.LOCALE_INDONESIA
import com.ihdyo.smarthome.utils.Const.LOCALE_JAVANESE
import java.util.Locale

@Suppress("DEPRECATION")
class SmartHome : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(applicationContext)

        val appPreferences = AppPreferences(this)

        // Push Notification
        val pushNotification = appPreferences.isPushNotificationOn
        if (pushNotification) {
            // TODO("Turn On Push Notification")
        }
        else {
            appPreferences.isPushNotificationOn = false
        }

        // Subscribe Newsletter
        val subscribeNewsletter = appPreferences.isSubscribeNewsletterOn
        if (subscribeNewsletter) {
            // TODO("Turn On Subscribe Newsletter")
        }
        else {
            appPreferences.isSubscribeNewsletterOn = false
        }

        // Dynamic Color
        val dynamicColor = appPreferences.isDynamicColorOn
        if (dynamicColor) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        else {
            appPreferences.isDynamicColorOn = false
        }

        // Theme
        val selectedTheme = appPreferences.selectedTheme
        AppCompatDelegate.setDefaultNightMode(
            when (selectedTheme) {
                AppPreferences.ThemeList.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppPreferences.ThemeList.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                AppPreferences.ThemeList.DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )

        // Languages
        val selectedLanguage = appPreferences.selectedLanguage
        val locale = Locale(
            when (selectedLanguage) {
                AppPreferences.LanguageList.ENGLISH -> LOCALE_ENGLISH
                AppPreferences.LanguageList.INDONESIA -> LOCALE_INDONESIA
                AppPreferences.LanguageList.JAVANESE -> LOCALE_JAVANESE
                else -> DEFAULT
            }
        )

        // TODO("Fix applied language while re-opening apps")
        Locale.setDefault(locale)
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

}
