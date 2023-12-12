package com.ihdyo.smarthome

import android.app.Application
import com.google.android.material.color.DynamicColors

class SmartHome : Application() {
    override fun onCreate() {
        super.onCreate()

        // Dynamic Color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
