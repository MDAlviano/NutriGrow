package com.alvin.nutrigrow.ui.aigrow.detail

import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Diagnosis
import com.alvin.nutrigrow.databinding.ActivityDetailAigrowBinding
import com.bumptech.glide.Glide

class DetailAIGrowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailAigrowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAigrowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Title"
        }

        val diag = intent.getParcelableExtra<Diagnosis>("DIAGNOSIS")
        diag?.let {
            Glide.with(this).load(it.imageUrl).into(binding.imgDetailAIPosted)
            binding.tvDetailAIResponse.text = Html.fromHtml(it.response, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}