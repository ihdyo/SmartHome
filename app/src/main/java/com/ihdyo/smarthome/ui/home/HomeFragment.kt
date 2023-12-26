package com.ihdyo.smarthome.ui.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.ViewModelFactory
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.repository.SmartHomeRepository
import com.ihdyo.smarthome.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class HomeFragment : Fragment() {

    companion object {
        const val WATT_POWER = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var roomAdapter: RoomAdapter
    private lateinit var lampAdapter: LampAdapter

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var currentRotationX = 0f
    private var currentRotationY = 0f
    private var previousDistance = 0f
    private val sensitivity = 0.001f // Adjust the sensitivity as needed
    private val maxRotation = 5f // Adjust the maximum rotation in degrees
    private val rotationThreshold = 0.5f // Adjust the threshold as needed
    private val rotationDuration = 500L // Adjust the duration for rotation animation

    private val UID = "n5BwXDohfZPzXVS3EjalXgwjGcI3"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(this, ViewModelFactory(SmartHomeRepository()))[HomeViewModel::class.java]

        // Observe user data
        homeViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            val fullName = user?.userName
            val firstName = fullName?.split(" ")?.firstOrNull()
            binding.textUsername.text = firstName
        }

        // Observe rooms data
        homeViewModel.roomsLiveData.observe(viewLifecycleOwner) { rooms ->
            if (rooms != null) {
                recyclerViewInit(rooms)
            }
        }

        // Fetch user data
        homeViewModel.fetchUser(UID)

        // Fetch rooms data
        homeViewModel.fetchRooms(UID)




//        lifecycleScope.launch {
//            homeViewModel.fetchRooms(UID).observe(viewLifecycleOwner) { lamps ->
//                recyclerViewInit(lamps)
//            }
//
//            homeViewModel.selectedRoom.observe(viewLifecycleOwner) { selectedRoom ->
//                homeViewModel.setSelectedLamp(selectedRoom)
//                updateOtherProperties(selectedRoom)
//            }
//
//            homeViewModel.fetchTotalPowerConsumed()
//            homeViewModel.totalPowerConsumed.observe(viewLifecycleOwner) { totalPowerConsumed ->
//            }
//        }


        // Time
        getCurrentTime { formattedTime ->
            binding.textGreeting.text = formattedTime
        }

        // Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation { city ->
            binding.textCity.text = city
        }

        // Image
        startFloatingAnimation(binding.imageRoom, AccelerateDecelerateInterpolator())

        binding.imageRoom.setOnTouchListener { _, event ->
            handleTouch(event)
        }

        binding.swipeRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.swipeRefresh.isRefreshing = false
            }, 300)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recyclerViewInit2(selectedRoom: RoomModel, lamps: List<LampModel>) {
        lampAdapter = LampAdapter(lamps, { selectedLamp -> updateOtherProperties2(selectedRoom, selectedLamp) }, homeViewModel)
        binding.rvIconLamp.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvIconLamp.adapter = lampAdapter
        lampAdapter.setInitialSelectedIndex(0)
    }

    private fun recyclerViewInit(rooms: List<RoomModel>) {
        if (!::roomAdapter.isInitialized) {
            roomAdapter = RoomAdapter(rooms, { selectedRoom -> updateOtherProperties(selectedRoom) }, homeViewModel)
            binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvIconRoom.adapter = roomAdapter
            roomAdapter.setInitialSelectedIndex(0)
        } else {
            roomAdapter.setItems(rooms)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateOtherProperties2(selectedRoom: RoomModel, selectedLamp: LampModel) {
        // Power Consumed
        homeViewModel.powerConsumedLiveData.observe(viewLifecycleOwner) { powerConsumedMap ->
            val powerConsumed = powerConsumedMap[selectedLamp.LID]
            val formattedText = "${powerConsumed}Wh"
            binding.textPowerConsumed.text = formattedText
        }

        // Total Power Consumed
        homeViewModel.totalPowerConsumedLiveData.observe(viewLifecycleOwner) { totalPowerConsumedMap ->
            val formattedText = "${totalPowerConsumedMap}Wh"
            binding.textTotalPowerConsumed.text = formattedText
        }

        // Lamp Brightness
        homeViewModel.lampBrightnessLiveData.observe(viewLifecycleOwner) { brightness ->
            binding.sliderLampBrightness.value = brightness.toFloat()
        }
        binding.sliderLampBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                homeViewModel.updateLampBrightness(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), value.toInt())
            }
        }

        // Lamp Switch Power
        homeViewModel.lampIsPowerOnLiveData.observe(viewLifecycleOwner) { isPowerOn ->
            binding.switchPower.isChecked = isPowerOn
        }
        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
            selectedLamp.lampIsPowerOn = isChecked
            homeViewModel.updateLampIsPowerOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), isChecked)
        }

        // Lamp Selected Mode
        homeViewModel.lampSelectedModeLiveData.observe(viewLifecycleOwner) { selectedMode ->
            getButtonState(selectedMode)
        }

        binding.buttonAutomatic.setOnClickListener {
            homeViewModel.updateLampSelectedMode(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), "automatic")
        }
        binding.buttonSchedule.setOnClickListener {
            homeViewModel.updateLampSelectedMode(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), "schedule")
        }
        binding.buttonManual.setOnClickListener {
            homeViewModel.updateLampSelectedMode(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), "manual")
        }

        // Lamp Schedule
        homeViewModel.lampScheduleLiveData.observe(viewLifecycleOwner) { schedule ->
            binding.textScheduleFrom.text = schedule.scheduleFrom
            binding.textScheduleTo.text = schedule.scheduleTo
        }
        binding.textScheduleFrom.setOnClickListener {
            openTimePicker("Select Start Time") { selectedTime ->
                val newSchedule = homeViewModel.lampScheduleLiveData.value?.copy(scheduleFrom = selectedTime)
                newSchedule?.let {
                    homeViewModel.updateLampSchedule(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), it)
                }
            }
        }
        binding.textScheduleTo.setOnClickListener {
            openTimePicker("Select Finish Time") { selectedTime ->
                val newSchedule = homeViewModel.lampScheduleLiveData.value?.copy(scheduleTo = selectedTime)
                newSchedule?.let {
                    homeViewModel.updateLampSchedule(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), it)

                }
            }
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateOtherProperties(selectedRoom: RoomModel) {

        // Observe lamps data
        homeViewModel.lampsLiveData.observe(viewLifecycleOwner) { lamps ->
            if (lamps != null) {
                recyclerViewInit2(selectedRoom, lamps)
            }
        }

        // Fetch lamps data
        homeViewModel.fetchLamps(UID, selectedRoom.RID.toString())



        // Room Name
        val roomName = selectedRoom.roomName
        binding.textRoom.text = roomName
        binding.textRoomDecoration.text = roomName

        // Room Floor
        binding.textRoomFloor.text = "${selectedRoom.roomFloor}F"

        // Room Image
        binding.imageRoom.load(selectedRoom.roomImage) {
            placeholder(R.drawable.bx_landscape)
            error(R.drawable.bx_error)
            crossfade(true)
            memoryCachePolicy(CachePolicy.ENABLED)
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
    private fun openTimePicker(title: String, callback: (String) -> Unit) {
        val isSystem24Hour = is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

        val picker =
            MaterialTimePicker.Builder()
                .setInputMode(INPUT_MODE_CLOCK)
                .setTimeFormat(clockFormat)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setTitleText(title)
                .build()
        picker.show(childFragmentManager, "TAG")

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute

            val formattedHour = String.format("%02d", hour)
            val formattedMinute = String.format("%02d", minute)

            val selectedTime = "$formattedHour:$formattedMinute"
            callback(selectedTime)
        }
    }

    private fun getButtonState(selectedMode: String) {
        binding.toggleMode.let { toggleGroup ->
            when (selectedMode) {
                "automatic" -> {
                    toggleGroup.check(R.id.button_automatic)
                    binding.switchPower.isEnabled = false
                    binding.textFrom.alpha = 0.5F
                    binding.textScheduleFrom.isEnabled = false
                    binding.textTo.alpha = 0.5F
                    binding.textScheduleTo.isEnabled = false
                }
                "schedule" -> {
                    toggleGroup.check(R.id.button_schedule)
                    binding.switchPower.isEnabled = false
                    binding.textFrom.alpha = 1F
                    binding.textScheduleFrom.isEnabled = true
                    binding.textTo.alpha = 1F
                    binding.textScheduleTo.isEnabled = true
                }
                "manual" -> {
                    toggleGroup.check(R.id.button_manual)
                    binding.switchPower.isEnabled = true
                    binding.textFrom.alpha = 0.5F
                    binding.textScheduleFrom.isEnabled = false
                    binding.textTo.alpha = 0.5F
                    binding.textScheduleTo.isEnabled = false
                }
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
        }
    }

    private fun startFloatingAnimation(imageView: ImageView, interpolator: TimeInterpolator) {
        val animator = ObjectAnimator.ofFloat(imageView, "translationY", -24f, 24f)
        animator.interpolator = interpolator
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.duration = 5000

        animator.start()
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialTouchX = event.x
                initialTouchY = event.y
                previousDistance = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - initialTouchX
                val deltaY = event.y - initialTouchY

                val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))

                // Apply the threshold for smooth rotation
                if (distance > rotationThreshold) {
                    val rotationX = deltaY * sensitivity * calculateWeight(distance)
                    val rotationY = deltaX * sensitivity * calculateWeight(distance)

                    // Update the current rotation values
                    currentRotationX += rotationX
                    currentRotationY += rotationY

                    // Limit rotation within a specific range
                    currentRotationX = currentRotationX.coerceIn(-maxRotation, maxRotation)
                    currentRotationY = currentRotationY.coerceIn(-maxRotation, maxRotation)

                    // Rotate the image around the X and Y axes
                    binding.imageRoom.rotationX = currentRotationX
                    binding.imageRoom.rotationY = currentRotationY

                    // Update the initial touch position for the next move
                    initialTouchX = event.x
                    initialTouchY = event.y
                    previousDistance = distance
                }
            }
            MotionEvent.ACTION_UP -> {
                // Return to the initial state with a smooth animation
                val rotationAnimatorX = ObjectAnimator.ofFloat(binding.imageRoom, "rotationX", 0f)
                rotationAnimatorX.duration = rotationDuration
                rotationAnimatorX.start()

                val rotationAnimatorY = ObjectAnimator.ofFloat(binding.imageRoom, "rotationY", 0f)
                rotationAnimatorY.duration = rotationDuration
                rotationAnimatorY.start()

                // Reset the current rotation values
                currentRotationX = 0f
                currentRotationY = 0f
            }
        }
        return true
    }

    private fun calculateWeight(distance: Float): Float {
        return 1 / (1 + distance / 100)
    }
}