package com.alvin.nutrigrow.ui.plantplan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.Navigation
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.databinding.FragmentPlantPlanBinding
import com.alvin.nutrigrow.ui.plantplan.create.CreatePlantPlanActivity

class PlantPlanFragment : Fragment() {

    private var _binding: FragmentPlantPlanBinding? = null
    private val binding get() = _binding!!

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

        setListener()
        setSpinnerItems()
        setPlantPlans()
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
    }

    private fun setSpinnerItems() {
        val spinner = binding.spMedium
        val mediums = listOf<String>("Hidroponik", "Tanah")
        val adapter =
            ArrayAdapter(requireContext(), R.layout.spinner_item, mediums)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setPlantPlans() {
        val plants = mutableListOf<Plan>()
        if (plants.isEmpty()) {
            binding.tvPlantNone.visibility = View.VISIBLE
            binding.rvPlantPlan.visibility = View.GONE
        } else {
            binding.tvPlantNone.visibility = View.GONE
            binding.rvPlantPlan.visibility = View.VISIBLE
        }
    }

}