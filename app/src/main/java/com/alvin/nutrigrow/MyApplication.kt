package com.alvin.nutrigrow

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val config = mapOf(
            "cloud_name" to R.string.cloudinary_cloud_name,
            "api_key" to R.string.cloudinary_api_key,
            "api_secret" to R.string.cloudinary_api_secret
        )
        MediaManager.init(this, config)
    }
}