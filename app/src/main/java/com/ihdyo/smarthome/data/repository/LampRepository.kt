package com.ihdyo.smarthome.data.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.model.LampModel
class LampRepository(firestore: FirebaseFirestore) {

    companion object {
        const val COLLECTION_LAMPS = "lamps"
        const val FIELD_MODE = "mode"
        const val FIELD_IS_POWER_ON = "isPowerOn"
        const val FIELD_IS_SCHEDULE_ON = "isScheduleOn"
        const val FIELD_IS_AUTOMATIC_ON = "isAutomaticOn"
        const val FIELD_SCHEDULE_FROM = "scheduleFrom"
        const val FIELD_SCHEDULE_TO = "scheduleTo"
        const val FIELD_LAMP_RUNTIME = "lampRuntime"
    }

    private val collectionRef = firestore.collection(COLLECTION_LAMPS)

    //  ===================================================== REQUEST METHOD: GET ===================================================== //
    fun getLamps(callback: (List<LampModel>) -> Unit) {
        collectionRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching lamp details", exception)
                callback(emptyList())
                return@addSnapshotListener
            }
            Log.d(TAG, "Success fetching lamp details")

            val lamps = mutableListOf<LampModel>()
            for (document in snapshot!!.documents) {
                val lamp = document.toObject(LampModel::class.java)
                lamp?.let {
                    lamps.add(it)
                }
            }
            callback(lamps)

        }
    }

    fun getMode(lamp: LampModel, callback: (String?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching selected mode", exception)
                callback(null)
                return@addSnapshotListener
            }

            Log.d(TAG, "Success fetching selected mode")

            val mode = snapshot?.getString(FIELD_MODE)
            callback(mode)
        }
    }

    fun getIsPowerOn(lamp: LampModel, callback: (Boolean?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching power state", exception)
                callback(null)
                return@addSnapshotListener
            }

            Log.d(TAG, "Success fetching power state")

            val isPowerOn = snapshot?.getBoolean(FIELD_IS_POWER_ON) ?: false
            callback(isPowerOn)
        }
    }

    fun getScheduleFrom(lamp: LampModel, callback: (String?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching schedule start time", exception)
                callback(null)
                return@addSnapshotListener
            }

            Log.d(TAG, "Success fetching schedule start time")

            val scheduleFrom = snapshot?.getString(FIELD_SCHEDULE_FROM)
            callback(scheduleFrom)
        }
    }

    fun getScheduleTo(lamp: LampModel, callback: (String?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching schedule finish time", exception)
                callback(null)
                return@addSnapshotListener
            }

            Log.d(TAG, "Success fetching schedule finish time")

            val scheduleTo = snapshot?.getString(FIELD_SCHEDULE_TO)
            callback(scheduleTo)
        }
    }

    fun getLampRuntime(lamp: LampModel, callback: (Int?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error fetching lamp runtime", exception)
                callback(null)
                return@addSnapshotListener
            }

            Log.d(TAG, "Success fetching lamp runtime")

            val lampRuntime = snapshot?.getLong(FIELD_LAMP_RUNTIME)?.toInt()
            callback(lampRuntime)
        }
    }

    fun getTotalLampRuntime(callback: (Int?) -> Unit) {
        collectionRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Error calculating total lamp runtime", exception)
                callback(null)
                return@addSnapshotListener
            }

            Log.d(TAG, "Success calculating total lamp runtime")

            var totalRuntime = 0

            for (document in snapshot!!.documents) {
                val lamp = document.toObject(LampModel::class.java)
                lamp?.lampRuntime?.let {
                    totalRuntime += it
                }
            }
            callback(totalRuntime)
        }
    }

    //  ===================================================== REQUEST METHOD: PUT ===================================================== //
    fun putMode(lamp: LampModel) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_MODE, lamp.mode)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating selected mode")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating selected mode", exception)
                throw exception
            }

        document.update(FIELD_IS_AUTOMATIC_ON, lamp.isAutomaticOn)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating automatic state")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating automatic state", exception)
                throw exception
            }

        document.update(FIELD_IS_SCHEDULE_ON, lamp.isScheduleOn)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating schedule state")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating schedule state", exception)
                throw exception
            }
    }

    fun putIsPowerOn(lamp: LampModel, callback: (Boolean) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_IS_POWER_ON, lamp.isPowerOn)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating power state")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating power state", exception)
                callback(false)
            }
    }

    fun putScheduleFrom(lamp: LampModel, callback: (Boolean, Exception?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_SCHEDULE_FROM, lamp.scheduleFrom)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating schedule start time")
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating schedule start time", exception)
                callback(false, exception)
            }
    }

    fun putScheduleTo(lamp: LampModel, callback: (Boolean, Exception?) -> Unit) {
        val document = collectionRef.document(lamp.id)

        document.update(FIELD_SCHEDULE_TO, lamp.scheduleTo)
            .addOnSuccessListener {
                Log.d(TAG, "Success updating schedule finish time")
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating schedule finish time", exception)
                callback(false, exception)
            }
    }
}