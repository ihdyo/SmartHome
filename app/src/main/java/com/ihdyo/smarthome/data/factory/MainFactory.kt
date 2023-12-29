package com.ihdyo.smarthome.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.viewmodel.MainViewModel

@Suppress("UNCHECKED_CAST")
class MainFactory(private val mainRepository: MainRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(mainRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
