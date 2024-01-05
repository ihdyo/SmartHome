package com.ihdyo.smarthome.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CampaignModel (
    val CID: String? = "",
    val campaignTitle: String? = null,
    val campaignLink: String? = null,
    val campaignName: String? = null,
    val campaignSummary: String? = null,
    val campaignVideo: String? = null
) : Parcelable