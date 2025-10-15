package com.alvin.nutrigrow.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.data.Plan
import com.alvin.nutrigrow.databinding.FragmentHomeBinding
import com.alvin.nutrigrow.ui.aigrow.AIGrowActivity
import com.alvin.nutrigrow.ui.article.ArticleAdapter
import com.alvin.nutrigrow.ui.article.detail.DetailArticleActivity
import com.alvin.nutrigrow.ui.plantplan.create.CreatePlantPlanActivity
import com.alvin.nutrigrow.ui.plantplan.detail.DetailPlantPlanActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var plantPlanAdapter: ContinuePlantPlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
        setupPlantPlanRecyclerView()
        setupArticleRecyclerView()
        observeViewModel()
        viewModel.fetchNewestArticles()
        viewModel.fetchNewestPlans()
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

    private fun setupPlantPlanRecyclerView() {
        plantPlanAdapter = ContinuePlantPlanAdapter(emptyList()) { plan ->
            val intent = Intent(requireContext(), DetailPlantPlanActivity::class.java).apply {
                putExtra("PLAN", plan)
            }
            startActivity(intent)
        }
        binding.rvContinuePlantPlan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContinuePlantPlan.adapter = plantPlanAdapter
    }

    private fun setupArticleRecyclerView() {
        articleAdapter = ArticleAdapter(emptyList()) { article ->
            val intent = Intent(requireContext(), DetailArticleActivity::class.java).apply {
                putExtra("ARTICLE", article)
            }
            startActivity(intent)
        }
        binding.rvNewestArticle.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNewestArticle.adapter = articleAdapter
    }

    private fun observeViewModel() {
        viewModel.plans.observe(viewLifecycleOwner) { plans ->
            plantPlanAdapter.updatePlans(plans)
            if (plans.isEmpty()) {
                binding.groupNone.visibility = View.VISIBLE
                binding.rvContinuePlantPlan.visibility = View.GONE
            } else {
                binding.groupNone.visibility = View.GONE
                binding.rvContinuePlantPlan.visibility = View.VISIBLE
            }
        }

        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            articleAdapter.updateArticles(articles)
            if (articles.isEmpty()) {
                binding.tvArticleNone.visibility = View.VISIBLE
                binding.rvNewestArticle.visibility = View.GONE
            } else {
                binding.tvArticleNone.visibility = View.GONE
                binding.rvNewestArticle.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }
}