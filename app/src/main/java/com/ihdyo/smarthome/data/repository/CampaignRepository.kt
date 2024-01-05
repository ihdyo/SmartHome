package com.ihdyo.smarthome.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.CampaignModel
import com.ihdyo.smarthome.utils.Const
import kotlinx.coroutines.tasks.await

class CampaignRepository(private val firestore: FirebaseFirestore) {

    suspend fun getAllCampaign(): List<CampaignModel>? {
        return try {
            val querySnapshot = firestore.collection(Const.COLLECTION_CAMPAIGN).get().await()
            val userList = mutableListOf<CampaignModel>()

            for (document in querySnapshot.documents) {
                val user = document.toObject(CampaignModel::class.java)
                user?.let { userList.add(it) }
            }

            Log.d(TAG, "Successfully retrieved all campaign.")
            userList
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all campaign: $e")
            null
        }
    }

    companion object {
        private const val TAG = "CampaignRepository"
    }

}