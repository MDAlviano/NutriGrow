package com.alvin.nutrigrow.ui.community.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.databinding.ActivityCommunityDetailBinding
import com.bumptech.glide.Glide

class DetailCommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        
        binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = intent.getParcelableExtra<CommunityPost>("POST")
        post?.let {
            Glide.with(this)
                .load(it.imageUrl)
                .into(binding.imgCommunity)

            binding.tvCommunityAuthor.text = it.author
            binding.tvCommunityDate.text = it.date
            binding.tvCommunityTitle.text = it.title
            binding.tvCommunityDescription.text = it.content
        }

    }
}