package com.alvin.nutrigrow.ui.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val formattedDate = when (val dateValue = comment.createdAt) {
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
        holder.tvDate.text = formattedDate
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvComment = view.findViewById<TextView>(R.id.tvComment)
        val tvDate = view.findViewById<TextView>(R.id.tvComment)
    }
}