package com.ihdyo.smarthome.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ihdyo.smarthome.data.model.LampModel
import kotlinx.coroutines.tasks.await

class LampRepository(private val firestore: FirebaseFirestore, private val storage: FirebaseStorage) {

    companion object {
        const val COLLECTION_LAMPS = "lamps"
    }

    // Updated method to fetch all lamps without specific IDs
    suspend fun getLamps(): List<LampModel> {
        val lamps = mutableListOf<LampModel>()

        try {
            val querySnapshot = firestore.collection(COLLECTION_LAMPS).get().await()
            for (document in querySnapshot.documents) {
                val lamp = document.toObject(LampModel::class.java)
                lamp?.let { lamps.add(it) }
            }
        } catch (e: Exception) {
            // Handle exceptions here (e.g., log or throw a custom exception)
            Log.e("LampRepository", "Error fetching all lamps", e)
        }

        return lamps
    }

    suspend fun getLampsById(lampIds: List<String>): List<LampModel> {
        val lamps = mutableListOf<LampModel>()

        for (lampId in lampIds) {
            try {
                val documentSnapshot = firestore.collection(COLLECTION_LAMPS).document(lampId).get().await()
                if (documentSnapshot.exists()) {
                    val lamp = documentSnapshot.toObject(LampModel::class.java)
                    lamp?.let { lamps.add(it) }
                } else {
                    // Handle the case where the document does not exist
                    // Log a message or handle it according to your needs
                    Log.d("LampRepository", "Document does not exist for lampId: $lampId")
                }
            } catch (e: Exception) {
                // Handle exceptions here (e.g., log or throw a custom exception)
                Log.e("LampRepository", "Error fetching lamp details for lampId: $lampId", e)
            }
        }
        return lamps
    }


    suspend fun getLampImage(storagePath: String): String {
        val storageRef = storage.reference.child(storagePath)
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun getTotalRuntime(): Int {
        var totalRuntime = 0

        try {
            val querySnapshot = firestore.collection(LampRepository.COLLECTION_LAMPS).get().await()
            for (document in querySnapshot.documents) {
                val lamp = document.toObject(LampModel::class.java)
                lamp?.totalRuntime?.let {
                    totalRuntime += it
                }
            }
        } catch (e: Exception) {
            // Handle exceptions here (e.g., log or throw a custom exception)
            Log.e("LampRepository", "Error fetching total runtime", e)
        }

        return totalRuntime
    }

}