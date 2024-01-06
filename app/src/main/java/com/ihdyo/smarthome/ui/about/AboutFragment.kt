package com.ihdyo.smarthome.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.data.factory.TeamViewModelFactory
import com.ihdyo.smarthome.data.repository.TeamRepository
import com.ihdyo.smarthome.data.viewmodel.TeamViewModel
import com.ihdyo.smarthome.databinding.FragmentAboutBinding
import com.ihdyo.smarthome.utils.Vibration

class AboutFragment : Fragment(), TeamAdapter.OnItemClickListener {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private lateinit var teamViewModel: TeamViewModel
    private lateinit var teamAdapter: TeamAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        teamViewModel = ViewModelProvider(
            this,
            TeamViewModelFactory(TeamRepository(FirebaseFirestore.getInstance()))
        )[TeamViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressLinear.visibility = View.VISIBLE

        teamViewModel.fetchAllTeam()
        teamViewModel.teamLiveData.observe(viewLifecycleOwner) { team ->
            if (team != null) {
                binding.progressLinear.visibility = View.GONE
                teamAdapter.setItems(ArrayList(team))
            }
        }

        teamViewModel.currentTeamLiveData.observe(viewLifecycleOwner) { currentTeamId ->
            if (currentTeamId != null) {
                showTeamProperties(currentTeamId)
            }
        }

        initTeamRecyclerView()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTeamItemClick(teamId: String) {
        Vibration.vibrate(requireContext())
        teamViewModel.setCurrentTeamId(teamId)
    }

    private fun initTeamRecyclerView() {
        teamAdapter = TeamAdapter(this)
        binding.rvTeam.layoutManager = CarouselLayoutManager()
        binding.rvTeam.adapter = teamAdapter
    }

    private fun showTeamProperties(currentTeamId: String) {
        val selectedTeam = teamViewModel.fetchTeamById(currentTeamId)
        if (selectedTeam != null) {

            // HERE

        }
    }

}