package com.ihdyo.smarthome.data.viewmodel

import android.util.Log
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

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit, onFailed: (errorMessage: String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.signInWithEmail(email, password)
                _currentUser.value = user
                Log.d(TAG, "Sign in with email successful: $email")
            } catch (e: Exception) {
                Log.e(TAG, "Error signing in with email: $email", e)
            }
        }
    }


    // ========================= GOOGLE SIGN IN ========================= //

    fun signInWithGoogle(idToken: String, onSuccess: (FirebaseUser) -> Unit, onFailed: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.signInWithGoogle(idToken)
                if (user != null) {
                    _currentUser.value = user
                    onSuccess(user)
                    Log.d(TAG, "Sign in with Google successful")
                } else {
                    onFailed("Authentication failed")
                }
            } catch (e: Exception) {
                onFailed("Error signing in with Google: ${e.message}")
                Log.e(TAG, "Error signing in with Google", e)
            }
        }
    }


    // ========================= OTHER METHOD ========================= //

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                val success = authRepository.forgotPassword(email)
                if (success) {
                    Log.d(TAG, "Password reset email sent successfully to $email")
                } else {
                    Log.e(TAG, "Error sending password reset email to $email")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending password reset email to $email", e)
            }
        }
    }

    fun requestEmailVerification() {
        viewModelScope.launch {
            try {
                val success = authRepository.emailVerification()
                if (success) {
                    Log.d(TAG, "Email verification sent successfully")
                } else {
                    Log.e(TAG, "Error sending email verification")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending email verification", e)
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
    }

    fun getCurrentUser() {
        _currentUser.value = authRepository.getCurrentUser()
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }

}