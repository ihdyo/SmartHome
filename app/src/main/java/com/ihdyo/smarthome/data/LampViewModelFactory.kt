package com.ihdyo.smarthome.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihdyo.smarthome.data.repository.LampRepository
import com.ihdyo.smarthome.ui.home.LampViewModel

class LampViewModelFactory(private val lampRepository: LampRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LampViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LampViewModel(lampRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
