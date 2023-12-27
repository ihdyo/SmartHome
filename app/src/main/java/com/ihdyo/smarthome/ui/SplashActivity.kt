package com.ihdyo.smarthome.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.color.DynamicColors
import com.ihdyo.smarthome.MainActivity
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.databinding.ActivitySplashBinding
import com.ihdyo.smarthome.ui.home.HomeFragment

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 100
    }

    private lateinit var binding: ActivitySplashBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askPermission()
    }

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
        } else {
            // Handle the case where the permission is not granted
            // You may want to show a message or request the permission again
        }
    }

    private fun startDelay() {
        Handler().postDelayed({
            val connectivityManager =
                this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo

            if (activeNetwork?.isConnected == true) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.prompt_connection_no))
                builder.setMessage(getString(R.string.prompt_connection_check))
                builder.setPositiveButton(getString(R.string.prompt_connection_connect)) { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    this.startActivity(settingsIntent)
                }
                builder.setNegativeButton(getString(R.string.prompt_connection_cancel)) { _, _ -> }
                builder.show()
            }
        }, 800)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionAndStartDelay()
            } else {
                // Handle the case where the permission is not granted
                // You may want to show a message or request the permission again
            }
        }
    }
}