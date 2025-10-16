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

class DetailCommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityDetailBinding
    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        
        binding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = intent.getParcelableExtra<CommunityPost>("POST")
        post?.let {
            setData(it)
            setupRecyclerView()
            viewModel.fetchComments(it.id)
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

    private fun setData(post: CommunityPost) {
        Glide.with(this)
            .load(post.imageUrl)
            .into(binding.imgCommunity)

        binding.tvCommunityAuthor.text = post.author
        binding.tvCommunityDate.text = post.date
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