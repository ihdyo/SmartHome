package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.utils.UiUpdater

class RoomAdapter(
    private var items: List<RoomModel>,
    private val onItemClickListener: (RoomModel) -> Unit,
    private val homeViewModel: HomeViewModel
    ): RecyclerView.Adapter<RoomAdapter.ItemViewHolder>() {

    private var activePosition: Int = RecyclerView.NO_POSITION
    private var uiUpdater: UiUpdater = UiUpdater()

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
            homeViewModel.setSelectedRoom(selectedRoom, selectedRoom.RID)
        }
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
                        homeViewModel.setSelectedRoom(selectedRoom, selectedRoom.RID)
                    }
                }
            }
        }

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

            uiUpdater.updateIconRoomState(itemView.context, iconRoom, isActive)
        }

    }

}