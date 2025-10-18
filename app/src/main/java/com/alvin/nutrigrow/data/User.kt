package com.alvin.nutrigrow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.sql.Timestamp

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp? = null,
): Parcelable
