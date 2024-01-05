package com.ihdyo.smarthome.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.AdminModel
import com.ihdyo.smarthome.utils.Const
import com.ihdyo.smarthome.utils.Const.DOCUMENT_ADMIN
import kotlinx.coroutines.tasks.await

class AdminRepository(private val firestore: FirebaseFirestore) {

    suspend fun getAdmin(): AdminModel? {
        return try {
            val documentSnapshot = firestore.collection(Const.COLLECTION_ADMIN).document(DOCUMENT_ADMIN).get().await()

            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(AdminModel::class.java)
            } else {
                Log.d(TAG, "Admin document does not exist.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting admin: $e")
            null
        }
    }

    companion object {
        private const val TAG = "AdminRepository"
    }

}