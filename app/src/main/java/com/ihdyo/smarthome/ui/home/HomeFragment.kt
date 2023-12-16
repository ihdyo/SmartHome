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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.ihdyo.smarthome.data.LampViewModelFactory
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.repository.LampRepository
import com.ihdyo.smarthome.databinding.FragmentHomeBinding
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var lampIconAdapter: LampIconAdapter

    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var isTime: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val lampViewModel: LampViewModel by viewModels {
            LampViewModelFactory(LampRepository(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance()))
        }

        // View Model
        lampViewModel.lampDetails.observe(viewLifecycleOwner, Observer { lamps ->
            if (lamps.isNotEmpty()) {
                val firstLamp = lamps.first()
                lampViewModel.loadLampImage(firstLamp.roomImage ?: "")
            }

            // Initialize RecyclerView with the fetched lamps
            initRecyclerView(lamps)
        })

        // Observe changes in the lamp image URL
        lampViewModel.lampImage.observe(viewLifecycleOwner, Observer { imageUrl ->
            // Load the lamp image using Glide
            Glide.with(requireContext())
                .load(imageUrl)
                .into(binding.imageRoom)
        })

        // Fetch lamp details from Firestore
        lampViewModel.fetchLampDetails()


        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recyclerview


        // TODO (fetch from db)

//        binding.imageRoom.setImageResource(R.drawable.img_living_room)

        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReference("image/img_room_bathroom.png")

//        storageReference.downloadUrl
//            .addOnSuccessListener { uri: Uri? ->
//                Glide.with(this)
//                    .load(uri)
//                    .apply(
//                        RequestOptions()
//                            .placeholder(R.drawable.ic_launcher_foreground)
//                            .error(R.drawable.app_icon)
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    )
//                    .into(binding.imageRoom)
//            }



        val username = "Yo" // retrieve
        binding.textUsername.text = " ${username}!"

        val totalTime = 100 // retrieve
        val totalTimeHour = totalTime / 1000 * 60
        val powerConsumed = (WATT_POWER * totalTimeHour).toString()
        val powerConsumedTotal = (WATT_POWER * powerConsumed.toInt() / 1000).toString()

        binding.textPowerConsumed.text = "${powerConsumed}Wh"
        binding.textPowerConsumedTotal.text = "${powerConsumedTotal}kWh"

//        val roomName = "Living Room" // retrieve
//
//        binding.textRoom.text = roomName
//        binding.textRoomDecoration.text = roomName

        binding.textRoomFloor.text = "1F" // retrieve

        // Time
        getCurrentTime { formattedTime ->
            binding.textGreeting.text = formattedTime
        }

        // Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getLastLocation { city ->
            binding.textCity.text = city
        }

        // Power Switch
        var power = false //retrieve

        binding.switchPower.isChecked = power

        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
            power = isChecked
        }
        if (power) {
            binding.switchPower.isChecked = false
        }

        // Mode
        var mode = "automatic" // retrieve
        updateUIForMode(getCheckedButtonId(mode))
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_automatic -> {
                        mode = "automatic"
                        updateUIForMode(checkedId)
                    }
                    R.id.button_schedule -> {
                        mode = "schedule"
                        updateUIForMode(checkedId)
                    }
                    R.id.button_manual -> {
                        mode = "manual"
                        updateUIForMode(checkedId)
                    }
                }
            }
        }
        binding.toggleMode.check(getCheckedButtonId(mode))

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(lamps: List<LampModel>) {
        // Initialize RecyclerView only once
        if (!::lampIconAdapter.isInitialized) {
            lampIconAdapter = LampIconAdapter(lamps) { selectedLamp ->
                updateOtherProperties(selectedLamp)
            }
            binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvIconRoom.adapter = lampIconAdapter
        } else {
            lampIconAdapter.setItems(lamps)
        }
    }

    private fun updateOtherProperties(selectedLamp: LampModel) {
        binding.textRoom.text = selectedLamp.roomName
        binding.textRoomDecoration.text = selectedLamp.roomName
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

    private fun updateUIForMode(checkedId: Int) {
        when (checkedId) {
            R.id.button_automatic -> {
                binding.textPower.alpha = 0.5F
                binding.switchPower.isEnabled = false
                binding.textScheduleFrom.alpha = 0.5F
                binding.textScheduleTimeFrom.isEnabled = false
                binding.textScheduleTo.alpha = 0.5F
                binding.textScheduleTimeTo.isEnabled = false
            }
            R.id.button_schedule -> {
                binding.textPower.alpha = 0.5F
                binding.switchPower.isEnabled = false
                binding.textScheduleFrom.alpha = 1F
                binding.textScheduleTimeFrom.isEnabled = true
                binding.textScheduleTo.alpha = 1F
                binding.textScheduleTimeTo.isEnabled = true
            }
            R.id.button_manual -> {
                binding.textPower.alpha = 1F
                binding.switchPower.isEnabled = true
                binding.textScheduleFrom.alpha = 0.5F
                binding.textScheduleTimeFrom.isEnabled = false
                binding.textScheduleTo.alpha = 0.5F
                binding.textScheduleTimeTo.isEnabled = false
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