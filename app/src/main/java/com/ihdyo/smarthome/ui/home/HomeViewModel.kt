package com.ihdyo.smarthome.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.UserModel
import com.ihdyo.smarthome.data.repository.SmartHomeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: SmartHomeRepository) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }


//    private val _selectedLamp = MutableLiveData<LampModel>()
//    val selectedLamp: LiveData<LampModel> get() = _selectedLamp



    private val _mode = MutableLiveData<String>()
    val mode: LiveData<String> get() = _mode

    private val _selectedMode = MutableLiveData<Int>()
    val selectedMode: LiveData<Int> get() = _selectedMode



    private val _powerConsumed = MutableLiveData<String>()
    val powerConsumed: LiveData<String> get() = _powerConsumed

    private val _totalPowerConsumed = MutableLiveData<String>()
    val totalPowerConsumed: LiveData<String> get() = _totalPowerConsumed



    private val _isPowerOn = MutableLiveData<Boolean>()
    val isPowerOn: LiveData<Boolean> get() = _isPowerOn



    private val _scheduleFrom = MutableLiveData<String>()
    val scheduleFrom: LiveData<String> get() = _scheduleFrom

    private val _scheduleTo = MutableLiveData<String>()
    val scheduleTo: LiveData<String> get() = _scheduleTo







    private val _selectedRoom = MutableLiveData<RoomModel>()
    val selectedRoom: LiveData<RoomModel> get() = _selectedRoom

    private val _selectedLamp = MutableLiveData<LampModel>()
    val selectedLamp: LiveData<LampModel> get() = _selectedLamp


    private val _userLiveData = MutableLiveData<UserModel?>()
    val userLiveData: MutableLiveData<UserModel?> get() = _userLiveData

    private val _roomsLiveData = MutableLiveData<List<RoomModel>?>()
    val roomsLiveData: MutableLiveData<List<RoomModel>?> get() = _roomsLiveData

    private val _lampsLiveData = MutableLiveData<List<LampModel>?>()
    val lampsLiveData: MutableLiveData<List<LampModel>?> get() = _lampsLiveData


    private val _lampBrightnessLiveData = MutableLiveData<Int>()
    val lampBrightnessLiveData: LiveData<Int> get() = _lampBrightnessLiveData

    private val _lampIsAutomaticOnLiveData = MutableLiveData<Boolean>()
    val lampIsAutomaticOnLiveData: LiveData<Boolean> get() = _lampIsAutomaticOnLiveData

    private val _lampIsPowerOnLiveData = MutableLiveData<Boolean>()
    val lampIsPowerOnLiveData: LiveData<Boolean> get() = _lampIsPowerOnLiveData

    private val _lampRuntimeLiveData = MutableLiveData<Int>()
    val lampRuntimeLiveData: LiveData<Int> get() = _lampRuntimeLiveData

    private val _lampScheduleLiveData = MutableLiveData<Map<String, String>>()
    val lampScheduleLiveData: LiveData<Map<String, String>> get() = _lampScheduleLiveData

    private val _lampSelectedModeLiveData = MutableLiveData<String>()
    val lampSelectedModeLiveData: LiveData<String> get() = _lampSelectedModeLiveData

    private val _lampWattPowerLiveData = MutableLiveData<Int>()
    val lampWattPowerLiveData: LiveData<Int> get() = _lampWattPowerLiveData



    private val _powerConsumedLiveData = MutableLiveData<Int>()
    val powerConsumedLiveData: LiveData<Int> get() = _powerConsumedLiveData

    private val _totalPowerConsumedLiveData = MutableLiveData<Int>()
    val totalPowerConsumedLiveData: LiveData<Int> get() = _totalPowerConsumedLiveData



    fun setSelectedRoom(room: RoomModel) {
        viewModelScope.launch {
            _selectedRoom.postValue(room)
        }
    }

    fun setSelectedLamp(lamp: LampModel) {
        viewModelScope.launch {
            _selectedLamp.postValue(lamp)
        }
    }



    fun fetchUser(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUser(userId)
                userLiveData.postValue(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user: $e")
            }
        }
    }

    fun fetchLamps(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rooms = repository.getRooms(userId)
                roomsLiveData.postValue(rooms)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rooms: $e")
            }
        }
    }

    fun fetchRooms(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rooms = repository.getRooms(userId)
                roomsLiveData.postValue(rooms)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching rooms: $e")
            }
        }
    }


    fun lampBrightness(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLampBrightness(userId, roomId, lampId) { brightness ->
                    _lampBrightnessLiveData.postValue(brightness)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing lampBrightness: $e")
            }
        }
    }

    fun lampIsAutomaticOn(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLampIsAutomaticOn(userId, roomId, lampId) { isAutomaticOn ->
                    _lampIsAutomaticOnLiveData.postValue(isAutomaticOn)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing lampIsAutomaticOn: $e")
            }
        }
    }

    fun lampIsPowerOn(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLampIsPowerOn(userId, roomId, lampId) { isPowerOn ->
                    _lampIsPowerOnLiveData.postValue(isPowerOn)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing lampIsPowerOn: $e")
            }
        }
    }

    fun lampRuntime(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLampRuntime(userId, roomId, lampId) { runtime ->
                    _lampRuntimeLiveData.postValue(runtime)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing lampRuntime: $e")
            }
        }
    }

    fun lampSchedule(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLampSchedule(userId, roomId, lampId) { schedule ->
                    _lampScheduleLiveData.postValue(schedule)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing lampSchedule: $e")
            }
        }
    }

    fun lampSelectedMode(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getLampSelectedMode(userId, roomId, lampId) { selectedMode ->
                    _lampSelectedModeLiveData.postValue(selectedMode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing lampSelectedMode: $e")
            }
        }
    }

    fun lampWattPower(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wattPower = repository.getLampWattPower(userId, roomId, lampId)
                _lampWattPowerLiveData.postValue(wattPower)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching lampWattPower: $e")
            }
        }
    }



    fun fetchPowerConsumed(userId: String, roomId: String, lampId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val runtime = _lampRuntimeLiveData.value ?: 0
                val wattPower = _lampWattPowerLiveData.value ?: 0

                val powerConsumed = (runtime.div(60).div(60)).times(wattPower)
                _powerConsumedLiveData.postValue(powerConsumed)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching powerConsumed: $e")
            }
        }
    }

//    fun fetchSelectedMode(userId: String, roomId: String, lampId: String, selectedIndex: Int) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val selectedMode = when (selectedIndex) {
//                    0 -> "automatic"
//                    1 -> "schedule"
//                    2 -> "manual"
//                    else -> throw IllegalArgumentException("Invalid index: $selectedIndex")
//                }
//
//                // Update lamp selectedMode
//                repository.updateLampSelectedMode(userId, roomId, lampId, selectedMode)
//
//                // Update lampIsAutomatic based on selectedMode
//                val lampIsAutomatic = when (selectedMode) {
//                    "automatic" -> true
//                    else -> false
//                }
//                repository.updateLampIsAutomatic(userId, roomId, lampId, lampIsAutomatic)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating lamp selected mode: $e")
//            }
//        }
//    }












//    @SuppressLint("NullSafeMutableLiveData")
//    fun fetchLampDetails(): LiveData<List<LampModel>> {
//        viewModelScope.launch {
//            try {
//                val lamps = smartHomeRepository.getLamps()
//                _lampDetails.postValue(lamps)
//            } catch (exception: Exception) {
//                Log.e(this.javaClass.simpleName, "Error fetching lamp details", exception)
//            }
//        }
//        return _lampDetails
//    }
//
//    fun setSelectedLamp(lamp: LampModel) {
//        viewModelScope.launch {
//            _selectedLamp.postValue(lamp)
//        }
//    }
//
//    // ========================== POWER CONSUMED =========================== //
//
//    fun fetchPowerConsumed(lamp: LampModel) {
//        viewModelScope.launch {
//            smartHomeRepository.getLampRuntime(lamp) { lampRuntime ->
//                if (lampRuntime != null) {
//                    try {
//                        Log.d(TAG, "Successfully fetched power consumed: $lampRuntime")
//
//                        val runtimeHours = lampRuntime.div(60).div(60)
//                        val powerConsumed = (WATT_POWER * runtimeHours).toString()
//
//                        _powerConsumed.postValue("${powerConsumed}Wh")
//                    } catch (exception: Exception) {
//                        Log.e(this.javaClass.simpleName, "Error calculating power consumed", exception)
//                    }
//                } else {
//                    Log.e(TAG, "Error fetching power consumed: lampRuntime is null.")
//                }
//            }
//        }
//    }
//
//    fun fetchTotalPowerConsumed() {
//        viewModelScope.launch {
//            try {
//                Log.d(TAG, "Successfully calculated total power consumed: $totalPowerConsumed")
//
//                smartHomeRepository.getTotalLampRuntime { totalLampRuntime ->
//                    val totalLampRuntimeHours = totalLampRuntime?.div(60)?.div(60) ?: 0
//                    val totalPowerConsumed = (WATT_POWER * totalLampRuntimeHours).toString()
//
//                    _totalPowerConsumed.postValue("${totalPowerConsumed}Wh")
//                }
//            } catch (exception: Exception) {
//                Log.e(this.javaClass.simpleName, "Error calculating totalLampRuntime", exception)
//            }
//        }
//    }
//
//    // ========================== SELECTED MODE =========================== //
//
//    fun fetchSelectedMode(lamp: LampModel) {
//        viewModelScope.launch {
//            smartHomeRepository.getMode(lamp) { mode ->
//                if (mode != null) {
//                    Log.d(TAG, "Successfully fetched selected mode: $mode")
//                    _mode.postValue(mode)
//                    _selectedMode.postValue(mapSelectedModeToCheckedId(mode))
//                } else {
//                    Log.e(TAG, "Error fetching selected mode: mode is null.")
//                }
//            }
//        }
//    }
//
//    fun updateSelectedMode(checkedId: Int) {
//        viewModelScope.launch {
//            try {
//                selectedLamp.value?.let { selectedLamp ->
//                    updateLampProperties(selectedLamp, checkedId)
//                    smartHomeRepository.putMode(selectedLamp)
//                    _selectedMode.postValue(checkedId)
//                }
//            } catch (e: Exception) {
//                Log.e(this.javaClass.simpleName, "Error updating mode", e)
//            }
//        }
//    }
//
//    private fun mapSelectedModeToCheckedId(mode: String): Int {
//        return when (mode) {
//            "automatic" -> R.id.button_automatic
//            "schedule" -> R.id.button_schedule
//            "manual" -> R.id.button_manual
//            else -> R.id.button_manual
//        }
//    }
//
//    private fun updateLampProperties(lamp: LampModel, checkedId: Int) {
//        when (checkedId) {
//            R.id.button_automatic -> {
//                lamp.mode = "automatic"
//                lamp.isAutomaticOn = true
//                lamp.isScheduleOn = false
//            }
//            R.id.button_schedule -> {
//                lamp.mode = "schedule"
//                lamp.isAutomaticOn = false
//                lamp.isScheduleOn = true
//            }
//            R.id.button_manual -> {
//                lamp.mode = "manual"
//                lamp.isAutomaticOn = false
//                lamp.isScheduleOn = false
//            }
//            else -> {
//                lamp.mode = "manual"
//                lamp.isAutomaticOn = false
//                lamp.isScheduleOn = false
//            }
//        }
//    }
//
//    // ========================== POWER STATE =========================== //
//
//    fun fetchPowerState(lamp: LampModel) {
//        viewModelScope.launch {
//            smartHomeRepository.getIsPowerOn(lamp) { isPowerOn ->
//                if (isPowerOn != null) {
//                    Log.d(TAG, "Successfully fetched power state: $isPowerOn")
//                    _isPowerOn.postValue(isPowerOn)
//                } else {
//                    Log.e(TAG, "Error fetching power state: isPowerOn is null.")
//                }
//            }
//        }
//    }
//
//    fun updatePowerState(isChecked: Boolean) {
//        viewModelScope.launch {
//            try {
//                selectedLamp.value?.let { selectedLamp ->
//                    smartHomeRepository.putIsPowerOn(selectedLamp)
//                    _isPowerOn.postValue(isChecked)
//                }
//            } catch (e: Exception) {
//                Log.e(this.javaClass.simpleName, "Error updating power state", e)
//            }
//        }
//    }
//
//    // =========================== SCHEDULE =========================== //
//
//    fun fetchScheduleStartTime(lamp: LampModel) {
//        viewModelScope.launch {
//            smartHomeRepository.getScheduleFrom(lamp) { scheduleFrom ->
//                if (scheduleFrom != null) {
//                    Log.d(TAG, "Successfully fetched schedule start time: $scheduleFrom")
//                    _scheduleFrom.postValue(scheduleFrom)
//                } else {
//                    Log.e(TAG, "Error fetching schedule start time: scheduleFrom is null.")
//                }
//            }
//        }
//    }
//
//    fun fetchScheduleFinishTime(lamp: LampModel) {
//        viewModelScope.launch {
//            smartHomeRepository.getScheduleTo(lamp) { scheduleTo ->
//                if (scheduleTo != null) {
//                    Log.d(TAG, "Successfully fetched schedule finish time: $scheduleTo")
//                    _scheduleTo.postValue(scheduleTo)
//                } else {
//                    Log.e(TAG, "Error fetching schedule finish time: scheduleTo time is null.")
//                }
//            }
//        }
//    }
//
//    fun updateScheduleStartTime(scheduleFrom: String) {
//        viewModelScope.launch {
//            try {
//                selectedLamp.value?.let { selectedLamp ->
//                    smartHomeRepository.putScheduleFrom(selectedLamp)
//                    _scheduleFrom.postValue(scheduleFrom)
//                }
//            } catch (e: Exception) {
//                Log.e(this.javaClass.simpleName, "Error updating schedule start time", e)
//            }
//        }
//    }
//
//    fun updateScheduleFinishTime(scheduleTo: String) {
//        viewModelScope.launch {
//            try {
//                selectedLamp.value?.let { selectedLamp ->
//                    smartHomeRepository.putScheduleTo(selectedLamp)
//                    _scheduleTo.postValue(scheduleTo)
//                }
//            } catch (e: Exception) {
//                Log.e(this.javaClass.simpleName, "Error updating schedule finish time", e)
//            }
//        }
//    }
}