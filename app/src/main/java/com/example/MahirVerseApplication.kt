package com.example

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MahirVerseApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Ensure Firebase is initialized. The google-services plugin normally
        // handles this automatically, but in some CI builds the config might
        // not be processed correctly. This is a safety net.
        ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized() {
        try {
            // Check if Firebase is already initialized by the google-services plugin
            val existingApp = FirebaseApp.getApps(this).find { it.name == FirebaseApp.DEFAULT_APP_NAME }
            if (existingApp != null) {
                Log.d("MahirVerse", "Firebase already initialized by google-services plugin")
                return
            }

            // Fallback: initialize manually with the config from google-services.json
            // Project: mahir-verse (860841631551)
            Log.w("MahirVerse", "Firebase not auto-initialized, initializing manually...")
            val options = FirebaseOptions.Builder()
                .setProjectId("mahir-verse")
                .setApplicationId("1:860841631551:android:aa2b16b7cc783f2538fd8a")
                .setApiKey("AIzaSyCyAEcK520m5n-_8tZeOgD3RX4na-3Ml_8")
                .setStorageBucket("mahir-verse.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(this, options)
            Log.d("MahirVerse", "Firebase initialized manually successfully")
        } catch (e: Exception) {
            Log.e("MahirVerse", "Firebase initialization failed", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
