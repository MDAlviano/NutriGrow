package com.alvin.nutrigrow.ui.article.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alvin.nutrigrow.databinding.ActivityDetailArticleBinding

class DetailArticleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityDetailArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}