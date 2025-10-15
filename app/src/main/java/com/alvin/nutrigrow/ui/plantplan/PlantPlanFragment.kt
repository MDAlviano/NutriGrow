package com.alvin.nutrigrow.ui.plantplan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.databinding.FragmentPlantPlanBinding
import com.alvin.nutrigrow.ui.plantplan.create.CreatePlantPlanActivity
import com.alvin.nutrigrow.ui.plantplan.detail.DetailPlantPlanActivity

class PlantPlanFragment : Fragment() {

    private var _binding: FragmentPlantPlanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlantPlanViewModel by viewModels()
    private lateinit var adapter: PlantPlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlantPlanBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setListener()
        setSpinnerItems()
        observeViewModel()
        viewModel.fetchUserPlans()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setListener() {
        binding.fabCreatePlantPlan.setOnClickListener {
            Intent(requireContext(), CreatePlantPlanActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.ivSearch.setOnClickListener {
            val query = binding.svPlantPlan.text.toString().trim()
            if (query.isNotEmpty()) {

            }
        }
    }

    private fun setSpinnerItems() {
        val spinner = binding.spMedium
        val mediums = listOf<String>("Semua", "Hidroponik", "Tanah")
        val adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, mediums)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedMedia = parent.getItemAtPosition(position) as String
                viewModel.fetchUserPlans(if (selectedMedia == "Semua") null else selectedMedia)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = PlantPlanAdapter(emptyList()) { plan ->
            val intent = Intent(requireContext(), DetailPlantPlanActivity::class.java).apply {
                putExtra("PLAN", plan)
            }
            startActivity(intent)
        }
        binding.rvPlantPlan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlantPlan.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.plans.observe(viewLifecycleOwner) { plans ->
            adapter.updatePlans(plans)
            if (plans.isEmpty()) {
                binding.tvPlantNone.visibility = View.VISIBLE
                binding.rvPlantPlan.visibility = View.GONE
            } else {
                binding.tvPlantNone.visibility = View.GONE
                binding.rvPlantPlan.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

}