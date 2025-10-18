package com.alvin.nutrigrow.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Plan(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val growingMedia: String = "",
    val plant: String = "",
    val day: Int = 0,
    val condition: String = "",
    val createdAt: Timestamp? = null
): Parcelable
