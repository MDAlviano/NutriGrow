package com.alvin.nutrigrow.ui.article.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.alvin.nutrigrow.databinding.ActivityDetailArticleBinding
import com.bumptech.glide.Glide

class DetailArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityDetailArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val article = intent.getParcelableExtra<Article>("ARTICLE")
        article?.let {
            Glide.with(this)
                .load(it.imageUrl)
                .into(binding.imgArticle)

            binding.tvArticleDate.text = it.date
            binding.tvArticleTitle.text = it.title
            binding.tvArticleDescription.text = it.content
        }
    }
}