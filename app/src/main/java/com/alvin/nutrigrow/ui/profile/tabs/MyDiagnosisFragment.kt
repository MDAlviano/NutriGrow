package com.alvin.nutrigrow.ui.profile.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.data.Diagnosis
import com.alvin.nutrigrow.databinding.FragmentMyDiagnosisBinding

class MyDiagnosisFragment : Fragment() {

    private var _binding: FragmentMyDiagnosisBinding? = null
    private val binding get() = _binding!!

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

        setDiagnosis()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setDiagnosis() {
        val diagnosis = mutableListOf<Diagnosis>()
        if (diagnosis.isEmpty()) {
            binding.tvMydiagnosisnone.visibility = View.VISIBLE
            binding.rvMyDiagnosis.visibility = View.GONE
        } else {
            binding.tvMydiagnosisnone.visibility = View.GONE
            binding.rvMyDiagnosis.visibility = View.VISIBLE
        }
    }
}