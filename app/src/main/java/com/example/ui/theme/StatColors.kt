package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object StatColors {
    @Composable
    fun blue(): Color = if (MaterialTheme.colorScheme.isLight()) LightInfoBlue else DarkInfoBlue

    @Composable
    fun green(): Color = if (MaterialTheme.colorScheme.isLight()) LightSuccessGreen else DarkSuccessGreen

    @Composable
    fun amber(): Color = if (MaterialTheme.colorScheme.isLight()) PriorityMedium else Color(0xFFFBBF24)

    @Composable
    fun purple(): Color = if (MaterialTheme.colorScheme.isLight()) LightWarmPurple else DarkWarmPurple

    @Composable
    fun red(): Color = if (MaterialTheme.colorScheme.isLight()) LightDangerRed else DarkDangerRed
}
