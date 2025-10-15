package com.alvin.nutrigrow.ui.plantplan.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.data.Progress
import com.alvin.nutrigrow.databinding.ActivityPlantPlanDetailBinding
import com.alvin.nutrigrow.ui.plantplan.PlantPlanViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class DetailPlantPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantPlanDetailBinding
    private val viewModel: PlantPlanViewModel by viewModels()
    private lateinit var adapter: PlantPlanProgressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantPlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val plan = intent.getParcelableExtra<Plan>("PLAN")
        plan?.let {
            setData(it)
            setupRecyclerView()
            viewModel.fetchPlanProgresses(it.id)
        }

        observeViewModel()
        setListener()
    }

    private fun setListener() {
        binding.btnUploadPlantCondition.setOnClickListener {
            Toast.makeText(this, "Upload condition clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = PlantPlanProgressAdapter(emptyList()) { progress ->
            Toast.makeText(this, "Progress clicked: Day ${progress.day}", Toast.LENGTH_SHORT).show()
        }
        binding.rvPlantCondition.layoutManager = LinearLayoutManager(this)
        binding.rvPlantCondition.adapter = adapter
    }

    private fun setData(plan: Plan) {
        binding.tvPlantPlanDay.text = "Hari ke: ${plan.day}"
        binding.tvPlantPlanCreated.text = plan.createdAt?.let {
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(it)
        } ?: "Unknown Date"
        binding.tvPlantPlanLatestCondition.text = "Kondisi Terbaru: ${plan.condition}"
    }

    private fun observeViewModel() {
        viewModel.progresses.observe(this) { progresses ->
            adapter.updateProgresses(progresses)
            if (progresses.isEmpty()) {
                binding.tvProgressNone.visibility = View.VISIBLE
                binding.rvPlantCondition.visibility = View.GONE
            } else {
                binding.tvProgressNone.visibility = View.GONE
                binding.rvPlantCondition.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
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