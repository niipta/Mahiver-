package com.example.data.admin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * App-wide config stored in Firestore at `app_config/config`.
 * Admin can edit this from the AdminScreen.
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

/**
 * User profile stored in Firestore at `users/{uid}/profile/profile`.
 * Used for leaderboard + subscription management.
 */
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

/**
 * Repository for admin operations via Firestore.
 */
class AdminRepository @javax.inject.Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val CONFIG_DOC = "app_config/config"
        private const val USERS_COLLECTION = "users"
        private const val PROFILE_DOC = "profile/profile"
        private const val LEADERBOARD_COLLECTION = "leaderboard"
    }

    /** Fetches app config. Returns default if not found. */
    suspend fun getAppConfig(): AppConfig {
        return try {
            val doc = db.document(CONFIG_DOC).get().await()
            if (doc.exists()) {
                AppConfig(
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
            } else {
                AppConfig()
            }
        } catch (e: Exception) {
            AppConfig()
        }
    }

    /** Saves app config (admin only). */
    suspend fun saveAppConfig(config: AppConfig): Boolean {
        return try {
            db.document(CONFIG_DOC).set(
                mapOf(
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
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Checks if current user is admin. */
    suspend fun isAdmin(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val config = getAppConfig()
        return config.adminUid == uid
    }

    /** Gets current user's profile from Firestore. */
    suspend fun getUserProfile(uid: String): UserProfile {
        return try {
            val doc = db.collection(USERS_COLLECTION).document(uid).collection("profile").document("profile").get().await()
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
            UserProfile()
        }
    }

    /** Updates current user's profile (points, streak, name). */
    suspend fun updateUserProfile(profile: UserProfile): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection(USERS_COLLECTION).document(uid).collection("profile").document("profile").set(
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
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Admin: gets all user profiles for the admin dashboard. */
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
            users.sortedByDescending { it.points }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Admin: toggles user subscription. */
    suspend fun toggleSubscription(uid: String, subscribed: Boolean, expiryDays: Int = 30): Boolean {
        return try {
            val expiry = if (subscribed) System.currentTimeMillis() + (expiryDays * 24 * 60 * 60 * 1000L) else 0L
            db.collection(USERS_COLLECTION).document(uid).collection("profile").document("profile")
                .update(mapOf("isSubscribed" to subscribed, "subscriptionExpiry" to expiry)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Admin: blocks/unblocks a user. */
    suspend fun toggleBlockUser(uid: String, blocked: Boolean): Boolean {
        return try {
            db.collection(USERS_COLLECTION).document(uid).collection("profile").document("profile")
                .update("isBlocked", blocked).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Gets leaderboard from Firestore (all users sorted by points). */
    suspend fun getLeaderboard(): List<UserProfile> {
        return getAllUsers()
    }

    /** Checks if current user has active subscription. */
    suspend fun hasActiveSubscription(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val profile = getUserProfile(uid)
        if (profile.isBlocked) return false
        if (!profile.isSubscribed) return false
        if (profile.subscriptionExpiry > 0 && profile.subscriptionExpiry < System.currentTimeMillis()) {
            return false // expired
        }
        return true
    }
}
