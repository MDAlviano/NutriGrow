package com.alvin.nutrigrow.ui.profile.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alvin.nutrigrow.data.CommunityPost
import com.alvin.nutrigrow.data.Progress
import com.alvin.nutrigrow.databinding.FragmentMyPostBinding

class MyPostFragment : Fragment() {

    private var _binding: FragmentMyPostBinding? = null
    private val binding get() = _binding!!

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

        setPost()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setPost() {
        val post = mutableListOf<CommunityPost>()
        if (post.isEmpty()) {
            binding.tvMypostnone.visibility = View.VISIBLE
            binding.rvMyPost.visibility = View.GONE
        } else {
            binding.tvMypostnone.visibility = View.GONE
            binding.rvMyPost.visibility = View.VISIBLE
        }
    }
}