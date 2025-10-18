package com.alvin.nutrigrow.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityPost(
    val id: String = "",
    val imageUrl: String = "",
    val createdAt: Timestamp? = null,
    val author: String = "",
    val title: String = "",
    val content: String = "",
    val replies: Int = 0
): Parcelable