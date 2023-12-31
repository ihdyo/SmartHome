package com.ihdyo.smarthome.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.databinding.ItemLampBinding
import com.ihdyo.smarthome.utils.UiUpdater

class LampAdapter(private var listener: OnItemClickListener) : RecyclerView.Adapter<LampAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onLampItemClick(lampId: String)
    }

    private val items = ArrayList<LampModel>()
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

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

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], position == selectedItemPosition)
    }

    inner class ItemViewHolder(private val itemBinding: ItemLampBinding, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {

        private lateinit var lamp: LampModel
        private val uiUpdater: UiUpdater = UiUpdater()

        init {
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(item: LampModel, isSelected: Boolean) {
            this.lamp = item
            uiUpdater.updateIconLampState(itemView.context, itemBinding.iconLamp, isSelected)
        }

        override fun onClick(v: View?) {
            val previousSelectedItemPosition = selectedItemPosition
            selectedItemPosition = adapterPosition

            notifyItemChanged(previousSelectedItemPosition)
            notifyItemChanged(selectedItemPosition)

            listener.onLampItemClick(lamp.LID!!)
        }
    }
}