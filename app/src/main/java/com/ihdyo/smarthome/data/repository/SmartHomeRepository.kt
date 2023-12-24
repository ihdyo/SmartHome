package com.ihdyo.smarthome.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.UserModel
import kotlinx.coroutines.tasks.await

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
            Log.e(TAG, "Error fetching user: $e")
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
            Log.e(TAG, "Error fetching rooms: $e")
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
            Log.e(TAG, "Error fetching lamps: $e")
            emptyList()
        }
    }



    suspend fun getLampWattPower(userId: String, roomId: String, lampId: String): Int {
        return try {
            val documentSnapshot = firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .get().await()

            documentSnapshot.getLong("lampWattPower")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lampWattPower: $e")
            0
        }
    }


    fun getLampBrightness(userId: String, roomId: String, lampId: String, callback: (Int) -> Unit) {
        try {
            firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing lampBrightness: $error")
                        callback(0)
                    } else {
                        val lampBrightness = documentSnapshot?.getLong("lampBrightness")?.toInt() ?: 0
                        callback(lampBrightness)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing lampBrightness: $e")
            callback(0)
        }
    }

    fun getLampIsAutomaticOn(userId: String, roomId: String, lampId: String, callback: (Boolean) -> Unit) {
        try {
            firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing lampIsAutomaticOn: $error")
                        callback(false)
                    } else {
                        val lampIsAutomaticOn = documentSnapshot?.getBoolean("lampIsAutomaticOn") ?: false
                        callback(lampIsAutomaticOn)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing lampIsAutomaticOn: $e")
            callback(false)
        }
    }

    fun getLampIsPowerOn(userId: String, roomId: String, lampId: String, callback: (Boolean) -> Unit) {
        try {
            firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing lampIsPowerOn: $error")
                        callback(false)
                    } else {
                        val lampIsPowerOn = documentSnapshot?.getBoolean("lampIsPowerOn") ?: false
                        callback(lampIsPowerOn)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing lampIsPowerOn: $e")
            callback(false)
        }
    }

    fun getLampRuntime(userId: String, roomId: String, lampId: String, callback: (Int) -> Unit) {
        try {
            firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing lampRuntime: $error")
                        callback(0)
                    } else {
                        val lampRuntime = documentSnapshot?.getLong("lampRuntime")?.toInt() ?: 0
                        callback(lampRuntime)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing lampRuntime: $e")
            callback(0)
        }
    }

    fun getLampSchedule(
        userId: String,
        roomId: String,
        lampId: String,
        callback: (Map<String, String>) -> Unit
    ) {
        try {
            firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing lampSchedule: $error")
                        callback(emptyMap())
                    } else {
                        val lampSchedule = documentSnapshot?.get("lampSchedule") as? Map<String, String> ?: emptyMap()
                        callback(lampSchedule)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing lampSchedule: $e")
            callback(emptyMap())
        }
    }

    fun getLampSelectedMode(userId: String, roomId: String, lampId: String, callback: (String) -> Unit) {
        try {
            firestore.collection("users").document(userId)
                .collection("rooms").document(roomId)
                .collection("lamps").document(lampId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing lampSelectedMode: $error")
                        callback("")
                    } else {
                        val lampSelectedMode = documentSnapshot?.getString("lampSelectedMode") ?: ""
                        callback(lampSelectedMode)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing lampSelectedMode: $e")
            callback("")
        }
    }





    // Update functions for Lamp attributes
    fun putIsPowerOn(userId: String, roomId: String, isPowerOn: Boolean) {
        updateLampField(userId, roomId, "isPowerOn", isPowerOn)
    }

    fun putIsAutomaticOn(userId: String, roomId: String, isAutomaticOn: Boolean) {
        updateLampField(userId, roomId, "isAutomaticOn", isAutomaticOn)
    }

    // Add similar functions for lampBrightness, lampSchedule, and lampSelectedMode

    // Helper function for updating lamp fields
    private fun updateLampField(userId: String, roomId: String, field: String, value: Any) {
        firestore.collection("users")
            .document(userId)
            .collection("rooms")
            .document(roomId)
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
//            Log.e(TAG, "Error fetching lamp details", e)
//            emptyList()
//        }
//    }

//    fun getMode(lamp: LampModel, callback: (String?) -> Unit) {
//        val document = collectionRef.document(lamp.id)
//
//        document.addSnapshotListener { snapshot, exception ->
//            if (exception != null) {
//                Log.e(TAG, "Error fetching selected mode", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success fetching selected mode")
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
//                Log.e(TAG, "Error fetching power state", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success fetching power state")
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
//                Log.e(TAG, "Error fetching schedule start time", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success fetching schedule start time")
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
//                Log.e(TAG, "Error fetching schedule finish time", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success fetching schedule finish time")
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
//                Log.e(TAG, "Error fetching lamp runtime", exception)
//                callback(null)
//                return@addSnapshotListener
//            }
//
//            Log.d(TAG, "Success fetching lamp runtime")
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