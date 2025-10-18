package com.alvin.nutrigrow.ui.community.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.databinding.ActivityCommunityDetailBinding
import com.alvin.nutrigrow.ui.community.CommunityViewModel
import com.alvin.nutrigrow.ui.community.adapter.CommentAdapter
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailCommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding
    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter

    private var post: CommunityPost? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        
        binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        post = intent.getParcelableExtra<CommunityPost>("POST")
        post?.let {
            setData(it)
            setupRecyclerView()
        }

        binding.btnSendCommunityComment.setOnClickListener {
            val commentText = binding.etCommunityComment.text?.toString() ?: ""
            post?.id?.let { postId ->
                viewModel.addComment(postId, commentText)
                binding.etCommunityComment.text?.clear()
            }
        }

        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        post?.let {
            viewModel.fetchComments(it.id)
        }
    }

    private fun setData(post: CommunityPost) {
        Glide.with(this)
            .load(post.imageUrl)
            .into(binding.imgCommunity)

        val formattedDate = when (val dateValue = post.createdAt) {
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
        binding.tvCommunityDate.text = formattedDate

        binding.tvCommunityAuthor.text = post.author
        binding.tvCommunityTitle.text = post.title
        binding.tvCommunityDescription.text = post.content
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(emptyList())
        binding.rvCommunityComments.layoutManager = LinearLayoutManager(this)
        binding.rvCommunityComments.adapter = commentAdapter
    }

    private fun observeViewModel() {
        viewModel.comments.observe(this) { comments ->
            commentAdapter.updateComments(comments)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.rvCommunityComments.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

}