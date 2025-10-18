package com.alvin.nutrigrow.ui.article

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.databinding.FragmentArticleBinding
import com.alvin.nutrigrow.ui.article.detail.DetailArticleActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArticleFragment : Fragment() {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArticleViewModel by viewModels()
    private lateinit var adapter: ArticleAdapter
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArticleBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()
        viewModel.fetchArticles()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = ArticleAdapter(emptyList()) { article ->
            val intent = Intent(requireContext(), DetailArticleActivity::class.java).apply {
                putExtra("ARTICLE", article)
            }
            startActivity(intent)
        }
        binding.rvArticle.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArticle.adapter = adapter
    }

    private fun setupSearch() {
        binding.svArticle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.fetchArticles(
                    category = selectedCategory,
                    searchQuery = s.toString().trim()
                )
            }
        })
        binding.ivSearch.setOnClickListener {
            viewModel.fetchArticles(
                category = selectedCategory,
                searchQuery = binding.svArticle.text.toString().trim()
            )
        }
    }

    private fun setupFilterButtons(categories: List<String>) {
        binding.filter.removeAllViews()
        val allButton = MaterialButton(requireContext()).apply {
            text = "Semua"
            textSize = 11f
            setTextColor(resources.getColor(R.color.white))
            setAllCaps(false)
            setOnClickListener {
                selectedCategory = null
                updateButtonStyles(this)
                viewModel.fetchArticles(searchQuery = binding.svArticle.text.toString().trim())
            }
        }
        binding.filter.addView(allButton)

        categories.forEach { category ->
            val button = MaterialButton(requireContext()).apply {
                text = category
                textSize = 11f
                setTextColor(resources.getColor(R.color.white))
                setAllCaps(false)
                setOnClickListener {
                    selectedCategory = category
                    updateButtonStyles(this)
                    viewModel.fetchArticles(
                        category = category,
                        searchQuery = binding.svArticle.text.toString().trim()
                    )
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8
                }
            }
            binding.filter.addView(button)
        }
    }

    private fun updateButtonStyles(selectedButton: MaterialButton) {
        binding.filter.children.forEach { view ->
            if (view is MaterialButton) {
                view.isSelected = view == selectedButton
                view.setBackgroundColor(
                    resources.getColor(
                        if (view.isSelected) R.color.primary else R.color.inactive
                    )
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            adapter.updateArticles(articles)
            if (articles.isEmpty()) {
                binding.tvArticleNone.visibility = View.VISIBLE
                binding.rvArticle.visibility = View.GONE
            } else {
                binding.tvArticleNone.visibility = View.GONE
                binding.rvArticle.visibility = View.VISIBLE
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupFilterButtons(categories)
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