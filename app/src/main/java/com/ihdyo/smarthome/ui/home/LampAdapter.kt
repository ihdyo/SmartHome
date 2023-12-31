package com.ihdyo.smarthome.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.LampModel
import com.ihdyo.smarthome.data.viewmodel.MainViewModel
import com.ihdyo.smarthome.utils.UiUpdater

class LampAdapter(
    private var items: List<LampModel>,
    private val onItemClickListener: (LampModel) -> Unit,
    private val mainViewModel: MainViewModel
    ): RecyclerView.Adapter<LampAdapter.ItemViewHolder>() {

    private var activePosition: Int = RecyclerView.NO_POSITION
    private var uiUpdater: UiUpdater = UiUpdater()

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

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(item: List<LampModel>) {
        this.items = emptyList()
        this.items = item
        notifyDataSetChanged()
    }

    fun setInitialSelectedIndex(index: Int) {
        if (index >= 0 && index < items.size) {
            activePosition = index
            notifyItemChanged(activePosition)
            val selectedLamp = items[activePosition]
            onItemClickListener(selectedLamp)
            mainViewModel.setSelectedLamp(selectedLamp, selectedLamp.LID)
        }
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
                        mainViewModel.setSelectedLamp(selectedLamp, selectedLamp.LID)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            currentPosition = position
            val isActive = position == activePosition

            uiUpdater.updateIconLampState(itemView.context, iconLamp, isActive)
        }

    }

}