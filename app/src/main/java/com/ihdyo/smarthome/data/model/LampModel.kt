package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LampModel (
    val id: String = "",
    var isAutomaticOn: Boolean? = null,
    var isPowerOn: Boolean? = null,
    var isScheduleOn: Boolean? = null,
    var mode: String? = null,
    val roomFloor: String? = null,
    val roomIcon: String? = null,
    val roomImage: String? = null,
    val roomName: String? = null,
    val scheduleFrom: String? = null,
    val scheduleTo: String? = null,
    val totalRuntime: Int = 0, // ms
) : Parcelable {
    // Default values for optional properties
    companion object {
        const val DEFAULT_MODE = "manual"
        const val DEFAULT_BOOLEAN = false
    }

    init {
        isAutomaticOn = DEFAULT_BOOLEAN
        isPowerOn = DEFAULT_BOOLEAN
        isScheduleOn = DEFAULT_BOOLEAN
        mode = DEFAULT_MODE
    }
}