package com.ihdyo.smarthome.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.LampModel
import kotlinx.coroutines.tasks.await

class LampRepository(firestore: FirebaseFirestore) {

    companion object {
        const val COLLECTION_LAMPS = "lamps"
        const val FIELD_MODE = "mode"
        const val FIELD_IS_POWER_ON = "isPowerOn"
        const val FIELD_IS_SCHEDULE_ON = "isScheduleOn"
        const val FIELD_IS_AUTOMATIC_ON = "isAutomaticOn"
        const val FIELD_SCHEDULE_FROM = "scheduleFrom"
        const val FIELD_SCHEDULE_TO = "scheduleTo"
    }

    private val collectionRef = firestore.collection(COLLECTION_LAMPS)

    // Get all data from lamps collection
    suspend fun getLamps(): List<LampModel> {
        val lamps = mutableListOf<LampModel>()

        try {
            val querySnapshot = collectionRef.get().await()
            for (document in querySnapshot.documents) {
                val lamp = document.toObject(LampModel::class.java)
                lamp?.let { lamps.add(it) }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Error fetching lamps", e)
        }

        return lamps
    }

    // Calculate all totalRuntime field
    suspend fun getTotalRuntime(): Int {
        var totalRuntime = 0

        try {
            val querySnapshot = collectionRef.get().await()
            for (document in querySnapshot.documents) {
                val lamp = document.toObject(LampModel::class.java)
                lamp?.totalRuntime?.let {
                    totalRuntime += it
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, "Error fetching totalRuntime", e)
        }

        return totalRuntime
    }

    // Update mode field
    fun putMode(lamp: LampModel) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_MODE, lamp.mode)
            .addOnFailureListener { e ->
                Log.e(this.javaClass.simpleName, "Error updating mode", e)
                throw e
            }

        document.update(FIELD_IS_AUTOMATIC_ON, lamp.isAutomaticOn)
            .addOnFailureListener { e ->
                Log.e(this.javaClass.simpleName, "Error updating isAutomaticOn", e)
                throw e
            }

        document.update(FIELD_IS_SCHEDULE_ON, lamp.isScheduleOn)
            .addOnFailureListener { e ->
                Log.e(this.javaClass.simpleName, "Error updating isScheduleOn", e)
                throw e
            }
    }

    // Update isPowerOn field
    fun putIsPowerOn(lamp: LampModel, callback: (Boolean) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_IS_POWER_ON, lamp.isPowerOn)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                callback(false)
                Log.e(this.javaClass.simpleName, "Error updating isPowerOn", e)
            }
    }

    // Update scheduleFrom field
    fun putScheduleFrom(lamp: LampModel) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_SCHEDULE_FROM, lamp.scheduleFrom)
            .addOnFailureListener { e ->
                Log.e(this.javaClass.simpleName, "Error updating schedule start time", e)
                throw e
            }
    }

    // Update scheduleTo field
    fun putScheduleTo(lamp: LampModel) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_SCHEDULE_TO, lamp.scheduleTo)
            .addOnFailureListener { e ->
                Log.e(this.javaClass.simpleName, "Error updating schedule finish time", e)
                throw e
            }
    }
}