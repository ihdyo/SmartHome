package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoomModel (
    val roomFloor: Int = 0,
    val roomIcon: String? = null,
    val roomImage: String? = null,
    val roomName: String? = null
) : Parcelable