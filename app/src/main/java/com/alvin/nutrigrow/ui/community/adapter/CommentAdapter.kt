package com.alvin.nutrigrow.ui.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Comment

class CommentAdapter(var comments: List<Comment>): RecyclerView.Adapter<CommentAdapter.MyViewHolder>() {
    fun updateComments(newComment: List<Comment>) {
        comments = newComment
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        val comment = comments[position]
        holder.tvComment.text = comment.comment
        holder.tvDate.text = comment.createdAt
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvComment = view.findViewById<TextView>(R.id.tvComment)
        val tvDate = view.findViewById<TextView>(R.id.tvComment)
    }
}