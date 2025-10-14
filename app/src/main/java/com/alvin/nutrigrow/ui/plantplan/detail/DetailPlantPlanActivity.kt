package com.alvin.nutrigrow.ui.plantplan.detail

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Progress
import com.alvin.nutrigrow.databinding.ActivityPlantPlanDetailBinding

class DetailPlantPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantPlanDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantPlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setProgresses()
    }

    private fun setProgresses() {
        val progresses = mutableListOf<Progress>()
        if (progresses.isEmpty()) {
            binding.tvProgressNone.visibility = View.VISIBLE
            binding.rvPlantCondition.visibility = View.GONE
        } else {
            binding.tvProgressNone.visibility = View.GONE
            binding.rvPlantCondition.visibility = View.VISIBLE
        }
    }
}