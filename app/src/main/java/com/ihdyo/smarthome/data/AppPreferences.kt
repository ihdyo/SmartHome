package com.ihdyo.smarthome.data

import android.content.Context
import android.content.SharedPreferences
import com.ihdyo.smarthome.utils.Const.DEFAULT
import com.ihdyo.smarthome.utils.Const.DYNAMIC_COLOR
import com.ihdyo.smarthome.utils.Const.LANGUAGE_DEFAULT
import com.ihdyo.smarthome.utils.Const.LANGUAGE_ENGLISH
import com.ihdyo.smarthome.utils.Const.LANGUAGE_INDONESIA
import com.ihdyo.smarthome.utils.Const.LANGUAGE_JAVANESE
import com.ihdyo.smarthome.utils.Const.LANGUAGE_SELECTED
import com.ihdyo.smarthome.utils.Const.PUSH_NOTIFICATION
import com.ihdyo.smarthome.utils.Const.SHARED_PREFERENCES
import com.ihdyo.smarthome.utils.Const.SUBSCRIBE_NEWSLETTER
import com.ihdyo.smarthome.utils.Const.THEME_DARK
import com.ihdyo.smarthome.utils.Const.THEME_LIGHT
import com.ihdyo.smarthome.utils.Const.THEME_SELECTED

class AppPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)

    enum class ThemeList {
        LIGHT, DARK, DEFAULT
    }

    enum class LanguageList {
        ENGLISH, INDONESIA, JAVANESE, DEFAULT
    }


    // ========================= PUSH NOTIFICATIONS ========================= //

    var isPushNotificationOn: Boolean
        get() = sharedPreferences.getBoolean(PUSH_NOTIFICATION, true)
        set(value) = sharedPreferences.edit().putBoolean(PUSH_NOTIFICATION, value).apply()


    // ========================= SUBSCRIBE NEWSLETTER ========================= //

    var isSubscribeNewsletterOn: Boolean
        get() = sharedPreferences.getBoolean(SUBSCRIBE_NEWSLETTER, false)
        set(value) = sharedPreferences.edit().putBoolean(SUBSCRIBE_NEWSLETTER, value).apply()


    // ========================= DYNAMIC COLOR ========================= //

    var isDynamicColorOn: Boolean
        get() = sharedPreferences.getBoolean(DYNAMIC_COLOR, false)
        set(value) = sharedPreferences.edit().putBoolean(DYNAMIC_COLOR, value).apply()


    // ========================= THEMES ========================= //

    var selectedTheme: ThemeList
        get() {
            return when (sharedPreferences.getString(THEME_SELECTED, DEFAULT)) {
                THEME_LIGHT -> ThemeList.LIGHT
                THEME_DARK -> ThemeList.DARK
                DEFAULT -> ThemeList.DEFAULT
                else -> ThemeList.DEFAULT
            }
        }
        set(value) {
            val themeToSave = when (value) {
                ThemeList.LIGHT -> THEME_LIGHT
                ThemeList.DARK -> THEME_DARK
                ThemeList.DEFAULT -> DEFAULT
            }
            sharedPreferences.edit().putString(THEME_SELECTED, themeToSave).apply()
        }


    // ========================= LANGUAGES ========================= //

    var selectedLanguage: LanguageList
        get() {
            return when (sharedPreferences.getString(LANGUAGE_SELECTED, LANGUAGE_DEFAULT)) {
                LANGUAGE_ENGLISH -> LanguageList.ENGLISH
                LANGUAGE_INDONESIA -> LanguageList.INDONESIA
                LANGUAGE_JAVANESE -> LanguageList.JAVANESE
                LANGUAGE_DEFAULT -> LanguageList.DEFAULT
                else -> LanguageList.DEFAULT
            }
        }
        set(value) {
            val themeToSave = when (value) {
                LanguageList.ENGLISH -> LANGUAGE_ENGLISH
                LanguageList.INDONESIA -> LANGUAGE_INDONESIA
                LanguageList.JAVANESE -> LANGUAGE_JAVANESE
                LanguageList.DEFAULT -> LANGUAGE_DEFAULT
            }
            sharedPreferences.edit().putString(LANGUAGE_SELECTED, themeToSave).apply()
        }

}
