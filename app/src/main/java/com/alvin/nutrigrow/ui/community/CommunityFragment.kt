package com.alvin.nutrigrow.ui.community

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.databinding.FragmentCommunityBinding
import com.alvin.nutrigrow.ui.community.create.CreateCommunityPostActivity
import com.alvin.nutrigrow.ui.community.detail.DetailCommunityActivity

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var adapter: CommunityPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setListener()
        observeViewModel()
        viewModel.fetchPosts()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setListener() {
        binding.fabCreateCommunity.setOnClickListener {
            Intent(requireContext(), CreateCommunityPostActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CommunityPostAdapter(emptyList()) { post ->
            val intent = Intent(requireContext(), DetailCommunityActivity::class.java).apply {
                putExtra("POST", post)
            }
            startActivity(intent)
        }
        binding.rvCommunity.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCommunity.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.updatePosts(posts)
            if (posts.isEmpty()) {
                binding.tvPostNone.visibility = View.VISIBLE
                binding.rvCommunity.visibility = View.GONE
            } else {
                binding.tvPostNone.visibility = View.GONE
                binding.rvCommunity.visibility = View.VISIBLE
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

}