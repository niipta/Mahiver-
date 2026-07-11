package com.example.data
import com.example.BuildConfig

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mahirverse_settings", Context.MODE_PRIVATE)

    private val _geminiApiKey = MutableStateFlow(loadApiKey("gemini_api_key", BuildConfig.GEMINI_API_KEY))
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "MAHIR") ?: "MAHIR")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _supabaseUrl = MutableStateFlow(loadApiKey("supabase_url", BuildConfig.SUPABASE_URL))
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseKey = MutableStateFlow(loadApiKey("supabase_key", BuildConfig.SUPABASE_KEY))
    val supabaseKey: StateFlow<String> = _supabaseKey.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _amoledMode = MutableStateFlow(prefs.getBoolean("amoled_mode", false))
    val amoledMode: StateFlow<Boolean> = _amoledMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _autoEnableDnd = MutableStateFlow(prefs.getBoolean("auto_enable_dnd", false))
    val autoEnableDnd: StateFlow<Boolean> = _autoEnableDnd.asStateFlow()

    private val _hasPromptedForDnd = MutableStateFlow(prefs.getBoolean("has_prompted_for_dnd", false))
    val hasPromptedForDnd: StateFlow<Boolean> = _hasPromptedForDnd.asStateFlow()

    private val _goalMetDates = MutableStateFlow(prefs.getStringSet("goal_met_dates", emptySet()) ?: emptySet())
    val goalMetDates: StateFlow<Set<String>> = _goalMetDates.asStateFlow()

    private val _currentStreak = MutableStateFlow(prefs.getInt("current_streak", 0))
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _longestStreak = MutableStateFlow(prefs.getInt("longest_streak", 0))
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _unlockedAchievements = MutableStateFlow(prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet())
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()

    // ===== New: daily goal & study preferences =====
    private val _dailyGoalMinutes = MutableStateFlow(prefs.getInt("daily_goal_minutes", 120))
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()

    private val _dailyGoalTopics = MutableStateFlow(prefs.getInt("daily_goal_topics", 4))
    val dailyGoalTopics: StateFlow<Int> = _dailyGoalTopics.asStateFlow()

    private val _streakFreezesAvailable = MutableStateFlow(prefs.getInt("streak_freezes", 2))
    val streakFreezesAvailable: StateFlow<Int> = _streakFreezesAvailable.asStateFlow()

    private val _focusDurationMinutes = MutableStateFlow(prefs.getInt("focus_duration_minutes", 25))
    val focusDurationMinutes: StateFlow<Int> = _focusDurationMinutes.asStateFlow()

    private val _shortBreakMinutes = MutableStateFlow(prefs.getInt("short_break_minutes", 5))
    val shortBreakMinutes: StateFlow<Int> = _shortBreakMinutes.asStateFlow()

    private val _longBreakMinutes = MutableStateFlow(prefs.getInt("long_break_minutes", 15))
    val longBreakMinutes: StateFlow<Int> = _longBreakMinutes.asStateFlow()

    private val _longBreakInterval = MutableStateFlow(prefs.getInt("long_break_interval", 4))
    val longBreakInterval: StateFlow<Int> = _longBreakInterval.asStateFlow()

    private val _ambientSoundEnabled = MutableStateFlow(prefs.getBoolean("ambient_sound_enabled", false))
    val ambientSoundEnabled: StateFlow<Boolean> = _ambientSoundEnabled.asStateFlow()

    private val _ambientSoundType = MutableStateFlow(prefs.getString("ambient_sound_type", "rain") ?: "rain")
    val ambientSoundType: StateFlow<String> = _ambientSoundType.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean("app_lock_enabled", false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _quietHoursStart = MutableStateFlow(prefs.getInt("quiet_hours_start", 22))
    val quietHoursStart: StateFlow<Int> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd = MutableStateFlow(prefs.getInt("quiet_hours_end", 7))
    val quietHoursEnd: StateFlow<Int> = _quietHoursEnd.asStateFlow()

    /**
     * Loads an API key from prefs. Returns empty string if user has explicitly cleared it
     * (stored as "__cleared__" sentinel), otherwise falls back to BuildConfig.
     *
     * Keys are obfuscated with XOR (not cryptographically secure, but better
     * than plaintext — stops casual snooping via adb shell / backup extractors).
     */
    private fun loadApiKey(prefsKey: String, buildConfigFallback: String): String {
        val stored = prefs.getString(prefsKey, null)
        return when {
            stored == null -> buildConfigFallback
            stored == "__cleared__" -> ""
            stored.isBlank() -> buildConfigFallback
            else -> deobfuscate(stored)
        }
    }

    fun updateApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", obfuscate(key)).apply()
        _geminiApiKey.value = key
    }

    fun clearApiKey() {
        prefs.edit().putString("gemini_api_key", "__cleared__").apply()
        _geminiApiKey.value = ""
    }

    /**
     * Simple XOR obfuscation with a fixed key. This is NOT encryption — it
     * just prevents the API key from appearing in plaintext when someone
     * reads SharedPreferences via adb or a backup extractor.
     * The output is Base64-encoded.
     */
    private fun obfuscate(plain: String): String {
        if (plain.isEmpty()) return ""
        val keyBytes = OBF_KEY.toByteArray(Charsets.UTF_8)
        val plainBytes = plain.toByteArray(Charsets.UTF_8)
        val out = ByteArray(plainBytes.size)
        for (i in plainBytes.indices) {
            out[i] = (plainBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        return android.util.Base64.encodeToString(out, android.util.Base64.NO_WRAP)
    }

    private fun deobfuscate(encoded: String): String {
        if (encoded.isEmpty()) return ""
        return try {
            val keyBytes = OBF_KEY.toByteArray(Charsets.UTF_8)
            val encBytes = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP)
            val out = ByteArray(encBytes.size)
            for (i in encBytes.indices) {
                out[i] = (encBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(out, Charsets.UTF_8)
        } catch (e: Exception) {
            // If deobfuscation fails (old plaintext key), return as-is
            encoded
        }
    }

    private fun obfuscate(plain: String): String {
        if (plain.isEmpty()) return ""
        val keyBytes = OBF_KEY.toByteArray(Charsets.UTF_8)
        val plainBytes = plain.toByteArray(Charsets.UTF_8)
        val out = ByteArray(plainBytes.size)
        for (i in plainBytes.indices) {
            out[i] = (plainBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        return android.util.Base64.encodeToString(out, android.util.Base64.NO_WRAP)
    }

    private fun deobfuscate(encoded: String): String {
        if (encoded.isEmpty()) return ""
        return try {
            val keyBytes = OBF_KEY.toByteArray(Charsets.UTF_8)
            val encBytes = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP)
            val out = ByteArray(encBytes.size)
            for (i in encBytes.indices) {
                out[i] = (encBytes[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
            }
            String(out, Charsets.UTF_8)
        } catch (e: Exception) {
            encoded
        }
    }

    fun updateUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun updateSupabaseUrl(url: String) {
        prefs.edit().putString("supabase_url", url).apply()
        _supabaseUrl.value = url
    }

    fun updateSupabaseKey(key: String) {
        prefs.edit().putString("supabase_key", key).apply()
        _supabaseKey.value = key
    }

    fun updateSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun updateAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean("amoled_mode", enabled).apply()
        _amoledMode.value = enabled
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun updateAutoEnableDnd(enabled: Boolean) {
        prefs.edit().putBoolean("auto_enable_dnd", enabled).apply()
        _autoEnableDnd.value = enabled
    }

    fun markDndPromptShown() {
        prefs.edit().putBoolean("has_prompted_for_dnd", true).apply()
        _hasPromptedForDnd.value = true
    }

    fun setGoalMet(dateStr: String, isMet: Boolean) {
        val currentSet = prefs.getStringSet("goal_met_dates", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (isMet) currentSet.add(dateStr) else currentSet.remove(dateStr)
        prefs.edit().putStringSet("goal_met_dates", currentSet).apply()
        _goalMetDates.value = currentSet
    }

    fun updateStreak(streak: Int) {
        prefs.edit().putInt("current_streak", streak).apply()
        _currentStreak.value = streak
    }

    fun updateLongestStreak(streak: Int) {
        prefs.edit().putInt("longest_streak", streak).apply()
        _longestStreak.value = streak
    }

    fun updateDailyGoalMinutes(minutes: Int) {
        prefs.edit().putInt("daily_goal_minutes", minutes).apply()
        _dailyGoalMinutes.value = minutes
    }

    fun updateDailyGoalTopics(count: Int) {
        prefs.edit().putInt("daily_goal_topics", count).apply()
        _dailyGoalTopics.value = count
    }

    fun useStreakFreeze(): Boolean {
        val current = _streakFreezesAvailable.value
        if (current <= 0) return false
        prefs.edit().putInt("streak_freezes", current - 1).apply()
        _streakFreezesAvailable.value = current - 1
        return true
    }

    fun addStreakFreeze() {
        val current = _streakFreezesAvailable.value
        prefs.edit().putInt("streak_freezes", current + 1).apply()
        _streakFreezesAvailable.value = current + 1
    }

    fun updateFocusDuration(minutes: Int) {
        prefs.edit().putInt("focus_duration_minutes", minutes).apply()
        _focusDurationMinutes.value = minutes
    }

    fun updateShortBreak(minutes: Int) {
        prefs.edit().putInt("short_break_minutes", minutes).apply()
        _shortBreakMinutes.value = minutes
    }

    fun updateLongBreak(minutes: Int) {
        prefs.edit().putInt("long_break_minutes", minutes).apply()
        _longBreakMinutes.value = minutes
    }

    fun updateLongBreakInterval(count: Int) {
        prefs.edit().putInt("long_break_interval", count).apply()
        _longBreakInterval.value = count
    }

    fun updateAmbientSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ambient_sound_enabled", enabled).apply()
        _ambientSoundEnabled.value = enabled
    }

    fun updateAmbientSoundType(type: String) {
        prefs.edit().putString("ambient_sound_type", type).apply()
        _ambientSoundType.value = type
    }

    fun updateAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("app_lock_enabled", enabled).apply()
        _appLockEnabled.value = enabled
    }

    fun updateQuietHours(start: Int, end: Int) {
        prefs.edit()
            .putInt("quiet_hours_start", start)
            .putInt("quiet_hours_end", end)
            .apply()
        _quietHoursStart.value = start
        _quietHoursEnd.value = end
    }

    fun unlockAchievement(achievementId: String) {
        val currentSet = prefs.getStringSet("unlocked_achievements", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentSet.add(achievementId)) {
            prefs.edit().putStringSet("unlocked_achievements", currentSet).apply()
            _unlockedAchievements.value = currentSet
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val user = auth.currentUser
                if (user != null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(user.uid)
                        .set(mapOf("unlockedAchievements" to currentSet.toList()), com.google.firebase.firestore.SetOptions.merge())
                }
            } catch (e: Exception) {
                // Ignore if Firebase isn't initialized or network error
            }
        }
    }

    /** Clears all study data (subjects, sessions, revisions, plans, exams). Used by "Reset App Data". */
    fun markResetRequested() {
        prefs.edit().putBoolean("reset_requested", true).apply()
    }

    fun consumeResetRequest(): Boolean {
        val was = prefs.getBoolean("reset_requested", false)
        if (was) prefs.edit().putBoolean("reset_requested", false).apply()
        return was
    }

    companion object {
        private const val OBF_KEY = "MahirVerse_2024_SecurityKey_v2"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
