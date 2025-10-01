package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Progress(
    val id: Int,
    val day: Int
): Parcelable
