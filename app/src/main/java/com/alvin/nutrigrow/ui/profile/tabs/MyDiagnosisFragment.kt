package com.alvin.nutrigrow.ui.profile.tabs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.data.Diagnosis
import com.alvin.nutrigrow.databinding.FragmentMyDiagnosisBinding
import com.alvin.nutrigrow.ui.aigrow.AIGrowViewModel
import com.alvin.nutrigrow.ui.aigrow.detail.DetailAIGrowActivity
import com.alvin.nutrigrow.ui.profile.adapter.DiagnosisAdapter

class MyDiagnosisFragment : Fragment() {

    private var _binding: FragmentMyDiagnosisBinding? = null
    private val binding get() = _binding!!
    private val vm: AIGrowViewModel by viewModels()
    private lateinit var adapter: DiagnosisAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyDiagnosisBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRV()
        vm.fetchMyDiagnosis()
        observeVM()
    }

    private fun setupRV() {
        adapter = DiagnosisAdapter(emptyList()) { diag ->
            val intent = Intent(requireContext(), DetailAIGrowActivity::class.java).apply {
                putExtra("DIAGNOSIS", diag)
            }
            startActivity(intent)
        }
        binding.rvMyDiagnosis.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyDiagnosis.adapter = adapter
    }

    private fun observeVM() {
        vm.myDiagnosis.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            binding.tvMydiagnosisnone.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvMyDiagnosis.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }
        vm.isLoading.observe(viewLifecycleOwner) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
        vm.error.observe(viewLifecycleOwner) { it?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() } }
    }
}