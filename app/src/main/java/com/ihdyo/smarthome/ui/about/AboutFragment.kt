package com.ihdyo.smarthome.ui.about

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.factory.TeamViewModelFactory
import com.ihdyo.smarthome.data.repository.TeamRepository
import com.ihdyo.smarthome.data.viewmodel.TeamViewModel
import com.ihdyo.smarthome.databinding.FragmentAboutBinding
import com.ihdyo.smarthome.ui.splash.SplashActivity
import com.ihdyo.smarthome.utils.Const
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

        initTeamRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTeamItemClick(teamId: String) {
        Vibration.vibrate(requireContext())
        teamViewModel.setCurrentTeamId(teamId)

        teamViewModel.fetchTeamById(teamId)
        teamViewModel.teamLiveData.observe(viewLifecycleOwner) { team ->
            if (team != null) {
                val clickedTeam = team.find { it.TID == teamId }

                if (clickedTeam != null) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.bx_link_external)
                        .setTitle("Open Another Apps")
                        .setMessage("Are you sure want to open another apps?")
                        .setNeutralButton(resources.getString(R.string.prompt_cancel)) { _, _ -> }
                        .setPositiveButton(resources.getString(R.string.prompt_ok)) { _, _ ->

                            val url = clickedTeam.teamSocial
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "This member hides their social", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun initTeamRecyclerView() {
        teamAdapter = TeamAdapter(this)
        binding.rvTeam.layoutManager = CarouselLayoutManager()
        binding.rvTeam.adapter = teamAdapter
    }

}