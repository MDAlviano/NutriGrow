package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Plan(
    val id: Int,
    val name: String,
    val growingMedia: String,
    val plant: String,
    val day: String,
    val condition: String,
    val created: Date
): Parcelable
