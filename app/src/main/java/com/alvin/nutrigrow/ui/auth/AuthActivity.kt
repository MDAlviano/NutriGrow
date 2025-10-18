package com.alvin.nutrigrow.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.alvin.nutrigrow.MainActivity
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(
                "AuthActivity",
                "Sign-in result: resultCode=${result.resultCode}, data=${result.data}"
            )
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d("AuthActivity", "Google account retrieved: ${account?.email}")
                    if (account != null && account.idToken != null) {
                        firebaseAuthWithGoogle(account.idToken!!)
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Gagal mendapatkan akun Google",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: ApiException) {
                    Log.e("AuthActivity", "Google Sign-In failed: ${e.statusCode}, ${e.message}")
                    Snackbar.make(binding.root, "Login gagal: ${e.message}", Snackbar.LENGTH_SHORT)
                        .show()
                } catch (e: Exception) {
                    Log.e("AuthActivity", "Unexpected error: ${e.message}", e)
                    Snackbar.make(binding.root, "Login gagal: ${e.message}", Snackbar.LENGTH_SHORT)
                        .show()
                }
            } else {
                Log.w(
                    "AuthActivity",
                    "Sign-in cancelled or failed: resultCode=${result.resultCode}"
                )
                Snackbar.make(binding.root, "Login dibatalkan", Snackbar.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            Log.d("AuthActivity", "User already logged in: ${auth.currentUser?.email}")
            navigateToMainActivity()
        }

        setLogin()
    }

    private fun setLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("69879537681-ta7557mi2q073kssd07el39vo9kdtld4.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.bLogin.setOnClickListener {
            Log.d("AuthActivity", "Login button clicked")
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d("AuthActivity", "Authenticating with Firebase using idToken")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("AuthActivity", "Firebase auth successful: ${user?.email}")
                    Snackbar.make(
                        binding.root,
                        "Selamat datang, ${user?.displayName}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    navigateToMainActivity()
                } else {
                    Log.e("AuthActivity", "Firebase auth failed: ${task.exception?.message}")
                    Snackbar.make(
                        binding.root,
                        "Login gagal: ${task.exception?.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        Log.d("AuthActivity", "Navigating to MainActivity")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

}