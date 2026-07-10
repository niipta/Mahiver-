package com.example.ui.more

import com.example.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val settings: SettingsRepository
) : ViewModel() {

    val userName: StateFlow<String> = settings.userName
    val themeMode: StateFlow<String> = settings.themeMode
    val amoledMode: StateFlow<Boolean> = settings.amoledMode
    val soundEnabled: StateFlow<Boolean> = settings.soundEnabled
    val vibrationEnabled: StateFlow<Boolean> = settings.vibrationEnabled
    val hapticsEnabled: StateFlow<Boolean> = settings.hapticsEnabled
    val notificationsEnabled: StateFlow<Boolean> = settings.notificationsEnabled
    val autoEnableDnd: StateFlow<Boolean> = settings.autoEnableDnd
    val dailyGoalMinutes: StateFlow<Int> = settings.dailyGoalMinutes
    val dailyGoalTopics: StateFlow<Int> = settings.dailyGoalTopics
    val ambientSoundEnabled: StateFlow<Boolean> = settings.ambientSoundEnabled
    val ambientSoundType: StateFlow<String> = settings.ambientSoundType
    val appLockEnabled: StateFlow<Boolean> = settings.appLockEnabled
    val streakFreezesAvailable: StateFlow<Int> = settings.streakFreezesAvailable

    // AI / Gemini API key
    val geminiApiKey: StateFlow<String> = settings.geminiApiKey

    fun updateUserName(name: String) = settings.updateUserName(name)
    fun updateThemeMode(mode: String) = settings.updateThemeMode(mode)
    fun updateAmoledMode(enabled: Boolean) = settings.updateAmoledMode(enabled)
    fun updateSoundEnabled(enabled: Boolean) = settings.updateSoundEnabled(enabled)
    fun updateVibrationEnabled(enabled: Boolean) = settings.updateVibrationEnabled(enabled)
    fun updateHapticsEnabled(enabled: Boolean) = settings.updateHapticsEnabled(enabled)
    fun updateNotificationsEnabled(enabled: Boolean) = settings.updateNotificationsEnabled(enabled)
    fun updateAutoEnableDnd(enabled: Boolean) = settings.updateAutoEnableDnd(enabled)
    fun updateDailyGoalMinutes(minutes: Int) = settings.updateDailyGoalMinutes(minutes)
    fun updateDailyGoalTopics(count: Int) = settings.updateDailyGoalTopics(count)
    fun updateAmbientSoundEnabled(enabled: Boolean) = settings.updateAmbientSoundEnabled(enabled)
    fun updateAmbientSoundType(type: String) = settings.updateAmbientSoundType(type)
    fun updateAppLockEnabled(enabled: Boolean) = settings.updateAppLockEnabled(enabled)

    // AI key management
    fun updateApiKey(key: String) = settings.updateApiKey(key)
    fun clearApiKey() = settings.clearApiKey()

    fun requestReset() = settings.markResetRequested()
}
