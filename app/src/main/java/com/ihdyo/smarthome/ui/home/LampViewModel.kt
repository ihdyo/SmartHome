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
import kotlinx.coroutines.launch

class LampViewModel(private val lampRepository: LampRepository) : ViewModel() {

    private val _lampDetails = MutableLiveData<List<LampModel>>()
    val lampDetails: LiveData<List<LampModel>> get() = _lampDetails

    private val _lampImage = MutableLiveData<String>()
    val lampImage: LiveData<String> get() = _lampImage

    @SuppressLint("NullSafeMutableLiveData")
    fun loadLampDetails(lampIds: List<String>) {
        viewModelScope.launch {
            try {
                val lamps = lampRepository.getLampsById(lampIds)
                // Check if the list is not null before posting the value
                if (lamps != null) {
                    _lampDetails.postValue(lamps)

                    // Set the image URL of the first lamp for display
                    if (lamps.isNotEmpty()) {
                        _lampImage.postValue(lamps.first().roomImage)
                    }
                } else {
                    // Handle the case where getLampsById returns null
                    Log.d("LampViewModel", "Lamps list is null")
                }
            } catch (e: Exception) {
                // Handle the exception (e.g., log or show an error message)
                Log.e("LampViewModel", "Error fetching lamp details", e)
            }
        }
    }

    // Use this function for a more general case where you fetch lamps without specific IDs
    @SuppressLint("NullSafeMutableLiveData")
    fun fetchLampDetails() {
        viewModelScope.launch {
            try {
                val lamps = lampRepository.getLamps()  // Update this line based on your repository method
                // Check if the list is not null before posting the value
                _lampDetails.postValue(lamps)

                // Set the image URL of the first lamp for display
                if (lamps.isNotEmpty()) {
                    _lampImage.postValue(lamps.first().roomImage)
                }
            } catch (e: Exception) {
                // Handle the exception (e.g., log or show an error message)
                Log.e("LampViewModel", "Error fetching lamp details", e)
            }
        }
    }

    fun loadLampImage(storagePath: String) {
        viewModelScope.launch {
            try {
                // Update the LiveData with the image URL
                val imageUrl = lampRepository.getLampImage(storagePath)

                if (imageUrl.isNotEmpty()) {
                    _lampImage.postValue(imageUrl)
                } else {
                    // Handle the case where the image URL is empty
                    Log.e("LampViewModel", "Empty image URL for storage path: $storagePath")
                }
            } catch (e: StorageException) {
                // Handle the case where the object does not exist
                Log.e("LampViewModel", "Object does not exist at location: $storagePath", e)
            } catch (e: Exception) {
                // Handle other exceptions
                Log.e("LampViewModel", "Error loading lamp image", e)
            }
        }
    }

}