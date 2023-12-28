package com.ihdyo.smarthome.ui.profile

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.databinding.FragmentProfileBinding
import com.ihdyo.smarthome.ui.splash.SplashActivity
import com.ihdyo.smarthome.utils.Const

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()

        // Log Out
        binding.buttonLogout.setOnClickListener {
            showLogOutDialog()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLogOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.bx_log_out_circle)
            .setTitle(resources.getString(R.string.prompt_logout))
            .setMessage(resources.getString(R.string.prompt_logout_check))
            .setNeutralButton(resources.getString(R.string.prompt_cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.prompt_logout)) { _, _ ->
                val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                    Const.WEB_CLIENT_ID
                ).requestEmail().build()
                val googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

                auth.signOut()
                googleSignInClient.signOut()

                val animationBundle = ActivityOptions.makeCustomAnimation(
                    requireActivity(),
                    R.anim.no_animation,
                    R.anim.no_animation
                ).toBundle()

                startActivity(Intent(requireContext(), SplashActivity::class.java), animationBundle)
                requireActivity().finishAffinity()
            }
            .show()
    }
}