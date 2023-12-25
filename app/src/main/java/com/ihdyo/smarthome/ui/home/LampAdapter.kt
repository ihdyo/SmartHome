package com.ihdyo.smarthome.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.LampModel

class LampAdapter(
    private var items: List<LampModel>,
    private val onItemClickListener: (LampModel) -> Unit,
    private val homeViewModel: HomeViewModel
    ): RecyclerView.Adapter<LampAdapter.ItemViewHolder>() {

    private var activePosition: Int = RecyclerView.NO_POSITION

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(item: List<LampModel>) {
        this.items = item
        notifyDataSetChanged()
    }

    fun setInitialSelectedIndex(index: Int) {
        if (index >= 0 && index < items.size) {
            activePosition = index
            notifyItemChanged(activePosition)
            val selectedLamp = items[activePosition]
            onItemClickListener(selectedLamp)
            homeViewModel.setSelectedLamp(selectedLamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lamp, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconLamp: ImageView = itemView.findViewById(R.id.icon_lamp)
        private var currentPosition: Int = RecyclerView.NO_POSITION

        init {
            iconLamp.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (position != activePosition) {
                        val previousActivePosition = activePosition
                        activePosition = position

                        notifyItemChanged(previousActivePosition)
                        notifyItemChanged(activePosition)

                        val selectedLamp = items[position]
                        onItemClickListener(selectedLamp)
                        homeViewModel.setSelectedLamp(selectedLamp)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            currentPosition = position
            val isActive = position == activePosition

            updateButtonState(isActive)

            val colorFilter = if (isActive) getThemeColor(com.google.android.material.R.attr.colorSurface) else getThemeColor(com.google.android.material.R.attr.colorPrimary)
            iconLamp.imageTintList = ColorStateList.valueOf(colorFilter)
        }

        private fun updateButtonState(isActive: Boolean) {
            val resources = itemView.resources
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

            val animatorSet = AnimatorSet()
            animatorSet.duration = 0
            animatorSet.playTogether(heightAnimator, widthAnimator)
            animatorSet.start()

            if (isActive) {
                iconLamp.setBackgroundResource(R.drawable.shape_squircle_active)
                iconLamp.setImageResource(R.drawable.bx_bulb)
            } else {
                iconLamp.setBackgroundResource(R.drawable.shape_squircle_inactive)
                iconLamp.setImageResource(0)
            }
        }

        private fun getThemeColor(attr: Int): Int {
            val typedValue = TypedValue()
            val theme = itemView.context.theme
            theme.resolveAttribute(attr, typedValue, true)
            return ContextCompat.getColor(itemView.context, typedValue.resourceId)
        }
    }
}