package com.ihdyo.smarthome.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.ViewModelFactory
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.repository.LampRepository
import com.ihdyo.smarthome.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    companion object {
        const val REQUEST_CODE = 100
        const val WATT_POWER = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var lampIconAdapter: LampIconAdapter

    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var isTime: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this, ViewModelFactory(LampRepository(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance())))
            .get(HomeViewModel::class.java)

        homeViewModel.lampDetails.observe(viewLifecycleOwner, Observer { lamps ->
            if (lamps.isNotEmpty()) {
                val firstLamp = lamps.first()
                homeViewModel.loadLampImage(firstLamp.roomIcon ?: "")
            }
            initRecyclerView(lamps)
        })

        homeViewModel.selectedLamp.observe(viewLifecycleOwner, Observer { selectedLamp ->
            updateOtherProperties(selectedLamp)
        })

        homeViewModel.totalPowerConsumed.observe(viewLifecycleOwner, Observer { totalPowerConsumed ->
            binding.textPowerConsumedTotal.text = totalPowerConsumed
        })

        homeViewModel.modeUpdateResult.observe(viewLifecycleOwner, Observer { isModeUpdateSuccessful ->
            if (isModeUpdateSuccessful) {
                // Mode update was successful, handle accordingly
                Toast.makeText(requireContext(), "Mode updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Mode update failed, handle accordingly
                Toast.makeText(requireContext(), "Failed to update mode", Toast.LENGTH_SHORT).show()
            }
        })

        homeViewModel.fetchLampDetails()

        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inside onViewCreated method
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.calculateTotalPowerConsumed()

            // Ensure that selectedLamp is not null before calling updateMode
            homeViewModel.selectedLamp.value?.let { selectedLamp ->
                homeViewModel.updateMode(selectedLamp)
            }
        }


        // Time
        getCurrentTime { formattedTime ->
            binding.textGreeting.text = formattedTime
        }

        // Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation { city ->
            binding.textCity.text = city
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(lamps: List<LampModel>) {
        // Initialize RecyclerView only once
        if (!::lampIconAdapter.isInitialized) {
            lampIconAdapter = LampIconAdapter(lamps, { selectedLamp ->
                updateOtherProperties(selectedLamp)
            }, homeViewModel)
            binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvIconRoom.adapter = lampIconAdapter
        } else {
            lampIconAdapter.setItems(lamps)
        }
    }


    private fun updateOtherProperties(selectedLamp: LampModel) {

        // Room Name
        val roomName = selectedLamp.roomName
        binding.textRoom.text = roomName
        binding.textRoomDecoration.text = roomName

        // Room Floor
        binding.textRoomFloor.text = selectedLamp.roomFloor

        // Room Image
        binding.imageRoom.load(selectedLamp.roomImage) {
            placeholder(R.drawable.bx_landscape)
            error(R.drawable.bx_error)
            crossfade(true)
            memoryCachePolicy(CachePolicy.ENABLED)
        }

        // Total Runtime
        val totalTime = selectedLamp.totalRuntime
        val totalTimeHour = totalTime / 60 / 60
        val powerConsumed = (WATT_POWER * totalTimeHour).toString()
        binding.textPowerConsumed.text = "${powerConsumed}Wh"

        // Power Switch
//        var power = selectedLamp.isPowerOn
//
//        binding.switchPower.isChecked = power == true
//
//        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
//            power = isChecked
//        }
//        if (power == false) {
//            binding.switchPower.isChecked = false
//        }

//        updateOtherProperties(selectedLamp)

        // Mode
        updateUIForMode(getCheckedButtonId(selectedLamp.mode!!), selectedLamp)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_automatic -> {
                        selectedLamp.mode = "automatic"
                        updateUIForMode(checkedId, selectedLamp)
                        // Update the mode in Firestore
                        homeViewModel.updateMode(selectedLamp)
                    }
                    R.id.button_schedule -> {
                        selectedLamp.mode = "schedule"
                        updateUIForMode(checkedId, selectedLamp)
                        // Update the mode in Firestore
                        homeViewModel.updateMode(selectedLamp)
                    }
                    R.id.button_manual -> {
                        selectedLamp.mode = "manual"
                        updateUIForMode(checkedId, selectedLamp)
                        // Update the mode in Firestore
                        homeViewModel.updateMode(selectedLamp)
                    }
                }
            }
        }
        binding.toggleMode.check(getCheckedButtonId(selectedLamp.mode!!))

        // Time Picker
        binding.textScheduleTimeFrom.setOnClickListener {
            isTime = "Select Start Time"
            openTimePicker { selectedTime ->
                binding.textScheduleTimeFrom.text = selectedTime
            }
        }

        binding.textScheduleTimeTo.setOnClickListener {
            isTime = "Select Finish Time"
            openTimePicker { selectedTime ->
                binding.textScheduleTimeTo.text = selectedTime
            }
        }
    }

    private fun getCurrentTime(callback: (String) -> Unit) {
        val currentTime = Calendar.getInstance()
        val greeting = when (currentTime.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> getString(R.string.text_greeting_night)
            in 6..11 -> getString(R.string.text_greeting_morning)
            in 12..17 -> getString(R.string.text_greeting_afternoon)
            else -> getString(R.string.text_greeting_evening)
        }
        callback(greeting)
    }

    @SuppressLint("SetTextI18n")
    private fun openTimePicker(callback: (String) -> Unit) {
        val isSystem24Hour = is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val picker =
            MaterialTimePicker.Builder()
                .setInputMode(INPUT_MODE_CLOCK)
                .setTimeFormat(clockFormat)
                .setHour(12)
                .setMinute(10)
                .setTitleText(isTime)
                .build()
        picker.show(childFragmentManager, "TAG")

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute

            val formattedHour = String.format("%02d", hour)
            val formattedMinute = String.format("%02d", minute)

            val timePicker = "$formattedHour:$formattedMinute"
            callback(timePicker)
        }
    }

    private fun getCheckedButtonId(mode: String): Int {
        return when (mode) {
            "automatic" -> R.id.button_automatic
            "schedule" -> R.id.button_schedule
            "manual" -> R.id.button_manual
            else -> -1
        }
    }

    private fun updateUIForMode(checkedId: Int, selectedLamp: LampModel) {
        when (checkedId) {
            R.id.button_automatic -> {
                binding.textPower.alpha = 0.5F
                binding.switchPower.isEnabled = false
                binding.textScheduleFrom.alpha = 0.5F
                binding.textScheduleTimeFrom.isEnabled = false
                binding.textScheduleTo.alpha = 0.5F
                binding.textScheduleTimeTo.isEnabled = false

                selectedLamp.isScheduleOn = false
                selectedLamp.isAutomaticOn = true
            }
            R.id.button_schedule -> {
                binding.textPower.alpha = 0.5F
                binding.switchPower.isEnabled = false
                binding.textScheduleFrom.alpha = 1F
                binding.textScheduleTimeFrom.isEnabled = true
                binding.textScheduleTo.alpha = 1F
                binding.textScheduleTimeTo.isEnabled = true

                selectedLamp.isScheduleOn = true
                selectedLamp.isAutomaticOn = true
            }
            R.id.button_manual -> {
                binding.textPower.alpha = 1F
                binding.switchPower.isEnabled = true
                binding.textScheduleFrom.alpha = 0.5F
                binding.textScheduleTimeFrom.isEnabled = false
                binding.textScheduleTo.alpha = 0.5F
                binding.textScheduleTimeTo.isEnabled = false

                selectedLamp.isScheduleOn = false
                selectedLamp.isAutomaticOn = true
            }
        }
    }

    private fun getLastLocation(callback: (String?) -> Unit) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val locationCity = addresses!!.firstOrNull()?.adminArea
                        callback(locationCity)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            askPermission()
        }
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
    }
}