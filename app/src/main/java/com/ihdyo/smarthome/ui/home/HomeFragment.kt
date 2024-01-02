package com.ihdyo.smarthome.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.factory.AuthViewModelFactory
import com.ihdyo.smarthome.data.factory.MainViewModelFactory
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel
import com.ihdyo.smarthome.data.viewmodel.MainViewModel
import com.ihdyo.smarthome.databinding.FragmentHomeBinding
import com.ihdyo.smarthome.utils.Const.LAMP_SELECTED_MODE_AUTOMATIC
import com.ihdyo.smarthome.utils.Const.LAMP_SELECTED_MODE_MANUAL
import com.ihdyo.smarthome.utils.Const.LAMP_SELECTED_MODE_SCHEDULE
import com.ihdyo.smarthome.utils.UiUpdater
import com.ihdyo.smarthome.utils.Vibration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment : Fragment(), RoomAdapter.OnItemClickListener, LampAdapter.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var lampAdapter: LampAdapter

    private val uiUpdater: UiUpdater = UiUpdater()
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var isRefreshTriggeredManually: Boolean? = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MainRepository(FirebaseFirestore.getInstance()))
        )[MainViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(FirebaseAuth.getInstance()))
        )[AuthViewModel::class.java]

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // ========================= INITIATE VIEW MODEL WITH UID ========================= //

        // User Data
        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                mainViewModel.setCurrentUserId(currentUser.uid)

                mainViewModel.fetchUser()
                mainViewModel.fetchRooms()
                mainViewModel.fetchEnvironments()
                mainViewModel.fetchTotalPowerConsumption()
            }
        }

        mainViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val userName = user.userName
                val firstName = userName?.split(" ")?.firstOrNull()
                binding.textUsername.text = firstName
            }
        }

        // Rooms Data
        mainViewModel.roomsLiveData.observe(viewLifecycleOwner) { rooms ->
            if (rooms != null) {
                binding.progressLinear.visibility = View.GONE
                binding.chipRoom.visibility = View.VISIBLE
                roomAdapter.setItems(ArrayList(rooms))
            }
        }
        mainViewModel.currentRoomIdLiveData.observe(viewLifecycleOwner) { currentRoomId ->
            if (currentRoomId != null) {
                showRoomProperties(currentRoomId)
            }
        }

        // Total Power Consumption
        mainViewModel.totalPowerConsumptionLiveData.observe(viewLifecycleOwner) { totalPowerConsumption ->
            if (totalPowerConsumption != null) {
                binding.textValueTotalPowerConsumption.text = "${totalPowerConsumption}${getString(R.string.text_power_unit)}"
            }
        }

        // Recycler View Init
        initRoomRecyclerView()
        initLampRecyclerView()

        // Basic View
        basicView()
    }

    // Item Click
    override fun onRoomItemClick(roomId: String) {
        Vibration.vibrate(requireContext())
        binding.progressLinear.visibility = View.VISIBLE
        binding.chipRoom.visibility = View.GONE
        binding.chipLamp.visibility = View.VISIBLE
        mainViewModel.setCurrentRoomId(roomId)

        // Lamps Data
        mainViewModel.fetchLamps()
        mainViewModel.lampsLiveData.observe(viewLifecycleOwner) { lamps ->
            if (lamps != null) {
                binding.progressLinear.visibility = View.GONE
                lampAdapter.setItems(ArrayList(lamps))
            }
        }
        mainViewModel.currentLampIdLiveData.observe(viewLifecycleOwner) { currentLampId ->
            if (currentLampId != null) {
                showLampProperties(currentLampId)
            }
        }
    }

    override fun onLampItemClick(lampId: String) {
        Vibration.vibrate(requireContext())
        binding.progressLinear.visibility = View.VISIBLE
        mainViewModel.setCurrentLampId(lampId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // ========================= BASIC VIEWS ========================= //

    @SuppressLint("ClickableViewAccessibility")
    private fun basicView() {

        // Show Views
        showViews(isRoomActive = false, isLampActive = false)
        binding.progressLinear.visibility = View.VISIBLE
        binding.chipRoom.visibility = View.GONE
        binding.chipLamp.visibility = View.GONE

        // Get Current Time
        getCurrentTime { formattedTime ->
            binding.textGreeting.text = formattedTime
        }

        // Get Last Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation { city ->
            binding.textCity.text = city
        }

        // Image Animator
        uiUpdater.startFloatingAnimation(binding.imageRoom)
        binding.imageRoom.setOnTouchListener { _, event ->
            uiUpdater.startTranslateTouch(binding.imageRoom, binding.swipeRefresh, event)
        }

        // Swipe Refresh
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                delay(300)
                isRefreshTriggeredManually = true
                findNavController().navigate(R.id.nav_home)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        // Prevent Nested Swipe
        binding.sliderLampBrightness.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Vibration.vibrate(requireContext())
                    binding.swipeRefresh.isEnabled = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.swipeRefresh.isEnabled = true
                }
            }
            false
        }
    }

    // Show Views
    private fun showViews(isRoomActive: Boolean, isLampActive: Boolean) {
        val roomViewsToToggle = listOf(
            binding.textRoom,
            binding.textRoomDecoration,
            binding.textRoomFloor,
            binding.imageRoom
        )

        val lampViewsToToggle = listOf(
            binding.textValuePowerConsumption,
            binding.sliderLampBrightness,
            binding.switchPower,
            binding.textFrom,
            binding.textScheduleFrom,
            binding.textTo,
            binding.textScheduleTo,
            binding.toggleMode
        )

        roomViewsToToggle.forEach { view ->
            view.visibility = if (isRoomActive) View.VISIBLE else View.GONE
        }

        lampViewsToToggle.forEach { view ->
            view.visibility = if (isLampActive) View.VISIBLE else View.GONE
        }
    }



    // ========================= INITIATE RECYCLERVIEW ========================= //

    private fun initRoomRecyclerView() {
        roomAdapter = RoomAdapter(this)
        binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvIconRoom.adapter = roomAdapter
    }

    private fun initLampRecyclerView() {
        lampAdapter = LampAdapter(this)
        binding.rvIconLamp.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvIconLamp.adapter = lampAdapter
    }

    // ========================= SHOW ROOM PROPERTIES ========================= //

    @SuppressLint("SetTextI18n")
    private fun showRoomProperties(currentRoomId: String) {

        // Show View
        showViews(isRoomActive = true, isLampActive = false)

        val selectedRoom = mainViewModel.fetchRoomById(currentRoomId)
        if (selectedRoom != null) {

            // Room Name
            val roomName = selectedRoom.roomName
            binding.textRoom.text = roomName
            binding.textRoomDecoration.text = roomName

            // Room Floor
            binding.textRoomFloor.text = "${selectedRoom.roomFloor}F"

            // Room Image
            binding.imageRoom.load(selectedRoom.roomImage) {
                error(R.drawable.bx_landscape)
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
            }
        }
    }


    // ========================= SHOW LAMP PROPERTIES ========================= //

    @SuppressLint("SetTextI18n")
    private fun showLampProperties(currentLampId: String) {
        binding.progressLinear.visibility = View.GONE
        binding.chipLamp.visibility = View.GONE

        // Show Views
        showViews(isRoomActive = true, isLampActive = true)

        // Power Consumption
        mainViewModel.powerConsumptionLiveData.observe(viewLifecycleOwner) { powerConsumptionMap ->
            if (powerConsumptionMap != null) {
                val powerConsumption = powerConsumptionMap[currentLampId]
                binding.textValuePowerConsumption.text = "${powerConsumption}${getString(R.string.text_power_unit)}"
            }
        }

        // Lamp Brightness
        mainViewModel.lampBrightnessLiveData.observe(viewLifecycleOwner) { brightnessMap ->
            if (brightnessMap != null) {
                val brightness = brightnessMap[currentLampId]
                if (brightness != null) {
                    binding.sliderLampBrightness.value = brightness.toFloat()
                }
            }
        }
        binding.sliderLampBrightness.addOnChangeListener { _, value, fromUser ->
            val lampBrightnessMap = mapOf(currentLampId to value.toInt())
            if (fromUser) {
                mainViewModel.updateLampBrightness(lampBrightnessMap)
            }
        }

        // Lamp Switch Power
        mainViewModel.lampIsPowerOnLiveData.observe(viewLifecycleOwner) { isPowerOnMap ->
            val isPowerOn = isPowerOnMap?.get(currentLampId)
            binding.switchPower.isChecked = isPowerOn == true
        }
        binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
            val lampIsPowerOnMap = mapOf(currentLampId to isChecked)
            mainViewModel.updateLampIsPowerOn(lampIsPowerOnMap)
        }

        // Lamp Selected Mode
        mainViewModel.lampSelectedModeLiveData.observe(viewLifecycleOwner) { selectedModeMap ->
            if (selectedModeMap != null) {
                val lampSelectedMode = selectedModeMap[currentLampId]
                if (lampSelectedMode != null) {
                    getButtonState(currentLampId, lampSelectedMode)
                }
            }
        }
        binding.buttonAutomatic.setOnClickListener {
            Vibration.vibrate(requireContext())
            val setAutomatic = mutableMapOf(currentLampId to LAMP_SELECTED_MODE_AUTOMATIC)
            mainViewModel.updateLampSelectedMode(setAutomatic)
        }
        binding.buttonSchedule.setOnClickListener {
            Vibration.vibrate(requireContext())
            val setSchedule = mutableMapOf(currentLampId to LAMP_SELECTED_MODE_SCHEDULE)
            mainViewModel.updateLampSelectedMode(setSchedule)
        }
        binding.buttonManual.setOnClickListener {
            Vibration.vibrate(requireContext())
            val setManual = mutableMapOf(currentLampId to LAMP_SELECTED_MODE_MANUAL)
            mainViewModel.updateLampSelectedMode(setManual)
        }

        // Lamp Schedule
        mainViewModel.lampScheduleLiveData.observe(viewLifecycleOwner) { scheduleMap ->
            if (scheduleMap != null) {
                val lampSchedule = scheduleMap[currentLampId]
                if (lampSchedule != null) {
                    binding.textScheduleFrom.text = lampSchedule.scheduleFrom
                    binding.textScheduleTo.text = lampSchedule.scheduleTo
                }
            }
        }
        binding.textScheduleFrom.setOnClickListener {
            openTimePicker(getString(R.string.text_schedule_title_start)) { selectedTime ->
                val currentLampSchedule = mainViewModel.lampScheduleLiveData.value?.get(currentLampId)
                if (currentLampSchedule != null) {
                    mainViewModel.updateScheduleFrom(currentLampId, selectedTime)
                }
            }
        }
        binding.textScheduleTo.setOnClickListener {
            openTimePicker(getString(R.string.text_schedule_title_finish)) { selectedTime ->
                val currentLampSchedule = mainViewModel.lampScheduleLiveData.value?.get(currentLampId)
                if (currentLampSchedule != null) {
                    mainViewModel.updateScheduleTo(currentLampId, selectedTime)
                }
            }
        }
    }


    // ========================= OTHER FUNCTION ========================= //

    private fun getButtonState(currentLampId: String, selectedMode: String) {

        val isAutomatic = when (selectedMode) {
            LAMP_SELECTED_MODE_AUTOMATIC -> {
                true
            }
            LAMP_SELECTED_MODE_SCHEDULE -> {
                mainViewModel.lampScheduleLiveData.observe(viewLifecycleOwner) { scheduleMap ->
                    if (scheduleMap != null) {
                        val lampSchedule = scheduleMap[currentLampId]
                        if (lampSchedule != null) {
                            updatePowerStateIfInSchedule(currentLampId, lampSchedule.scheduleFrom!!, lampSchedule.scheduleTo!!)
                        }
                    }
                }
                false
            }
            LAMP_SELECTED_MODE_MANUAL -> false
            else -> return
        }

        val lampIsAutomaticOnMap = mapOf(currentLampId to isAutomatic)
        mainViewModel.updateLampIsAutomaticOn(lampIsAutomaticOnMap)

        binding.toggleMode.check(
            when (selectedMode) {
                LAMP_SELECTED_MODE_AUTOMATIC -> R.id.button_automatic
                LAMP_SELECTED_MODE_SCHEDULE -> R.id.button_schedule
                LAMP_SELECTED_MODE_MANUAL -> R.id.button_manual
                else -> return
            }
        )

        binding.switchPower.isEnabled = selectedMode == LAMP_SELECTED_MODE_MANUAL
        binding.textFrom.alpha = if (selectedMode == LAMP_SELECTED_MODE_SCHEDULE) 1F else 0.5F
        binding.textScheduleFrom.isEnabled = selectedMode == LAMP_SELECTED_MODE_SCHEDULE
        binding.textTo.alpha = if (selectedMode == LAMP_SELECTED_MODE_SCHEDULE) 1F else 0.5F
        binding.textScheduleTo.isEnabled = selectedMode == LAMP_SELECTED_MODE_SCHEDULE
    }

    private fun updatePowerStateIfInSchedule(currentLampId: String, scheduleFrom: String, scheduleTo: String) {

        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val scheduleFromDateTime = LocalDateTime.of(currentTime.toLocalDate(), LocalTime.parse(scheduleFrom, formatter))
        val scheduleToDateTime = LocalDateTime.of(
            if (LocalTime.parse(scheduleTo, formatter).isBefore(LocalTime.parse(scheduleFrom, formatter))) {
                currentTime.toLocalDate().plusDays(1)
            } else {
                currentTime.toLocalDate()
            },
            LocalTime.parse(scheduleTo, formatter)
        )
        if (currentTime.isAfter(scheduleFromDateTime) && currentTime.isBefore(scheduleToDateTime)) {
            val lampIsPowerOn = mapOf(currentLampId to true)
            mainViewModel.updateLampIsPowerOn(lampIsPowerOn)
            binding.switchPower.isChecked = true
        } else {
            val lampIsPowerOn = mapOf(currentLampId to false)
            mainViewModel.updateLampIsPowerOn(lampIsPowerOn)
            binding.switchPower.isChecked = false
        }
    }

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
            Vibration.vibrate(requireContext())
            val hour = picker.hour
            val minute = picker.minute

            val formattedHour = String.format("%02d", hour)
            val formattedMinute = String.format("%02d", minute)

            val selectedTime = "$formattedHour:$formattedMinute"
            callback(selectedTime)
        }
    }

    private fun getCurrentTime(callback: (String) -> Unit) {
        val currentTime = Calendar.getInstance()
        val greeting = when (currentTime.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> getString(R.string.text_greeting_night) + " "
            in 6..11 -> getString(R.string.text_greeting_morning) + " "
            in 12..17 -> getString(R.string.text_greeting_afternoon) + " "
            else -> getString(R.string.text_greeting_evening) + " "
        }
        callback(greeting)
    }

    @Suppress("DEPRECATION")
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
            binding.textCity.visibility = View.GONE
            callback(null)
        }
    }

}