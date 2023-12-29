package com.ihdyo.smarthome.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ihdyo.smarthome.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String> get() = _authError

    // ========================= EMAIL AUTH ========================= //

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            val user = authRepository.signInWithEmail(email, password)
            if (user != null) {
                _currentUser.value = user
            } else {
                _authError.value = "Error signing in with email"
            }
        }
    }

    // ========================= GOOGLE AUTH ========================= //

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            val user = authRepository.signInWithGoogle(idToken)
            if (user != null) {
                _currentUser.value = user
            } else {
                _authError.value = "Error signing in with Google"
            }
        }
    }

    // ========================= OTHER METHOD ========================= //

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
    }

    fun getCurrentUser() {
        _currentUser.value = authRepository.getCurrentUser()
    }

}