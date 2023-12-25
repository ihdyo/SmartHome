package com.ihdyo.smarthome.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SmartHomeRepository() {

    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_ROOMS = "rooms"
        const val COLLECTION_LAMPS = "lamps"
        const val FIELD_MODE = "mode"
        const val FIELD_IS_POWER_ON = "isPowerOn"
        const val FIELD_IS_SCHEDULE_ON = "isScheduleOn"
        const val FIELD_IS_AUTOMATIC_ON = "isAutomaticOn"
        const val FIELD_SCHEDULE_FROM = "scheduleFrom"
        const val FIELD_SCHEDULE_TO = "scheduleTo"
        const val FIELD_LAMP_RUNTIME = "lampRuntime"

        private const val TAG = "SmartHomeRepository"
    }

    //  ===================================================== REQUEST METHOD: GET ===================================================== //

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUser(userId: String): UserModel? {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            documentSnapshot.toObject(UserModel::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: $e")
            null
        }
    }

    suspend fun getRooms(userId: String): List<RoomModel> {
        return try {
            val querySnapshot = firestore.collection("users").document(userId)
                .collection("rooms").get().await()

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
            val querySnapshot = firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").get().await()

            querySnapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(LampModel::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lamps: $e")
            emptyList()
        }
    }



    // Update functions for Lamp attributes
    fun putLampBrightness(userId: String, roomId: String, lampId: String, lampBrightness: Int) {
        updateLampField(userId, roomId, lampId, "lampBrightness", lampBrightness)
    }

    fun putLampIsAutomaticOn(userId: String, roomId: String, lampId: String, isAutomaticOn: Boolean) {
        updateLampField(userId, roomId, lampId, "lampIsAutomaticOn", isAutomaticOn)
    }

    fun putLampIsPowerOn(userId: String, roomId: String, lampId: String, isPowerOn: Boolean) {
        updateLampField(userId, roomId, lampId, "lampIsPowerOn", isPowerOn)
    }

//    fun putLampSchedule(userId: String, roomId: String, lampId: String, lampSchedule: String) {
//        updateLampField(userId, roomId, lampId, "lampSchedule", lampSchedule)
//    }

    fun putLampSelectedMode(userId: String, roomId: String, lampId: String, selectedMode: String) {
        updateLampField(userId, roomId, lampId, "lampSelectedMode", selectedMode)
    }


    // Helper function for updating lamp fields
    private fun updateLampField(userId: String, roomId: String, lampId: String, field: String, value: Any) {
        firestore.collection("users")
            .document(userId)
            .collection("rooms")
            .document(roomId)
            .collection("lamps")
            .document(lampId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated $field")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating $field", exception)
            }
    }










//    suspend fun getLamps(): List<LampModel> {
//        return try {
//            val querySnapshot = collectionRef.get().await()
//
//            val lamps = mutableListOf<LampModel>()
//            for (document in querySnapshot.documents) {
//                val lamp = document.toObject(LampModel::class.java)
//                lamp?.let {
//                    lamps.add(it)
//                }
//            }
//
//            lamps
//        } catch (e: Exception) {
//            Log.e(TAG, "Error getting lamp details", e)
//            emptyList()
//        }
//    }

//    fun getMode(lamp: LampModel, callback: (String?) -> Unit) {
//        val document = collectionRef.document(lamp.id)
//
//        document.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error getting selected mode", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success getting selected mode")
//
//            val mode = snapshot?.getString(FIELD_MODE)
//            callback(mode)
//        }
//    }
//
//    fun getIsPowerOn(lamp: LampModel, callback: (Boolean?) -> Unit) {
//        val document = collectionRef.document(lamp.id)
//
//        document.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error getting power state", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success getting power state")
//
//            val isPowerOn = snapshot?.getBoolean(FIELD_IS_POWER_ON) ?: false
//            callback(isPowerOn)
//        }
//    }
//
//    fun getScheduleFrom(lamp: LampModel, callback: (String?) -> Unit) {
//        val document = collectionRef.document(lamp.id)
//
//        document.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error getting schedule start time", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success getting schedule start time")
//
//            val scheduleFrom = snapshot?.getString(FIELD_SCHEDULE_FROM)
//            callback(scheduleFrom)
//        }
//    }
//
//    fun getScheduleTo(lamp: LampModel, callback: (String?) -> Unit) {
//        val document = collectionRef.document(lamp.id)
//
//        document.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error getting schedule finish time", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success getting schedule finish time")
//
//            val scheduleTo = snapshot?.getString(FIELD_SCHEDULE_TO)
//            callback(scheduleTo)
//        }
//    }
//
//    fun getLampRuntime(lamp: LampModel, callback: (Int?) -> Unit) {
//        val document = collectionRef.document(lamp.id)
//
//        document.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error getting lamp runtime", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success getting lamp runtime")
//
//            val lampRuntime = snapshot?.getLong(FIELD_LAMP_RUNTIME)?.toInt()
//            callback(lampRuntime)
//        }
//    }
//
//    fun getTotalLampRuntime(callback: (Int?) -> Unit) {
//        collectionRef.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error calculating total lamp runtime", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success calculating total lamp runtime")
//
//            var totalRuntime = 0
//
//            for (document in snapshot!!.documents) {
//                val lamp = document.toObject(LampModel::class.java)
//                lamp?.lampRuntime?.let {
//                    totalRuntime += it
//                }
//            }
//            callback(totalRuntime)
//        }
//    }

    //  ===================================================== REQUEST METHOD: PUT ===================================================== //
//    fun putMode(lamp: LampModel) {
//        val document = collectionRef.document(lamp.id)
//
//        document.update(FIELD_MODE, lamp.mode)
//            .addOnSuccessListener {
//                Log.d(TAG, "Success updating selected mode")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error updating selected mode", exception)
//                throw exception
//            }
//
//        document.update(FIELD_IS_AUTOMATIC_ON, lamp.isAutomaticOn)
//            .addOnSuccessListener {
//                Log.d(TAG, "Success updating automatic state")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error updating automatic state", exception)
//                throw exception
//            }
//
//        document.update(FIELD_IS_SCHEDULE_ON, lamp.isScheduleOn)
//            .addOnSuccessListener {
//                Log.d(TAG, "Success updating schedule state")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error updating schedule state", exception)
//                throw exception
//            }
//    }
//
//    fun putIsPowerOn(lamp: LampModel) {
//        val document = collectionRef.document(lamp.id)
//
//        document.update(FIELD_IS_POWER_ON, lamp.isPowerOn)
//            .addOnSuccessListener {
//                Log.d(TAG, "Success updating power state")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error updating power state", exception)
//            }
//    }
//
//    fun putScheduleFrom(lamp: LampModel) {
//        val document = collectionRef.document(lamp.id)
//
//        document.update(FIELD_SCHEDULE_FROM, lamp.scheduleFrom)
//            .addOnSuccessListener {
//                Log.d(TAG, "Success updating schedule start time")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error updating schedule start time", exception)
//            }
//    }
//
//    fun putScheduleTo(lamp: LampModel) {
//        val document = collectionRef.document(lamp.id)
//
//        document.update(FIELD_SCHEDULE_TO, lamp.scheduleTo)
//            .addOnSuccessListener {
//                Log.d(TAG, "Success updating schedule finish time")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(TAG, "Error updating schedule finish time", exception)
//            }
//    }
}