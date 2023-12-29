package com.ihdyo.smarthome.ui.profile

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.factory.AuthViewModelFactory
import com.ihdyo.smarthome.data.factory.MainViewModelFactory
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel
import com.ihdyo.smarthome.data.viewmodel.MainViewModel
import com.ihdyo.smarthome.databinding.FragmentProfileBinding
import com.ihdyo.smarthome.ui.splash.SplashActivity
import com.ihdyo.smarthome.utils.ModalBottomSheet

class ProfileFragment : Fragment(), ModalBottomSheet.BottomSheetListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MainRepository(FirebaseFirestore.getInstance()))
        )[MainViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(FirebaseAuth.getInstance()))
        )[AuthViewModel::class.java]

        // Verification
        binding.buttonVerification.setOnClickListener {
            emailVerification()
        }

        // Change Username
        binding.buttonChangeUsername.setOnClickListener {
            changeUsername()
        }

        // Change Email
        binding.buttonChangeEmail.setOnClickListener {
            changeEmail()
        }

        // Change Password
        binding.buttonChangePassword.setOnClickListener {
            changePassword()
        }

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


    // ========================= GET DIALOG ========================= //

    override fun onTextEntered(title: String, text: String) {
        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {

                // Change Username
                if (title == getString(R.string.text_change_username)) {
                    mainViewModel.updateUserName(currentUser.uid, text)
                    Snackbar.make(binding.root, R.string.prompt_change_username_success, Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.prompt_ok)) { }
                        .show()
                }

                // Change Email
                if (title == getString(R.string.text_change_email)) {
                    Snackbar.make(binding.root, "Belom implementasi, malas", Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.prompt_ok)) { }
                        .show()
                }

                // Change Password
                if (title == getString(R.string.text_change_password)) {
                    Snackbar.make(binding.root, "Belom implementasi, malas", Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.prompt_ok)) { }
                        .show()
                }
            }
        }
    }


    // ========================= EMAIL VERIFICATION ========================= //

    private fun emailVerification() {
        authViewModel.requestEmailVerification()
        authViewModel.currentUser.observe(viewLifecycleOwner) { updatedUser ->

            if (updatedUser?.isEmailVerified == true) {

                Snackbar.make(binding.root, R.string.prompt_email_verification_success, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_ok)) { }
                    .show()
                binding.buttonVerification.visibility = View.GONE

            } else {

                Snackbar.make(binding.root, R.string.prompt_email_verification_failed, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_close)) { }
                    .show()
                binding.buttonVerification.visibility = View.VISIBLE

            }
        }
    }


    // ========================= CHANGE USERNAME ========================= //

    private fun changeUsername() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            getString(R.string.text_change_username),
            getString(R.string.hint_new_username),
            TextInputLayout.END_ICON_NONE,
            R.drawable.bx_user,
            android.text.InputType.TYPE_CLASS_TEXT,
            getString(R.string.text_change_username)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= CHANGE EMAIL ========================= //

    private fun changeEmail() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            getString(R.string.text_change_email),
            getString(R.string.hint_old_email),
            TextInputLayout.END_ICON_NONE,
            R.drawable.bx_envelope,
            android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            getString(R.string.prompt_next)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= CHANGE PASSWORD ========================= //

    private fun changePassword() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            getString(R.string.text_change_password),
            getString(R.string.hint_old_password),
            TextInputLayout.END_ICON_PASSWORD_TOGGLE,
            R.drawable.bx_lock_alt,
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD,
            getString(R.string.prompt_next)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= LOG OUT ========================= //

    private fun showLogOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.bx_log_out_circle)
            .setTitle(resources.getString(R.string.prompt_logout))
            .setMessage(resources.getString(R.string.prompt_logout_check))
            .setNeutralButton(resources.getString(R.string.prompt_cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.prompt_logout)) { _, _ ->

                authViewModel.signOut()

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