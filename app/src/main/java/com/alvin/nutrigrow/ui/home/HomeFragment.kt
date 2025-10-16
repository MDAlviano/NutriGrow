package com.alvin.nutrigrow.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var plantPlanAdapter: ContinuePlantPlanAdapter

    companion object {
        private const val REQUEST_LOCATION = 100
    }

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
        setCurrentDate()
        setupPlantPlanRecyclerView()
        setupArticleRecyclerView()
        requestLocationPermission()
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

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION)
        } else {
            viewModel.fetchWeather(requireContext())
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.fetchWeather(requireContext())
        } else {
            binding.tvLocation.text = "Unknown"
            binding.tvTodayWeather.text = "Cuaca: Tidak diketahui"
            binding.tvDegree.text = "N/A"
            binding.tvConclusion.text = "Tidak ada data cuaca"
        }
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

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        binding.tvDateToday.text = sdf.format(Date())
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
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            binding.tvLocation.text = weather.city
            binding.tvTodayWeather.text = "Cuaca Hari ini: ${weather.condition}"
            binding.tvDegree.text = "${weather.temp}°C"
            binding.tvConclusion.text = weather.advice
        }
    }
}