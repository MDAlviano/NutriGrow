package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val comment: String,
    val createdAt: String
): Parcelable