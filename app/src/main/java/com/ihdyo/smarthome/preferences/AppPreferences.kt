package com.ihdyo.smarthome.preferences

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    companion object {
        const val USED_THEME = "used_theme"
        const val LIGHT_THEME = "light_theme"
        const val DARK_THEME = "dark_theme"
        const val SYSTEM_DEFAULT_THEME = "system_default_theme"
    }

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM_DEFAULT
    }

    var themeMode: ThemeMode
        get() {
            return when (sharedPreferences.getString(USED_THEME, LIGHT_THEME)) {
                LIGHT_THEME -> ThemeMode.LIGHT
                DARK_THEME -> ThemeMode.DARK
                SYSTEM_DEFAULT_THEME -> ThemeMode.SYSTEM_DEFAULT
                else -> ThemeMode.LIGHT
            }
        }
        set(value) {
            val themeToSave = when (value) {
                ThemeMode.LIGHT -> LIGHT_THEME
                ThemeMode.DARK -> DARK_THEME
                ThemeMode.SYSTEM_DEFAULT -> SYSTEM_DEFAULT_THEME
            }
            sharedPreferences.edit().putString(USED_THEME, themeToSave).apply()
        }
}