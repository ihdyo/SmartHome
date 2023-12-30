package com.ihdyo.smarthome.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.model.EnvironmentModel
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.LampSchedule
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

    private val _selectedRoom = MutableLiveData<Pair<RoomModel, String?>>()
    val selectedRoom: LiveData<Pair<RoomModel, String?>> get() = _selectedRoom

    private val _selectedLamp = MutableLiveData<LampModel>()
    val selectedLamp: LiveData<LampModel> get() = _selectedLamp



    private val _userLiveData = MutableLiveData<UserModel?>()
    val userLiveData: LiveData<UserModel?> get() = _userLiveData

    private val _environmentsLiveData = MutableLiveData<List<EnvironmentModel>?>()
    val environmentsLiveData: LiveData<List<EnvironmentModel>?> get() = _environmentsLiveData

    private val _roomsLiveData = MutableLiveData<List<RoomModel>?>()
    val roomsLiveData: LiveData<List<RoomModel>?> get() = _roomsLiveData

    private val _lampsLiveData = MutableLiveData<List<LampModel>?>()
    val lampsLiveData: LiveData<List<LampModel>?> get() = _lampsLiveData


    private val _currentUserIdLiveData = MutableLiveData<String>()
    val currentUserIdLiveData: LiveData<String> get() = _currentUserIdLiveData

    private val _currentRoomIdLiveData = MutableLiveData<String>()
    val currentRoomIdLiveData: LiveData<String> get() = _currentRoomIdLiveData

    private val _currentLampIdLiveData = MutableLiveData<String>()
    val currentLampIdLiveData: LiveData<String> get() = _currentLampIdLiveData


    private val _userNameLiveData = MutableLiveData<String>()
    val userNameLiveData: LiveData<String> get() = _userNameLiveData

    private val _powerConsumedLiveData = MutableLiveData<Map<String, Int>>()
    val powerConsumedLiveData: LiveData<Map<String, Int>> get() = _powerConsumedLiveData

    private val _sensorValueLiveData = MutableLiveData<Boolean>()
    val sensorValueLiveData: LiveData<Boolean> get() = _sensorValueLiveData

    private val _lampBrightnessLiveData = MutableLiveData<Map<String, Int>>()
    val lampBrightnessLiveData: LiveData<Map<String, Int>> get() = _lampBrightnessLiveData

    private val _lampIsAutomaticOnLiveData = MutableLiveData<Map<String, Boolean>>()
    val lampIsAutomaticOnLiveData: LiveData<Map<String, Boolean>> get() = _lampIsAutomaticOnLiveData

    private val _lampIsPowerOnLiveData = MutableLiveData<Map<String, Boolean>>()
    val lampIsPowerOnLiveData: LiveData<Map<String, Boolean>> get() = _lampIsPowerOnLiveData

    private val _lampScheduleLiveData = MutableLiveData<Map<String, LampSchedule>>()
    val lampScheduleLiveData: LiveData<Map<String, LampSchedule>> get() = _lampScheduleLiveData

    private val _lampSelectedModeLiveData = MutableLiveData<Map<String, String>>()
    val lampSelectedModeLiveData: LiveData<Map<String, String>> get() = _lampSelectedModeLiveData


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


    fun setCurrentUserId(userId: String) {
        _currentUserIdLiveData.value = userId
    }

    fun setCurrentRoomId(roomId: String) {
        _currentRoomIdLiveData.value = roomId
    }

    fun setCurrentLampId(lampId: String) {
        _currentLampIdLiveData.value = lampId
    }


    // ========================= FETCH OPERATOR ========================= //

    fun fetchUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = mainRepository.getUser(currentUserIdLiveData.value.toString())
                _userLiveData.postValue(user)

                val userNameMap = fetchUserName(user!!)
                _userNameLiveData.postValue(userNameMap)

                Log.d(TAG, "Successfully fetched user: ${currentUserIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user: $e")
            }
        }
    }

    private fun fetchUserName(users: UserModel): String {
        return try {
            val userName = users.userName ?: ""
            Log.d(TAG, "Successfully fetched username: $userName")
            userName
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching: $e")
            ""
        }
    }

    fun fetchEnvironments() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val environments = mainRepository.getEnvironments(currentUserIdLiveData.value.toString())
                _environmentsLiveData.postValue(environments)

                val sensorValueMap = fetchSensorValue(environments)
                _sensorValueLiveData.postValue(sensorValueMap)

                Log.d(TAG, "Successfully fetched environments for user with ID: ${currentUserIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching environments: $e")
            }
        }
    }

    fun fetchRooms() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rooms = mainRepository.getRooms(currentUserIdLiveData.value.toString())
                _roomsLiveData.postValue(rooms)
                Log.d(TAG, "Successfully fetched rooms for user with ID: ${currentUserIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rooms: $e")
            }
        }
    }

    private fun fetchSensorValue(environments: List<EnvironmentModel>): Boolean {
        return try {
            val value = environments.firstOrNull()?.sensorValue ?: false
            Log.d(TAG, "Successfully fetched sensor value: $environments")
            value
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sensor value: $e")
            false
        }
    }

    fun fetchLamps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lamps = mainRepository.getLamps(currentUserIdLiveData.value.toString(), currentRoomIdLiveData.value.toString())
                _lampsLiveData.postValue(lamps)

                val powerConsumedMap = fetchPowerConsumed(lamps)
                _powerConsumedLiveData.postValue(powerConsumedMap)

                val lampBrightnessMap = fetchLampBrightness(lamps)
                _lampBrightnessLiveData.postValue(lampBrightnessMap)

                val lampIsPowerOnMap = fetchLampIsPowerOn(lamps)
                _lampIsPowerOnLiveData.postValue(lampIsPowerOnMap)

                val lampScheduleMap = fetchLampSchedule(lamps)
                _lampScheduleLiveData.postValue(lampScheduleMap)

                val lampSelectedModeMap = fetchLampSelectedMode(lamps)
                _lampSelectedModeLiveData.postValue(lampSelectedModeMap)

                Log.d(TAG, "Successfully fetched lamps for user ${currentUserIdLiveData.value} And room ${currentRoomIdLiveData.value}")
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
                powerConsumedMap[currentLampIdLiveData.value.toString()] = powerConsumed
            }
            Log.d(TAG, "Successfully calculating power consumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating power consumed: $e")
        }

        return powerConsumedMap
    }

    private fun fetchLampBrightness(lamps: List<LampModel>): Map<String, Int> {
        val lampBrightnessMap = mutableMapOf<String, Int>()

        try {
            for (lamp in lamps) {
                lampBrightnessMap[currentLampIdLiveData.value.toString()] = lamp.lampBrightness
            }
            Log.d(TAG, "Successfully fetching lamp brightness")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp brightness: $e")
        }

        return lampBrightnessMap
    }

    private fun fetchLampIsPowerOn(lamps: List<LampModel>): Map<String, Boolean> {
        val lampIsPowerOnMap = mutableMapOf<String, Boolean>()

        try {
            for (lamp in lamps) {
                lampIsPowerOnMap[currentLampIdLiveData.value.toString()] = lamp.lampIsPowerOn!!
            }
            Log.d(TAG, "Successfully fetching lamp power state")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp power state: $e")
        }

        return lampIsPowerOnMap
    }

    private fun fetchLampSchedule(lamps: List<LampModel>): Map<String, LampSchedule> {
        val lampScheduleMap = mutableMapOf<String, LampSchedule>()

        try {
            for (lamp in lamps) {
                lampScheduleMap[lamp.LID.orEmpty()] = lamp.lampSchedule
            }
            Log.d(TAG, "Successfully fetching lamp schedules")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching lamp schedules: $e")
        }

        return lampScheduleMap
    }

    private fun fetchLampSelectedMode(lamps: List<LampModel>): Map<String, String> {
        val lampSelectedModeMap = mutableMapOf<String, String>()

        try {
            for (lamp in lamps) {
                lampSelectedModeMap[lamp.LID.orEmpty()] = lamp.lampSelectedMode
            }
            Log.d(TAG, "Successfully fetching selected mode")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching selected mode: $e")
        }

        return lampSelectedModeMap
    }


    // ========================= UPDATE OPERATOR ========================= //

    fun updateUserName(userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mainRepository.putUserName(currentUserIdLiveData.value.toString(), userName)
                _userNameLiveData.postValue(userName)
                Log.d(TAG, "Success updating username $userName to ${currentUserIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating username: $e")
            }
        }
    }

    fun updateLampBrightness(lampBrightnessMap: Map<String, Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mainRepository.putLampBrightness(currentUserIdLiveData.value.toString(), currentRoomIdLiveData.value.toString(), lampBrightnessMap)
                _lampBrightnessLiveData.postValue(lampBrightnessMap)

                Log.d(TAG, "Success updating lamp brightness $lampBrightnessMap in ${currentRoomIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp brightness: $e")
            }
        }
    }

    fun updateLampIsAutomaticOn(lampIsAutomaticOnMap: Map<String, Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mainRepository.putLampIsAutomaticOn(currentUserIdLiveData.value.toString(), currentRoomIdLiveData.value.toString(), lampIsAutomaticOnMap)
                _lampIsAutomaticOnLiveData.postValue(lampIsAutomaticOnMap)

                Log.d(TAG, "Success updating lamp automatic state: $lampIsAutomaticOnMap in ${currentRoomIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp automatic state: $e")
            }
        }
    }

    fun updateLampIsPowerOn(lampIsPowerOnMap: Map<String, Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mainRepository.putLampIsPowerOn(currentUserIdLiveData.value.toString(), currentRoomIdLiveData.value.toString(), lampIsPowerOnMap)
                _lampIsPowerOnLiveData.postValue(lampIsPowerOnMap)

                Log.d(TAG, "Success updating lamp power state: $lampIsPowerOnMap in ${currentRoomIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp power state: $e")
            }
        }
    }

    fun updateLampSchedule(lampScheduleMap: Map<String, LampSchedule>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mainRepository.putLampSchedule(currentUserIdLiveData.value.toString(), currentRoomIdLiveData.value.toString(), lampScheduleMap)
                _lampScheduleLiveData.postValue(lampScheduleMap)

                Log.d(TAG, "Success updating lamp schedule: $lampScheduleMap in ${currentRoomIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp schedule: $e")
            }
        }
    }

    fun updateLampSelectedMode(lampSelectedModeMap: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mainRepository.putLampSelectedMode(currentUserIdLiveData.value.toString(), currentRoomIdLiveData.value.toString(), lampSelectedModeMap)
                _lampSelectedModeLiveData.postValue(lampSelectedModeMap)
                Log.d(TAG, "Success updating selected mode: $lampSelectedModeMap in ${currentRoomIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating selected mode: $e")
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

}