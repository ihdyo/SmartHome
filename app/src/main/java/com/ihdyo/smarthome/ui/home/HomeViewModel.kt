package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.repository.LampRepository
import com.ihdyo.smarthome.ui.home.HomeFragment.Companion.WATT_POWER
import kotlinx.coroutines.launch

class HomeViewModel(private val lampRepository: LampRepository) : ViewModel() {

    private val _lampDetails = MutableLiveData<List<LampModel>>()
    val lampDetails: LiveData<List<LampModel>> get() = _lampDetails

    private val _selectedLamp = MutableLiveData<LampModel>()
    val selectedLamp: LiveData<LampModel> get() = _selectedLamp



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


    @SuppressLint("NullSafeMutableLiveData")
    fun fetchLampDetails(): LiveData<List<LampModel>> {
        viewModelScope.launch {
            try {
                lampRepository.getLamps { lamps ->
                    _lampDetails.postValue(lamps)
                }
            } catch (exception: Exception) {
                Log.e(this.javaClass.simpleName, "Error fetching lamp details", exception)
            }
        }
        return _lampDetails
    }

    fun setSelectedLamp(lamp: LampModel) {
        viewModelScope.launch {
            _selectedLamp.postValue(lamp)
        }
    }

    // ========================== POWER CONSUMED =========================== //

    fun fetchPowerConsumed(lamp: LampModel) {
        viewModelScope.launch {
            lampRepository.getLampRuntime(lamp) { lampRuntime ->
                if (lampRuntime != null) {
                    try {
                        Log.d(TAG, "Successfully fetched power consumed: $lampRuntime")

                        val runtimeHours = lampRuntime.div(60).div(60)
                        val powerConsumed = (WATT_POWER * runtimeHours).toString()

                        _powerConsumed.postValue("${powerConsumed}Wh")
                    } catch (exception: Exception) {
                        Log.e(this.javaClass.simpleName, "Error calculating power consumed", exception)
                    }
                } else {
                    Log.e(TAG, "Error fetching power consumed: lampRuntime is null.")
                }
            }
        }
    }

    fun fetchTotalPowerConsumed() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Successfully calculated total power consumed: $totalPowerConsumed")

                lampRepository.getTotalLampRuntime { totalLampRuntime ->
                    val totalLampRuntimeHours = totalLampRuntime?.div(60)?.div(60) ?: 0
                    val totalPowerConsumed = (WATT_POWER * totalLampRuntimeHours).toString()

                    _totalPowerConsumed.postValue("${totalPowerConsumed}Wh")
                }
            } catch (exception: Exception) {
                Log.e(this.javaClass.simpleName, "Error calculating totalLampRuntime", exception)
            }
        }
    }

    // ========================== SELECTED MODE =========================== //

    fun fetchSelectedMode(lamp: LampModel) {
        viewModelScope.launch {
            lampRepository.getMode(lamp) { mode ->
                if (mode != null) {
                    Log.d(TAG, "Successfully fetched selected mode: $mode")
                    _mode.postValue(mode)
                    _selectedMode.postValue(mapSelectedModeToCheckedId(mode))
                } else {
                    Log.e(TAG, "Error fetching selected mode: mode is null.")
                }
            }
        }
    }

    fun updateSelectedMode(checkedId: Int) {
        viewModelScope.launch {
            try {
                selectedLamp.value?.let { selectedLamp ->
                    updateLampProperties(selectedLamp, checkedId)
                    lampRepository.putMode(selectedLamp)
                    _selectedMode.postValue(checkedId)
                }
            } catch (e: Exception) {
                Log.e(this.javaClass.simpleName, "Error updating mode", e)
            }
        }
    }

    private fun mapSelectedModeToCheckedId(mode: String): Int {
        return when (mode) {
            "automatic" -> R.id.button_automatic
            "schedule" -> R.id.button_schedule
            "manual" -> R.id.button_manual
            else -> R.id.button_manual
        }
    }

    private fun updateLampProperties(lamp: LampModel, checkedId: Int) {
        when (checkedId) {
            R.id.button_automatic -> {
                lamp.mode = "automatic"
                lamp.isAutomaticOn = true
                lamp.isScheduleOn = false
            }
            R.id.button_schedule -> {
                lamp.mode = "schedule"
                lamp.isAutomaticOn = false
                lamp.isScheduleOn = true
            }
            R.id.button_manual -> {
                lamp.mode = "manual"
                lamp.isAutomaticOn = false
                lamp.isScheduleOn = false
            }
            else -> {
                lamp.mode = "manual"
                lamp.isAutomaticOn = false
                lamp.isScheduleOn = false
            }
        }
    }

    // ========================== POWER STATE =========================== //

    fun fetchPowerState(lamp: LampModel) {
        viewModelScope.launch {
            lampRepository.getIsPowerOn(lamp) { isPowerOn ->
                if (isPowerOn != null) {
                    Log.d(TAG, "Successfully fetched power state: $isPowerOn")
                    _isPowerOn.postValue(isPowerOn)
                } else {
                    Log.e(TAG, "Error fetching power state: isPowerOn is null.")
                }
            }
        }
    }

    fun updatePowerState(lamp: LampModel) {
        viewModelScope.launch {
            lampRepository.putIsPowerOn(lamp) { success ->
                if (success) {
                    _isPowerOn.postValue(lamp.isPowerOn)
                    Log.d(TAG, "Success updating power state")
                } else {
                    Log.e(TAG, "Error updating power state")
                }
            }
        }
    }

    // =========================== SCHEDULE =========================== //

    fun fetchScheduleStartTime(lamp: LampModel) {
        viewModelScope.launch {
            lampRepository.getScheduleFrom(lamp) { scheduleFrom ->
                if (scheduleFrom != null) {
                    Log.d(TAG, "Successfully fetched schedule start time: $scheduleFrom")
                    _scheduleFrom.postValue(scheduleFrom)
                } else {
                    Log.e(TAG, "Error fetching schedule start time: scheduleFrom is null.")
                }
            }
        }
    }

    fun fetchScheduleFinishTime(lamp: LampModel) {
        viewModelScope.launch {
            lampRepository.getScheduleTo(lamp) { scheduleTo ->
                if (scheduleTo != null) {
                    Log.d(TAG, "Successfully fetched schedule finish time: $scheduleTo")
                    _scheduleTo.postValue(scheduleTo)
                } else {
                    Log.e(TAG, "Error fetching schedule finish time: scheduleTo time is null.")
                }
            }
        }
    }

    fun updateScheduleStartTime(scheduleFrom: String) {
        viewModelScope.launch {
            selectedLamp.value?.let { lamp ->
                val updatedLamp = lamp.copy(scheduleFrom = scheduleFrom)

                lampRepository.putScheduleFrom(updatedLamp) { success, exception ->
                    if (success) {
                        _scheduleFrom.postValue(scheduleFrom)
                        Log.d(TAG, "Success updating schedule start time")
                    } else {
                        Log.e(TAG, "Error updating schedule start time", exception)
                    }
                }
            }
        }
    }

    fun updateScheduleFinishTime(scheduleTo: String) {
        viewModelScope.launch {
            selectedLamp.value?.let { lamp ->
                val updatedLamp = lamp.copy(scheduleTo = scheduleTo)

                lampRepository.putScheduleTo(updatedLamp) { success, exception ->
                    if (success) {
                        _scheduleTo.postValue(scheduleTo)
                        Log.d(TAG, "Success updating schedule finish time")
                    } else {
                        Log.e(TAG, "Error updating schedule finish time", exception)
                    }
                }
            }
        }
    }
}