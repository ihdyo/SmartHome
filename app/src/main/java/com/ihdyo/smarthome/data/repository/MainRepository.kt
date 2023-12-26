package com.ihdyo.smarthome.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.LampSchedule
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.UserModel
import com.ihdyo.smarthome.utils.Const.COLLECTION_LAMPS
import com.ihdyo.smarthome.utils.Const.COLLECTION_ROOMS
import com.ihdyo.smarthome.utils.Const.COLLECTION_USERS
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_BRIGHTNESS
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_IS_AUTOMATIC_ON
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_IS_POWER_ON
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_SCHEDULE
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_SELECTED_MODE
import com.ihdyo.smarthome.utils.Const.MAP_FIELD_SCHEDULE_FROM
import com.ihdyo.smarthome.utils.Const.MAP_FIELD_SCHEDULE_TO
import kotlinx.coroutines.tasks.await

class MainRepository(private val firestore: FirebaseFirestore) {


    // ========================= GET METHOD ========================= //

    suspend fun getUser(userId: String): UserModel? {
        return try {
            val documentSnapshot = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            documentSnapshot.toObject(UserModel::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: $e")
            null
        }
    }

    suspend fun getRooms(userId: String): List<RoomModel> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_ROOMS).get().await()

            querySnapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(RoomModel::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting rooms: $e")
            emptyList()
        }
    }

    suspend fun getLamps(userId: String, roomId: String): List<LampModel> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_ROOMS).document(roomId)
                .collection(COLLECTION_LAMPS).get().await()

            querySnapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(LampModel::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lamps: $e")
            emptyList()
        }
    }


    // ========================= PUT METHOD ========================= //

    fun putLampBrightness(userId: String, roomId: String, lampId: String, lampBrightness: Int) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_BRIGHTNESS, lampBrightness)
    }

    fun putLampIsAutomaticOn(userId: String, roomId: String, lampId: String, isAutomaticOn: Boolean) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_IS_AUTOMATIC_ON, isAutomaticOn)
    }

    fun putLampIsPowerOn(userId: String, roomId: String, lampId: String, isPowerOn: Boolean) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_IS_POWER_ON, isPowerOn)
    }

    fun putLampSchedule(userId: String, roomId: String, lampId: String, lampSchedule: LampSchedule) {
        val scheduleMap = mapOf(
            MAP_FIELD_SCHEDULE_FROM to lampSchedule.scheduleFrom,
            MAP_FIELD_SCHEDULE_TO to lampSchedule.scheduleTo
        )
        putLampField(userId, roomId, lampId, FIELD_LAMP_SCHEDULE, scheduleMap)
    }

    fun putLampSelectedMode(userId: String, roomId: String, lampId: String, selectedMode: String) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_SELECTED_MODE, selectedMode)
    }

    private fun putLampField(userId: String, roomId: String, lampId: String, field: String, value: Any) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ROOMS)
            .document(roomId)
            .collection(COLLECTION_LAMPS)
            .document(lampId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated $field")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating $field", exception)
            }
    }

    companion object {
        private const val TAG = "MainRepository"
    }

}