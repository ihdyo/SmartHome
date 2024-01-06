package com.ihdyo.smarthome.ui.about

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.model.TeamModel
import com.ihdyo.smarthome.databinding.ItemTeamBinding

class TeamAdapter(private var listener: OnItemClickListener) : RecyclerView.Adapter<TeamAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onTeamItemClick(teamId: String)
    }

    private val items = ArrayList<TeamModel>()
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<TeamModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemTeamBinding = ItemTeamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(items[position])

    inner class ItemViewHolder(private val itemBinding: ItemTeamBinding, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemBinding.root),
        View.OnClickListener {

        private lateinit var team: TeamModel

        init {
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(item: TeamModel) {
            this.team = item

            itemBinding.imageTeam.load(item.teamImage) {
                error(R.drawable.bx_error)
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            itemBinding.textTeamName.text = item.teamName
            itemBinding.textTeamNumber.text = item.teamNumber
            itemBinding.textTeamRole.text = item.teamRole

        }

        override fun onClick(v: View?) {
            val previousSelectedItemPosition = selectedItemPosition
            selectedItemPosition = adapterPosition

            notifyItemChanged(previousSelectedItemPosition)
            notifyItemChanged(selectedItemPosition)

            listener.onTeamItemClick(team.TID!!)
        }
    }
}