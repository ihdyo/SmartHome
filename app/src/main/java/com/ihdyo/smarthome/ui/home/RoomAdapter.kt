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
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.RoomModel

class RoomAdapter(
    private var items: List<RoomModel>,
    private val onItemClickListener: (RoomModel) -> Unit,
    private val homeViewModel: HomeViewModel
    ): RecyclerView.Adapter<RoomAdapter.ItemViewHolder>() {

    private var activePosition: Int = RecyclerView.NO_POSITION

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(item: List<RoomModel>) {
        this.items = item
        notifyDataSetChanged()
    }

    fun setInitialSelectedIndex(index: Int) {
        if (index >= 0 && index < items.size) {
            activePosition = index
            notifyItemChanged(activePosition)
            val selectedRoom = items[activePosition]
            onItemClickListener(selectedRoom)
            homeViewModel.setSelectedRoom(selectedRoom)
        }
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
                    if (position != activePosition) {
                        val previousActivePosition = activePosition
                        activePosition = position

                        notifyItemChanged(previousActivePosition)
                        notifyItemChanged(activePosition)

                        val selectedRoom = items[position]
                        onItemClickListener(selectedRoom)
                        homeViewModel.setSelectedRoom(selectedRoom)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: RoomModel, position: Int) {
            currentPosition = position
            val isActive = position == activePosition

            iconRoom.load(item.roomIcon) {
                placeholder(R.drawable.bx_landscape)
                error(R.drawable.bx_error)
                crossfade(true)
                decoder(SvgDecoder(itemView.context))
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            updateButtonState(isActive)

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