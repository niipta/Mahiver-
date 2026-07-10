package com.example.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wrapper around Compose's HapticFeedback that respects the user's
 * "haptics_enabled" preference. Provides differentiated haptic patterns
 * for tap / confirm / reject / success / selection.
 */
class MahirHaptics(
    private val hapticFeedback: HapticFeedback,
    private val coroutineScope: CoroutineScope,
    private val hapticsEnabled: () -> Boolean
) {
    fun tap() {
        if (!hapticsEnabled()) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun selection() {
        if (!hapticsEnabled()) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun confirm() {
        if (!hapticsEnabled()) return
        coroutineScope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(60)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun success() {
        if (!hapticsEnabled()) return
        coroutineScope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(80)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(80)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun reject() {
        if (!hapticsEnabled()) return
        coroutineScope.launch {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(120)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(120)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberMahirHaptics(): MahirHaptics {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val settings = remember { SettingsRepository.getInstance(context) }
    return remember(haptic, scope) {
        MahirHaptics(haptic, scope) { settings.hapticsEnabled.value }
    }
}
