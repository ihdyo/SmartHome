package com.ihdyo.smarthome.ui.login

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.data.factory.AdminViewModelFactory
import com.ihdyo.smarthome.data.factory.AuthViewModelFactory
import com.ihdyo.smarthome.data.factory.MainViewModelFactory
import com.ihdyo.smarthome.data.repository.AdminRepository
import com.ihdyo.smarthome.data.repository.AuthRepository
import com.ihdyo.smarthome.data.repository.MainRepository
import com.ihdyo.smarthome.data.viewmodel.AdminViewModel
import com.ihdyo.smarthome.data.viewmodel.AuthViewModel
import com.ihdyo.smarthome.data.viewmodel.MainViewModel
import com.ihdyo.smarthome.databinding.ActivityLoginBinding
import com.ihdyo.smarthome.ui.MainActivity
import com.ihdyo.smarthome.utils.Const.ARG_FORGOT_PASSWORD
import com.ihdyo.smarthome.utils.Const.RC_SIGN_IN
import com.ihdyo.smarthome.utils.Const.WEB_CLIENT_ID
import com.ihdyo.smarthome.utils.ModalBottomSheet
import com.ihdyo.smarthome.utils.ProgressBarLayout
import com.ihdyo.smarthome.utils.Vibration
import java.util.Base64


@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.O)
class LoginActivity : AppCompatActivity(), ModalBottomSheet.BottomSheetListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adminViewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MainRepository(FirebaseFirestore.getInstance()))
        )[MainViewModel::class.java]

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(AuthRepository(FirebaseAuth.getInstance()))
        )[AuthViewModel::class.java]

        adminViewModel = ViewModelProvider(
            this,
            AdminViewModelFactory(AdminRepository(FirebaseFirestore.getInstance()))
        )[AdminViewModel::class.java]

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        mainViewModel.fetchAllUsers()

        // Basic View
        basicView()

        // Google Sign In
        binding.buttonGoogle.setOnClickListener {
            Vibration.vibrate(this)
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }

        // Email Sign In
        binding.buttonLogin.setOnClickListener {
            Vibration.vibrate(this)
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (!validateEmailFormat(email)) {
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.textPassword.error = getString(R.string.prompt_empty_password)
                binding.textEmail.requestFocus()
                return@setOnClickListener
            }
            emailSignIn(email, password)
        }
    }


    // ========================= GET DIALOG ========================= //

    override fun onTextEntered(arg: String, text: String) {
        if (arg == ARG_FORGOT_PASSWORD) {
            authViewModel.requestPasswordReset(text)
            Snackbar.make(binding.root, "${getString(R.string.prompt_reset_password_check)} $text", Snackbar.LENGTH_SHORT)
                .setAction(getString(R.string.prompt_ok)) { }
                .show()
        }
    }


    // ========================= GOOGLE SIGN IN ========================= //

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            ProgressBarLayout.showLoading(this)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    signInWithGoogle(account.idToken.toString())
                }
            } catch (e: ApiException) {
                Snackbar.make(binding.root, R.string.prompt_auth_google_failed, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_ok)) { }
                    .show()
            }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        authViewModel.signInWithGoogle(
            idToken,
            onSuccess = { user ->
                ProgressBarLayout.hideLoading()
                checkRegistered(user)
            },
            onFailed = { errorMessage ->
                ProgressBarLayout.hideLoading()
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_ok)) { }
                    .show()
            }
        )
    }


    // ========================= EMAIL SIGN IN ========================= //

    private fun emailSignIn(email: String, password: String) {
        ProgressBarLayout.showLoading(this)

        authViewModel.signInWithEmail(email.trim(), password.trim(),
            onSuccess = { user ->
                checkRegistered(user)
            },
            onFailed = { errorMessage ->
                ProgressBarLayout.hideLoading()
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.prompt_ok)) { }
                    .show()
            }
        )
    }


    // ========================= CHECK IF USER REGISTERED ========================= //

    private fun checkRegistered(user: FirebaseUser?) {
        val registeredUsers = mainViewModel.allUsersLiveData.value

        if (user != null && !registeredUsers.isNullOrEmpty()) {
            Log.d(TAG, "Current user UID: ${user.uid}")

            try {
                val isRegistered = registeredUsers.any { it.UID == user.uid }

                if (isRegistered) {

                    // User is registered
                    Toast.makeText(this, getString(R.string.prompt_auth_success), Toast.LENGTH_SHORT).show()

                    val animationBundle = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_top,
                        R.anim.slide_out_bottom
                    ).toBundle()

                    ProgressBarLayout.hideLoading()

                    startActivity(Intent(this, MainActivity::class.java), animationBundle)
                    finish()
                } else {

                    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(WEB_CLIENT_ID)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

                    // User is not registered
                    ProgressBarLayout.hideLoading()
                    MaterialAlertDialogBuilder(this)
                        .setIcon(R.drawable.bx_error)
                        .setTitle(resources.getString(R.string.prompt_auth_failed))
                        .setMessage(resources.getString(R.string.prompt_auth_failed_check))
                        .setNeutralButton(resources.getString(R.string.prompt_close)) { _, _ ->
                            googleSignInClient.signOut()
                            authViewModel.signOut()
                            closeContextMenu()
                        }
                        .setPositiveButton(resources.getString(R.string.prompt_register)) { _, _ ->
                            googleSignInClient.signOut()
                            authViewModel.signOut()
                            registerNewMember()
                        }
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking user registration: ${e.message}", e)
            }
        } else {
            Log.e(TAG, "Invalid user or empty registered users list")
        }
    }





    // ========================= BASIC VIEW ========================= //

    private fun basicView() {

        // Register New Member
        binding.textRegister.setOnClickListener {
            registerNewMember()
            Vibration.vibrate(this)
        }

        // Forgot Password
        binding.textForgotPassword.setOnClickListener {
            forgotPassword()
            Vibration.vibrate(this)
        }

        // Scan Sign In
        binding.buttonScan.setOnClickListener {
            scanQRCode()
            Vibration.vibrate(this)
        }

        // Input Field Validation
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmailFormat(charSequence)
            }

            override fun afterTextChanged(editable: Editable?) { }
        })
    }

    private fun validateEmailFormat(email: CharSequence?): Boolean {
        if (email.isNullOrEmpty()) {
            binding.textEmail.error = getString(R.string.prompt_empty_email)
            binding.textEmail.requestFocus()
            return false
        }

        val isValid = isValidEmail(email.toString())
        if (!isValid) {
            binding.textEmail.error = getString(R.string.prompt_invalid_email)
            binding.textEmail.requestFocus()
        } else {
            binding.textEmail.error = null
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun forgotPassword() {
        val bottomSheetFragment = ModalBottomSheet.newInstance(
            ARG_FORGOT_PASSWORD,
            getString(R.string.text_forgot_password),
            getString(R.string.hint_email),
            TextInputLayout.END_ICON_NONE,
            R.drawable.bx_user,
            android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            getString(R.string.prompt_send)
        )

        bottomSheetFragment.setListener(this)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }


    // ========================= REGISTER NEW USER ========================= //

    @SuppressLint("QueryPermissionsNeeded")
    private fun registerNewMember() {
        ProgressBarLayout.showLoading(this)

        adminViewModel.fetchAdmin()
        adminViewModel.adminLiveData.observe(this) { admin ->
            if (admin != null) {
                val recipient = admin.adminEmail
                val subject = "${getString(R.string.app_name)}: ${admin.adminEmailSubject} (${admin.adminName})"
                val body = admin.adminEmailBody
                val emailIntent = Intent(Intent.ACTION_SENDTO)

                val uriText = "mailto:$recipient?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
                emailIntent.data = Uri.parse(uriText)

                try {
                    ProgressBarLayout.hideLoading()
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    Log.e(TAG, "No email client found on the device")
                }
            }
        }
    }



    // ========================= GOOGLE QR SCANNER ========================= //

    private fun scanQRCode() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->

                // Login with QRCode
                val scanned = barcode.rawValue.toString()

                // Decode Base 64
                val decodedBytes = Base64.getDecoder().decode(scanned)
                val decodedString = String(decodedBytes, Charsets.UTF_8)

                val (email, password) = decodedString.split(" ")

                emailSignIn(email, password)
            }
            .addOnCanceledListener {

            }
            .addOnFailureListener {

                // Clear Cache
                MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.bx_error)
                    .setTitle(resources.getString(R.string.prompt_qr_scanner_error))
                    .setMessage(resources.getString(R.string.prompt_qr_scanner_error_check))
                    .setNeutralButton(resources.getString(R.string.prompt_cancel)) { _, _ -> }
                    .setPositiveButton(resources.getString(R.string.prompt_ok)) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:com.google.android.gms")
                        startActivity(intent)
                    }
                    .show()
            }

    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}