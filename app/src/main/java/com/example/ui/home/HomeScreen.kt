package com.example.ui.home

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.components.AnimatedEntry
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.components.ProgressRing
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatCard
import com.example.ui.theme.Dimens
import com.example.ui.theme.MahirColors
import com.example.ui.theme.StatColors
import com.example.util.rememberMahirHaptics
import java.util.Calendar


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val haptics = rememberMahirHaptics()
    val state by viewModel.fullUiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { com.example.data.SettingsRepository.getInstance(context) }
    val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                MahirBottomNavigation(navController = navController)
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.screenPaddingHorizontal),
                contentPadding = PaddingValues(top = Dimens.screenPaddingTop, bottom = Dimens.screenPaddingBottom),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingXxl)
            ) {
                item {
                    AnimatedEntry(0) {
                        HeaderSection(
                            userName = state.userName,
                            themeMode = themeMode,
                            onThemeToggle = {
                                haptics.tap()
                                val newMode = if (themeMode == "DARK") "LIGHT" else "DARK"
                                settingsRepository.updateThemeMode(newMode)
                            }
                        )
                    }
                }

                item {
                    AnimatedEntry(1) {
                        DailyGoalCard(
                            streak = state.currentStreak,
                            todayMinutes = state.todayFocusMinutes,
                            goalMinutes = state.dailyGoalMinutes,
                            onClick = { haptics.tap() }
                        )
                    }
                }

                item {
                    AnimatedEntry(2) {
                        StatsGrid(
                            studyHours = state.studyHoursOverview,
                            revisionHours = state.revisionHoursOverview,
                            topicsCompleted = state.topicsCompletedOverview,
                            revisionsDone = state.revisionsDoneOverview
                        )
                    }
                }

                item {
                    AnimatedEntry(3) {
                        QuickActions(navController, haptics)
                    }
                }

                if (state.exams.isNotEmpty()) {
                    item {
                        AnimatedEntry(4) {
                            UpcomingExamsCard(exams = state.exams.take(3))
                        }
                    }
                }

                item {
                    AnimatedEntry(5) {
                        RecentActivitySection(state, navController)
                    }
                }

                // Mahir watermark
                item {
                    com.example.ui.components.MahirWatermark()
                }
            }
        }

        com.example.ui.components.AchievementUnlockOverlay(
            achievement = state.newUnlockedAchievement,
            onDismiss = { viewModel.dismissAchievement() }
        )
    }
}

@Composable
fun HeaderSection(userName: String, themeMode: String, onThemeToggle: () -> Unit) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.spacingXs))
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MahirColors.subtleBackground())
        ) {
            val isDark = themeMode == "DARK" || (themeMode == "SYSTEM" && androidx.compose.foundation.isSystemInDarkTheme())
            Icon(
                imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun DailyGoalCard(
    streak: Int,
    todayMinutes: Int,
    goalMinutes: Int,
    onClick: () -> Unit = {}
) {
    val fireScale = if (streak >= 7) {
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.12f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(1500),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            )
        ).value
    } else 1.0f

    val goalFloat = goalMinutes.toFloat().coerceAtLeast(1f)
    val progress = (todayMinutes / goalFloat).coerceIn(0f, 1f)
    val percentText = "${(progress * 100).toInt()}%"

    MahirCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                ProgressRing(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = MahirColors.gold(),
                    strokeWidth = 8f
                )
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MahirColors.gold(),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacingLg))
            // Center text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Today's Goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                Text(
                    text = "$todayMinutes / $goalMinutes min",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                val remaining = (goalMinutes - todayMinutes).coerceAtLeast(0)
                val msg = if (remaining == 0) "Goal achieved! Keep going 🎯" else "$remaining min to go"
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacingMd))
            // Streak badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MahirColors.gold(),
                    modifier = Modifier.size(28.dp).scale(fireScale)
                )
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.titleLarge,
                    color = MahirColors.gold(),
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "day streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsGrid(
    studyHours: String,
    revisionHours: String,
    topicsCompleted: Int,
    revisionsDone: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingLg)
        ) {
            StatCard(
                icon = Icons.Rounded.Timer,
                value = studyHours,
                label = "Today's Focus",
                iconTint = MahirColors.gold(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Rounded.Sync,
                value = revisionHours,
                label = "Rev. Time",
                iconTint = StatColors.blue(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(Dimens.spacingLg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingLg)
        ) {
            StatCard(
                icon = Icons.Rounded.CheckCircle,
                value = topicsCompleted.toString(),
                label = "Topics Done",
                iconTint = StatColors.green(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Rounded.Refresh,
                value = revisionsDone.toString(),
                label = "Revisions",
                iconTint = StatColors.amber(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActions(navController: NavController, haptics: com.example.util.MahirHaptics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        QuickActionPill(
            text = "Focus",
            icon = Icons.Rounded.PlayArrow,
            onClick = {
                haptics.confirm()
                navController.navigate("focus")
            },
            modifier = Modifier.weight(1f),
            isPrimary = true
        )
        QuickActionPill(
            text = "Syllabus",
            icon = Icons.Rounded.MenuBook,
            onClick = {
                haptics.tap()
                navController.navigate("syllabus")
            },
            modifier = Modifier.weight(1f)
        )
        QuickActionPill(
            text = "Mock",
            icon = Icons.Rounded.Quiz,
            onClick = {
                haptics.tap()
                navController.navigate("mocks")
            },
            modifier = Modifier.weight(1f)
        )
        QuickActionPill(
            text = "Plan",
            icon = Icons.Rounded.CalendarMonth,
            onClick = {
                haptics.tap()
                navController.navigate("planner")
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    val bgColor = if (isPrimary) MahirColors.gold() else MahirColors.subtleBackground()
    val contentColor = if (isPrimary) MahirColors.goldForeground() else MaterialTheme.colorScheme.onBackground
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.pillRadius),
        color = bgColor,
        modifier = modifier.height(Dimens.pillHeight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.spacingXs))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun UpcomingExamsCard(exams: List<com.example.data.ExamEntity>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Upcoming Exams")
        MahirCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                exams.forEach { exam ->
                    ExamRow(exam)
                }
            }
        }
    }
}

@Composable
fun ExamRow(exam: com.example.data.ExamEntity) {
    val daysLeft = ((exam.dateMillis - System.currentTimeMillis()) / 86_400_000L).toInt()
    val daysText = when {
        daysLeft < 0 -> "Passed"
        daysLeft == 0 -> "Today"
        daysLeft == 1 -> "Tomorrow"
        else -> "$daysLeft days left"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(StatColors.red().copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Event, contentDescription = null, tint = StatColors.red(), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(Dimens.spacingLg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exam.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = daysText,
                style = MaterialTheme.typography.bodySmall,
                color = if (daysLeft in 0..7) StatColors.red() else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentActivitySection(state: HomeUiState, navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Recent Activity")

        MahirCard {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingLg)) {
                if (state.upcomingRevisions.isEmpty()) {
                    com.example.ui.components.EmptyState(
                        icon = Icons.Rounded.EventNote,
                        title = "No sessions yet",
                        subtitle = "Start your first focus session to see it here",
                        actionText = "Start Focus",
                        onAction = { navController.navigate("focus") }
                    )
                } else {
                    state.upcomingRevisions.take(3).forEach { revision ->
                        ActivityRow(
                            topicName = revision.title,
                            subjectName = revision.subjectName,
                            timeText = "Pending"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityRow(topicName: String, subjectName: String, timeText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(StatColors.blue(), CircleShape)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingMd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topicName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subjectName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = timeText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
