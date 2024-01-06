package com.ihdyo.smarthome.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.data.model.CampaignModel
import com.ihdyo.smarthome.data.repository.CampaignRepository
import kotlinx.coroutines.launch

class CampaignViewModel(private val campaignRepository: CampaignRepository) : ViewModel() {

    private val _campaignLiveData = MutableLiveData<List<CampaignModel>?>()
    val campaignLiveData: LiveData<List<CampaignModel>?> get() = _campaignLiveData

    fun fetchAllCampaign() {
        viewModelScope.launch {
            val allCampaign = campaignRepository.getAllCampaign()
            if (allCampaign != null) {
                _campaignLiveData.postValue(allCampaign)
                Log.d(TAG, "Successfully fetched all campaign")
            } else {
                Log.e(TAG, "Error fetching all campaign")
            }
        }
    }

    companion object {
        private const val TAG = "CampaignViewModel"
    }

}