package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Progress(
    val id: String,
    val plantPlanId: String,
    val day: Int,
    val imageUrl: String,
    val response: String,
    val condition: String,
    val createdAt: String
): Parcelable
