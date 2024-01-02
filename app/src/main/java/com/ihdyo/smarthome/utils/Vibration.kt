@file:Suppress("DEPRECATION")

package com.ihdyo.smarthome.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Vibration {

    fun vibrate(context: Context, durationMillis: Long = 1) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        durationMillis,
                        50
                    )
                )
            } else {
                vibrator.vibrate(durationMillis)
            }
        }
    }
}