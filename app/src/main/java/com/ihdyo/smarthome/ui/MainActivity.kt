package com.ihdyo.smarthome.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.AppPreferences
import com.ihdyo.smarthome.data.factory.AuthViewModelFactory
import com.ihdyo.smarthome.data.factory.MainViewModelFactory
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel
import com.ihdyo.smarthome.data.viewmodel.MainViewModel
import com.ihdyo.smarthome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var appPreferences: AppPreferences
    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreferences = AppPreferences(this)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MainRepository(FirebaseFirestore.getInstance()))
        )[MainViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(FirebaseAuth.getInstance()))
        )[AuthViewModel::class.java]


        // ========================= SET UP APP BAR ========================= //

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_tutorial, R.id.nav_campaign, R.id.nav_about
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Header
        val headerView = binding.navView.getHeaderView(0)
        val username = headerView.findViewById<TextView>(R.id.text_username)
        val email = headerView.findViewById<TextView>(R.id.text_email)
        val verified = headerView.findViewById<ImageView>(R.id.icon_verification)

        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(this) { currentUser ->
            if (currentUser != null) {

                mainViewModel.fetchUser()
                mainViewModel.userLiveData.observe(this) {user ->
                    if (user != null) {
                        username.text = user.userName
                    }
                }

                email.text = currentUser.email.toString()

                if (currentUser.isEmailVerified) {
                    verified.visibility = View.VISIBLE
                } else {
                    verified.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_profile)
                return true
            }
            R.id.action_theme -> {
                showThemeSubMenu(item)
                return true
            }
            R.id.action_language -> {
                showLanguageSubMenu(item)
                return true
            }
            R.id.action_light_theme -> {
                setThemeMode(AppPreferences.ThemeMode.LIGHT)
                return true
            }
            R.id.action_dark_theme -> {
                setThemeMode(AppPreferences.ThemeMode.DARK)
                return true
            }
            R.id.action_default_theme -> {
                setThemeMode(AppPreferences.ThemeMode.SYSTEM_DEFAULT)
                return true
            }
            R.id.action_english -> {
                setLanguage("en")
                return true
            }
            R.id.action_bahasa -> {
                setLanguage("in")
                return true
            }
            R.id.action_javanese -> {
                setLanguage("jv")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    // ========================= SET THEME ========================= //

    private fun showThemeSubMenu(item: MenuItem) {
        val view = findViewById<View>(R.id.action_theme)
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.app_bar_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_light_theme, R.id.action_dark_theme, R.id.action_default_theme -> {
                    onOptionsItemSelected(menuItem)
                }
            }
            true
        }
    }

    private fun setThemeMode(themeMode: AppPreferences.ThemeMode) {
        val appPreferences = AppPreferences(this)
        appPreferences.themeMode = themeMode
        updateTheme(themeMode)
    }

    private fun updateTheme(themeMode: AppPreferences.ThemeMode) {
        when (themeMode) {
            AppPreferences.ThemeMode.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            AppPreferences.ThemeMode.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            AppPreferences.ThemeMode.SYSTEM_DEFAULT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }


    // ========================= SET LANGUAGE ========================= //

    private fun showLanguageSubMenu(item: MenuItem) {
        val view = findViewById<View>(R.id.action_language)
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.app_bar_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_english, R.id.action_bahasa, R.id.action_javanese -> {
                    onOptionsItemSelected(menuItem)
                }
            }
            true
        }
    }

    private fun setLanguage(languageCode: String) {
        appPreferences.selectedLanguage = languageCode
    }

}