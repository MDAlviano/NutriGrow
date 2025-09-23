package com.alvin.nutrigrow.ui.aigrow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityAigrowBinding

class AIGrowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAigrowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAigrowBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}