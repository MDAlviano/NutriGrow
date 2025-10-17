package com.alvin.nutrigrow.ui.plantplan.detail.progress

import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.alvin.nutrigrow.data.Progress
import com.alvin.nutrigrow.databinding.ActivityDetailPlantProgressBinding
import com.bumptech.glide.Glide

class DetailPlantProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPlantProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detail"
        }

        val progress = intent.getParcelableExtra<Progress>("PROGRESS")
        progress?.let {
            Glide.with(this).load(it.imageUrl).into(binding.imgDetailAIPosted)
            binding.tvDetailAIResponse.text = Html.fromHtml(it.response, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
}