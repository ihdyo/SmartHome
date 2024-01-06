package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TeamModel (
    val TID: String? = "",
    val teamImage: String? = null,
    val teamName: String? = null,
    val teamNumber: String? = null,
    val teamRole: String? = null,
    val teamSocial: String? = null,
) : Parcelable