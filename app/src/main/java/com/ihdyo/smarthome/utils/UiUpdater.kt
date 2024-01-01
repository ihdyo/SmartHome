package com.ihdyo.smarthome.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ihdyo.smarthome.R

class UiUpdater {

    private var floatingValuesTop = 24f
    private var floatingValuesBot = -24f
    private var floatingDuration = 8000L
    private var translateInitialX = 0F
    private val translateSensitivity = 1f
    private val translationDuration = 500L


    // ========================= ROOM IMAGE ========================= //

    fun startFloatingAnimation(imageView: ImageView) {
        val animator = ObjectAnimator.ofFloat(imageView, "translationY", floatingValuesBot, floatingValuesTop)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.duration = floatingDuration

        animator.start()
    }

    fun startTranslateTouch(imageView: ImageView, swipeRefresh: SwipeRefreshLayout, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                swipeRefresh.isEnabled = false
                translateInitialX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - translateInitialX

                if (deltaX > 0) {
                    val distance = deltaX
                    val weight = calculateWeight(distance)
                    val translationX = deltaX * translateSensitivity * weight

                    imageView.translationX = translationX
                }
            }
            MotionEvent.ACTION_UP -> {
                swipeRefresh.isEnabled = true
                val translationAnimator = ObjectAnimator.ofFloat(imageView, "translationX", 0f)
                translationAnimator.duration = translationDuration
                translationAnimator.start()
            }
        }
        return true
    }

    private fun calculateWeight(distance: Float): Float {
        return 1 / (1 + distance / 250)
    }


    // ========================= STYLE ITEM ========================= //

    fun updateIconRoomState(context: Context, iconRoom: ImageView, isActive: Boolean) {
        val resources = context.resources
        val newHeight = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_5xl) else resources.getDimensionPixelSize(R.dimen.sp_4xl)
        val newWidth = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_5xl) else resources.getDimensionPixelSize(R.dimen.sp_4xl)
        val padding = if (isActive) R.dimen.sp_lg else R.dimen.sp_sm
        val elevation = if (isActive) 36f else 0f

        val heightAnimator = ObjectAnimator.ofInt(iconRoom.layoutParams.height, newHeight)
        heightAnimator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = iconRoom.layoutParams
            layoutParams.height = animatedValue
            iconRoom.layoutParams = layoutParams
        }

        val widthAnimator = ObjectAnimator.ofInt(iconRoom.layoutParams.width, newWidth)
        widthAnimator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = iconRoom.layoutParams
            layoutParams.width = animatedValue
            iconRoom.layoutParams = layoutParams
        }

        if (isActive) {
            iconRoom.setBackgroundResource(R.drawable.shape_squircle_md)
        } else {
            iconRoom.setBackgroundResource(R.drawable.shape_squircle_lg)
        }

        val paddingAnimator = resources.getDimensionPixelSize(padding)
        iconRoom.setPadding(paddingAnimator, paddingAnimator, paddingAnimator, paddingAnimator)

        val elevationAnimator = ObjectAnimator.ofFloat(iconRoom, "elevation", iconRoom.elevation, elevation)

        val iconColor = if (isActive) getThemeColor(context, com.google.android.material.R.attr.colorSurface) else getThemeColor(context, com.google.android.material.R.attr.colorPrimary)
        val backgroundColor = if (isActive) getThemeColor(context, com.google.android.material.R.attr.colorPrimary) else getThemeColor(context, com.google.android.material.R.attr.colorSurfaceVariant)

        iconRoom.imageTintList = ColorStateList.valueOf(iconColor)
        iconRoom.backgroundTintList = ColorStateList.valueOf(backgroundColor)

        val animatorSet = AnimatorSet()
        animatorSet.duration = 150
        animatorSet.playTogether(heightAnimator, widthAnimator, elevationAnimator)
        animatorSet.start()

    }


    fun updateIconLampState(context: Context, iconLamp: ImageView, isActive: Boolean) {
        val resources = context.resources
        val newHeight = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_2xl) else resources.getDimensionPixelSize(R.dimen.sp_lg)
        val newWidth = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_2xl) else resources.getDimensionPixelSize(R.dimen.sp_lg)

        val heightAnimator = ObjectAnimator.ofInt(iconLamp.layoutParams.height, newHeight)
        heightAnimator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = iconLamp.layoutParams
            layoutParams.height = animatedValue
            iconLamp.layoutParams = layoutParams
        }

        val widthAnimator = ObjectAnimator.ofInt(iconLamp.layoutParams.width, newWidth)
        widthAnimator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Int
            val layoutParams = iconLamp.layoutParams
            layoutParams.width = animatedValue
            iconLamp.layoutParams = layoutParams
        }

        if (isActive) {
            iconLamp.setImageResource(R.drawable.bx_bulb)
        } else {
            iconLamp.setImageResource(0)
        }

        val iconColor = if (isActive) getThemeColor(context, com.google.android.material.R.attr.colorSurface) else getThemeColor(context, com.google.android.material.R.attr.colorPrimary)
        val backgroundColor = if (isActive) getThemeColor(context, com.google.android.material.R.attr.colorPrimary) else getThemeColor(context, com.google.android.material.R.attr.colorSurfaceVariant)

        iconLamp.imageTintList = ColorStateList.valueOf(iconColor)
        iconLamp.backgroundTintList = ColorStateList.valueOf(backgroundColor)

        val animatorSet = AnimatorSet()
        animatorSet.duration = 50
        animatorSet.playTogether(heightAnimator, widthAnimator)
        animatorSet.start()
    }


    // ========================= OTHER FUNCTION ========================= //

    fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }

}