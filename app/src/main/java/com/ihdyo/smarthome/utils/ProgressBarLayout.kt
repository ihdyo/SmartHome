package com.ihdyo.smarthome.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.ihdyo.smarthome.R

object ProgressBarLayout {

    private var dialog: Dialog? = null

    // ========================= PROGRESS BAR ========================= //

    @SuppressLint("InflateParams")
    fun showLoading(activity: Activity){
        val dialogView = activity.layoutInflater.inflate(R.layout.helper_progress_bar, null, false)

        dialog = Dialog(activity)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setContentView(dialogView)

        dialog?.show()
    }

    fun hideLoading(){
        dialog?.dismiss()
    }
}