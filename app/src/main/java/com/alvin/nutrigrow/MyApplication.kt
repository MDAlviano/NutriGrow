package com.alvin.nutrigrow

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.FirebaseApp

class MyApplication : Application() {

    companion object {
        lateinit var geminiModel: GenerativeModel
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        val config = mapOf(
            "cloud_name" to R.string.cloudinary_cloud_name,
            "api_key" to R.string.cloudinary_api_key,
            "api_secret" to R.string.cloudinary_api_secret
        )
        MediaManager.init(this, config)

        geminiModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = R.string.gemini_api_key.toString()
        )
    }
}