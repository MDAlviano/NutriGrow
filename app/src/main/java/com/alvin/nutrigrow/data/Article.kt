package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val id: String,
    val imageUrl: String,
    val date: String,
    val title: String,
    val content: String,
    val category: String,
): Parcelable
