package com.ihdyo.smarthome.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.LampSchedule
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.UserModel
import com.ihdyo.smarthome.data.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MainRepository) : ViewModel() {

    private val _selectedRoom = MutableLiveData<Pair<RoomModel, String?>>()
    val selectedRoom: LiveData<Pair<RoomModel, String?>> get() = _selectedRoom

    private val _selectedLamp = MutableLiveData<LampModel>()
    val selectedLamp: LiveData<LampModel> get() = _selectedLamp


    private val _userLiveData = MutableLiveData<UserModel?>()
    val userLiveData: MutableLiveData<UserModel?> get() = _userLiveData

    private val _roomsLiveData = MutableLiveData<List<RoomModel>?>()
    val roomsLiveData: MutableLiveData<List<RoomModel>?> get() = _roomsLiveData

    private val _lampsLiveData = MutableLiveData<List<LampModel>?>()
    val lampsLiveData: MutableLiveData<List<LampModel>?> get() = _lampsLiveData


    private val _powerConsumedLiveData = MutableLiveData<Map<String, Int>>()
    val powerConsumedLiveData: LiveData<Map<String, Int>> get() = _powerConsumedLiveData

    private val _totalPowerConsumedLiveData = MutableLiveData<Int>()
    val totalPowerConsumedLiveData: LiveData<Int> get() = _totalPowerConsumedLiveData


    private val _lampBrightnessLiveData = MutableLiveData<Int>()
    val lampBrightnessLiveData: LiveData<Int> get() = _lampBrightnessLiveData

    private val _lampIsAutomaticOnLiveData = MutableLiveData<Boolean>()
    val lampIsAutomaticOnLiveData: LiveData<Boolean> get() = _lampIsAutomaticOnLiveData

    private val _lampIsPowerOnLiveData = MutableLiveData<Boolean>()
    val lampIsPowerOnLiveData: LiveData<Boolean> get() = _lampIsPowerOnLiveData

    private val _lampScheduleLiveData = MutableLiveData<LampSchedule>()
    val lampScheduleLiveData: LiveData<LampSchedule> get() = _lampScheduleLiveData

    private val _lampSelectedModeLiveData = MutableLiveData<String>()
    val lampSelectedModeLiveData: LiveData<String> get() = _lampSelectedModeLiveData


    // ========================= SET OPERATOR ========================= //

    fun setSelectedRoom(room: RoomModel, documentId: String?) {
        viewModelScope.launch {
            _selectedRoom.postValue(Pair(room, documentId))
        }
    }

    fun setSelectedLamp(lamp: LampModel) {
        viewModelScope.launch {
            _selectedLamp.postValue(lamp)
        }
    }


    // ========================= FETCH OPERATOR ========================= //

    fun fetchUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUser(userId)
                _userLiveData.postValue(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user: $e")
            }
        }
    }

    fun fetchRooms(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rooms = repository.getRooms(userId)
                _roomsLiveData.postValue(rooms)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rooms: $e")
            }
        }
    }

    fun fetchLamps(userId: String, roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lamps = repository.getLamps(userId, roomId)
                _lampsLiveData.postValue(lamps)

                val powerConsumedMap = fetchPowerConsumed(lamps)
                _powerConsumedLiveData.postValue(powerConsumedMap)

                val totalPowerConsumedMap = fetchTotalPowerConsumed(powerConsumedMap)
                _totalPowerConsumedLiveData.postValue(totalPowerConsumedMap)


                val lampBrightness = fetchLampBrightness(lamps)
                _lampBrightnessLiveData.postValue(lampBrightness)

                val lampIsPowerOn = fetchLampIsPowerOn(lamps)
                _lampIsPowerOnLiveData.postValue(lampIsPowerOn)

                val lampSchedule = fetchLampSchedule(lamps)
                _lampScheduleLiveData.postValue(lampSchedule)

                val lampSelectedMode = fetchLampSelectedMode(lamps)
                _lampSelectedModeLiveData.postValue(lampSelectedMode)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching lamps: $e")
            }
        }
    }

    private fun fetchPowerConsumed(lamps: List<LampModel>): Map<String, Int> {
        val powerConsumedMap = mutableMapOf<String, Int>()

        try {
            for (lamp in lamps) {
                val powerConsumed = (lamp.lampRuntime.div(3600)).times(lamp.lampWattPower)
                powerConsumedMap[lamp.LID.orEmpty()] = powerConsumed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating power consumption: $e")
        }

        return powerConsumedMap
    }

    private fun fetchTotalPowerConsumed(powerConsumedMap: Map<String, Int>): Int {
        var totalPowerConsumed = 0

        try {
            totalPowerConsumed = powerConsumedMap.values.sum()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total power consumption: $e")
        }

        return totalPowerConsumed
    }


    private fun fetchLampBrightness(lamps: List<LampModel>): Int {
        return try {
            lamps.firstOrNull()?.lampBrightness ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp brightness: $e")
            0
        }
    }

    private fun fetchLampIsPowerOn(lamps: List<LampModel>): Boolean {
        return try {
            lamps.firstOrNull()?.lampIsPowerOn ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp power state: $e")
            false
        }
    }

    private fun fetchLampSchedule(lamps: List<LampModel>): LampSchedule {
        return try {
            lamps.firstOrNull()?.lampSchedule ?: LampSchedule("", "")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp schedule: $e")
            LampSchedule("", "")
        }
    }

    private fun fetchLampSelectedMode(lamps: List<LampModel>): String {
        return try {
            lamps.firstOrNull()?.lampSelectedMode ?: "manual"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp selected mode: $e")
            "manual"
        }
    }


    // ========================= UPDATE OPERATOR ========================= //

    fun updateLampBrightness(userId: String, roomId: String, lampId: String, brightness: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.putLampBrightness(userId, roomId, lampId, brightness)
                _lampBrightnessLiveData.postValue(brightness)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp brightness: $e")
            }
        }
    }

    fun updateLampIsAutomaticOn(userId: String, roomId: String, lampId: String, isAutomaticOn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.putLampIsAutomaticOn(userId, roomId, lampId, isAutomaticOn)
                _lampIsAutomaticOnLiveData.postValue(isAutomaticOn)
            } catch (e: Exception) {
                Log.e(TAG, "Error set mode to automatic: $e")
            }
        }
    }

    fun updateLampIsPowerOn(userId: String, roomId: String, lampId: String, isPowerOn: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.putLampIsPowerOn(userId, roomId, lampId, isPowerOn)
                _lampIsPowerOnLiveData.postValue(isPowerOn)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating power state: $e")
            }
        }
    }

    fun updateLampSchedule(userId: String, roomId: String, lampId: String, newSchedule: LampSchedule) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.putLampSchedule(userId, roomId, lampId, newSchedule)
                _lampScheduleLiveData.postValue(newSchedule)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lampSchedule: $e")
            }
        }
    }

    fun updateLampSelectedMode(userId: String, roomId: String, lampId: String, selectedMode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.putLampSelectedMode(userId, roomId, lampId, selectedMode)
                _lampSelectedModeLiveData.postValue(selectedMode)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating selected mode: $e")
            }
        }
    }

    companion object {
        const val TAG = "HomeViewModel"
    }

}