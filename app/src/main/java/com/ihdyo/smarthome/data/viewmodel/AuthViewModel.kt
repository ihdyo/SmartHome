package com.ihdyo.smarthome.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ihdyo.smarthome.data.repository.AuthRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> get() = _currentUser

    private val _isCurrentUserVerified = MutableLiveData<Boolean?>()
    val isCurrentUserVerified: LiveData<Boolean?> get() = _isCurrentUserVerified

    private val _reAuthResult = MutableLiveData<Boolean>()
    val reAuthResult: LiveData<Boolean> get() = _reAuthResult

    private val _currentEmail = MutableLiveData<String>()
    val currentEmail: LiveData<String> get() = _currentEmail

    private val _currentPassword = MutableLiveData<String>()
    val currentPassword: LiveData<String> get() = _currentPassword



    private val _changeEmailResult = MutableLiveData<Boolean>()
    val changeEmailResult: LiveData<Boolean> get() = _changeEmailResult

    private val _changePassword = MutableLiveData<String>()
    val changePassword: LiveData<String> get() = _changePassword

    private val _changePasswordResult = MutableLiveData<Boolean>()
    val changePasswordResult: LiveData<Boolean> get() = _changePasswordResult



    // ========================= GET METHOD ========================= //

    fun getCurrentUser() {
        _currentUser.value = authRepository.getCurrentUser()
    }

    fun isVerified() {
        viewModelScope.launch {
            val user = currentUser.value?.isEmailVerified
            _isCurrentUserVerified.value = user
        }
    }


    // ========================= EMAIL SIGN IN ========================= //

    fun signInWithEmail(email: String, password: String, onSuccess: (FirebaseUser) -> Unit, onFailed: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.authWithEmail(email, password)
                if (user != null) {
                    _currentUser.value = user
                    onSuccess(user)
                    Log.d(TAG, "Sign in with email successful")
                } else {
                    onFailed("Authentication failed")
                }
            } catch (e: Exception) {
                onFailed("Error signing in with email: ${e.message}")
                Log.e(TAG, "Error signing in with email", e)
            }
        }
    }


    // ========================= GOOGLE SIGN IN ========================= //

    fun signInWithGoogle(idToken: String, onSuccess: (FirebaseUser) -> Unit, onFailed: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.authWithGoogle(idToken)
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


    // ========================= UPDATE CREDENTIALS ========================= //

    fun reAuth(currentEmail: String, currentPassword: String) {
        viewModelScope.launch {

            val result = authRepository.reAuth(currentEmail, currentPassword)
            _reAuthResult.postValue(result)

            if (result) {
                _currentEmail.postValue(currentEmail)
                _currentPassword.postValue(currentPassword)

                Log.d(TAG, "Re-Authentication Successful")
            } else {
                Log.d(TAG, "Re-Authentication Failed")
            }
        }
    }

    suspend fun changeEmail(newEmail: String): Boolean {
        if (reAuthResult.value == true) {
            val result = authRepository.changeEmail(
                currentEmail.value.orEmpty(),
                currentPassword.value.orEmpty(),
                newEmail
            )

            if (result) {
                _currentEmail.postValue(newEmail)
                Log.d(TAG, "Email changed successfully to $newEmail")
            } else {
                Log.e(TAG, "Failed to change email")
            }
            return result
        } else {
            Log.e(TAG, "Re-Authentication failed, cannot change email")
            return false
        }
    }

    fun checkPassword(newPassword: String) {
        viewModelScope.launch {
            _changePassword.postValue(newPassword)
        }
    }

    fun changePassword(reNewPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.changePassword(reNewPassword)
            _changePasswordResult.postValue(result)

            if (result) {
                Log.d(TAG, "Password change successful")
            } else {
                Log.e(TAG, "Failed to change password")
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

    companion object {
        private const val TAG = "AuthViewModel"
    }

}