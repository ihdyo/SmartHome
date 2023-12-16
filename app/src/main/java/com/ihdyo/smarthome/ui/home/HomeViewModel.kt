package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.StorageException
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.repository.LampRepository
import com.ihdyo.smarthome.ui.home.HomeFragment.Companion.WATT_POWER
import kotlinx.coroutines.launch

class HomeViewModel(private val lampRepository: LampRepository) : ViewModel() {

    private val _lampDetails = MutableLiveData<List<LampModel>>()
    val lampDetails: LiveData<List<LampModel>> get() = _lampDetails

    private val _selectedLamp = MutableLiveData<LampModel>()
    val selectedLamp: LiveData<LampModel> get() = _selectedLamp

    private val _lampImage = MutableLiveData<String>()
    val lampImage: LiveData<String> get() = _lampImage

    private val _totalPowerConsumed = MutableLiveData<String>()
    val totalPowerConsumed: LiveData<String> get() = _totalPowerConsumed

    @SuppressLint("NullSafeMutableLiveData")
    fun loadLampDetails(lampIds: List<String>) {
        viewModelScope.launch {
            try {
                val lamps = lampRepository.getLampsById(lampIds)
                _lampDetails.postValue(lamps)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching lamp details", e)
            }
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun fetchLampDetails() {
        viewModelScope.launch {
            try {
                val lamps = lampRepository.getLamps()
                _lampDetails.postValue(lamps)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching lamp details", e)
            }
        }
    }

    fun loadLampImage(storagePath: String) {
        viewModelScope.launch {
            try {
                val imageUrl = lampRepository.getLampImage(storagePath)
                if (imageUrl.isNotEmpty()) {
                    _lampImage.postValue(imageUrl)
                } else {
                    Log.e("LampViewModel", "Empty image URL for storage path: $storagePath")
                }
            } catch (e: StorageException) {
                Log.e("LampViewModel", "Object does not exist at location: $storagePath", e)
            } catch (e: Exception) {
                Log.e("LampViewModel", "Error loading lamp image", e)
            }
        }
    }

    // Set the selected lamp when a room icon is clicked
    fun setSelectedLamp(lamp: LampModel) {
        _selectedLamp.postValue(lamp)
    }

    suspend fun calculateTotalPowerConsumed() {
        try {
            val totalRuntime = lampRepository.getTotalRuntime()
            val totalRuntimeHours = totalRuntime / 60 / 60
            val totalPowerConsumed = (WATT_POWER * totalRuntimeHours).toString()

            _totalPowerConsumed.postValue("${totalPowerConsumed}Wh")
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error calculating total power consumed", e)
        }
    }
}