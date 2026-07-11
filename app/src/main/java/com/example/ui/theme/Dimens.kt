package com.example.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp

/**
 * Premium design tokens for MahirVerse — SaaS-level polish.
 * Inspired by Linear, Notion, Vercel design systems.
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

    // Card properties — premium rounded corners + subtle elevation
    val cardRadius: Dp = 16.dp
    val cardRadiusSm: Dp = 12.dp
    val cardRadiusLg: Dp = 24.dp
    val cardPadding: Dp = 16.dp
    val cardPaddingLg: Dp = 20.dp
    val cardBorderWidth: Dp = 1.dp
    val cardElevation: Dp = 0.dp  // Use border instead of shadow (cleaner look)

    // Pill / chip
    val pillRadius: Dp = 999.dp
    val pillHeight: Dp = 36.dp

    // Icon sizes
    val iconXs: Dp = 14.dp
    val iconSm: Dp = 18.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 32.dp
    val iconXl: Dp = 48.dp

    // Bottom nav
    val bottomNavHeight: Dp = 60.dp
    val bottomNavRadius: Dp = 20.dp
    val bottomNavElevation: Dp = 0.dp  // Border + blur instead
}

object AnimTokens {
    const val FAST = 150
    const val MEDIUM = 250
    const val SLOW = 400
    const val STAGGER_MS = 40L
}
