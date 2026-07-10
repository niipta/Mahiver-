package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.SettingsRepository

private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightCard,
    onBackground = LightForeground,
    onSurface = LightForeground,
    primary = LightGold,
    onPrimary = LightGoldForeground,
    primaryContainer = LightGold.copy(alpha = 0.15f),
    onPrimaryContainer = LightGold,
    secondary = LightMuted,
    onSecondary = LightBackground,
    surfaceVariant = LightSecondaryBg,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    outlineVariant = LightMutedBg,
    tertiary = LightGold,
    error = PriorityHigh,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkCard,
    onBackground = DarkForeground,
    onSurface = DarkForeground,
    primary = DarkGold,
    onPrimary = DarkGoldForeground,
    primaryContainer = DarkGold.copy(alpha = 0.15f),
    onPrimaryContainer = DarkGold,
    secondary = DarkMuted,
    onSecondary = DarkBackground,
    surfaceVariant = DarkSecondaryBg,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    outlineVariant = DarkInputBg,
    tertiary = DarkGold,
    error = PriorityHigh,
    onError = Color.White
)

object MahirColors {
    fun gold(isLight: Boolean): Color = if (isLight) LightGold else DarkGold

    fun goldForeground(isLight: Boolean): Color = if (isLight) LightGoldForeground else DarkGoldForeground

    @Composable
    fun gold(): Color {
        val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
        return if (isLight) LightGold else DarkGold
    }

    @Composable
    fun goldForeground(): Color {
        val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
        return if (isLight) LightGoldForeground else DarkGoldForeground
    }

    @Composable
    fun cardBackground(): Color = MaterialTheme.colorScheme.surface

    @Composable
    fun subtleBackground(): Color = MaterialTheme.colorScheme.surfaceVariant
}

@Composable
fun ColorScheme.isLight(): Boolean = this.surface.luminance() > 0.5f

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository.getInstance(context) }
    val themeMode by settingsRepository.themeMode.collectAsState(initial = "SYSTEM")
    val amoledMode by settingsRepository.amoledMode.collectAsState(initial = false)

    val useDarkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> darkTheme
    }

    val colorScheme = when {
        themeMode == "DYNAMIC" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (useDarkTheme) {
                if (amoledMode) {
                    androidx.compose.material3.dynamicDarkColorScheme(context).copy(
                        background = Color.Black,
                        surface = Color(0xFF080808)
                    )
                } else {
                    androidx.compose.material3.dynamicDarkColorScheme(context)
                }
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        useDarkTheme -> {
            if (amoledMode) {
                DarkColorScheme.copy(
                    background = Color.Black,
                    surface = Color(0xFF080808)
                )
            } else {
                DarkColorScheme
            }
        }
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDarkTheme
            controller.isAppearanceLightNavigationBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
