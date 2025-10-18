package com.alvin.nutrigrow.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Progress(
    val id: String = "",
    val plantPlanId: String = "",
    val day: Int = 0,
    val imageUrl: String = "",
    val response: String = "",
    val condition: String = "",
    val createdAt: Timestamp? = null
): Parcelable
