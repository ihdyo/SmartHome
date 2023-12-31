package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LampModel (
    val LID: String? = "",
    var lampBrightness: Int = 0,
    var lampIsAutomaticOn: Boolean? = false,
    var lampIsPowerOn: Boolean? = false,
    val lampRuntime: Int = 0,
    var lampSchedule: LampSchedule = LampSchedule(),
    var lampSelectedMode: String = "manual",
    val lampWattPower: Int = 0
) : Parcelable