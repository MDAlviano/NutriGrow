package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityPost(
    val id: String,
    val imageUrl: String,
    val date: String,
    val author: String,
    val title: String,
    val content: String,
    val replies: Int
): Parcelable