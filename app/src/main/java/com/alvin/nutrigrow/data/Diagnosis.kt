package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Diagnosis(
    val id: Int,
    val title: String,
    val date: String,
    val imageUrl: String,
    val response: String
): Parcelable
