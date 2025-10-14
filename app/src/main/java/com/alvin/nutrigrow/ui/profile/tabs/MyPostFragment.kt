package com.alvin.nutrigrow.ui.profile.tabs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.data.Progress
import com.alvin.nutrigrow.databinding.FragmentMyPostBinding
import com.alvin.nutrigrow.ui.community.CommunityPostAdapter
import com.alvin.nutrigrow.ui.community.detail.DetailCommunityActivity
import com.alvin.nutrigrow.ui.profile.ProfileViewModel

class MyPostFragment : Fragment() {

    private var _binding: FragmentMyPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var adapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPostBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.fetchUserPosts()
    }

    private fun setupRecyclerView() {
        adapter = CommunityPostAdapter(emptyList()) { post ->
            val intent = Intent(requireContext(), DetailCommunityActivity::class.java).apply {
                putExtra("POST", post)
            }
            startActivity(intent)
        }
        binding.rvMyPost.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyPost.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
            if (posts.isEmpty()) {
                binding.tvMypostnone.visibility = View.VISIBLE
                binding.rvMyPost.visibility = View.GONE
            } else {
                binding.tvMypostnone.visibility = View.GONE
                binding.rvMyPost.visibility = View.VISIBLE
            }
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}