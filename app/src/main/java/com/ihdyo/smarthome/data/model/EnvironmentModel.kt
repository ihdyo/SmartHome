package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EnvironmentModel (
    val SID: String? = "",
    val SensorName: String? = null,
    val sensorValue: Boolean? = false
) : Parcelable