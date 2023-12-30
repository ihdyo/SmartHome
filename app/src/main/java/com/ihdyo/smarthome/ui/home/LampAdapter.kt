package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.databinding.ItemLampBinding
import com.ihdyo.smarthome.utils.UiUpdater

class LampAdapter(private var listener: OnItemClickListener, ) : RecyclerView.Adapter<LampAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onLampItemClick(lampId: String)
    }

    private var uiUpdater: UiUpdater = UiUpdater()

    private val items = ArrayList<LampModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<LampModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemLampBinding = ItemLampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    inner class ItemViewHolder(private val itemBinding: ItemLampBinding, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {

        private lateinit var lamp: LampModel

        init {
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(item: LampModel) {
            this.lamp = item

//            uiUpdater.updateIconLampState(itemView.context, itemBinding.iconLamp, isActive)
        }

        override fun onClick(v: View?) {
            listener.onLampItemClick(lamp.LID!!)
        }
    }
}