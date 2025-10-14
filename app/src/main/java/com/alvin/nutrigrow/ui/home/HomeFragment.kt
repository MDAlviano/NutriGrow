package com.alvin.nutrigrow.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.databinding.FragmentHomeBinding
import com.alvin.nutrigrow.ui.aigrow.AIGrowActivity
import com.alvin.nutrigrow.ui.plantplan.create.CreatePlantPlanActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
        setRecentPlant()
        setArticles()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListener() {
        binding.fabToAIGrow.setOnClickListener {
            Intent(requireContext(), AIGrowActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnToCreatePlant.setOnClickListener {
            Intent(requireContext(), CreatePlantPlanActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun setRecentPlant() {
        val plants = mutableListOf<Plan>()
        if (plants.isEmpty()) {
            binding.groupNone.visibility = View.VISIBLE
            binding.rvContinuePlantPlan.visibility = View.GONE
        } else {
            binding.groupNone.visibility = View.GONE
            binding.rvContinuePlantPlan.visibility = View.VISIBLE
        }
    }

    private fun setArticles() {
        val articles = mutableListOf<Article>()
        if (articles.isEmpty()) {
            binding.tvArticleNone.visibility = View.VISIBLE
            binding.rvNewestArticle.visibility = View.GONE
        } else {
            binding.tvArticleNone.visibility = View.GONE
            binding.rvNewestArticle.visibility = View.VISIBLE
        }
    }
}