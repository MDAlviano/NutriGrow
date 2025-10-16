package com.alvin.nutrigrow.ui.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.CommunityPost
import com.bumptech.glide.Glide

class CommunityPostAdapter(var listPost: List<CommunityPost>, val onClick: (CommunityPost) -> Unit): RecyclerView.Adapter<CommunityPostAdapter.MainViewHolder>() {
    fun updatePosts(newPosts: List<CommunityPost>) {
        listPost = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MainViewHolder,
        position: Int
    ) {
        val post = listPost[position]

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .into(holder.img)

        holder.author.text = post.author
        holder.date.text = post.date
        holder.title.text = post.title
        holder.replies.text = "${post.replies} Replies"

        holder.itemView.setOnClickListener {
            onClick(post)
        }
    }

    override fun getItemCount(): Int {
        return listPost.size
    }

    class MainViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val author = view.findViewById<TextView>(R.id.tvCommunityAuthorItem)
        val date = view.findViewById<TextView>(R.id.tvCommunityDateItem)
        val title = view.findViewById<TextView>(R.id.tvCommunityTitle)
        val img = view.findViewById<ImageView>(R.id.imgCommunityImageItem)
        val replies = view.findViewById<TextView>(R.id.tvCommunityRepliesItem)
    }
}