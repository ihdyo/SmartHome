package com.ihdyo.smarthome.ui.login

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ihdyo.smarthome.R
import com.ihdyo.smarthome.databinding.ActivityLoginBinding
import com.ihdyo.smarthome.ui.MainActivity
import com.ihdyo.smarthome.utils.AuthDialog
import com.ihdyo.smarthome.utils.Const.COLLECTION_USERS
import com.ihdyo.smarthome.utils.Const.RC_SIGN_IN
import com.ihdyo.smarthome.utils.Const.WEB_CLIENT_ID
import com.ihdyo.smarthome.utils.ProgressBar
import com.ihdyo.smarthome.utils.UiUpdater

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Register New Member
        binding.textRegister.setOnClickListener {
            registerNewMember()
        }

        // Register New Member
        binding.textForgotPassword.setOnClickListener {
            val authDialog = AuthDialog(this, R.layout.dialog_change_password)
            authDialog.show()
        }


        // Input Field Validation

        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmailFormat(charSequence)
            }

            override fun afterTextChanged(editable: Editable?) { }
        })


        // Google Sign In

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(WEB_CLIENT_ID).requestEmail().build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.buttonGoogle.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }


        // Email Sign In

        binding.buttonLogin.setOnClickListener {
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

            performLogin(email, password)
        }
    }


    // ========================= INPUT FIELD VALIDATION ========================= //

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


    // ========================= EMAIL SIGN IN ========================= //

    private fun performLogin(email: String, password: String) {
        ProgressBar.showLoading(this)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                ProgressBar.hideLoading()

                if (task.isSuccessful) {
                    checkRegistered()
                } else {
                    Snackbar.make(binding.root, R.string.prompt_auth_email_failed, Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.prompt_ok)) { }
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                ProgressBar.hideLoading()
                Snackbar.make(binding.root, exception.message.toString(), Snackbar.LENGTH_INDEFINITE)
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

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential).addOnCompleteListener { taskSignin ->
                        if (taskSignin.isSuccessful) {
                            checkRegistered()
                        } else {
                            Snackbar.make(binding.root, R.string.prompt_auth_google_failed, Snackbar.LENGTH_SHORT)
                                .setAction(getString(R.string.prompt_ok)) { }
                                .show()
                        }
                    }
                }
            } catch (_: ApiException) { }
        }
    }


    // ========================= CHECK IF USER REGISTERED ========================= //

    private fun checkRegistered() {
        val userRef = firestore.collection(COLLECTION_USERS).document(FirebaseAuth.getInstance().currentUser!!.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    Toast.makeText(this, getString(R.string.prompt_auth_success), Toast.LENGTH_SHORT).show()

                    val animationBundle = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_top,
                        R.anim.slide_out_bottom
                    ).toBundle()

                    startActivity(Intent(this, MainActivity::class.java), animationBundle)
                    finish()
                } else {
                    Snackbar.make(binding.root, R.string.prompt_auth_failed, Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.prompt_register)) {

                            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(WEB_CLIENT_ID).requestEmail().build()
                            val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

                            auth.signOut()
                            googleSignInClient.signOut()

                            registerNewMember()
                        }
                        .show()
                }
            }
    }


    // ========================= REGISTER NEW MEMBER ========================= //

    @SuppressLint("QueryPermissionsNeeded")
    private fun registerNewMember() {

        // Send email
        val recipient = "yodhi.HIMATIKA@gmail.com"
        val subject = "Lumos New Member"

        val emailIntent = Intent(Intent.ACTION_SENDTO)

        emailIntent.data = Uri.parse("mailto:$recipient")
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }
}