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
            "cloud_name" to "dm6bkhvyn",
            "api_key" to "991948627825862",
            "api_secret" to "-887M_T9Xl5MUyeP_KlZ8Vkc0Co"
        )
        MediaManager.init(this, config)

        geminiModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyDfZnxDxyHr0qk-Ni5QUNC1y0WIk6PrZ7E"
        )
    }
}