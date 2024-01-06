package com.ihdyo.smarthome.data.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ihdyo.smarthome.data.repository.TeamRepository
import com.ihdyo.smarthome.data.viewmodel.TeamViewModel

@Suppress("UNCHECKED_CAST")
class TeamViewModelFactory(private val teamRepository: TeamRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            return TeamViewModel(teamRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
