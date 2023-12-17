package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
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

    private val _lampImage = MutableLiveData<String>()
    val lampImage: LiveData<String> get() = _lampImage

    private val _totalPowerConsumed = MutableLiveData<String>()
    val totalPowerConsumed: LiveData<String> get() = _totalPowerConsumed

    private val _modeUpdateResult = MutableLiveData<Boolean>()
    val modeUpdateResult: LiveData<Boolean> get() = _modeUpdateResult

    private val _isPowerOn = MutableLiveData<Boolean>()
    val isPowerOn: LiveData<Boolean> get() = _isPowerOn

    private val _scheduleFrom = MutableLiveData<String>()
    val scheduleFrom: LiveData<String> get() = _scheduleFrom

    private val _scheduleTo = MutableLiveData<String>()
    val scheduleTo: LiveData<String> get() = _scheduleTo



    private var currentPowerState: Boolean = false


    // Show all data from lamps
    @SuppressLint("NullSafeMutableLiveData")
    fun fetchLampDetails() {
        viewModelScope.launch {
            try {
                val lamps = lampRepository.getLamps()
                _lampDetails.postValue(lamps)
            } catch (e: Exception) {
                Log.e(this.javaClass.simpleName, "Error fetching lamp details", e)
            }
        }
    }

    // Select lamp document when item clicked
    fun setSelectedLamp(lamp: LampModel) {
        viewModelScope.launch {
            _selectedLamp.postValue(lamp)
        }
    }

    // Calculate all power consumed
    suspend fun calculateTotalPowerConsumed() {
        viewModelScope.launch {
            try {
                val totalRuntime = lampRepository.getTotalRuntime()
                val totalRuntimeHours = totalRuntime / 60 / 60
                val totalPowerConsumed = (WATT_POWER * totalRuntimeHours).toString()

                _totalPowerConsumed.postValue("${totalPowerConsumed}Wh")
            } catch (e: Exception) {
                Log.e(this.javaClass.simpleName, "Error calculating totalRuntime", e)
            }
        }
    }

    // Update mode state
    fun updateMode(checkedId: Int) {
        viewModelScope.launch {
            try {
                selectedLamp.value?.let { selectedLamp ->
                    when (checkedId) {
                        R.id.button_automatic -> {
                            selectedLamp.mode = "automatic"
                            selectedLamp.isAutomaticOn = true
                            selectedLamp.isScheduleOn = false
                        }

                        R.id.button_schedule -> {
                            selectedLamp.mode = "schedule"
                            selectedLamp.isAutomaticOn = false
                            selectedLamp.isScheduleOn = true
                        }

                        R.id.button_manual -> {
                            selectedLamp.mode = "manual"
                            selectedLamp.isAutomaticOn = false
                            selectedLamp.isScheduleOn = false
                        }
                    }
                    lampRepository.putMode(selectedLamp)
                    _modeUpdateResult.postValue(true)
                }
            } catch (e: Exception) {
                _modeUpdateResult.postValue(false)
                Log.e(this.javaClass.simpleName, "Error updating mode", e)
            }
        }
    }

    // Update power state
    fun updatePowerState(powerState: Boolean) {
        viewModelScope.launch {
            if (powerState != currentPowerState) {
                selectedLamp.value?.let { lamp ->
                    lamp.isPowerOn = powerState
                    lampRepository.putIsPowerOn(lamp) { isSuccess ->
                        if (isSuccess) {
                            _isPowerOn.postValue(powerState)
                            currentPowerState = powerState
                        } else {
                            Log.e(this.javaClass.simpleName, "Error updating power state")
                        }
                    }
                }
            }
        }
    }

    // Update schedule start time
    fun updateScheduleFrom(scheduleFrom: String) {
        viewModelScope.launch {
            selectedLamp.value?.let { lamp ->
                _scheduleFrom.postValue(scheduleFrom)
                lampRepository.putScheduleFrom(lamp)
            }
        }
    }

    // Update schedule finish time
    fun updateScheduleTo(scheduleTo: String) {
        viewModelScope.launch {
            selectedLamp.value?.let { lamp ->
                _scheduleTo.postValue(scheduleTo)
                lampRepository.putScheduleTo(lamp)
            }
        }
    }

}