package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Diagnosis(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String,
    val imageUrl: String,
    val response: String,
    val condition: String,
): Parcelable
