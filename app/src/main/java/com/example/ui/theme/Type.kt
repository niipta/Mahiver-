package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val frauncesName = GoogleFont("Fraunces")
val FrauncesFamily = FontFamily(
    Font(googleFont = frauncesName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = frauncesName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = frauncesName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = frauncesName, fontProvider = provider, weight = FontWeight.Black)
)

val interName = GoogleFont("Inter")
val InterFamily = FontFamily(
    Font(googleFont = interName, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = interName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = interName, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    // Page Title (h1)
    headlineSmall = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.8.sp // 24 * 1.2
    ),
    // Stat Value
    titleLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp // 20 * 1.2
    ),
    // Card Title
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 19.5.sp // 15 * 1.3
    ),
    // Section Title
    titleSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.2.sp // 14 * 1.3
    ),
    // Body Text
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp // 14 * 1.5
    ),
    // Secondary Text
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.8.sp // 12 * 1.4
    ),
    // Caption / Label
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.3.sp // 11 * 1.3
    ),
    // Tab Label
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp // 10 * 1.2
    ),
    // Timer Display
    displayLarge = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        lineHeight = 48.sp // 48 * 1.0
    )
)

// Timer tabular nums style (we will just use the displayLarge)
val MahirverseMono = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Light,
    fontSize = 48.sp,
    lineHeight = 48.sp
)
