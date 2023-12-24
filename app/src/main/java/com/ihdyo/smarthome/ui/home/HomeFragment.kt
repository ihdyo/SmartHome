package com.ihdyo.smarthome.ui.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat.is24HourFormat
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

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var isTime: String? = null
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
            binding.textUsername.text = user?.userName
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

        // Fetch lamps data
        homeViewModel.fetchLamps(UID)




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

    private fun recyclerViewInit(rooms: List<RoomModel>) {
        if (!::roomAdapter.isInitialized) {
            roomAdapter = RoomAdapter(rooms, { selectedRoom -> updateOtherProperties(selectedRoom) }, homeViewModel)
            binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvIconRoom.adapter = roomAdapter
        } else {
            roomAdapter.setItems(rooms)
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateOtherProperties(selectedRoom: RoomModel) {
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

//        // Power Used
//        homeViewModel.powerConsumed.observe(viewLifecycleOwner) { powerConsumed ->
//        }
//
//        // Mode State
//        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, _ ->
//            homeViewModel.updateSelectedMode(checkedId)
//        }
//        homeViewModel.mode.observe(viewLifecycleOwner) { mode ->
//            binding.textTest.text = mode
//        }
//        homeViewModel.selectedMode.observe(viewLifecycleOwner) { selectedModeId ->
//            binding.toggleMode.check(selectedModeId)
//        }
//
//        // Switch Power
//        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked != homeViewModel.isPowerOn.value) {
//                homeViewModel.updatePowerState(isChecked)
//            }
//        }
//        homeViewModel.isPowerOn.observe(viewLifecycleOwner) { isPowerOn ->
//            if (binding.switchPower.isChecked != isPowerOn) {
//                binding.switchPower.isChecked = isPowerOn
//            }
//        }
//
//
//
//        // Schedule
//        homeViewModel.scheduleFrom.observe(viewLifecycleOwner) { scheduleFrom ->
//            binding.textScheduleFrom.text = scheduleFrom
//        }
//
//        homeViewModel.scheduleTo.observe(viewLifecycleOwner) { scheduleTo ->
//            binding.textScheduleTo.text = scheduleTo
//        }



    }

//    private fun setupScheduleTimeListeners(selectedRoom: RoomModel) {
//        // Schedule Time From
//        binding.textScheduleFrom.text = selectedRoom.scheduleFrom
//        binding.textScheduleFrom.setOnClickListener {
//            isTime = "Select Start Time"
//            openTimePicker { selectedTime ->
//                binding.textScheduleFrom.text = selectedTime
//                homeViewModel.updateScheduleFrom(selectedTime)
//            }
//        }
//
//        // Schedule Time To
//        binding.textScheduleTo.text = selectedRoom.scheduleTo
//        binding.textScheduleTo.setOnClickListener {
//            isTime = "Select Finish Time"
//            openTimePicker { selectedTime ->
//                binding.textScheduleTo.text = selectedTime
//                homeViewModel.updateScheduleTo(selectedTime)
//            }
//        }
//    }

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
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

        val picker =
            MaterialTimePicker.Builder()
                .setInputMode(INPUT_MODE_CLOCK)
                .setTimeFormat(clockFormat)
                .setHour(currentHour)
                .setMinute(currentMinute)
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
                binding.switchPower.isEnabled = false
                binding.textScheduleFrom.alpha = 0.5F
                binding.textScheduleFrom.isEnabled = false
                binding.textScheduleTo.alpha = 0.5F
                binding.textScheduleTo.isEnabled = false
            }
            R.id.button_schedule -> {
                binding.switchPower.isEnabled = false
                binding.textScheduleFrom.alpha = 1F
                binding.textScheduleFrom.isEnabled = true
                binding.textScheduleTo.alpha = 1F
                binding.textScheduleTo.isEnabled = true
            }
            R.id.button_manual -> {
                binding.switchPower.isEnabled = true
                binding.textScheduleFrom.alpha = 0.5F
                binding.textScheduleFrom.isEnabled = false
                binding.textScheduleTo.alpha = 0.5F
                binding.textScheduleTo.isEnabled = false
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