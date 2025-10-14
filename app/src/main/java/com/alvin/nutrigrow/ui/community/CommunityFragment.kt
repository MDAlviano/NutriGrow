package com.alvin.nutrigrow.ui.community

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.databinding.FragmentCommunityBinding
import com.alvin.nutrigrow.ui.community.create.CreateCommunityPostActivity

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

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

        setListener()
        setPost()
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

    private fun setPost() {
        val post = mutableListOf<CommunityPost>()
        if (post.isEmpty()) {
            binding.tvPostNone.visibility = View.VISIBLE
            binding.rvCommunity.visibility = View.GONE
        } else {
            binding.tvPostNone.visibility = View.GONE
            binding.rvCommunity.visibility = View.VISIBLE
        }
    }

}