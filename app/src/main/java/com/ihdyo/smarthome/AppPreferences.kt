package com.ihdyo.smarthome

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    companion object {
        const val DARK_MODE = "dark_mode"
    }

    var isDarkModeEnabled: Boolean
        get() = sharedPreferences.getBoolean(DARK_MODE, false)
        set(value) = sharedPreferences.edit().putBoolean(DARK_MODE, value).apply()
}
