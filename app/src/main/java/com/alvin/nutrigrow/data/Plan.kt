package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Plan(
    val id: String,
    val userId: String,
    val name: String,
    val growingMedia: String,
    val plant: String,
    val day: Int,
    val condition: String,
    val createdAt: String
): Parcelable
