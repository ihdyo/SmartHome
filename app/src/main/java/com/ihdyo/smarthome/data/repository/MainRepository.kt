package com.ihdyo.smarthome.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_RUNTIME
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_SCHEDULE
import com.ihdyo.smarthome.utils.Const.FIELD_LAMP_SELECTED_MODE
import com.ihdyo.smarthome.utils.Const.FIELD_USER_NAME
import com.ihdyo.smarthome.utils.Const.MAP_FIELD_SCHEDULE_FROM
import com.ihdyo.smarthome.utils.Const.MAP_FIELD_SCHEDULE_TO
import kotlinx.coroutines.tasks.await

class MainRepository(private val firestore: FirebaseFirestore) {


    // ========================= GET METHOD ========================= //

    suspend fun getAllUsers(): List<UserModel>? {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS).get().await()
            val userList = mutableListOf<UserModel>()

            for (document in querySnapshot.documents) {
                val user = document.toObject(UserModel::class.java)
                user?.let { userList.add(it) }
            }

            Log.d(TAG, "Successfully retrieved all users.")
            userList
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users: $e")
            null
        }
    }


    suspend fun getUser(userId: String): UserModel? {
        return try {
            val documentSnapshot = firestore.collection(COLLECTION_USERS).document(userId).get(Source.DEFAULT).await()
            val user = documentSnapshot.toObject(UserModel::class.java)
            Log.d(TAG, "Successfully get user with ID: $userId")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: $e")
            null
        }
    }

    suspend fun getRooms(userId: String): List<RoomModel> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_ROOMS).get(Source.DEFAULT).await()

            val rooms = querySnapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(RoomModel::class.java)
            }
            Log.d(TAG, "Successfully get rooms for user with ID: $userId")
            rooms
        } catch (e: Exception) {
            Log.e(TAG, "Error getting rooms: $e")
            emptyList()
        }
    }

    suspend fun getLamps(userId: String, roomId: String): List<LampModel> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS).document(userId)
                .collection(COLLECTION_ROOMS).document(roomId)
                .collection(COLLECTION_LAMPS).get(Source.DEFAULT).await()

            val lamps = querySnapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(LampModel::class.java)
            }
            Log.d(TAG, "Successfully get lamps for user with ID: $userId and room ID: $roomId")
            lamps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lamps: $e")
            emptyList()
        }
    }

    suspend fun getAllLamps(userId: String): List<LampModel> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_ROOMS)
                .get(Source.DEFAULT)
                .await()

            val lamps = mutableListOf<LampModel>()

            for (roomDocument in querySnapshot.documents) {
                val roomId = roomDocument.id
                val roomLampsCollection = firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_ROOMS)
                    .document(roomId)
                    .collection(COLLECTION_LAMPS)

                val roomLamps = roomLampsCollection.get(Source.DEFAULT).await().documents
                    .mapNotNull { documentSnapshot ->
                        documentSnapshot.toObject(LampModel::class.java)
                    }

                lamps.addAll(roomLamps)
            }

            Log.d(TAG, "Successfully get all lamps for user with ID: $userId")
            lamps
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lamps: $e")
            emptyList()
        }
    }


    // ========================= PUT METHOD ========================= //

    fun putLampBrightness(userId: String, roomId: String, lampId: String, lampBrightness: Int) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_BRIGHTNESS, lampBrightness)
    }

    fun putLampIsAutomaticOn(userId: String, roomId: String, lampId: String, lampIsAutomaticOn: Boolean) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_IS_AUTOMATIC_ON, lampIsAutomaticOn)
    }

    fun putLampIsPowerOn(userId: String, roomId: String, lampId: String, lampIsPowerOn: Boolean) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_IS_POWER_ON, lampIsPowerOn)
    }

    fun putLampSchedule(userId: String, roomId: String, lampId: String, lampSchedule: LampSchedule) {
        val scheduleMap = mapOf(
            MAP_FIELD_SCHEDULE_FROM to lampSchedule.scheduleFrom.orEmpty(),
            MAP_FIELD_SCHEDULE_TO to lampSchedule.scheduleTo.orEmpty()
        )
        putLampField(userId, roomId, lampId, FIELD_LAMP_SCHEDULE, scheduleMap)
    }

    fun putLampSelectedMode(userId: String, roomId: String, lampId: String, lampSelectedMode: String) {
        putLampField(userId, roomId, lampId, FIELD_LAMP_SELECTED_MODE, lampSelectedMode)
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
                Log.d(TAG, "Successfully updated $field to $value for $lampId in $roomId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating $field for $lampId in $roomId", exception)
            }
    }

    fun putUserName(userId: String, value: String) {
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .update(FIELD_USER_NAME, value)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated $FIELD_USER_NAME to $value")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating $FIELD_USER_NAME for $value", exception)
            }
    }

    fun updateAllLampRuntimes(userId: String, lampRuntime: Int) {
        val userRef = firestore.collection(COLLECTION_USERS)
            .document(userId)

        Log.d(TAG, "Fetching rooms for user: $userId")

        userRef.collection(COLLECTION_ROOMS)
            .get()
            .addOnSuccessListener { roomsSnapshot ->
                for (roomDocument in roomsSnapshot.documents) {
                    val roomId = roomDocument.id
                    val lampsRef = userRef
                        .collection(COLLECTION_ROOMS)
                        .document(roomId)
                        .collection(COLLECTION_LAMPS)

                    Log.d(TAG, "Fetching lamps for room: $roomId")

                    lampsRef.get()
                        .addOnSuccessListener { lampsSnapshot ->
                            for (lampDocument in lampsSnapshot.documents) {
                                val lampId = lampDocument.id
                                val lampRef = lampsRef.document(lampId)

                                Log.d(TAG, "Updating lamp runtime for lamp: $lampId")

                                lampRef.update(FIELD_LAMP_RUNTIME, lampRuntime)
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, "Error updating lamp runtime", exception)
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error fetching lamps for room: $roomId", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching rooms for user: $userId", exception)
            }
    }

    companion object {
        private const val TAG = "MainRepository"
    }

}