package com.ihdyo.smarthome.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.model.IconModel

class HomeAdapterIcon(private var items: List<IconModel>) : RecyclerView.Adapter<HomeAdapterIcon.ItemViewHolder>() {

    private var activePosition: Int = RecyclerView.NO_POSITION

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<IconModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconRoom: ImageButton = itemView.findViewById(R.id.icon_room)
        private var currentPosition: Int = RecyclerView.NO_POSITION

        init {
            iconRoom.setOnClickListener {
                setActivePosition(currentPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: IconModel, position: Int) {
            currentPosition = position
            val isActive = position == activePosition
            updateButtonState(isActive)

            iconRoom.setImageResource(item.icon)

            // Set color filter based on active state
            val colorFilter = if (isActive) getThemeColor(com.google.android.material.R.attr.colorOnPrimary) else getThemeColor(com.google.android.material.R.attr.colorPrimary)
            iconRoom.imageTintList = ColorStateList.valueOf(colorFilter)
        }

        private fun setActivePosition(position: Int) {
            val previousActivePosition = activePosition
            activePosition = position
            notifyItemChanged(previousActivePosition)
            notifyItemChanged(activePosition)
        }

        private fun updateButtonState(isActive: Boolean) {
            val resources = itemView.resources
            val newHeight = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_5xl) else resources.getDimensionPixelSize(R.dimen.sp_4xl)
            val newWidth = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_5xl) else resources.getDimensionPixelSize(R.dimen.sp_4xl)
            val elevation = if (isActive) 36f else 0f

            // Animate size changes
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

            // Animate elevation changes
            val elevationAnimator = ObjectAnimator.ofFloat(iconRoom, "elevation", iconRoom.elevation, elevation)

            // Set up the animator duration and start them together
            val animatorSet = AnimatorSet()
            animatorSet.duration = 300 // Adjust the duration as needed
            animatorSet.playTogether(heightAnimator, widthAnimator, elevationAnimator)
            animatorSet.start()

            // Change the background resource
            if (isActive) iconRoom.setBackgroundResource(R.drawable.shape_squircle_active) else iconRoom.setBackgroundResource(R.drawable.shape_squircle_inactive)
        }

        private fun getThemeColor(attr: Int): Int {
            val typedValue = TypedValue()
            val theme = itemView.context.theme
            theme.resolveAttribute(attr, typedValue, true)
            return ContextCompat.getColor(itemView.context, typedValue.resourceId)
        }
    }
}