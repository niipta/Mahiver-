package com.example.ui.analytics

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.example.ui.components.AnimatedEntry
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import com.example.data.SubjectWithTopics
import com.example.data.FocusSessionEntity



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
        bottomBar = {
            MahirBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 56.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AnimatedEntry(0) {
                    HeaderSection(onShareClick = {
                        coroutineScope.launch {
                            shareAnalyticsText(context, state)
                        }
                    })
                }
            }

            item {
                AnimatedEntry(1) {
                    KeyMetricsRow(
                        totalHours = (state.lifetimeFocusMinutes / 60).toInt(),
                        streakDays = state.currentStreak
                    )
                }
            }

            item {
                AnimatedEntry(2) {
                    // FIX: consistent ordering — oldest→newest across all ranges
                    val data = when (selectedRange) {
                        "30D" -> state.monthlyStudyData
                        "90D" -> state.quarterlyStudyData
                        else -> state.weeklyStudyData
                    }
                    val maxMinutes = data.maxOfOrNull { it.minutes.toFloat() } ?: 1f
                    val normalizedMax = if (maxMinutes == 0f) 1f else maxMinutes
                    MainChartCard(
                        data = data,
                        maxValue = normalizedMax,
                        selectedRange = selectedRange,
                        onRangeSelected = { selectedRange = it }
                    )
                }
            }
            
            item {
                AnimatedEntry(3) {
                    SubjectBreakdownCard(
                        subjectsData = state.subjectsData,
                        focusSessions = state.focusSessions,
                        selectedRange = selectedRange
                    )
                }
            }

            item {
                AnimatedEntry(4) {
                    StatsList(
                        productivityScore = calculateProductivityScore(state),
                        longestStreak = state.longestStreak,
                        topicsMastered = state.topicsCompleted
                    )
                }
            }
            
            item {
                AnimatedEntry(5) {
                    val radarData = remember(state.subjectsData) {
                        val mapped = state.subjectsData.map { swt ->
                            val progress = if (swt.totalTopics > 0) swt.completedTopics.toFloat() / swt.totalTopics else 0f
                            val color = try {
                                Color(swt.subject.color)
                            } catch (e: Exception) {
                                Color.Gray
                            }
                            com.example.ui.components.RadarData(swt.subject.name, progress, color)
                        }.sortedByDescending { it.progress }

                        if (mapped.size > 6) {
                            val top5 = mapped.take(5)
                            val rest = mapped.drop(5)
                            val otherProgress = if (rest.isNotEmpty()) rest.map { it.progress }.average().toFloat() else 0f
                            top5 + com.example.ui.components.RadarData("Other", otherProgress, Color.Gray)
                        } else {
                            mapped
                        }
                    }

                    MahirCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Subject Balance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(16.dp))
                            com.example.ui.components.RadarChart(data = radarData)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Use FlowRow for legend since it could wrap
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                radarData.forEach { data ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(data.color))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(data.label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun shareAnalyticsText(context: android.content.Context, state: AnalyticsUiState) {
    try {
        val report = buildString {
            appendLine("📊 MahirVerse Analytics Report")
            appendLine("Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine()
            appendLine("⏱ Study Time: ${state.lifetimeFocusMinutes / 60}h ${state.lifetimeFocusMinutes % 60}m")
            appendLine("🔥 Current Streak: ${state.currentStreak} days")
            appendLine("🏆 Longest Streak: ${state.longestStreak} days")
            appendLine("📚 Topics Mastered: ${state.topicsCompleted}/${state.totalTopics}")
            appendLine("📝 Revisions Done: ${state.focusSessions.count { it.sessionType == "Revision" }}")
            appendLine()
            if (state.weakSubjects.isNotEmpty()) {
                appendLine("⚠️ Weak Subjects: ${state.weakSubjects.joinToString(", ")}")
            }
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

private fun calculateProductivityScore(state: AnalyticsUiState): Int {
    val score = (state.topicsCompleted * 10) + (state.lifetimeFocusMinutes / 60 * 5)
    return (score.toInt()).coerceIn(0, 1000)
}

@Composable
fun HeaderSection(onShareClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your study journey in numbers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onShareClick) {
            Icon(Icons.Rounded.Share, contentDescription = "Export as Image", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun KeyMetricsRow(totalHours: Int, streakDays: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MetricCard(
            title = "Study Time",
            value = "${totalHours}h",
            icon = Icons.Rounded.Timer,
            color = Color(0xFF3B82F6),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Consistency",
            value = "$streakDays Days",
            icon = Icons.Rounded.LocalFireDepartment,
            color = MahirColors.gold(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MahirColors.subtleBackground(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MainChartCard(data: List<DailyStudyTime>, maxValue: Float, selectedRange: String, onRangeSelected: (String) -> Unit) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Focus Time",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Segmented control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MahirColors.subtleBackground(), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("7D", "30D", "90D").forEach { range ->
                    val isSelected = selectedRange == range
                    Surface(
                        onClick = { onRangeSelected(range) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MahirColors.gold() else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = range,
                            color = if (isSelected) MahirColors.goldForeground() else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            val barColor = MahirColors.gold()
            val trackColor = MaterialTheme.colorScheme.outlineVariant
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                if (data.isEmpty()) {
                    com.example.ui.components.EmptyState(
                        icon = Icons.Rounded.BarChart,
                        title = "No data yet",
                        subtitle = "Complete focus sessions to see your analytics"
                    )
                } else {
                    data.forEach { day ->
                        val ratio = if (maxValue > 0) (day.minutes / maxValue).coerceIn(0f, 1f) else 0f
                        val animatedRatio by animateFloatAsState(
                            targetValue = ratio,
                            animationSpec = tween(800, easing = FastOutSlowInEasing),
                            label = "barRatio"
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                // Background track
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .background(trackColor, RoundedCornerShape(8.dp))
                                )
                                // Active bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedRatio)
                                        .background(barColor, RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = day.dayName.take(6), 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectBreakdownCard(subjectsData: List<SubjectWithTopics>, focusSessions: List<FocusSessionEntity>, selectedRange: String) {
    val rangeDays = when (selectedRange) {
        "30D" -> 30L
        "90D" -> 90L
        else -> 7L
    }
    
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
    if (othersTime > 0) {
        displayList.add(java.util.AbstractMap.SimpleEntry("Other", othersTime))
    }

    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Time by Subject",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (displayList.isEmpty()) {
                com.example.ui.components.EmptyState(
                    icon = Icons.Rounded.PieChart,
                    title = "No data yet",
                    subtitle = "Complete focus sessions for your subjects"
                )
            } else {
                displayList.forEach { entry ->
                    val subjectName = entry.key
                    val minutes = entry.value
                    val ratio = if (totalTime > 0) minutes.toFloat() / totalTime else 0f
                    
                    val colorHex = if (subjectName == "Other") 0xFF888888L else 
                        (subjectsData.find { it.subject.name == subjectName }?.subject?.color ?: 0xFF888888L)
                    val barColor = Color(colorHex.toULong())
                    
                    val animatedRatio by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(800, easing = FastOutSlowInEasing),
                        label = "breakdownRatio"
                    )
                    
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = subjectName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "${minutes / 60}h ${minutes % 60}m", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(animatedRatio).fillMaxHeight().background(barColor, RoundedCornerShape(4.dp)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsList(productivityScore: Int, longestStreak: Int, topicsMastered: Int) {
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            StatListRow(
                icon = Icons.Rounded.Stars,
                title = "Productivity Score",
                value = productivityScore.toString(),
                color = Color(0xFF8A74F9) // Purple
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))
            StatListRow(
                icon = Icons.Rounded.WorkspacePremium,
                title = "Longest Streak",
                value = "$longestStreak Days",
                color = MahirColors.gold()
            )
            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))
            StatListRow(
                icon = Icons.Rounded.School,
                title = "Topics Mastered",
                value = topicsMastered.toString(),
                color = Color(0xFF10B981) // Green
            )
        }
    }
}

@Composable
fun StatListRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
