package com.example.drawingapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class DrawingApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // manual options setting because it couldn't find the google-services.json for some reason.
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:862182328364:android:0c40d658c970998075a989")
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setProjectId("drawingapp-e9cef")
                .setStorageBucket("drawingapp-e9cef.firebasestorage.app")
                .setGcmSenderId("862182328364")
                .build()

            FirebaseApp.initializeApp(this, options)
        }
    }
}
