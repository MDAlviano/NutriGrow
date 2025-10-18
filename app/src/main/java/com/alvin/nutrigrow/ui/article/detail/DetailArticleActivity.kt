package com.alvin.nutrigrow.ui.article.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.databinding.ActivityDetailArticleBinding
import com.alvin.nutrigrow.ui.article.ArticleAdapter
import com.alvin.nutrigrow.ui.article.ArticleViewModel
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailArticleBinding
    private val viewModel: ArticleViewModel by viewModels()
    private lateinit var adapter: ArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityDetailArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val article = intent.getParcelableExtra<Article>("ARTICLE")
        article?.let {
            setData(it)
            setupRecyclerView()
            viewModel.fetchRecommendedArticles(it.id)
        }
        observeViewModel()
    }

    private fun setData(article: Article) {
        Glide.with(this)
            .load(article.imageUrl)
            .into(binding.imgArticle)

        val formattedDate = when (val dateValue = article.date) {
            is com.google.firebase.Timestamp -> {
                val date = dateValue.toDate()
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(date)
            }
            is Date -> {
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(dateValue)
            }
            is String -> dateValue // kalau disimpan sebagai string
            else -> "-"
        }
        binding.tvArticleDateAndaCategory.text = "$formattedDate - ${article.category}"
        binding.tvArticleTitle.text = article.title
        binding.tvArticleDescription.text = article.content
    }

    private fun setupRecyclerView() {
        adapter = ArticleAdapter(emptyList()) { recommendedArticle ->
            val intent = Intent(this, DetailArticleActivity::class.java).apply {
                putExtra("ARTICLE", recommendedArticle)
            }
            startActivity(intent)
        }
        binding.rvRecommendationArticle.layoutManager = LinearLayoutManager(this)
        binding.rvRecommendationArticle.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.recommendedArticles.observe(this) { articles ->
            adapter.updateArticles(articles)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // ProgressBar not present in layout, can be added if needed
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }
}