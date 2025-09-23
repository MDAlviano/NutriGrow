package com.alvin.nutrigrow.ui.aigrow.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityDetailAigrowBinding

class DetailAIGrowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailAigrowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAigrowBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}