package com.ihdyo.smarthome.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.model.TeamModel
import com.ihdyo.smarthome.data.repository.TeamRepository
import kotlinx.coroutines.launch

class TeamViewModel(private val teamRepository: TeamRepository) : ViewModel() {

    private val _teamLiveData = MutableLiveData<List<TeamModel>?>()
    val teamLiveData: LiveData<List<TeamModel>?> get() = _teamLiveData

    private val _currentTeamLiveData = MutableLiveData<String>()
    val currentTeamLiveData: LiveData<String> get() = _currentTeamLiveData

    fun setCurrentTeamId(teamId: String) {
        _currentTeamLiveData.value = teamId
    }

    fun fetchAllTeam() {
        viewModelScope.launch {
            val allTeam = teamRepository.getAllTeam()
            if (allTeam != null) {
                _teamLiveData.postValue(allTeam)
                Log.d(TAG, "Successfully fetched all team")
            } else {
                Log.e(TAG, "Error fetching all team")
            }
        }
    }

    fun fetchTeamById(teamId: String): TeamModel? {
        return try {
            val teamDetail = _teamLiveData.value?.find { it.TID == teamId }
            if (teamDetail != null) {
                Log.d(TAG, "Successfully fetched: $teamId")
            }
            teamDetail
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching: $teamId", e)
            null
        }
    }

    companion object {
        private const val TAG = "TeamViewModel"
    }

}