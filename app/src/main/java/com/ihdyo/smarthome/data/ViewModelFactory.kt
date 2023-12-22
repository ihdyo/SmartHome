package com.ihdyo.smarthome.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihdyo.smarthome.data.repository.SmartHomeRepository
import com.ihdyo.smarthome.ui.home.HomeViewModel

class ViewModelFactory(private val smartHomeRepository: SmartHomeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(smartHomeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
