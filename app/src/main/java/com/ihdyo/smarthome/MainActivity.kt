package com.ihdyo.smarthome

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.ihdyo.smarthome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreferences = AppPreferences(this)

        // Navigation Drawer
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
            R.id.action_theme -> {
                showThemeSubMenu(item)
                return true
            }
            R.id.action_light_theme -> {
                setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                return true
            }
            R.id.action_dark_theme -> {
                setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                return true
            }
            R.id.action_default_theme -> {
                setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

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

    private fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        appPreferences.isDarkModeEnabled = mode == AppCompatDelegate.MODE_NIGHT_YES
        recreate()
    }
}