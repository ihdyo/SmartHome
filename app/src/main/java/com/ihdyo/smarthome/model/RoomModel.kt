package com.ihdyo.smarthome.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class RoomModel (
    var automatic: Boolean? = null,
    var floor: String? = null,
    var image: String? = null,
    var power: Boolean? = null,
    var room: String? = null
) : Parcelable