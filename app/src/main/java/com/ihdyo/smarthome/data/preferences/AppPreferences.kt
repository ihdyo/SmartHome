package com.ihdyo.smarthome.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.ihdyo.smarthome.utils.Const.DARK_THEME
import com.ihdyo.smarthome.utils.Const.LIGHT_THEME
import com.ihdyo.smarthome.utils.Const.SELECTED_LANGUAGE
import com.ihdyo.smarthome.utils.Const.SHARED_PREFERENCES
import com.ihdyo.smarthome.utils.Const.SYSTEM_DEFAULT_LANGUAGE
import com.ihdyo.smarthome.utils.Const.SYSTEM_DEFAULT_THEME
import com.ihdyo.smarthome.utils.Const.USED_THEME

class AppPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)


    // ========================= THEMES ========================= //

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


    // ========================= LANGUAGES ========================= //

    var selectedLanguage: String
        get() = sharedPreferences.getString(SELECTED_LANGUAGE, SYSTEM_DEFAULT_LANGUAGE) ?: SYSTEM_DEFAULT_LANGUAGE
        set(value) = sharedPreferences.edit().putString(SELECTED_LANGUAGE, value).apply()

}