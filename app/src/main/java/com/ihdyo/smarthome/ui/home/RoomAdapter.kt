package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.RoomModel
import com.ihdyo.smarthome.databinding.ItemRoomBinding
import com.ihdyo.smarthome.utils.UiUpdater

class RoomAdapter(private var listener: OnItemClickListener ) : RecyclerView.Adapter<RoomAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onRoomItemClick(roomId: String)
    }

    private val items = ArrayList<RoomModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<RoomModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemRoomBinding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    inner class ItemViewHolder(private val itemBinding: ItemRoomBinding, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {

        private lateinit var room: RoomModel
        private var isItemClicked: Boolean = false
        private val uiUpdater: UiUpdater = UiUpdater()

        init {
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(item: RoomModel) {
            this.room = item

            itemBinding.iconRoom.load(item.roomIcon) {
                placeholder(R.drawable.bx_landscape)
                error(R.drawable.bx_error)
                crossfade(true)
                decoder(SvgDecoder(itemView.context))
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            uiUpdater.updateIconRoomState(itemView.context, itemBinding.iconRoom, isItemClicked)
        }

        override fun onClick(v: View?) {
            isItemClicked = !isItemClicked
            bind(room)
            listener.onRoomItemClick(room.RID!!)
        }
    }
}