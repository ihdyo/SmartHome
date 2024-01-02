package com.ihdyo.smarthome.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

class AppInfo(private val context: Context) {

    fun getAppName(): String {
        val packageManager: PackageManager = context.packageManager
        val applicationInfo: ApplicationInfo = context.applicationInfo

        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    fun getAppVersion(): String {
        val packageManager: PackageManager = context.packageManager
        val packageName: String = context.packageName

        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "N/A"
        }
    }
}