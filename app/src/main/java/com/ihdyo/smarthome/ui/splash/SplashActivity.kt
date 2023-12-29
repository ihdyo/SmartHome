package com.ihdyo.smarthome.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.factory.AuthFactory
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel
import com.ihdyo.smarthome.databinding.ActivitySplashBinding
import com.ihdyo.smarthome.ui.MainActivity
import com.ihdyo.smarthome.ui.login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var authViewModel: AuthViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textAppName.text = getAppName()
        binding.textAppVersion.text = "${getString(R.string.app_version)} ${getAppVersion()}"

        authViewModel = ViewModelProvider(
            this,
            AuthFactory(AuthRepository(FirebaseAuth.getInstance()))
        )[AuthViewModel::class.java]

        askPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionAndStartDelay()
            }
        }
    }


    // ========================= APP INFO ========================= //

    private fun getAppName(): String {
        val packageManager: PackageManager = packageManager
        val applicationInfo: ApplicationInfo = applicationInfo

        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    private fun getAppVersion(): String {
        val packageManager: PackageManager = packageManager
        val packageName: String = packageName

        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "N/A"
        }
    }


    // ========================= PERMISSION ========================= //

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }

    private fun checkPermissionAndStartDelay() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startDelay()
        }
    }


    // ========================= DELAY ========================= //

    private fun startDelay() {

        // Delay
        lifecycleScope.launch {
            delay(3000)
        }

        // Network Check
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }

        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {

            // Animation Bundle
            val animationBundle = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_top,
                R.anim.slide_out_bottom
            ).toBundle()

            // User Check
            authViewModel.getCurrentUser()
            authViewModel.currentUser.observe(this) { currentUser ->
                if (currentUser != null) {
                    startActivity(Intent(this, MainActivity::class.java), animationBundle)
                } else {
                    startActivity(Intent(this, LoginActivity::class.java), animationBundle)
                }
                finish()
            }

        } else {

            // Alert Dialog
            MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.bx_wifi_off)
                .setTitle(resources.getString(R.string.prompt_connection_no))
                .setMessage(resources.getString(R.string.prompt_connection_check))
                .setNeutralButton(resources.getString(R.string.prompt_connection_restart)) { _, _ ->
                    val animationBundle = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.no_animation,
                        R.anim.no_animation
                    ).toBundle()
                    startActivity(Intent(this, SplashActivity::class.java), animationBundle)
                    finish()
                }
                .setNegativeButton(resources.getString(R.string.prompt_cancel)) { _, _ ->
                    finishAffinity()
                }
                .setPositiveButton(resources.getString(R.string.prompt_connection_connect)) { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                    this.startActivity(settingsIntent)
                }
                .show()
        }
    }

    companion object {
        const val REQUEST_CODE = 100
    }

}