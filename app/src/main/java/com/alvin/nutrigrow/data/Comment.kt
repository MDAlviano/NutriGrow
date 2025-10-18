package com.alvin.nutrigrow.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val comment: String = "",
    val createdAt: Timestamp? = null
): Parcelable