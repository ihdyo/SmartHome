package com.ihdyo.smarthome.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.model.IconModel

class HomeViewModel : ViewModel() {
    val items: MutableLiveData<List<IconModel>> = MutableLiveData()

    init {
        fetchData()
    }

    private fun fetchData() {
        val itemList = listOf(
            IconModel(R.drawable.bx_tv),
            IconModel(R.drawable.bx_bowl_hot),
            IconModel(R.drawable.bx_bed),
            IconModel(R.drawable.bx_bath)
        )
        items.value = itemList
    }
}