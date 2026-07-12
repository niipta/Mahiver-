package com.example.ui.analytics

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.FocusSessionEntity
import com.example.data.SubjectWithTopics
import com.example.ui.components.AnimatedEntry
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedDay by remember { mutableStateOf<CalendarDay?>(null) }
    var selectedRange by remember { mutableStateOf("7D") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { MahirBottomNavigation(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 56.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                AnimatedEntry(0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Analytics", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                            Text("Your study journey in numbers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = {
                            coroutineScope.launch { shareAnalyticsText(context, state) }
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // KPI grid — 2x2
            item {
                AnimatedEntry(1) { KpiGrid(state) }
            }

            // Points + Streak Freezes banner
            item {
                AnimatedEntry(2) { PointsBanner(state) }
            }

            // Calendar Heatmap (35 days)
            item {
                AnimatedEntry(3) {
                    CalendarHeatmap(
                        days = state.calendarDays,
                        onDayClick = { selectedDay = it }
                    )
                }
            }

            // Focus time bar chart with range selector
            item {
                AnimatedEntry(4) {
                    val data = when (selectedRange) {
                        "30D" -> state.monthlyStudyData
                        "90D" -> state.quarterlyStudyData
                        else -> state.weeklyStudyData
                    }
                    val maxMinutes = data.maxOfOrNull { it.minutes.toFloat() } ?: 1f
                    val normalizedMax = if (maxMinutes == 0f) 1f else maxMinutes
                    FocusTimeChart(data, normalizedMax, selectedRange) { selectedRange = it }
                }
            }

            // Subject breakdown
            item {
                AnimatedEntry(5) {
                    SubjectBreakdown(state.subjectsData, state.focusSessions, selectedRange)
                }
            }

            // Subject progress
            item {
                AnimatedEntry(6) { SubjectProgress(state.subjectsData) }
            }

            // Leaderboard
            item {
                AnimatedEntry(7) { LeaderboardSection(state.leaderboard) }
            }

            // Lifetime stats
            item {
                AnimatedEntry(8) { LifetimeStats(state) }
            }
        }
    }

    // Day detail bottom sheet
    selectedDay?.let { day ->
        DayDetailSheet(day = day, onDismiss = { selectedDay = null })
    }
}

// ============================================================
// KPI GRID
// ============================================================
@Composable
private fun KpiGrid(state: AnalyticsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiTile("${state.lifetimeFocusMinutes / 60}h ${state.lifetimeFocusMinutes % 60}m", "Total Focus", Icons.Rounded.Timer, MahirColors.gold(), Modifier.weight(1f))
            KpiTile("${state.currentStreak}", "Day Streak", Icons.Rounded.LocalFireDepartment, Color(0xFFFF6B35), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiTile("${state.topicsCompleted}/${state.totalTopics}", "Topics Done", Icons.Rounded.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
            KpiTile("${state.longestStreak}", "Best Streak", Icons.Rounded.WorkspacePremium, Color(0xFF8B5CF6), Modifier.weight(1f))
        }
    }
}

@Composable
private fun KpiTile(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    MahirCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============================================================
// POINTS BANNER — shows total points + streak freeze info
// ============================================================
@Composable
private fun PointsBanner(state: AnalyticsUiState) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Stars, contentDescription = null, tint = MahirColors.gold(), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${state.totalPoints}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = MahirColors.gold())
                    Text(" pts", style = MaterialTheme.typography.titleMedium, color = MahirColors.gold())
                }
                Text("Total Points", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AcUnit, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${state.streakFreezesAvailable}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                }
                Text("Streak Freezes (4 free/month)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ============================================================
// CALENDAR HEATMAP — 35 days (5 weeks x 7 days)
// ============================================================
@Composable
private fun CalendarHeatmap(days: List<CalendarDay>, onDayClick: (CalendarDay) -> Unit) {
    val today = System.currentTimeMillis()
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")

    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Study Calendar", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tap a day to see details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            if (days.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Week day labels
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    weekDays.forEach { day ->
                        Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                // 5 weeks x 7 days
                for (week in 0 until 5) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (dayIdx in 0 until 7) {
                            val index = week * 7 + dayIdx
                            if (index < days.size) {
                                val day = days[index]
                                val intensity = when {
                                    day.minutes == 0L -> 0f
                                    day.minutes < 30 -> 0.2f
                                    day.minutes < 60 -> 0.4f
                                    day.minutes < 120 -> 0.6f
                                    day.minutes < 180 -> 0.8f
                                    else -> 1f
                                }
                                val cellColor = if (intensity == 0f) {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                } else {
                                    MahirColors.gold().copy(alpha = intensity)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(cellColor)
                                        .clickable { onDayClick(day) }
                                ) {
                                    if (day.isToday) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(1.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color.Transparent)
                                        )
                                    }
                                    if (day.minutes > 0) {
                                        Text(
                                            "${day.minutes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            color = if (intensity > 0.5f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).padding(2.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Legend
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    listOf(0.2f, 0.4f, 0.6f, 0.8f, 1f).forEach { intensity ->
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MahirColors.gold().copy(alpha = intensity)))
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("More", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ============================================================
// DAY DETAIL SHEET — shows when user taps a calendar cell
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(day: CalendarDay, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(dateFormat.format(Date(day.dateMillis)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            if (day.minutes == 0L) {
                Text("No study session on this day.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                // Total time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = MahirColors.gold(), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${day.minutes / 60}h ${day.minutes % 60}m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MahirColors.gold())
                    Text(" total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Subject breakdown
                Text("Subjects Studied", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                day.subjects.forEach { subject ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(try { Color(subject.color.toInt()) } catch (e: Exception) { Color.Gray }))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(subject.subjectName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                        Text("${subject.minutes / 60}h ${subject.minutes % 60}m", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ============================================================
// FOCUS TIME CHART
// ============================================================
@Composable
private fun FocusTimeChart(data: List<DailyStudyTime>, maxValue: Float, selectedRange: String, onRangeSelected: (String) -> Unit) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Focus Time", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("7D", "30D", "90D").forEach { range ->
                    val isSelected = selectedRange == range
                    Surface(onClick = { onRangeSelected(range) }, shape = RoundedCornerShape(10.dp), color = if (isSelected) MahirColors.gold() else Color.Transparent, modifier = Modifier.weight(1f)) {
                        Text(range, color = if (isSelected) MahirColors.goldForeground() else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val barColor = MahirColors.gold()
                val trackColor = MaterialTheme.colorScheme.outlineVariant
                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val barCount = data.size
                    if (barCount == 0) return@Canvas
                    val totalWidth = size.width
                    val totalHeight = size.height
                    val labelAreaHeight = 20.dp.toPx()
                    val chartAreaHeight = totalHeight - labelAreaHeight
                    val barSpacing = 3.dp.toPx()
                    val totalSpacing = barSpacing * (barCount + 1)
                    val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
                    val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    data.forEachIndexed { index, day ->
                        val ratio = if (maxValue > 0f) (day.minutes / maxValue).coerceIn(0f, 1f) else 0f
                        val barHeight = chartAreaHeight * ratio
                        val x = barSpacing + index * (barWidth + barSpacing)
                        val y = chartAreaHeight - barHeight
                        drawRoundRect(color = trackColor, topLeft = Offset(x, 0f), size = Size(barWidth, chartAreaHeight), cornerRadius = cornerRadius)
                        if (barHeight > 0) {
                            drawRoundRect(color = barColor, topLeft = Offset(x, y), size = Size(barWidth, barHeight), cornerRadius = cornerRadius)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    data.forEachIndexed { index, day ->
                        val shouldShow = when { data.size <= 7 -> true; data.size <= 13 -> index % 2 == 0; else -> index % 3 == 0 }
                        Text(text = if (shouldShow) day.dayName.take(6) else "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ============================================================
// SUBJECT BREAKDOWN
// ============================================================
@Composable
private fun SubjectBreakdown(subjectsData: List<SubjectWithTopics>, focusSessions: List<FocusSessionEntity>, selectedRange: String) {
    val rangeDays = when (selectedRange) { "30D" -> 30L; "90D" -> 90L; else -> 7L }
    val cutoffTime = System.currentTimeMillis() - (rangeDays * 24 * 60 * 60 * 1000L)
    val validSessions = focusSessions.filter { it.timestamp >= cutoffTime && (it.sessionType == "Focus" || it.sessionType == "Study") }
    val subjectTimes = validSessions.groupBy { session ->
        subjectsData.find { swt -> swt.topics.any { t -> t.topic.id == session.topicId } }?.subject?.name ?: "Unknown"
    }.mapValues { it.value.sumOf { s -> s.actualDurationSeconds } / 60L }
    val totalTime = subjectTimes.values.sum()
    val sortedSubjects = subjectTimes.entries.sortedByDescending { it.value }
    val top5 = sortedSubjects.take(5)
    val others = sortedSubjects.drop(5)
    val othersTime = others.sumOf { it.value }
    val displayList = top5.toMutableList()
    if (othersTime > 0) displayList.add(java.util.AbstractMap.SimpleEntry("Other", othersTime))

    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Time by Subject", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(14.dp))
            if (displayList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("No subject data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                displayList.forEach { entry ->
                    val subjectName = entry.key
                    val minutes = entry.value
                    val ratio = if (totalTime > 0) minutes.toFloat() / totalTime else 0f
                    val colorHex = if (subjectName == "Other") 0xFF888888L else (subjectsData.find { it.subject.name == subjectName }?.subject?.color ?: 0xFF888888L)
                    val barColor = try { Color(colorHex.toInt()) } catch (e: Exception) { Color.Gray }
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(subjectName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                            Text("${minutes / 60}h ${minutes % 60}m", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(3.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(ratio).fillMaxHeight().background(barColor, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// SUBJECT PROGRESS — circular rings
// ============================================================
@Composable
private fun SubjectProgress(subjectsData: List<SubjectWithTopics>) {
    val safeData = subjectsData.filter { it.topics.isNotEmpty() || it.subject.name.isNotBlank() }.take(6)
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Subject Progress", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(14.dp))
            if (safeData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("Add subjects to track progress", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                safeData.forEach { swt ->
                    val total = swt.totalTopics
                    val completed = swt.completedTopics
                    val progress = if (total > 0) completed.toFloat() / total else 0f
                    val subjectColor = try { Color(swt.subject.color.toInt()) } catch (e: Exception) { MahirColors.gold() }
                    val trackColor = MaterialTheme.colorScheme.outlineVariant
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 3.dp.toPx()
                                val radius = (size.minDimension / 2f) - (strokeWidth / 2f)
                                val center = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(color = trackColor, radius = radius, center = center, style = Stroke(width = strokeWidth))
                                if (progress > 0f) {
                                    drawArc(color = subjectColor, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2), style = Stroke(width = strokeWidth))
                                }
                            }
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(swt.subject.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            Text("$completed/$total topics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// LEADERBOARD
// ============================================================
@Composable
private fun LeaderboardSection(leaderboard: List<LeaderboardEntry>) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = MahirColors.gold(), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Leaderboard", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Compete with other students", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            if (leaderboard.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("No leaderboard data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                leaderboard.take(10).forEach { entry ->
                    val rankColor = when (entry.rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val bgAlpha = if (entry.isCurrentUser) 0.15f else 0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (entry.isCurrentUser) MahirColors.gold().copy(alpha = bgAlpha) else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(rankColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${entry.rank}", style = MaterialTheme.typography.labelMedium, color = rankColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))

                        // Name + streak
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.name + if (entry.isCurrentUser) " (You)" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (entry.isCurrentUser) MahirColors.gold() else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF6B35), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${entry.streak} day streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            }
                        }

                        // Points
                        Text("${entry.points}", style = MaterialTheme.typography.titleSmall, color = if (entry.isCurrentUser) MahirColors.gold() else MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                        Text(" pts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ============================================================
// LIFETIME STATS
// ============================================================
@Composable
private fun LifetimeStats(state: AnalyticsUiState) {
    val productivityScore = ((state.topicsCompleted * 10) + (state.lifetimeFocusMinutes / 60 * 5)).coerceIn(0, 1000)
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            StatRow(Icons.Rounded.Stars, "Productivity Score", productivityScore.toString(), Color(0xFF8B5CF6))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 10.dp))
            StatRow(Icons.Rounded.WorkspacePremium, "Longest Streak", "${state.longestStreak} days", MahirColors.gold())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 10.dp))
            StatRow(Icons.Rounded.School, "Topics Mastered", state.topicsCompleted.toString(), Color(0xFF10B981))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 10.dp))
            StatRow(Icons.Rounded.Schedule, "Revision Time", "${state.totalRevisionMinutes / 60}h ${state.totalRevisionMinutes % 60}m", Color(0xFF3B82F6))
        }
    }
}

@Composable
private fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
    }
}

// ============================================================
// SHARE
// ============================================================
fun shareAnalyticsText(context: android.content.Context, state: AnalyticsUiState) {
    try {
        val report = buildString {
            appendLine("MahirVerse Analytics Report")
            appendLine("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}")
            appendLine()
            appendLine("Total Focus: ${state.lifetimeFocusMinutes / 60}h ${state.lifetimeFocusMinutes % 60}m")
            appendLine("Current Streak: ${state.currentStreak} days")
            appendLine("Longest Streak: ${state.longestStreak} days")
            appendLine("Topics: ${state.topicsCompleted}/${state.totalTopics}")
            appendLine("Points: ${state.totalPoints}")
            appendLine()
            appendLine("— Shared from MahirVerse")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "MahirVerse Analytics Report")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        context.startActivity(Intent.createChooser(intent, "Share Analytics"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
