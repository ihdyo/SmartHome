package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LampSchedule(
    var scheduleFrom: String? = null,
    var scheduleTo: String? = null
) : Parcelable
