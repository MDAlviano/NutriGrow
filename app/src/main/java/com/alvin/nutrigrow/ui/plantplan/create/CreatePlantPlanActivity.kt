package com.alvin.nutrigrow.ui.plantplan.create

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.ActivityCreatePlantPlanBinding
import com.alvin.nutrigrow.ui.plantplan.PlantPlanViewModel

class CreatePlantPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePlantPlanBinding
    private val viewModel: PlantPlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePlantPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        setListener()
        setSpinnerItems()
        observeViewModel()
    }

    private fun setListener() {
        binding.btnCreatePlantPlan.setOnClickListener {
            val name = binding.etPlantPlanTitle.text?.toString()?.trim() ?: ""
            val growingMedia = binding.spMediumPicker.selectedItem?.toString() ?: ""
            val plant = binding.etPlantPlanPlant.text?.toString()?.trim() ?: ""

            viewModel.createPlan(name, growingMedia, plant)
        }
    }

    private fun setSpinnerItems() {
        val spinner = binding.spMediumPicker
        val mediums = listOf<String>("Hidroponik", "Tanah")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mediums)

        spinner.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCreatePlantPlan.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Plan created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}