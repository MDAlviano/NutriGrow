package com.alvin.nutrigrow.ui.plantplan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.Navigation
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.FragmentPlantPlanBinding

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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setListener() {
        binding.fabCreatePlantPlan.setOnClickListener {
            Navigation.createNavigateOnClickListener(R.id.action_navigation_plantplan_to_navigation_create_plant_plan_activity2)
        }
    }

    private fun setSpinnerItems() {
        val spinner = binding.spMedium
        val mediums = listOf<String>("Hidroponik", "Tanah")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mediums)

        spinner.adapter = adapter
    }

}