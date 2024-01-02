package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdminModel (
    val AID: String? = "",
    val adminEmail: String? = null,
    val adminName: String? = null,
    val adminNumber: String? = null
) : Parcelable