package com.alvin.nutrigrow.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val id: String = "",
    val imageUrl: String = "",
    val date: Timestamp? = null,
    val title: String = "",
    val content: String = "",
    val category: String = "",
): Parcelable
