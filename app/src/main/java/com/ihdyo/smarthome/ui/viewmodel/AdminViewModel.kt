package com.ihdyo.smarthome.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.data.model.AdminModel
import com.ihdyo.smarthome.data.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminViewModel(private val adminRepository: AdminRepository) : ViewModel() {

    private val _adminLiveData = MutableLiveData<AdminModel?>()
    val adminLiveData: LiveData<AdminModel?> get() = _adminLiveData

    fun fetchAdmin() {
        viewModelScope.launch {
            val admin = adminRepository.getAdmin()
            if (admin != null) {
                _adminLiveData.postValue(admin)
                Log.d(TAG, "Successfully fetched admin")
            } else {
                Log.e(TAG, "Error fetching admin")
            }
        }
    }

    companion object {
        private const val TAG = "CampaignViewModel"
    }

}