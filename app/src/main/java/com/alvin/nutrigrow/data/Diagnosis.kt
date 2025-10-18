package com.alvin.nutrigrow.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Diagnosis(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val createdAt: Timestamp? = null,
    val imageUrl: String = "",
    val response: String = "",
    val condition: String = "",
): Parcelable
