package com.example.ui.analytics

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Path
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

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            Text(
                                "Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Your study journey in numbers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            coroutineScope.launch { shareAnalyticsText(context, state) }
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // KPI grid — 4 tiles in 2x2
            item {
                AnimatedEntry(1) { KpiGrid(state) }
            }

            // Focus time chart with range selector
            item {
                AnimatedEntry(2) {
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

            // Subject breakdown — horizontal bars
            item {
                AnimatedEntry(3) {
                    SubjectBreakdown(state.subjectsData, state.focusSessions, selectedRange)
                }
            }

            // Subject progress — circular progress rings
            item {
                AnimatedEntry(4) { SubjectProgress(state.subjectsData) }
            }

            // Lifetime stats
            item {
                AnimatedEntry(5) { LifetimeStats(state) }
            }
        }
    }
}

// ============================================================
// KPI GRID — 4 tiles
// ============================================================
@Composable
private fun KpiGrid(state: AnalyticsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiTile(
                value = "${state.lifetimeFocusMinutes / 60}h ${state.lifetimeFocusMinutes % 60}m",
                label = "Total Focus",
                icon = Icons.Rounded.Timer,
                color = MahirColors.gold(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                value = "${state.currentStreak}",
                label = "Day Streak",
                icon = Icons.Rounded.LocalFireDepartment,
                color = Color(0xFFFF6B35),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiTile(
                value = "${state.topicsCompleted}/${state.totalTopics}",
                label = "Topics Done",
                icon = Icons.Rounded.CheckCircle,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                value = "${state.longestStreak}",
                label = "Best Streak",
                icon = Icons.Rounded.WorkspacePremium,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiTile(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    MahirCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============================================================
// FOCUS TIME CHART — Canvas bar chart with range selector
// ============================================================
@Composable
private fun FocusTimeChart(
    data: List<DailyStudyTime>,
    maxValue: Float,
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Focus Time", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            // Range selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("7D", "30D", "90D").forEach { range ->
                    val isSelected = selectedRange == range
                    Surface(
                        onClick = { onRangeSelected(range) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MahirColors.gold() else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            range,
                            color = if (isSelected) MahirColors.goldForeground() else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                EmptyChartState()
            } else {
                BarChartCanvas(data, maxValue)
            }
        }
    }
}

@Composable
private fun EmptyChartState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Complete focus sessions to see analytics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BarChartCanvas(data: List<DailyStudyTime>, maxValue: Float) {
    val barColor = MahirColors.gold()
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
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

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, chartAreaHeight),
                cornerRadius = cornerRadius
            )
            if (barHeight > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = cornerRadius
                )
            }
        }
    }

    // Labels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEachIndexed { index, day ->
            val shouldShow = when {
                data.size <= 7 -> true
                data.size <= 13 -> index % 2 == 0
                else -> index % 3 == 0
            }
            Text(
                text = if (shouldShow) day.dayName.take(6) else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================
// SUBJECT BREAKDOWN — horizontal progress bars
// ============================================================
@Composable
private fun SubjectBreakdown(
    subjectsData: List<SubjectWithTopics>,
    focusSessions: List<FocusSessionEntity>,
    selectedRange: String
) {
    val rangeDays = when (selectedRange) {
        "30D" -> 30L
        "90D" -> 90L
        else -> 7L
    }

    val cutoffTime = System.currentTimeMillis() - (rangeDays * 24 * 60 * 60 * 1000L)
    val validSessions = focusSessions.filter {
        it.timestamp >= cutoffTime && (it.sessionType == "Focus" || it.sessionType == "Study")
    }

    val subjectTimes = validSessions.groupBy { session ->
        subjectsData.find { swt -> swt.topics.any { t -> t.topic.id == session.topicId } }?.subject?.name ?: "Unknown"
    }.mapValues { it.value.sumOf { s -> s.actualDurationSeconds } / 60L }

    val totalTime = subjectTimes.values.sum()
    val sortedSubjects = subjectTimes.entries.sortedByDescending { it.value }
    val top5 = sortedSubjects.take(5)
    val others = sortedSubjects.drop(5)
    val othersTime = others.sumOf { it.value }

    val displayList = top5.toMutableList()
    if (othersTime > 0) {
        displayList.add(java.util.AbstractMap.SimpleEntry("Other", othersTime))
    }

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

                    val colorHex = if (subjectName == "Other") 0xFF888888L else
                        (subjectsData.find { it.subject.name == subjectName }?.subject?.color ?: 0xFF888888L)
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
// SUBJECT PROGRESS — circular progress rings (Canvas, no RadarChart)
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini circular progress
                        val trackColor = MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 3.dp.toPx()
                                val radius = (size.minDimension / 2f) - (strokeWidth / 2f)
                                val center = Offset(size.width / 2f, size.height / 2f)

                                drawCircle(
                                    color = trackColor,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = strokeWidth)
                                )
                                if (progress > 0f) {
                                    drawArc(
                                        color = subjectColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                }
                            }
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
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
// LIFETIME STATS — clean list
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
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
            appendLine("Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine()
            appendLine("Total Focus: ${state.lifetimeFocusMinutes / 60}h ${state.lifetimeFocusMinutes % 60}m")
            appendLine("Current Streak: ${state.currentStreak} days")
            appendLine("Longest Streak: ${state.longestStreak} days")
            appendLine("Topics: ${state.topicsCompleted}/${state.totalTopics}")
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
