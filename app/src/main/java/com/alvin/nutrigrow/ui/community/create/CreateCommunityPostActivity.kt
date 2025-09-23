package com.alvin.nutrigrow.ui.community.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityCreateCommunityPostBinding

class CreateCommunityPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCommunityPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCommunityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}