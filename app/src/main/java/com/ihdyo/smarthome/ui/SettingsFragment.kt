package com.ihdyo.smarthome.ui

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.AppPreferences
import com.ihdyo.smarthome.data.factory.AdminViewModelFactory
import com.ihdyo.smarthome.data.factory.AuthViewModelFactory
import com.ihdyo.smarthome.data.factory.MainViewModelFactory
import com.ihdyo.smarthome.data.repository.AdminRepository
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.ui.viewmodel.AdminViewModel
import com.ihdyo.smarthome.ui.viewmodel.AuthViewModel
import com.ihdyo.smarthome.ui.viewmodel.MainViewModel
import com.ihdyo.smarthome.databinding.FragmentSettingsBinding
import com.ihdyo.smarthome.utils.AppInfo
import com.ihdyo.smarthome.utils.Const
import com.ihdyo.smarthome.utils.Const.ARG_CHANGE_EMAIL
import com.ihdyo.smarthome.utils.Const.ARG_CHANGE_PASSWORD
import com.ihdyo.smarthome.utils.Const.ARG_CHANGE_USERNAME
import com.ihdyo.smarthome.utils.Const.ARG_CHECK_EMAIL
import com.ihdyo.smarthome.utils.Const.ARG_CHECK_PASSWORD
import com.ihdyo.smarthome.utils.Const.ARG_RECHECK_PASSWORD
import com.ihdyo.smarthome.utils.Const.DEFAULT
import com.ihdyo.smarthome.utils.Const.LOCALE_ENGLISH
import com.ihdyo.smarthome.utils.Const.LOCALE_INDONESIA
import com.ihdyo.smarthome.utils.Const.LOCALE_JAVANESE
import com.ihdyo.smarthome.utils.Const.WEB_CLIENT_ID
import com.ihdyo.smarthome.utils.ModalBottomSheet
import com.ihdyo.smarthome.utils.ProgressBarLayout
import com.ihdyo.smarthome.utils.Vibration
import kotlinx.coroutines.launch
import java.util.Locale

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.O)
class SettingsFragment : Fragment(), ModalBottomSheet.BottomSheetListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adminViewModel: AdminViewModel
    private lateinit var appPreferences: AppPreferences
    private lateinit var appInfo: AppInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        appPreferences = AppPreferences(requireContext())
        appInfo = AppInfo(requireContext())

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MainRepository(FirebaseFirestore.getInstance()))
        )[MainViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(FirebaseAuth.getInstance()))
        )[AuthViewModel::class.java]

        adminViewModel = ViewModelProvider(
            this,
            AdminViewModelFactory(AdminRepository(FirebaseFirestore.getInstance()))
        )[AdminViewModel::class.java]

        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressLinear.visibility = View.VISIBLE

        // App Info
        binding.textAppName.text = appInfo.getAppName()
        binding.textAppVersion.text = "${getString(R.string.app_version)} ${appInfo.getAppVersion()}"


        // ========================= LIVE DATA ========================= //

        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                mainViewModel.setCurrentUserId(currentUser.uid)

                // Button Verification View
                authViewModel.isVerified()
                authViewModel.isCurrentUserVerified.observe(viewLifecycleOwner) { isVerified ->
                    if (isVerified == true) {
                        binding.buttonVerification.visibility = View.GONE
                    } else {
                        binding.buttonVerification.visibility = View.VISIBLE
                    }
                }

                // Lamp Power Consumption & Average
                mainViewModel.fetchTotalPowerConsumption()
                mainViewModel.totalPowerConsumptionLiveData.observe(viewLifecycleOwner) { totalPowerConsumption ->
                    if (totalPowerConsumption != null) {
                        binding.textValueMonthlyPowerConsumption.text = "${totalPowerConsumption}${getString(R.string.text_power_unit)}"
                    }
                }
                mainViewModel.averageLampConsumptionLiveData.observe(viewLifecycleOwner) { averagePowerConsumption ->
                    if (averagePowerConsumption != null) {
                        binding.progressLinear.visibility = View.GONE
                        binding.textValueAveragePowerConsumption.text = "${averagePowerConsumption}${getString(R.string.text_power_unit)}"

                        val category = when {
                            averagePowerConsumption < 50 -> getString(R.string.text_power_consumption_average_very_low)
                            averagePowerConsumption in 50..100 -> getString(R.string.text_power_consumption_average_low)
                            averagePowerConsumption in 100..200 -> getString(R.string.text_power_consumption_average_moderate)
                            averagePowerConsumption in 200..300 -> getString(R.string.text_power_consumption_average_high)
                            else -> getString(R.string.text_power_consumption_average_very_high)
                        }

                        binding.textCategory.text = category
                    }
                }
            }
        }


        // TODO("Implement Notification")
        // ========================= NOTIFICATION ========================= //

        // Push Notification
//        binding.switchPushNotification.setOnCheckedChangeListener { _, isChecked ->
//            appPreferences.isPushNotificationOn = isChecked
//        }

        // Email Notification
//        binding.switchSubscribeNewsletter.setOnCheckedChangeListener { _, isChecked ->
//            appPreferences.isSubscribeNewsletterOn = isChecked
//        }

        // >>>>> DELETE THIS <<<<<

        binding.iconPushNotification.alpha = 0.5F
        binding.textPushNotification.alpha = 0.5F
        binding.switchPushNotification.isEnabled = false
        binding.wrapperPushNotification.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.text_upcoming_feature), Toast.LENGTH_SHORT)
                .show()
        }
        binding.iconSubscribeNewsletter.alpha = 0.5F
        binding.textSubscribeNewsletter.alpha = 0.5F
        binding.switchSubscribeNewsletter.isEnabled = false
        binding.wrapperSubscribeNewsletter.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.text_upcoming_feature), Toast.LENGTH_SHORT)
                .show()
        }

        // >>>>> DELETE THIS <<<<<


        // ========================= APPLICATION ========================= //


        // Dynamic Color
        binding.switchDynamicColor.isChecked = appPreferences.isDynamicColorOn
        binding.switchDynamicColor.setOnCheckedChangeListener { _, isChecked ->
            if (DynamicColors.isDynamicColorAvailable()) {
                appPreferences.isDynamicColorOn = isChecked
                Snackbar.make(binding.root, getString(R.string.prompt_dynamic_color_success), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_ok)) { }
                    .show()
            } else {
                Snackbar.make(binding.root, getString(R.string.prompt_dynamic_color_failed), Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_close)) { }
                    .show()
                appPreferences.isDynamicColorOn = false
            }
        }

        // Themes
        binding.wrapperThemes.setOnClickListener {
            Vibration.vibrate(requireContext())
            showThemesMenu(it)
        }
        binding.textCurrentThemes.text = when (appPreferences.selectedTheme) {
            AppPreferences.ThemeList.LIGHT -> resources.getString(R.string.item_theme_light)
            AppPreferences.ThemeList.DARK -> resources.getString(R.string.item_theme_dark)
            AppPreferences.ThemeList.DEFAULT -> resources.getString(R.string.item_default)
        }

        // Languages
        binding.wrapperLanguages.setOnClickListener {
            Vibration.vibrate(requireContext())
            showLanguagesMenu(it)
        }
        binding.textCurrentLanguages.text = when (appPreferences.selectedLanguage) {
            AppPreferences.LanguageList.ENGLISH -> LOCALE_ENGLISH
            AppPreferences.LanguageList.INDONESIA -> LOCALE_INDONESIA
            AppPreferences.LanguageList.JAVANESE -> LOCALE_JAVANESE
            AppPreferences.LanguageList.DEFAULT -> resources.getString(R.string.item_default)
        }


        // ========================= PROFILE ========================= //

        // Verification
        binding.buttonVerification.visibility = View.VISIBLE
        binding.buttonVerification.setOnClickListener {
            Vibration.vibrate(requireContext())
            emailVerification()
        }

        // Change Username
        binding.wrapperChangeUsername.setOnClickListener {
            Vibration.vibrate(requireContext())
            changeUsername()
        }

        // Change Email
        binding.wrapperChangeEmail.setOnClickListener {
            Vibration.vibrate(requireContext())
            checkEmail()
        }

        // Change Password
        binding.wrapperChangePassword.setOnClickListener {
            Vibration.vibrate(requireContext())
            checkPassword()
        }

        // Log Out
        binding.buttonLogout.setOnClickListener {
            Vibration.vibrate(requireContext())
            showLogOutDialog()
        }


        // ========================= CALL CENTER ========================= //

        binding.textCallCustomerService.setOnClickListener {
            Vibration.vibrate(requireContext())

            adminViewModel.fetchAdmin()
            adminViewModel.adminLiveData.observe(viewLifecycleOwner) { admin ->
                if (admin != null) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${admin.adminNumber}"))
                    startActivity(intent)
                }
            }
        }

        // GitHub
        binding.textDevName.setOnClickListener {
            val url = Const.SMART_HOME_GITHUB_LINK
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        _binding = null
    }


    // ========================= GET DIALOG ========================= //

    override fun onTextEntered(arg: String, text: String) {
        authViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                mainViewModel.setCurrentUserId(currentUser.uid)

                when (arg) {

                    // Change Username
                    ARG_CHANGE_USERNAME -> {
                        mainViewModel.updateUserName(text)
                        Snackbar.make(binding.root, R.string.prompt_change_username_success, Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.prompt_ok)) { }
                            .show()
                        ProgressBarLayout.hideLoading()
                    }

                    // Check Email
                    ARG_CHECK_EMAIL -> {
                        authViewModel.reAuth(currentUser.email.orEmpty(), text)
                        authViewModel.reAuthResult.observe(viewLifecycleOwner) { result ->
                            if (result) {
                                changeEmail()
                            } else {
                                Snackbar.make(binding.root, getString(R.string.prompt_password_incorrect), Snackbar.LENGTH_SHORT)
                                    .setAction(getString(R.string.prompt_ok)) { }
                                    .show()
                            }
                            ProgressBarLayout.hideLoading()
                        }
                    }

                    // Change Email
                    ARG_CHANGE_EMAIL -> {
                        lifecycleScope.launch {
                            try {
                                val success = authViewModel.changeEmail(text)

                                if (success) {
                                    Snackbar.make(binding.root, getString(R.string.prompt_change_email_verify), Snackbar.LENGTH_SHORT)
                                        .setAction(getString(R.string.prompt_ok)) { }
                                        .show()
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.prompt_change_email_failed), Toast.LENGTH_SHORT).show()
                                }
                                ProgressBarLayout.hideLoading()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error changing email: ${e.message}")
                            }
                        }
                    }

                    // Check Password
                    ARG_CHECK_PASSWORD -> {
                        authViewModel.reAuth(currentUser.email.orEmpty(), text)
                        authViewModel.reAuthResult.observe(viewLifecycleOwner) { result ->
                            if (result) {
                                recheckPassword()
                            } else {
                                Snackbar.make(binding.root, getString(R.string.prompt_password_incorrect), Snackbar.LENGTH_SHORT)
                                    .setAction(getString(R.string.prompt_ok)) { }
                                    .show()
                            }
                            ProgressBarLayout.hideLoading()
                        }
                    }

                    // Recheck Password
                    ARG_RECHECK_PASSWORD -> {
                        authViewModel.checkPassword(text)
                        changePassword()
                        ProgressBarLayout.hideLoading()
                    }

                    // Change Password
                    ARG_CHANGE_PASSWORD -> {
                        if (authViewModel.changePassword.value == text) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setIcon(R.drawable.bx_lock_alt)
                                .setTitle(resources.getString(R.string.prompt_change_password))
                                .setMessage(resources.getString(R.string.prompt_change_password_check))
                                .setNegativeButton(resources.getString(R.string.prompt_cancel)) { _, _ -> }
                                .setPositiveButton(resources.getString(R.string.text_change_password)) { _, _ ->
                                    authViewModel.changePassword(text)
                                }
                                .show()
                        }
                        else {
                            Snackbar.make(binding.root, getString(R.string.prompt_change_password_new_not_match), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.prompt_retry)) {
                                    changePassword()
                                }
                                .show()
                        }
                        ProgressBarLayout.hideLoading()
                    }

                }
            }
        }
    }


    // ========================= EMAIL VERIFICATION ========================= //

    private fun emailVerification() {
        authViewModel.getCurrentUser()
        authViewModel.requestEmailVerification()

        Snackbar.make(binding.root, R.string.prompt_email_verification_sending, Snackbar.LENGTH_SHORT)
            .setAction(getString(R.string.prompt_ok)) { }
            .show()
    }


    // ========================= CHANGE USERNAME ========================= //

    private fun changeUsername() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_CHANGE_USERNAME,
            getString(R.string.text_change_username),
            getString(R.string.hint_new_username),
            TextInputLayout.END_ICON_NONE,
            R.drawable.bx_user,
            android.text.InputType.TYPE_CLASS_TEXT,
            getString(R.string.text_change_username)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= CHANGE EMAIL ========================= //

    private fun checkEmail() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_CHECK_EMAIL,
            getString(R.string.text_change_email),
            getString(R.string.hint_old_password),
            TextInputLayout.END_ICON_PASSWORD_TOGGLE,
            R.drawable.bx_lock_alt,
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD,
            getString(R.string.prompt_next)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    private fun changeEmail() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_CHANGE_EMAIL,
            getString(R.string.text_change_email),
            getString(R.string.hint_new_email),
            TextInputLayout.END_ICON_NONE,
            R.drawable.bx_envelope,
            android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            getString(R.string.text_change_email)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= CHANGE PASSWORD ========================= //

    private fun checkPassword() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_CHECK_PASSWORD,
            getString(R.string.text_change_password),
            getString(R.string.hint_old_password),
            TextInputLayout.END_ICON_PASSWORD_TOGGLE,
            R.drawable.bx_lock_alt,
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD,
            getString(R.string.prompt_next)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    private fun recheckPassword() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_RECHECK_PASSWORD,
            getString(R.string.text_change_password),
            getString(R.string.hint_new_password),
            TextInputLayout.END_ICON_PASSWORD_TOGGLE,
            R.drawable.bx_lock_alt,
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD,
            getString(R.string.prompt_next)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }

    private fun changePassword() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_CHANGE_PASSWORD,
            getString(R.string.text_change_password),
            getString(R.string.hint_retype_password),
            TextInputLayout.END_ICON_PASSWORD_TOGGLE,
            R.drawable.bx_lock_alt,
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD,
            getString(R.string.text_change_password)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= LOG OUT ========================= //

    private fun showLogOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.bx_log_out_circle)
            .setTitle(resources.getString(R.string.prompt_logout))
            .setMessage(resources.getString(R.string.prompt_logout_check))
            .setNeutralButton(resources.getString(R.string.prompt_cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.prompt_logout)) { _, _ ->

                val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
                googleSignInClient.signOut()
                authViewModel.signOut()

                val animationBundle = ActivityOptions.makeCustomAnimation(
                    requireActivity(),
                    R.anim.no_animation,
                    R.anim.no_animation
                ).toBundle()

                startActivity(Intent(requireContext(), SplashActivity::class.java), animationBundle)
                requireActivity().finishAffinity()
            }
            .show()
    }


    // ========================= THEME ========================= //

    @SuppressLint("RestrictedApi", "ObsoleteSdkInt")
    private fun showThemesMenu(view: View) {
        val popup = PopupMenu(requireContext(), view, Gravity.END)
        popup.menuInflater.inflate(R.menu.menu_themes, popup.menu)

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12.toFloat(), resources.displayMetrics).toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                    } else {
                        item.icon =
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.theme_light -> {
                    setSelectedTheme(AppPreferences.ThemeList.LIGHT)
                    binding.textCurrentThemes.text = getString(R.string.item_theme_light)
                    true
                }
                R.id.theme_dark -> {
                    setSelectedTheme(AppPreferences.ThemeList.DARK)
                    binding.textCurrentThemes.text = getString(R.string.item_theme_dark)
                    true
                }
                R.id.theme_default -> {
                    setSelectedTheme(AppPreferences.ThemeList.DEFAULT)
                    binding.textCurrentThemes.text = getString(R.string.item_default)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setSelectedTheme(selectedTheme: AppPreferences.ThemeList) {
        appPreferences.selectedTheme = selectedTheme

        when (selectedTheme) {
            AppPreferences.ThemeList.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            AppPreferences.ThemeList.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            AppPreferences.ThemeList.DEFAULT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        recreateActivity()
    }


    // ========================= LANGUAGES ========================= //

    private fun showLanguagesMenu(view: View) {
        val popup = PopupMenu(requireContext(), view, Gravity.END)
        popup.menuInflater.inflate(R.menu.menu_languages, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.language_english -> {
                    setSelectedLanguage(AppPreferences.LanguageList.ENGLISH)
                    binding.textCurrentLanguages.text = LOCALE_ENGLISH
                    true
                }
                R.id.language_bahasa -> {
                    setSelectedLanguage(AppPreferences.LanguageList.INDONESIA)
                    binding.textCurrentLanguages.text = LOCALE_INDONESIA
                    true
                }
                R.id.language_javanese -> {
                    setSelectedLanguage(AppPreferences.LanguageList.JAVANESE)
                    binding.textCurrentLanguages.text = LOCALE_JAVANESE
                    true
                }
                R.id.language_default -> {
                    setSelectedLanguage(AppPreferences.LanguageList.DEFAULT)
                    binding.textCurrentLanguages.text = getString(R.string.item_default)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setSelectedLanguage(selectedLanguage: AppPreferences.LanguageList) {
        appPreferences.selectedLanguage = selectedLanguage

        when (selectedLanguage) {
            AppPreferences.LanguageList.ENGLISH -> {
                setLocale(LOCALE_ENGLISH)
            }
            AppPreferences.LanguageList.INDONESIA -> {
                setLocale(LOCALE_INDONESIA)
            }
            AppPreferences.LanguageList.JAVANESE -> {
                setLocale(LOCALE_JAVANESE)
            }
            AppPreferences.LanguageList.DEFAULT -> {
                setLocale(DEFAULT)
            }
        }

        recreateActivity()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = requireActivity().resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }


    // ========================= OTHER FUNCTION ========================= //

    private fun recreateActivity() {
        val animationBundle = ActivityOptions.makeCustomAnimation(
            requireActivity(),
            R.anim.no_animation,
            R.anim.no_animation
        ).toBundle()

        startActivity(Intent(requireContext(), MainActivity::class.java), animationBundle)
        requireActivity().finishAffinity()
    }

    companion object {
        const val TAG = "SettingsFragment"
    }

}