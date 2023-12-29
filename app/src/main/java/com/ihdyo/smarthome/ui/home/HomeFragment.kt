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
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel
import com.ihdyo.smarthome.data.viewmodel.MainViewModel
import com.ihdyo.smarthome.databinding.FragmentHomeBinding
import com.ihdyo.smarthome.utils.Const.LAMP_SELECTED_MODE_AUTOMATIC
import com.ihdyo.smarthome.utils.Const.LAMP_SELECTED_MODE_MANUAL
import com.ihdyo.smarthome.utils.Const.LAMP_SELECTED_MODE_SCHEDULE
import com.ihdyo.smarthome.utils.UiUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var lampAdapter: LampAdapter
    private lateinit var uiUpdater: UiUpdater

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var isRefreshTriggeredManually: Boolean? = false
    private var UID: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        uiUpdater = UiUpdater()

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // ========================= INITIATE VIEW MODEL WITH UID ========================= //

        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            UID = currentUser?.uid.toString()

            mainViewModel.fetchUser(UID)
            mainViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
                val fullName = user?.userName
                val firstName = fullName?.split(" ")?.firstOrNull()
                binding.textUsername.text = firstName
            }

            mainViewModel.fetchRooms(UID)
            mainViewModel.roomsLiveData.observe(viewLifecycleOwner) { rooms ->
                if (rooms != null) {
                    initRoomRecyclerView(rooms)
                }
            }
        }


        // ========================= BASIC VIEW ========================= //

        // Progress Bar
        binding.progressLinear.visibility = View.VISIBLE

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
            refreshHomeFragment()
        }

        // Prevent Nested Swipe
        binding.sliderLampBrightness.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.swipeRefresh.isEnabled = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.swipeRefresh.isEnabled = true
                }
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshHomeFragment() {
        lifecycleScope.launch {
            delay(300)
            isRefreshTriggeredManually = true
            findNavController().navigate(R.id.nav_home)
            binding.swipeRefresh.isRefreshing = false
        }
    }


    // ========================= INITIATE RECYCLERVIEW ========================= //

    private fun initRoomRecyclerView(rooms: List<RoomModel>) {
        if (!::roomAdapter.isInitialized) {
            roomAdapter = RoomAdapter(rooms, { selectedRoom -> showRoomProperties(selectedRoom) }, mainViewModel)
            binding.rvIconRoom.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvIconRoom.adapter = roomAdapter
            roomAdapter.setInitialSelectedIndex(0)
        } else {
            roomAdapter.setItems(rooms)
        }
    }

    private fun initLampRecyclerView(lamps: List<LampModel>) {
        if (!::lampAdapter.isInitialized) {
            lampAdapter = LampAdapter(lamps, { selectedLamp -> showLampProperties(selectedLamp) }, mainViewModel)
            binding.rvIconLamp.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvIconLamp.adapter = lampAdapter
            lampAdapter.setInitialSelectedIndex(0)
        } else {
            lampAdapter.setItems(lamps)
        }
    }

    // ========================= SHOW ROOM PROPERTIES ========================= //

    @SuppressLint("SetTextI18n")
    private fun showRoomProperties(selectedRoom: RoomModel) {

        mainViewModel.lampsLiveData.observe(viewLifecycleOwner) { lamps ->
            if (lamps != null) {
                initLampRecyclerView(lamps)
            }
        }
        mainViewModel.fetchLamps(UID, selectedRoom.RID.toString())


        // Room Name
        val roomName = selectedRoom.roomName
        binding.textRoom.text = roomName
        binding.textRoomDecoration.text = roomName

        // Room Floor
        binding.textRoomFloor.text = "${selectedRoom.roomFloor}F"

        // Room Image
        binding.imageRoom.load(selectedRoom.roomImage) {
            placeholder(R.drawable.shape_placeholder)
            error(R.drawable.bx_landscape)
            crossfade(true)
            memoryCachePolicy(CachePolicy.ENABLED)
        }
    }


    // ========================= SHOW LAMP PROPERTIES ========================= //

    private fun showLampProperties(selectedLamp: LampModel) {

        // Power Consumed
        mainViewModel.powerConsumedLiveData.observe(viewLifecycleOwner) { powerConsumedMap ->
            val powerConsumed = powerConsumedMap[selectedLamp.LID]
            val formattedText = "${powerConsumed}Wh"
            binding.textPowerConsumed.text = formattedText
        }

        // Total Power Consumed
        mainViewModel.totalPowerConsumedLiveData.observe(viewLifecycleOwner) { totalPowerConsumedMap ->
            val formattedText = "${totalPowerConsumedMap}Wh"
            binding.textTotalPowerConsumed.text = formattedText
        }

        // Lamp Brightness
        mainViewModel.lampBrightnessLiveData.observe(viewLifecycleOwner) { brightness ->
            binding.sliderLampBrightness.value = brightness.toFloat()
        }

        // Lamp Switch Power
        mainViewModel.lampIsPowerOnLiveData.observe(viewLifecycleOwner) { isPowerOn ->
            binding.switchPower.isChecked = isPowerOn
        }

        // Lamp Schedule
        mainViewModel.lampScheduleLiveData.observe(viewLifecycleOwner) { schedule ->
            binding.textScheduleFrom.text = schedule.scheduleFrom
            binding.textScheduleTo.text = schedule.scheduleTo
        }

        binding.progressLinear.visibility = View.GONE

        // Update
        updateLampProperties()

    }


    // ========================= UPDATE LAMP PROPERTIES ========================= //

    private fun updateLampProperties() {
        mainViewModel.selectedRoom.observe(viewLifecycleOwner) { pair ->
            mainViewModel.selectedLamp.observe(viewLifecycleOwner) { selectedLamp ->

                // TEST
                binding.textTest.text = "${pair.second}, ${selectedLamp.LID}"



                // Lamp Brightness
                binding.sliderLampBrightness.addOnChangeListener { _, value, fromUser ->
                    if (fromUser) {
                        mainViewModel.updateLampBrightness(UID, pair.second.toString(), selectedLamp.LID.toString(), value.toInt())
                    }
                }

                // Lamp Switch Power
                binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
                    selectedLamp.lampIsPowerOn = isChecked
                    mainViewModel.updateLampIsPowerOn(UID, pair.second.toString(), selectedLamp.LID.toString(), isChecked)
                }

                // Lamp Selected Mode
                mainViewModel.lampSelectedModeLiveData.observe(viewLifecycleOwner) { selectedMode ->
                    getButtonState(pair.first, selectedLamp, selectedMode)
                }
                binding.buttonAutomatic.setOnClickListener {
                    mainViewModel.updateLampSelectedMode(UID, pair.second.toString(), selectedLamp.LID.toString(), LAMP_SELECTED_MODE_AUTOMATIC)
                }
                binding.buttonSchedule.setOnClickListener {
                    mainViewModel.updateLampSelectedMode(UID, pair.second.toString(), selectedLamp.LID.toString(), LAMP_SELECTED_MODE_SCHEDULE)
                }
                binding.buttonManual.setOnClickListener {
                    mainViewModel.updateLampSelectedMode(UID, pair.second.toString(), selectedLamp.LID.toString(), LAMP_SELECTED_MODE_MANUAL)
                }

                // Lamp Schedule
                binding.textScheduleFrom.setOnClickListener {
                    openTimePicker(getString(R.string.text_schedule_title_start)) { selectedTime ->
                        val newSchedule = mainViewModel.lampScheduleLiveData.value?.copy(scheduleFrom = selectedTime)
                        newSchedule?.let {
                            mainViewModel.updateLampSchedule(UID, pair.second.toString(), selectedLamp.LID.toString(), it)
                        }
                    }
                }
                binding.textScheduleTo.setOnClickListener {
                    openTimePicker(getString(R.string.text_schedule_title_start)) { selectedTime ->
                        val newSchedule = mainViewModel.lampScheduleLiveData.value?.copy(scheduleTo = selectedTime)
                        newSchedule?.let {
                            mainViewModel.updateLampSchedule(UID, pair.second.toString(), selectedLamp.LID.toString(), it)

                        }
                    }
                }

            }
        }
    }


    // ========================= OTHER FUNCTION ========================= //

    private fun getButtonState(selectedRoom: RoomModel, selectedLamp: LampModel, selectedMode: String) {
        val isAutomatic = when (selectedMode) {
            LAMP_SELECTED_MODE_AUTOMATIC -> {
                mainViewModel.sensorValueLiveData.observe(viewLifecycleOwner) { value ->
                    mainViewModel.updateLampIsPowerOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), value)
                }
                mainViewModel.fetchEnvironments(UID)
                true
            }
            LAMP_SELECTED_MODE_SCHEDULE -> {
                mainViewModel.lampScheduleLiveData.observe(viewLifecycleOwner) { schedule ->
                    updatePowerStateIfInSchedule(selectedRoom, selectedLamp, schedule.scheduleFrom!!, schedule.scheduleTo!!)
                }
                false
            }
            LAMP_SELECTED_MODE_MANUAL -> false
            else -> return
        }

        mainViewModel.updateLampIsAutomaticOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), isAutomatic)

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

    private fun updatePowerStateIfInSchedule(selectedRoom: RoomModel, selectedLamp: LampModel, scheduleFrom: String, scheduleTo: String) {
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
            binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
                selectedLamp.lampIsPowerOn = isChecked
                mainViewModel.updateLampIsPowerOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), isChecked)
            }
            mainViewModel.updateLampIsPowerOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), true)
        } else {
            binding.switchPower.setOnCheckedChangeListener { _, isChecked ->
                selectedLamp.lampIsPowerOn = isChecked
                mainViewModel.updateLampIsPowerOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), isChecked)
            }
            mainViewModel.updateLampIsPowerOn(UID, selectedRoom.RID.toString(), selectedLamp.LID.toString(), false)
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
            in 0..5 -> getString(R.string.text_greeting_night)
            in 6..11 -> getString(R.string.text_greeting_morning)
            in 12..17 -> getString(R.string.text_greeting_afternoon)
            else -> getString(R.string.text_greeting_evening)
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
        }
    }

}