package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoomModel (
    var lampIsAutomaticOn: Boolean? = false,
    var lampIsPowerOn: Boolean? = false,
    var lampBrightness: Int = 0,
    var lampRuntime: Int = 0,
    var lampSchedule: LampSchedule = LampSchedule("", ""),
    var lampSelectedMode: String = "manual",
    val lampWattPower: Int = 0,
    val roomFloor: Int = 0,
    val roomIcon: String? = null,
    val roomImage: String? = null,
    val roomName: String? = null,
) : Parcelable