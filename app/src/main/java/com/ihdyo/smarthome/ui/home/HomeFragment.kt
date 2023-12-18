package com.ihdyo.smarthome.ui.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
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
import java.lang.Math.sqrt
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

@Suppress("NAME_SHADOWING", "DEPRECATION")
class HomeFragment : Fragment() {

    companion object {
        const val WATT_POWER = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var lampIconAdapter: LampIconAdapter

    var fusedLocationProviderClient: FusedLocationProviderClient? = null

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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this, ViewModelFactory(LampRepository(FirebaseFirestore.getInstance())))[HomeViewModel::class.java]

        homeViewModel.fetchLampDetails().observe(viewLifecycleOwner) { lamps ->
            if (!::lampIconAdapter.isInitialized) {
                lampIconAdapter = LampIconAdapter(lamps, { selectedLamp -> updateOtherProperties(selectedLamp) }, homeViewModel)
                binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                binding.rvIconRoom.adapter = lampIconAdapter
            } else {
                lampIconAdapter.setItems(lamps)
            }
        }

        return root
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.selectedLamp.observe(viewLifecycleOwner) { selectedLamp ->
                updateOtherProperties(selectedLamp)
            }

            homeViewModel.totalPowerConsumed.observe(viewLifecycleOwner){ totalPowerConsumed ->
                binding.textPowerConsumedTotal.text = totalPowerConsumed
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

        // Set up the floating animation with ease-in and ease-out effect
//        startFloatingAnimation(binding.imageRoom, AccelerateDecelerateInterpolator())

        // Set up the rotation on swipe gesture
        binding.imageRoom.setOnTouchListener { _, event ->
            handleTouch(event)
        }

        binding.swipeRefresh.setOnRefreshListener {
            Handler().postDelayed({
                val fragmentTransaction = requireFragmentManager().beginTransaction()
                fragmentTransaction.detach(this)
                fragmentTransaction.attach(this)
                fragmentTransaction.commit()
                homeViewModel.fetchLampDetails()
                binding.swipeRefresh.isRefreshing = false
            }, 300)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun updateOtherProperties(selectedLamp: LampModel) {
        homeViewModel.selectedLamp.observe(viewLifecycleOwner) { selectedLamp ->

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

            // Power Used
            homeViewModel.fetchPowerConsumed(selectedLamp)
            homeViewModel.powerConsumed.observe(viewLifecycleOwner) { powerConsumed ->
                binding.textPowerConsumed.text = powerConsumed
            }

            // Mode State
            val initialCheckedButtonId = getCheckedButtonId(homeViewModel.fetchSelectedMode(selectedLamp)
                .toString())
            binding.toggleMode.check(initialCheckedButtonId)
            binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    updateUIForMode(checkedId)
                    homeViewModel.updateSelectedMode(checkedId)
                }
            }

//            // In onViewCreated or onCreateView method
            homeViewModel.isPowerOn.observe(viewLifecycleOwner) {
                val initialPowerState = getCheckedButtonId(selectedLamp.mode)
                binding.switchPower.isChecked = selectedLamp.isPowerOn
            }

            // Set the switch listener
//            binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
//                setPowerState(isChecked)
//                homeViewModel.updatePowerState(isChecked)
//            }




            // Call the function to set up schedule time listeners
//            setupScheduleTimeListeners(selectedLamp)


        }
    }

//    private fun setupScheduleTimeListeners(selectedLamp: LampModel) {
//        // Schedule Time From
//        binding.textScheduleTimeFrom.text = selectedLamp.scheduleFrom
//        binding.textScheduleTimeFrom.setOnClickListener {
//            isTime = "Select Start Time"
//            openTimePicker { selectedTime ->
//                binding.textScheduleTimeFrom.text = selectedTime
//                homeViewModel.updateScheduleFrom(selectedTime)
//            }
//        }
//
//        // Schedule Time To
//        binding.textScheduleTimeTo.text = selectedLamp.scheduleTo
//        binding.textScheduleTimeTo.setOnClickListener {
//            isTime = "Select Finish Time"
//            openTimePicker { selectedTime ->
//                binding.textScheduleTimeTo.text = selectedTime
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
        }
    }

    private fun startFloatingAnimation(imageView: ImageView, interpolator: TimeInterpolator) {
        val animator = ObjectAnimator.ofFloat(imageView, "translationY", -24f, 24f)
        animator.interpolator = interpolator
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.duration = 5000

        // Start the animation
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
        // Adjust this function to control the effect of the distance on the rotation
        return 1 / (1 + distance / 100)
    }
}