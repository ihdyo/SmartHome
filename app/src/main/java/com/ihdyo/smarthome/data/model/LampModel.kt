package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class LampModel (
    @DocumentId
    val isAutomaticOn: Boolean? = null,
    val isPowerOn: Boolean? = null,
    val isScheduleOn: Boolean? = null,
    val mode: String? = null,
    val roomFloor: String? = null,
    val roomIcon: String? = null,
    val roomImage: String? = null,
    val roomName: String? = null,
    val scheduleFrom: String? = null,
    val scheduleTo: String? = null,
    val totalRuntime: Int = 0, // ms

    var imageUrl: String? = null // Add this property for image URL
) : Parcelable