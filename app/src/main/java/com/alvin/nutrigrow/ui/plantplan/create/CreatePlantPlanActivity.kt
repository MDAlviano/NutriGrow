package com.alvin.nutrigrow.ui.plantplan.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityCreatePlantPlanBinding

class CreatePlantPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePlantPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePlantPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}