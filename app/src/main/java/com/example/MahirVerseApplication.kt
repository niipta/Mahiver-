package com.example

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
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
        ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized() {
        try {
            val existingApp = FirebaseApp.getApps(this).find { it.name == FirebaseApp.DEFAULT_APP_NAME }
            if (existingApp != null) {
                Log.d("MahirVerse", "Firebase already initialized")
                return
            }

            Log.w("MahirVerse", "Firebase not auto-initialized, initializing manually...")
            val options = FirebaseOptions.Builder()
                .setProjectId("mahir-verse")
                .setApplicationId("1:860841631551:android:aa2b16b7cc783f2538fd8a")
                .setApiKey("AIzaSyCyAEcK520m5n-_8tZeOgD3RX4na-3Ml_8")
                .setStorageBucket("mahir-verse.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(this, options)
            Log.d("MahirVerse", "Firebase initialized manually")
        } catch (e: Exception) {
            Log.e("MahirVerse", "Firebase init failed", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

/**
 * ContentProvider that initializes Firebase BEFORE Application.onCreate().
 * This runs before any other code in the app, ensuring Firebase is ready
 * when AuthViewModel tries to use FirebaseAuth.
 *
 * ContentProviders initialize in order of priority (highest first).
 */
class FirebaseInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false
        try {
            val existingApp = FirebaseApp.getApps(context).find { it.name == FirebaseApp.DEFAULT_APP_NAME }
            if (existingApp != null) {
                Log.d("FirebaseInit", "Firebase already initialized")
                return true
            }

            Log.w("FirebaseInit", "Initializing Firebase via ContentProvider...")
            val options = FirebaseOptions.Builder()
                .setProjectId("mahir-verse")
                .setApplicationId("1:860841631551:android:aa2b16b7cc783f2538fd8a")
                .setApiKey("AIzaSyCyAEcK520m5n-_8tZeOgD3RX4na-3Ml_8")
                .setStorageBucket("mahir-verse.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(context, options)
            Log.d("FirebaseInit", "Firebase initialized via ContentProvider")
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Firebase init failed", e)
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}
