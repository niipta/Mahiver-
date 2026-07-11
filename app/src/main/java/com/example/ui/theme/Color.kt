package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// PREMIUM SAAS COLOR SYSTEM
// Inspired by Linear, Notion, Vercel — clean, modern, professional
// ============================================================

// ===== Premium Light Theme =====
val LightBackground = Color(0xFFFAFAFA)        // Near-white (warm-neutral)
val LightCard = Color(0xFFFFFFFF)              // Pure white card
val LightForeground = Color(0xFF0F172A)        // Slate-900 (rich ink)
val LightMuted = Color(0xFF64748B)             // Slate-500 (cool muted)
val LightBorder = Color(0xFFE2E8F0)            // Slate-200 (soft border)
val LightGold = Color(0xFFB08433)              // Brand gold (refined)
val LightGoldForeground = Color(0xFFFFFFFF)
val LightSecondaryBg = Color(0xFFF1F5F9)       // Slate-100 (subtle fill)
val LightMutedBg = Color(0xFFF8FAFC)           // Slate-50 (whisper bg)
val LightSuccessGreen = Color(0xFF10B981)      // Emerald-500
val LightDangerRed = Color(0xFFEF4444)         // Red-500
val LightInfoBlue = Color(0xFF3B82F6)          // Blue-500
val LightWarmPurple = Color(0xFF8B5CF6)        // Violet-500

// ===== Premium Dark Theme =====
val DarkBackground = Color(0xFF09090B)         // Zinc-950 (true dark)
val DarkCard = Color(0xFF18181B)               // Zinc-900 (card surface)
val DarkForeground = Color(0xFFFAFAFA)         // Zinc-50 (clean white text)
val DarkMuted = Color(0xFFA1A1AA)              // Zinc-400 (readable muted)
val DarkBorder = Color(0xFF27272A)             // Zinc-800 (subtle border)
val DarkGold = Color(0xFFD4A853)               // Warm gold (premium accent)
val DarkGoldForeground = Color(0xFF09090B)
val DarkSecondaryBg = Color(0xFF1F1F23)        // Slightly elevated
val DarkInputBg = Color(0xFF27272A)            // Input field bg
val DarkSuccessGreen = Color(0xFF34D399)
val DarkDangerRed = Color(0xFFF87171)
val DarkInfoBlue = Color(0xFF60A5FA)
val DarkWarmPurple = Color(0xFFA78BFA)

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
