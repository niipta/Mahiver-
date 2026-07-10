package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Premium Light Theme =====
val LightBackground = Color(0xFFFAF9F7)        // Warm off-white (paper)
val LightCard = Color(0xFFFFFFFF)              // Pure white card
val LightForeground = Color(0xFF15171C)        // Near-black ink
val LightMuted = Color(0xFF6E7178)             // Muted gray
val LightBorder = Color(0xFFE8E5E0)            // Soft border
val LightGold = Color(0xFFB08433)              // Refined gold (less saturated)
val LightGoldForeground = Color(0xFFFFFFFF)
val LightSecondaryBg = Color(0xFFF1EEE9)       // Cream secondary
val LightMutedBg = Color(0xFFF4F1EC)           // Subtle background
val LightSuccessGreen = Color(0xFF0F9D58)
val LightDangerRed = Color(0xFFD93025)
val LightInfoBlue = Color(0xFF1A73E8)
val LightWarmPurple = Color(0xFF8E44AD)

// ===== Premium Dark Theme =====
val DarkBackground = Color(0xFF0A0B10)         // Deep ink-black
val DarkCard = Color(0xFF12141C)               // Card with slight elevation
val DarkForeground = Color(0xFFEDEAE3)         // Warm off-white text
val DarkMuted = Color(0xFF848893)              // Cool muted gray
val DarkBorder = Color(0x14FFFFFF)             // rgba(255,255,255,0.08)
val DarkGold = Color(0xFFD4A853)               // Warm gold (premium)
val DarkGoldForeground = Color(0xFF0A0B10)
val DarkSecondaryBg = Color(0xFF1A1D27)        // Elevated secondary
val DarkInputBg = Color(0x14FFFFFF)            // rgba(255,255,255,0.08)
val DarkSuccessGreen = Color(0xFF34D399)
val DarkDangerRed = Color(0xFFF87171)
val DarkInfoBlue = Color(0xFF60A5FA)
val DarkWarmPurple = Color(0xFFB984FF)

// ===== Priority Badge Colors =====
val PriorityHigh = Color(0xFFEF4444)
val PriorityMedium = Color(0xFFF59E0B)
val PriorityLow = Color(0xFF10B981)

// ===== Legacy Compatibility =====
val ProgressGreen = Color(0xFF10B981)
val ProgressOrange = Color(0xFFF59E0B)
val ProgressCyan = Color(0xFF3B82F6)
val ProgressPurple = Color(0xFFA855F7)

// ===== Subject Color Palette (user-selectable) =====
val SubjectPalette = listOf(
    Color(0xFFEF4444) to Color(0xFFF87171),   // Red
    Color(0xFFF59E0B) to Color(0xFFFBBF24),   // Amber
    Color(0xFF10B981) to Color(0xFF34D399),   // Green
    Color(0xFF3B82F6) to Color(0xFF60A5FA),   // Blue
    Color(0xFF8B5CF6) to Color(0xFFA78BFA),   // Violet
    Color(0xFFEC4899) to Color(0xFFF472B6),   // Pink
    Color(0xFF14B8A6) to Color(0xFF2DD4BF),   // Teal
    Color(0xFFF97316) to Color(0xFFFB923C),   // Orange
    Color(0xFF6366F1) to Color(0xFF818CF8),   // Indigo
    Color(0xFF84CC16) to Color(0xFFA3E635)    // Lime
)
// Same colors as Long literals (for SubjectEntity.color field, which is Long)
val SubjectPaletteLongs = listOf(
    0xFFEF4444L, 0xFFF59E0BL, 0xFF10B981L, 0xFF3B82F6L, 0xFF8B5CF6L,
    0xFFEC4899L, 0xFF14B8A6L, 0xFFF97316L, 0xFF6366F1L, 0xFF84CC16L
)
