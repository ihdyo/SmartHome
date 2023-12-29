package com.ihdyo.smarthome.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel

@Suppress("UNCHECKED_CAST")
class AuthFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
