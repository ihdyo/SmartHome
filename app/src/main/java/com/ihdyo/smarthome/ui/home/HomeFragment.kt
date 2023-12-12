package com.ihdyo.smarthome.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.databinding.FragmentHomeBinding
import java.io.IOException
import java.util.Locale

class HomeFragment : Fragment() {

    companion object {
        private val REQUEST_CODE = 100
        private val WATT_POWER = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var homeAdapterIcon: HomeAdapterIcon

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var timePicker: String? = null
    private var isTime: String? = null
    private var isScheduleOn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.items.observe(viewLifecycleOwner) { items ->
            homeAdapterIcon.setItems(items)
        }

        // TODO (fetch from db)
        binding.imageRoom.setImageResource(R.drawable.img_bedroom)


        binding.textSalute.text = "Good Morning" + ", " // retrieve
        binding.textUsername.text = "Mamat" + "!" // retrieve

        val totalTime = 100 // retrieve
        val totalTimeHour = totalTime / 1000 * 60
        val powerConsumed = WATT_POWER * totalTimeHour
        val powerConsumedTotal = WATT_POWER * powerConsumed / 1000

        binding.textPowerConsumed.text = powerConsumed.toString() + "Wh"
        binding.textPowerConsumedTotal.text = powerConsumedTotal.toString() + "kWh"

        val roomName = "Bedroom" // retrieve

        binding.textRoom.text = roomName
        binding.textRoomDecoration.text = roomName

        binding.textRoomFloor.text = "1F" // retrieve
        binding.textRoomFloor.text = "1F" // retrieve

        // Power Switch
        var power = false //retrieve

        binding.switchPower.isChecked = power

        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
            power = isChecked
        }
        if (power) {
            binding.switchPower.isChecked = false
        }

        // Schedule
        isScheduleOn = false //retrieve
        updateIconSchedule()

        binding.iconSchedule.setOnClickListener {
            isScheduleOn = !isScheduleOn
            updateIconSchedule()
        }

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


        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recyclerview
        homeAdapterIcon = HomeAdapterIcon(emptyList())
        recyclerView = binding.rvIconRoom
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = homeAdapterIcon
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

            timePicker = "$hour:$minute"
            callback(timePicker!!)
        }
    }

    private fun updateIconSchedule() {
        val colorFilter: Int

        if (isScheduleOn) {
            val colorOnSurface = getThemeColor(com.google.android.material.R.attr.colorPrimary)
            colorFilter = colorOnSurface
        } else {
            val colorPrimary = getThemeColor(com.google.android.material.R.attr.colorOnSurface)
            colorFilter = colorPrimary
        }
        binding.iconSchedule.setColorFilter(colorFilter)
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireActivity(), Locale("ID"))
                    val addresses: List<Address>?
                    try {
                        addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        binding.textCity.text = addresses!![0].subAdminArea
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

    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attr, typedValue, true)
        return ContextCompat.getColor(requireContext(), typedValue.resourceId)
    }
}