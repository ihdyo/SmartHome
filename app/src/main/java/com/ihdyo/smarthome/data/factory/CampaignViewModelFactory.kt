package com.ihdyo.smarthome.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihdyo.smarthome.data.repository.CampaignRepository
import com.ihdyo.smarthome.ui.viewmodel.CampaignViewModel

@Suppress("UNCHECKED_CAST")
class CampaignViewModelFactory(private val campaignRepository: CampaignRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            return CampaignViewModel(campaignRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}