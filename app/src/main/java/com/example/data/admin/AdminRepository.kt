package com.example.data.admin

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * App-wide config stored in Firestore.
 * Stored at: users/{adminUid}/app_config/config
 * (Inside user's own collection so Firestore security rules allow write)
 */
data class AppConfig(
    val adminUid: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val contactInstagram: String = "",
    val upiId: String = "",
    val upiName: String = "",
    val subscriptionPrice: String = "₹99/month",
    val appEnabled: Boolean = true,
    val maintenanceMessage: String = ""
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Long = 0,
    val streak: Int = 0,
    val isSubscribed: Boolean = false,
    val subscriptionExpiry: Long = 0L,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class AdminRepository() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AdminRepo"
        private const val USERS_COLLECTION = "users"
        private const val PROFILE_DOC = "profile/profile"
        private const val CONFIG_SUBCOLLECTION = "app_config"
        private const val CONFIG_DOC_NAME = "config"
        // Also store config at root level for public read (any authenticated user)
        private const val ROOT_CONFIG_DOC = "app_config/config"
    }

    /**
     * Fetches app config from Firestore.
     * Tries root-level first, then falls back to admin's user collection.
     */
    suspend fun getAppConfig(): AppConfig {
        return try {
            // Try root-level config first (publicly readable)
            val doc = db.document(ROOT_CONFIG_DOC).get().await()
            if (doc.exists()) {
                Log.d(TAG, "Config found at root level")
                return parseConfig(doc)
            }

            // If no root config, check if current user is admin and has config in their collection
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val userConfigDoc = db.collection(USERS_COLLECTION).document(uid)
                    .collection(CONFIG_SUBCOLLECTION).document(CONFIG_DOC_NAME).get().await()
                if (userConfigDoc.exists()) {
                    Log.d(TAG, "Config found in admin's user collection")
                    return parseConfig(userConfigDoc)
                }
            }

            Log.d(TAG, "No config found anywhere — returning default")
            AppConfig()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app config", e)
            AppConfig()
        }
    }

    private fun parseConfig(doc: com.google.firebase.firestore.DocumentSnapshot): AppConfig {
        return AppConfig(
            adminUid = doc.getString("adminUid") ?: "",
            contactEmail = doc.getString("contactEmail") ?: "",
            contactPhone = doc.getString("contactPhone") ?: "",
            contactInstagram = doc.getString("contactInstagram") ?: "",
            upiId = doc.getString("upiId") ?: "",
            upiName = doc.getString("upiName") ?: "",
            subscriptionPrice = doc.getString("subscriptionPrice") ?: "₹99/month",
            appEnabled = doc.getBoolean("appEnabled") ?: true,
            maintenanceMessage = doc.getString("maintenanceMessage") ?: ""
        )
    }

    /**
     * Saves app config to Firestore.
     * Saves to BOTH root level (for public read) and admin's user collection.
     */
    suspend fun saveAppConfig(config: AppConfig): Boolean {
        return try {
            val data = mapOf(
                "adminUid" to config.adminUid,
                "contactEmail" to config.contactEmail,
                "contactPhone" to config.contactPhone,
                "contactInstagram" to config.contactInstagram,
                "upiId" to config.upiId,
                "upiName" to config.upiName,
                "subscriptionPrice" to config.subscriptionPrice,
                "appEnabled" to config.appEnabled,
                "maintenanceMessage" to config.maintenanceMessage
            )

            // Save to root level (may fail if rules block it — that's OK)
            try {
                db.document(ROOT_CONFIG_DOC).set(data).await()
                Log.d(TAG, "Config saved to root level")
            } catch (e: Exception) {
                Log.w(TAG, "Could not save config to root level (rules may block) — trying user collection", e)
            }

            // Also save to admin's user collection (always works because rules allow user's own data)
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection(USERS_COLLECTION).document(uid)
                    .collection(CONFIG_SUBCOLLECTION).document(CONFIG_DOC_NAME).set(data).await()
                Log.d(TAG, "Config saved to admin's user collection")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save app config", e)
            false
        }
    }

    suspend fun isAdmin(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val config = getAppConfig()
        return config.adminUid == uid
    }

    suspend fun getUserProfile(uid: String): UserProfile {
        return try {
            val doc = db.collection(USERS_COLLECTION).document(uid)
                .collection("profile").document("profile").get().await()
            if (doc.exists()) {
                UserProfile(
                    uid = doc.getString("uid") ?: uid,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    points = doc.getLong("points") ?: 0L,
                    streak = doc.getLong("streak")?.toInt() ?: 0,
                    isSubscribed = doc.getBoolean("isSubscribed") ?: false,
                    subscriptionExpiry = doc.getLong("subscriptionExpiry") ?: 0L,
                    isBlocked = doc.getBoolean("isBlocked") ?: false,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else {
                UserProfile()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile", e)
            UserProfile()
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection(USERS_COLLECTION).document(uid)
                .collection("profile").document("profile").set(
                    mapOf(
                        "uid" to uid,
                        "name" to profile.name,
                        "email" to profile.email,
                        "points" to profile.points,
                        "streak" to profile.streak,
                        "isSubscribed" to profile.isSubscribed,
                        "subscriptionExpiry" to profile.subscriptionExpiry,
                        "isBlocked" to profile.isBlocked,
                        "createdAt" to profile.createdAt
                    )
                ).await()
            Log.d(TAG, "User profile saved for $uid")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user profile", e)
            false
        }
    }

    suspend fun getAllUsers(): List<UserProfile> {
        return try {
            val users = mutableListOf<UserProfile>()
            val userDocs = db.collection(USERS_COLLECTION).get().await()
            for (userDoc in userDocs.documents) {
                try {
                    val profileDoc = userDoc.reference.collection("profile").document("profile").get().await()
                    if (profileDoc.exists()) {
                        users.add(UserProfile(
                            uid = profileDoc.getString("uid") ?: userDoc.id,
                            name = profileDoc.getString("name") ?: "Unknown",
                            email = profileDoc.getString("email") ?: "",
                            points = profileDoc.getLong("points") ?: 0L,
                            streak = profileDoc.getLong("streak")?.toInt() ?: 0,
                            isSubscribed = profileDoc.getBoolean("isSubscribed") ?: false,
                            subscriptionExpiry = profileDoc.getLong("subscriptionExpiry") ?: 0L,
                            isBlocked = profileDoc.getBoolean("isBlocked") ?: false,
                            createdAt = profileDoc.getLong("createdAt") ?: 0L
                        ))
                    }
                } catch (e: Exception) { /* skip */ }
            }
            Log.d(TAG, "Found ${users.size} users")
            users.sortedByDescending { it.points }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all users", e)
            emptyList()
        }
    }

    suspend fun toggleSubscription(uid: String, subscribed: Boolean, expiryDays: Int = 30): Boolean {
        return try {
            val expiry = if (subscribed) System.currentTimeMillis() + (expiryDays * 24 * 60 * 60 * 1000L) else 0L
            db.collection(USERS_COLLECTION).document(uid).collection("profile").document("profile")
                .update(mapOf("isSubscribed" to subscribed, "subscriptionExpiry" to expiry)).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle subscription", e)
            false
        }
    }

    suspend fun toggleBlockUser(uid: String, blocked: Boolean): Boolean {
        return try {
            db.collection(USERS_COLLECTION).document(uid).collection("profile").document("profile")
                .update("isBlocked", blocked).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle block", e)
            false
        }
    }

    suspend fun hasActiveSubscription(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val profile = getUserProfile(uid)
        if (profile.isBlocked) return false
        if (!profile.isSubscribed) return false
        if (profile.subscriptionExpiry > 0 && profile.subscriptionExpiry < System.currentTimeMillis()) return false
        return true
    }
}
