package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LampModel (
    val id: String = "",
    var isAutomaticOn: Boolean = false,
    var isPowerOn: Boolean = false,
    var isScheduleOn: Boolean = false,
    var lampRuntime: Int = 0,
    var mode: String = "",
    val roomFloor: String? = null,
    val roomIcon: String? = null,
    val roomImage: String? = null,
    val roomName: String? = null,
    var scheduleFrom: String? = null,
    var scheduleTo: String? = null,
) : Parcelable