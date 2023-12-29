package com.ihdyo.smarthome.data.repository
import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {


    // ========================= EMAIL AUTH ========================= //

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            Log.d(TAG, "Successfully signed in with email: $email")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with email: $email", e)
            null
        }
    }


    // ========================= GOOGLE AUTH ========================= //

    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        return try {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            Log.d(TAG, "Successfully signed in with Google")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with Google", e)
            null
        }
    }


    // ========================= OTHER METHOD ========================= //

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    companion object {
        private const val TAG = "AuthRepository"
    }

}
