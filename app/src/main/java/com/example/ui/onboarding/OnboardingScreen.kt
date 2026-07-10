package com.example.ui.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.Dimens
import com.example.ui.theme.MahirColors
import com.example.util.rememberMahirHaptics
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(navController: NavController) {
    val haptics = rememberMahirHaptics()
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OnboardingPage(
                        icon = Icons.Rounded.School,
                        title = "MahirVerse",
                        description = "Master your study, master your life. A premium companion for serious learners.",
                        iconTint = MahirColors.gold()
                    )
                    1 -> OnboardingPage(
                        icon = Icons.Rounded.Timer,
                        title = "Focus Sessions",
                        description = "Built-in Pomodoro timer with DND, ambient sounds, and detailed analytics for deep work.",
                        iconTint = MahirColors.gold()
                    )
                    2 -> OnboardingPage(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "Syllabus & Spaced Repetition",
                        description = "Track topics, subtopics, and let our SM-2-inspired scheduler keep your memory sharp.",
                        iconTint = MahirColors.gold()
                    )
                    3 -> OnboardingPage(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "AI Study Planner",
                        description = "Get personalized daily plans powered by Gemini. Study smarter, not harder.",
                        iconTint = MahirColors.gold()
                    )
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacingXxl, vertical = Dimens.spacing3xl)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pager Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MahirColors.gold() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pagerState.currentPage < 3) {
                        TextButton(onClick = {
                            val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("onboarding_complete", true).apply()
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }) {
                            Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.size(Dimens.spacingSm))
                    }
                    AnimatedVisibility(
                        visible = pagerState.currentPage == 3,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Button(
                            onClick = {
                                haptics.confirm()
                                val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putBoolean("onboarding_complete", true).apply()
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MahirColors.gold(),
                                contentColor = MahirColors.goldForeground()
                            ),
                            shape = RoundedCornerShape(Dimens.cardRadiusSm)
                        ) {
                            Text("Get Started", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (pagerState.currentPage < 3) {
                        Button(
                            onClick = {
                                haptics.tap()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(Dimens.cardRadiusSm)
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacing4xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing icon background
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(iconTint.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = iconTint
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.spacing3xl))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.spacingLg))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = Dimens.spacingLg)
        )
    }
}
