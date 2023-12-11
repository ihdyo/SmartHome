package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.model.IconModel

class HomeAdapterIcon(private var items: List<IconModel>) : RecyclerView.Adapter<HomeAdapterIcon.ItemViewHolder>() {
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
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconRoom: ImageButton = itemView.findViewById(R.id.icon_room)

        init {
            iconRoom.setOnClickListener {
                iconRoom.setBackgroundResource(R.drawable.shape_squircle_active)
                val resources = itemView.resources
                val newHeight = resources.getDimensionPixelSize(R.dimen.sp_5xl)
                val newWidth = resources.getDimensionPixelSize(R.dimen.sp_5xl)
                val layoutParams = iconRoom.layoutParams
                layoutParams.height = newHeight
                layoutParams.width = newWidth
                iconRoom.layoutParams = layoutParams
                iconRoom.elevation = 4f
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: IconModel) {
            iconRoom.setImageResource(item.icon)
        }
    }
}