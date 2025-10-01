package com.alvin.nutrigrow.ui.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Article
import com.bumptech.glide.Glide

class ArticleAdapter(val articles: List<Article>, val onClick: (Article) -> Unit): RecyclerView.Adapter<ArticleAdapter.MainViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MainViewHolder,
        position: Int
    ) {
        val article = articles[position]

        Glide.with(holder.itemView.context)
            .load(article.imageUrl)
            .into(holder.img)
        holder.tvTitle.text = article.title
        holder.tvDesc.text = article.content

        holder.itemView.setOnClickListener {
            onClick(article)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    class MainViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgArticleItem)
        val tvTitle = view.findViewById<TextView>(R.id.tvArticleTitle)
        val tvDesc = view.findViewById<TextView>(R.id.tvArticleDescriptionItem)
    }
}