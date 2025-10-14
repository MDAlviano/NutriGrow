package com.alvin.nutrigrow.ui.plantplan.create

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
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

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        setSpinnerItems()
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
}