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
            // Loading state: show a simple loading indicator while data
            // is being fetched from Room on cold start. This prevents the
            // "blank screen" perception during late loads.
            val isInitialLoad = state.userName == "MAHIR" && state.lifetimeFocusMinutes == 0 && state.subjects.isEmpty()
            if (isInitialLoad) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = MahirColors.gold()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Loading your study data…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
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

                // === AI SMART PLAN SECTION ===
                item {
                    AnimatedEntry(4) {
                        AiSmartPlanSection(
                            state = state,
                            onGenerate = {
                                haptics.confirm()
                                viewModel.generateSmartPlan()
                            }
                        )
                    }
                }

                // === DAILY MOTIVATIONAL QUOTE ===
                item {
                    AnimatedEntry(5) {
                        DailyQuoteCard(quote = state.dailyQuote)
                    }
                }

                // === AI PERSONAL ANALYSIS ===
                item {
                    AnimatedEntry(6) {
                        AiAnalysisSection(
                            state = state,
                            onGenerate = {
                                haptics.confirm()
                                viewModel.generateAnalysis()
                            }
                        )
                    }
                }

                if (state.exams.isNotEmpty()) {
                    item {
                        AnimatedEntry(7) {
                            UpcomingExamsCard(exams = state.exams.take(3))
                        }
                    }
                }

                item {
                    AnimatedEntry(8) {
                        RecentActivitySection(state, navController)
                    }
                }

                // Mahir watermark
                item {
                    com.example.ui.components.MahirWatermark()
                }
            }
            } // end else (loading state)
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
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val isDark = themeMode == "DARK" || (themeMode == "SYSTEM" && androidx.compose.foundation.isSystemInDarkTheme())
            Icon(
                imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = "Toggle Theme",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
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

// ============================================================
// AI SMART PLAN SECTION
// ============================================================
@Composable
fun AiSmartPlanSection(
    state: HomeUiState,
    onGenerate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("AI Smart Plan")
            Surface(
                onClick = onGenerate,
                shape = RoundedCornerShape(12.dp),
                color = StatColors.purple(),
                enabled = !state.aiPlanLoading
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.aiPlanLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (state.aiPlanLoading) "Generating…" else "Generate",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        when {
            // Error state
            state.error != null -> {
                MahirCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = StatColors.red(),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            state.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Loading state with no prior results
            state.aiPlanLoading && state.suggestedTopics.isEmpty() -> {
                MahirCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = StatColors.purple()
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "AI is analysing your study data…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Results available
            state.suggestedTopics.isNotEmpty() || state.priorities.isNotBlank() -> {
                MahirCard {
                    Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                        // Priorities message
                        if (state.priorities.isNotBlank() && state.priorities != "Review your pending tasks") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = StatColors.amber(), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    state.priorities,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Suggested topics
                        if (state.suggestedTopics.isNotEmpty()) {
                            Text(
                                "Suggested Topics",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            state.suggestedTopics.forEach { topic ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(StatColors.purple(), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        topic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        // Weak topics
                        if (state.weakTopics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Weak Topics Detected",
                                style = MaterialTheme.typography.labelMedium,
                                color = StatColors.red(),
                                fontWeight = FontWeight.SemiBold
                            )
                            state.weakTopics.forEach { topic ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = StatColors.red(), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        topic,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Default empty state
            else -> {
                MahirCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = StatColors.purple(),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Get an AI-powered study plan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Analyses your syllabus, revisions & focus time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// DAILY MOTIVATIONAL QUOTE CARD
// ============================================================
@Composable
fun DailyQuoteCard(quote: String) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Rounded.FormatQuote,
                contentDescription = null,
                tint = StatColors.purple().copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "— Daily Motivation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// AI PERSONAL ANALYSIS SECTION
// ============================================================
@Composable
fun AiAnalysisSection(
    state: HomeUiState,
    onGenerate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("AI Coach")
            Surface(
                onClick = onGenerate,
                shape = RoundedCornerShape(12.dp),
                color = StatColors.blue(),
                enabled = !state.analysisLoading
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.analysisLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (state.analysisLoading) "Analysing…" else "Analyse Me",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        when {
            // Loading state with no prior results
            state.analysisLoading && state.analysisSummary == null -> {
                MahirCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = StatColors.blue()
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "AI is analysing your study patterns…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Results available
            state.analysisSummary != null -> {
                MahirCard {
                    Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                        // Summary
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = StatColors.blue(), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                state.analysisSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Strengths
                        if (state.analysisStrengths.isNotEmpty()) {
                            Text(
                                "Strengths",
                                style = MaterialTheme.typography.labelMedium,
                                color = StatColors.green(),
                                fontWeight = FontWeight.Bold
                            )
                            state.analysisStrengths.forEach { strength ->
                                Text(
                                    "• $strength",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Improvements
                        if (state.analysisImprovements.isNotEmpty()) {
                            Text(
                                "Needs Work",
                                style = MaterialTheme.typography.labelMedium,
                                color = StatColors.amber(),
                                fontWeight = FontWeight.Bold
                            )
                            state.analysisImprovements.forEach { improvement ->
                                Text(
                                    "• $improvement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        // Tonight's task
                        state.tonightTask?.let { task ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MahirColors.gold().copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = MahirColors.gold(), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Tonight: $task",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Motivational message
                        state.motivationalMessage?.let { msg ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Default empty state
            else -> {
                MahirCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = StatColors.blue(),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Get your personal AI analysis",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Strengths, improvements & tonight's task",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
