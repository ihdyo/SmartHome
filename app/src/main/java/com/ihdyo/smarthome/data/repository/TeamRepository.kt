package com.ihdyo.smarthome.data.repository
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.TeamModel
import com.ihdyo.smarthome.utils.Const
import kotlinx.coroutines.tasks.await

class TeamRepository(private val firestore: FirebaseFirestore) {


    // ========================= GET TEAM ========================= //

    suspend fun getAllTeam(): List<TeamModel>? {
        return try {
            val querySnapshot = firestore.collection(Const.COLLECTION_TEAM).get().await()
            val teamList = mutableListOf<TeamModel>()

            for (document in querySnapshot.documents) {
                val team = document.toObject(TeamModel::class.java)
                team?.let { teamList.add(it) }
            }

            Log.d(TAG, "Successfully retrieved all team")
            teamList
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all team: $e")
            null
        }
    }

    companion object {
        private const val TAG = "TeamRepository"
    }

}
