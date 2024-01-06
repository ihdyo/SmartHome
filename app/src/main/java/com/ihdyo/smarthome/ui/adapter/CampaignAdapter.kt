package com.ihdyo.smarthome.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ihdyo.smarthome.data.model.CampaignModel
import com.ihdyo.smarthome.databinding.ItemCampaignBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener


class CampaignAdapter(private val context: Context) : RecyclerView.Adapter<CampaignAdapter.ItemViewHolder>() {

    private val items = ArrayList<CampaignModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: ArrayList<CampaignModel>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemCampaignBinding = ItemCampaignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ItemViewHolder(private val itemBinding: ItemCampaignBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        private lateinit var room: CampaignModel

        fun bind(item: CampaignModel) {
            this.room = item

            // -- Youtube --
            itemBinding.youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.cueVideo(item.campaignVideo.orEmpty(), 0F)
                }
            })

            itemBinding.textCampaignTitle.text = item.campaignTitle
            itemBinding.textCampaignSummary.text = item.campaignSummary

            itemBinding.buttonMore.setOnClickListener {
                val url = item.campaignLink
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        }
    }
}
