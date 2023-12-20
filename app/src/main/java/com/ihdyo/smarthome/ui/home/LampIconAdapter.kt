package com.ihdyo.smarthome.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.LampModel

class LampIconAdapter(private var items: List<LampModel>, private val onItemClickListener: (LampModel) -> Unit, private val lampViewModel: HomeViewModel) : RecyclerView.Adapter<LampIconAdapter.ItemViewHolder>() {

    private var activePosition: Int = RecyclerView.NO_POSITION

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<LampModel>) {
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
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Check if a different item is clicked
                    if (position != activePosition) {
                        // Update the active position
                        val previousActivePosition = activePosition
                        activePosition = position

                        // Notify the adapter to rebind the items at the previous and current positions
                        notifyItemChanged(previousActivePosition)
                        notifyItemChanged(activePosition)

                        // Handle item click
                        val selectedLamp = items[position]
                        onItemClickListener(selectedLamp)
                        lampViewModel.setSelectedLamp(selectedLamp)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: LampModel, position: Int) {
            currentPosition = position
            val isActive = position == activePosition
            updateButtonState(isActive)

            // Load image using Glide
            iconRoom.load(item.roomIcon) {
                placeholder(R.drawable.bx_landscape)
                error(R.drawable.bx_error)
                crossfade(true)
                decoder(SvgDecoder(itemView.context))
                memoryCachePolicy(CachePolicy.ENABLED)
            }

//            lampViewModel.loadLampImage(item.roomIcon!!)

            val colorFilter = if (isActive) getThemeColor(com.google.android.material.R.attr.colorOnPrimary) else getThemeColor(com.google.android.material.R.attr.colorPrimary)
            iconRoom.imageTintList = ColorStateList.valueOf(colorFilter)
        }

        private fun updateButtonState(isActive: Boolean) {
            val resources = itemView.resources
            val newHeight = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_5xl) else resources.getDimensionPixelSize(R.dimen.sp_4xl)
            val newWidth = if (isActive) resources.getDimensionPixelSize(R.dimen.sp_5xl) else resources.getDimensionPixelSize(R.dimen.sp_4xl)
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
            val elevationAnimator = ObjectAnimator.ofFloat(iconRoom, "elevation", iconRoom.elevation, elevation)

            val animatorSet = AnimatorSet()
            animatorSet.duration = 0
            animatorSet.playTogether(heightAnimator, widthAnimator, elevationAnimator)
            animatorSet.start()

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