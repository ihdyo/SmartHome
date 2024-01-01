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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

    private val mutex = Mutex()


    private val _allUsersLiveData = MutableLiveData<List<UserModel>?>()
    val allUsersLiveData: MutableLiveData<List<UserModel>?> get() = _allUsersLiveData

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

    private val _totalPowerConsumedLiveData = MutableLiveData<Int>()
    val totalPowerConsumedLiveData: LiveData<Int> get() = _totalPowerConsumedLiveData

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

    fun fetchAllUsers() {
        viewModelScope.launch {
            val allUsers = mainRepository.getAllUsers()
            if (allUsers != null) {
                _allUsersLiveData.postValue(allUsers)
                Log.d(TAG, "Successfully fetched all user")
            } else {
                Log.e(TAG, "Error fetching all user")
            }
        }
    }

    fun fetchUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = mainRepository.getUser(currentUserIdLiveData.value.orEmpty())
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
                val environments = mainRepository.getEnvironments(currentUserIdLiveData.value.orEmpty())
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
                val rooms = mainRepository.getRooms(currentUserIdLiveData.value.orEmpty())
                _roomsLiveData.postValue(rooms)
                Log.d(TAG, "Successfully fetched rooms for user with ID: ${currentUserIdLiveData.value}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rooms: $e")
            }
        }
    }

    fun fetchRoomById(roomId: String): RoomModel? {
        return try {
            val roomDetails = _roomsLiveData.value?.find { it.RID == roomId }
            if (roomDetails != null) {
                Log.d(TAG, "Successfully fetched: $roomId")
            }
            roomDetails
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching: $roomId", e)
            null
        }
    }

    private fun fetchSensorValue(environments: List<EnvironmentModel>): Boolean {
        return try {
            val value = environments.firstOrNull()?.sensorValue!!
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
                val lamps = mainRepository.getLamps(currentUserIdLiveData.value.orEmpty(), currentRoomIdLiveData.value.orEmpty())
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

    fun fetchTotalPowerConsumed() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lamps = mainRepository.getAllLamps(currentUserIdLiveData.value.orEmpty())

                val totalPowerConsumed = lamps.sumOf { lamp ->
                    (lamp.lampRuntime.div(3600)).times(lamp.lampWattPower)
                }
                _totalPowerConsumedLiveData.postValue(totalPowerConsumed)
                Log.d(TAG, "Total Power Consumed: $totalPowerConsumed")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching lamps and calculating power consumption: $e")
            }
        }
    }

    private fun fetchPowerConsumed(lamps: List<LampModel>): Map<String, Int> {
        val powerConsumedMap = mutableMapOf<String, Int>()

        try {
            for (lamp in lamps) {
                powerConsumedMap[lamp.LID.orEmpty()] = (lamp.lampRuntime.div(3600)).times(lamp.lampWattPower)
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
                lampBrightnessMap[lamp.LID.orEmpty()] = lamp.lampBrightness
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
                lampIsPowerOnMap[lamp.LID.orEmpty()] = lamp.lampIsPowerOn ?: false
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
                mainRepository.putUserName(currentUserIdLiveData.value.orEmpty(), userName)
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
                val currentUserId = currentUserIdLiveData.value.orEmpty()
                val currentRoomId = currentRoomIdLiveData.value.orEmpty()
                val currentLampId = currentLampIdLiveData.value.orEmpty()
                val currentLampBrightness = lampBrightnessMap[currentLampId]

                if (currentLampBrightness != null) {
                    mutex.withLock {
                        val updatedMap = _lampBrightnessLiveData.value?.toMutableMap() ?: mutableMapOf()
                        updatedMap[currentLampId] = currentLampBrightness

                        mainRepository.putLampBrightness(
                            currentUserId,
                            currentRoomId,
                            currentLampId,
                            currentLampBrightness
                        )

                        _lampBrightnessLiveData.postValue(updatedMap)
                    }
                    Log.d(TAG, "Success updating lamp brightness $currentLampBrightness in $currentLampId, $currentRoomId")
                } else {
                    Log.e(TAG, "Error updating lamp brightness: Entry for $currentLampId not found in $lampBrightnessMap")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp brightness: $e")
            }
        }
    }

    fun updateLampIsAutomaticOn(lampIsAutomaticOnMap: Map<String, Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = currentUserIdLiveData.value.orEmpty()
                val currentRoomId = currentRoomIdLiveData.value.orEmpty()
                val currentLampId = currentLampIdLiveData.value.orEmpty()
                val currentLampIsAutomaticOn = lampIsAutomaticOnMap[currentLampId]

                if (currentLampIsAutomaticOn != null) {
                    mutex.withLock {
                        val updatedMap = _lampIsAutomaticOnLiveData.value?.toMutableMap() ?: mutableMapOf()
                        updatedMap[currentLampId] = currentLampIsAutomaticOn

                        mainRepository.putLampIsAutomaticOn(
                            currentUserId,
                            currentRoomId,
                            currentLampId,
                            currentLampIsAutomaticOn
                        )

                        _lampIsAutomaticOnLiveData.postValue(updatedMap)
                    }
                    Log.d(TAG, "Success updating lamp automatic state $currentLampIsAutomaticOn in $currentLampId, $currentRoomId")
                } else {
                    Log.e(TAG, "Error updating lamp automatic state: Entry for $currentLampId not found in $lampIsAutomaticOnMap")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp automatic state: $e")
            }
        }
    }

    fun updateLampIsPowerOn(lampIsPowerOnMap: Map<String, Boolean>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = currentUserIdLiveData.value.orEmpty()
                val currentRoomId = currentRoomIdLiveData.value.orEmpty()
                val currentLampId = currentLampIdLiveData.value.orEmpty()
                val currentLampIsPowerOn = lampIsPowerOnMap[currentLampId]

                if (currentLampIsPowerOn != null) {
                    mutex.withLock {
                        val updatedMap = _lampIsPowerOnLiveData.value?.toMutableMap() ?: mutableMapOf()
                        updatedMap[currentLampId] = currentLampIsPowerOn

                        mainRepository.putLampIsPowerOn(
                            currentUserId,
                            currentRoomId,
                            currentLampId,
                            currentLampIsPowerOn
                        )

                        _lampIsPowerOnLiveData.postValue(updatedMap)
                    }
                    Log.d(TAG, "Success updating lamp power state $currentLampIsPowerOn in $currentLampId, $currentRoomId")
                } else {
                    Log.e(TAG, "Error updating lamp power state: Entry for $currentLampId not found in $lampIsPowerOnMap")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating lamp power state: $e")
            }
        }
    }


    fun updateScheduleFrom(lampId: String, newScheduleFrom: String) {
        val updatedSchedule = _lampScheduleLiveData.value?.get(lampId)?.copy(scheduleFrom = newScheduleFrom)
        updatedSchedule?.let { schedule ->
            val updatedMap = _lampScheduleLiveData.value?.toMutableMap() ?: mutableMapOf()
            updatedMap[lampId] = schedule

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    mutex.withLock {
                        mainRepository.putLampSchedule(
                            currentUserIdLiveData.value.orEmpty(),
                            currentRoomIdLiveData.value.orEmpty(),
                            lampId,
                            schedule
                        )
                        _lampScheduleLiveData.postValue(updatedMap)
                    }
                    Log.d(TAG, "Success updating lamp scheduleFrom $newScheduleFrom in $lampId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating lamp scheduleFrom: $e")
                }
            }
        }
    }

    fun updateScheduleTo(lampId: String, newScheduleTo: String) {
        val updatedSchedule = _lampScheduleLiveData.value?.get(lampId)?.copy(scheduleTo = newScheduleTo)
        updatedSchedule?.let { schedule ->
            val updatedMap = _lampScheduleLiveData.value?.toMutableMap() ?: mutableMapOf()
            updatedMap[lampId] = schedule

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    mutex.withLock {
                        mainRepository.putLampSchedule(
                            currentUserIdLiveData.value.orEmpty(),
                            currentRoomIdLiveData.value.orEmpty(),
                            lampId,
                            schedule
                        )
                        _lampScheduleLiveData.postValue(updatedMap)
                    }
                    Log.d(TAG, "Success updating lamp scheduleTo $newScheduleTo for $lampId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating lamp scheduleTo: $e")
                }
            }
        }
    }


    fun updateLampSelectedMode(lampSelectedModeMap: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = currentUserIdLiveData.value.orEmpty()
                val currentRoomId = currentRoomIdLiveData.value.orEmpty()
                val currentLampId = currentLampIdLiveData.value.orEmpty()
                val lampSelectedMode = lampSelectedModeMap[currentLampId]

                if (lampSelectedMode != null) {
                    mutex.withLock {
                        val updatedMap = _lampSelectedModeLiveData.value?.toMutableMap() ?: mutableMapOf()
                        updatedMap[currentLampId] = lampSelectedMode

                        mainRepository.putLampSelectedMode(
                            currentUserId,
                            currentRoomId,
                            currentLampId,
                            lampSelectedMode
                        )
                        _lampSelectedModeLiveData.postValue(updatedMap)
                    }
                    Log.d(TAG, "Success updating selected mode $lampSelectedMode in $currentLampId, $currentRoomId")
                } else {
                    Log.e(TAG, "Error updating selected mode: Entry for $currentLampId not found in $lampSelectedModeMap")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating selected mode: $e")
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }

}