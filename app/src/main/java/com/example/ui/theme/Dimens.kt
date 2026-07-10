package com.example.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp

/**
 * Premium design tokens for MahirVerse.
 * Use these consistently across all screens for a cohesive, premium feel.
 */
object Dimens {
    // Spacing scale (4-pt grid)
    val spacingXs: Dp = 4.dp
    val spacingSm: Dp = 8.dp
    val spacingMd: Dp = 12.dp
    val spacingLg: Dp = 16.dp
    val spacingXl: Dp = 20.dp
    val spacingXxl: Dp = 24.dp
    val spacing3xl: Dp = 32.dp
    val spacing4xl: Dp = 48.dp

    // Screen horizontal padding
    val screenPaddingHorizontal: Dp = 20.dp
    val screenPaddingTop: Dp = 56.dp
    val screenPaddingBottom: Dp = 100.dp

    // Card properties
    val cardRadius: Dp = 20.dp
    val cardRadiusSm: Dp = 14.dp
    val cardRadiusLg: Dp = 28.dp
    val cardPadding: Dp = 18.dp
    val cardPaddingLg: Dp = 22.dp
    val cardBorderWidth: Dp = 1.dp
    val cardElevation: Dp = 0.dp

    // Pill / chip
    val pillRadius: Dp = 999.dp
    val pillHeight: Dp = 40.dp

    // Icon sizes
    val iconXs: Dp = 14.dp
    val iconSm: Dp = 18.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 32.dp
    val iconXl: Dp = 48.dp

    // Bottom nav
    val bottomNavHeight: Dp = 64.dp
    val bottomNavRadius: Dp = 24.dp
    val bottomNavElevation: Dp = 12.dp
}

object AnimTokens {
    const val FAST = 180
    const val MEDIUM = 300
    const val SLOW = 500
    const val STAGGER_MS = 50L
}
