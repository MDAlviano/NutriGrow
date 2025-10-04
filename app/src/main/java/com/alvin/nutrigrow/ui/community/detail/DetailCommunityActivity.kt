package com.alvin.nutrigrow.ui.community.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityCommunityDetailBinding

class DetailCommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        
        binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}