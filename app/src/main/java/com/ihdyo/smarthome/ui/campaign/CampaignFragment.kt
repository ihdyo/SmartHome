package com.ihdyo.smarthome.ui.campaign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.factory.CampaignViewModelFactory
import com.ihdyo.smarthome.data.repository.CampaignRepository
import com.ihdyo.smarthome.data.viewmodel.CampaignViewModel
import com.ihdyo.smarthome.databinding.FragmentCampaignBinding

class CampaignFragment : Fragment() {

    private var _binding: FragmentCampaignBinding? = null
    private val binding get() = _binding!!

    private lateinit var campaignAdapter: CampaignAdapter
    private lateinit var campaignViewModel: CampaignViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCampaignBinding.inflate(inflater, container, false)

        campaignViewModel = ViewModelProvider(
            this,
            CampaignViewModelFactory(CampaignRepository(FirebaseFirestore.getInstance()))
        )[CampaignViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressLinear.visibility = View.VISIBLE

        campaignViewModel.fetchAllCampaign()
        campaignViewModel.campaignLiveData.observe(viewLifecycleOwner) { campaign ->
            if (campaign != null) {
                binding.progressLinear.visibility = View.GONE
                campaignAdapter.setItems(ArrayList(campaign))
            }
        }

        initCampaignRecyclerView()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initCampaignRecyclerView() {
        campaignAdapter = CampaignAdapter(requireActivity())
        binding.rvCampaign.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvCampaign.adapter = campaignAdapter
    }
}