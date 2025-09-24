package com.alvin.nutrigrow.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.MainActivity
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityAuthBinding
import com.google.android.material.snackbar.Snackbar

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bLogin.setOnClickListener {
            Snackbar.make(binding.root, "Are you sure?", Snackbar.LENGTH_SHORT).setAction("Yes") {
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            }.show()
        }

    }
}