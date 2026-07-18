package com.example.ui.mocks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.ErrorCategory
import com.example.data.ErrorCategoryBreakdown
import com.example.data.MockCategory
import com.example.data.MockTestEntity
import com.example.data.MockTestWithQuestions
import com.example.data.SmartRecommendation
import com.example.data.SubjectAnalytics
import com.example.data.SubjectWithTopics
import com.example.data.TopicWeightage
import com.example.ui.components.AnimatedEntry
import com.example.ui.components.EmptyState
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.components.SectionTitle
import com.example.ui.theme.Dimens
import com.example.ui.theme.MahirColors
import com.example.ui.theme.StatColors
import com.example.util.rememberMahirHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MocksScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MocksViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val haptics = rememberMahirHaptics()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingAnalysisFor by remember { mutableStateOf<MockTestWithQuestions?>(null) }
    var deletingTest by remember { mutableStateOf<MockTestEntity?>(null) }

    // Search + category filter (local UI state — does not trigger DB requery)
    var searchQuery by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredMocks by remember(state.mockTests, searchQuery, categoryFilter) {
        derivedStateOf {
            state.mockTests.filter { mwt ->
                val matchesCategory = categoryFilter == null || mwt.mockTest.category == categoryFilter
                val matchesSearch = searchQuery.isBlank() ||
                    mwt.mockTest.title.contains(searchQuery, ignoreCase = true) ||
                    mwt.mockTest.subjectName.contains(searchQuery, ignoreCase = true) ||
                    mwt.mockTest.tags.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { MahirBottomNavigation(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPaddingHorizontal),
            contentPadding = PaddingValues(top = Dimens.screenPaddingTop, bottom = Dimens.screenPaddingBottom),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLg)
        ) {
            // === HEADER ===
            item(key = "header") {
                AnimatedEntry(0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Mock Tests",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Analysis Planner — log mocks, find weak spots, improve.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = {
                                haptics.tap()
                                showAddDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MahirColors.gold()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = MahirColors.goldForeground(), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Mock", color = MahirColors.goldForeground(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // === KPI GRID ===
            item(key = "kpis") {
                AnimatedEntry(1) {
                    KpiGrid(state)
                }
            }

            // === SMART RECOMMENDATIONS ===
            if (analytics.recommendations.isNotEmpty()) {
                item(key = "recommendations") {
                    AnimatedEntry(2) {
                        SmartRecommendationsSection(analytics.recommendations)
                    }
                }
            }

            // === ERROR PATTERN ANALYSIS ===
            if (analytics.errorBreakdown.isNotEmpty()) {
                item(key = "error_pattern") {
                    AnimatedEntry(3) {
                        ErrorPatternSection(analytics.errorBreakdown)
                    }
                }
            }

            // === TOPIC WEIGHTAGE ===
            if (analytics.topicWeightage.isNotEmpty()) {
                item(key = "topic_weightage") {
                    AnimatedEntry(4) {
                        TopicWeightageSection(analytics.topicWeightage)
                    }
                }
            }

            // === SUBJECT PERFORMANCE ===
            if (analytics.subjectAnalytics.isNotEmpty()) {
                item(key = "subject_performance") {
                    AnimatedEntry(5) {
                        SubjectPerformanceSection(analytics.subjectAnalytics)
                    }
                }
            }

            // === SEARCH + FILTER CHIPS ===
            item(key = "filter_bar") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search mocks by title, subject, tag…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item(key = "all") {
                            FilterChip(
                                selected = categoryFilter == null,
                                onClick = { categoryFilter = null },
                                label = { Text("All") }
                            )
                        }
                        items(MockCategory.ALL, key = { it }) { cat ->
                            FilterChip(
                                selected = categoryFilter == cat,
                                onClick = { categoryFilter = if (categoryFilter == cat) null else cat },
                                label = { Text(MockCategory.label(cat)) }
                            )
                        }
                    }
                }
            }

            // === MOCK LIST ===
            item(key = "mocks_title") {
                SectionTitle("Logged Mocks (${filteredMocks.size})")
            }

            if (state.mockTests.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.Quiz,
                        title = "No mocks logged yet",
                        subtitle = "Log your first mock test to unlock analysis, error patterns and smart recommendations.",
                        actionText = "Add Mock",
                        onAction = { showAddDialog = true },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else if (filteredMocks.isEmpty()) {
                item(key = "no_matches") {
                    Text(
                        "No mocks match your filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filteredMocks, key = { it.mockTest.id }) { mwt ->
                    AnimatedEntry(6) {
                        MockTestCard(
                            item = mwt,
                            onViewAnalysis = {
                                haptics.tap()
                                viewingAnalysisFor = mwt
                            },
                            onDelete = {
                                haptics.reject()
                                deletingTest = it
                            }
                        )
                    }
                }
            }
        }
    }

    // === Add Mock Dialog ===
    if (showAddDialog) {
        AddMockDialog(
            subjects = state.subjects,
            onDismiss = { showAddDialog = false },
            onCreate = { data ->
                viewModel.addMockTest(
                    title = data.title,
                    category = data.category,
                    subjectId = data.subjectId,
                    subjectName = data.subjectName,
                    totalQuestions = data.totalQuestions,
                    durationMinutes = data.durationMinutes,
                    totalMarks = data.totalMarks,
                    positiveMark = data.positiveMark,
                    negativeMark = data.negativeMark,
                    marksObtained = data.marksObtained,
                    correctCount = data.correctCount,
                    wrongCount = data.wrongCount,
                    unattemptedCount = data.unattemptedCount,
                    actualDurationSeconds = data.actualDurationSeconds,
                    percentile = data.percentile,
                    rank = data.rank,
                    totalCandidates = data.totalCandidates,
                    attemptedAt = data.attemptedAt,
                    description = data.description,
                    tags = data.tags,
                    questions = data.questions
                )
                showAddDialog = false
            }
        )
    }

    // === Mock Analysis Bottom Sheet ===
    viewingAnalysisFor?.let { mwt ->
        MockAnalysisSheet(
            mockWithQuestions = mwt,
            onDismiss = { viewingAnalysisFor = null }
        )
    }

    // === Delete confirmation ===
    deletingTest?.let { test ->
        AlertDialog(
            onDismissRequest = { deletingTest = null },
            title = { Text("Delete Mock") },
            text = { Text("Delete '${test.title}'? All question logs for this mock will also be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMockTest(test)
                    deletingTest = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingTest = null }) { Text("Cancel") } }
        )
    }
}

// ============================================================
// KPI GRID — 6 tiles
// ============================================================
@Composable
private fun KpiGrid(state: MocksUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            KpiTile(
                value = if (state.averagePercentage > 0f) "%.1f%%".format(state.averagePercentage) else "—",
                label = "Avg Score",
                icon = Icons.Rounded.Insights,
                color = StatColors.blue(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                value = if (state.bestPercentage > 0f) "%.1f%%".format(state.bestPercentage) else "—",
                label = "Best Score",
                icon = Icons.Rounded.EmojiEvents,
                color = StatColors.amber(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                value = if (state.averagePercentile > 0f) "%.1f".format(state.averagePercentile) else "—",
                label = "Avg %ile",
                icon = Icons.Rounded.TrendingUp,
                color = StatColors.purple(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
        ) {
            KpiTile(
                value = if (state.bestPercentile > 0f) "%.1f".format(state.bestPercentile) else "—",
                label = "Best %ile",
                icon = Icons.Rounded.MilitaryTech,
                color = StatColors.green(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                value = state.totalQuestionCount.toString(),
                label = "Questions",
                icon = Icons.Rounded.Quiz,
                color = MahirColors.gold(),
                modifier = Modifier.weight(1f)
            )
            KpiTile(
                value = formatDuration(state.totalMockTimeSeconds),
                label = "Practice",
                icon = Icons.Rounded.Timer,
                color = StatColors.red(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
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
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============================================================
// SMART RECOMMENDATIONS
// ============================================================
@Composable
private fun SmartRecommendationsSection(recommendations: List<SmartRecommendation>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
        SectionTitle("Smart Recommendations")
        recommendations.forEach { rec ->
            RecommendationCard(rec)
        }
    }
}

@Composable
private fun RecommendationCard(rec: SmartRecommendation) {
    val color = Color(rec.colorLong.toInt())
    val icon = when (rec.iconName) {
        "warning" -> Icons.Rounded.WarningAmber
        "menu_book" -> Icons.Rounded.MenuBook
        "lightbulb" -> Icons.Rounded.Lightbulb
        "timer" -> Icons.Rounded.Timer
        "target" -> Icons.Rounded.GpsFixed
        "school" -> Icons.Rounded.School
        "check_circle" -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.TipsAndUpdates
    }
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    rec.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    rec.description.replace("%%", "%"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Priority badge
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rec.priority.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================
// ERROR PATTERN ANALYSIS — horizontal stacked bar + legend
// ============================================================
@Composable
private fun ErrorPatternSection(breakdown: List<ErrorCategoryBreakdown>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
        SectionTitle("Error Pattern Analysis")
        MahirCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Stacked bar — precompute colors outside Canvas (DrawScope is not @Composable)
                val total = breakdown.sumOf { it.count }.toFloat().coerceAtLeast(1f)
                val barColors = breakdown.map { ErrorCategory.color(it.category) to it.count }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    var startX = 0f
                    barColors.forEach { (color, count) ->
                        val width = size.width * (count / total)
                        drawRect(
                            color = color,
                            topLeft = Offset(startX, 0f),
                            size = Size(width, size.height)
                        )
                        startX += width
                    }
                }
                // Legend
                breakdown.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(ErrorCategory.color(item.category))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            ErrorCategory.label(item.category),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${item.count}  •  %.0f%%".format(item.percentage),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// TOPIC WEIGHTAGE — table with accuracy bars + WEAK TOPIC tags
// ============================================================
@Composable
private fun TopicWeightageSection(topics: List<com.example.data.TopicWeightage>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
        SectionTitle("Topic Weightage")
        topics.take(10).forEach { topic ->
            TopicWeightageRow(topic)
        }
    }
}

@Composable
private fun TopicWeightageRow(topic: com.example.data.TopicWeightage) {
    val accuracyColor = when {
        topic.accuracy >= 75f -> StatColors.green()
        topic.accuracy >= 60f -> StatColors.amber()
        else -> StatColors.red()
    }
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        topic.topicName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        topic.subjectName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (topic.isWeak) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StatColors.red().copy(alpha = 0.15f)
                    ) {
                        Text(
                            "WEAK TOPIC",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatColors.red(),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            // Accuracy bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(topic.accuracy / 100f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accuracyColor)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${topic.totalQuestions} Q", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${topic.correctCount} ✓", style = MaterialTheme.typography.labelSmall, color = StatColors.green())
                Text("${topic.wrongCount} ✗", style = MaterialTheme.typography.labelSmall, color = StatColors.red())
                Text("${topic.unattemptedCount} –", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.0f%% acc".format(topic.accuracy), style = MaterialTheme.typography.labelSmall, color = accuracyColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ============================================================
// SUBJECT PERFORMANCE CARDS
// ============================================================
@Composable
private fun SubjectPerformanceSection(subjects: List<com.example.data.SubjectAnalytics>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
        SectionTitle("Subject Performance")
        subjects.forEach { subj ->
            SubjectPerformanceCard(subj)
        }
    }
}

@Composable
private fun SubjectPerformanceCard(subj: com.example.data.SubjectAnalytics) {
    val accuracyColor = when {
        subj.accuracy >= 75f -> StatColors.green()
        subj.accuracy >= 50f -> StatColors.amber()
        else -> StatColors.red()
    }
    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accuracyColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        subj.subjectName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = accuracyColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(subj.subjectName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("${subj.totalQuestions} questions logged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("%.0f%%".format(subj.accuracy), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accuracyColor)
            }
            // Breakdown bar — precompute colors outside Canvas (DrawScope is not @Composable)
            val attempted = subj.correctCount + subj.wrongCount + subj.unattemptedCount
            if (attempted > 0) {
                val greenColor = StatColors.green()
                val redColor = StatColors.red()
                val skipColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                ) {
                    val correctW = size.width * subj.correctCount / attempted
                    val wrongW = size.width * subj.wrongCount / attempted
                    drawRect(greenColor, Offset(0f, 0f), Size(correctW, size.height))
                    drawRect(redColor, Offset(correctW, 0f), Size(wrongW, size.height))
                    drawRect(skipColor, Offset(correctW + wrongW, 0f), Size(size.width - correctW - wrongW, size.height))
                }
            }
        }
    }
}

// ============================================================
// MOCK TEST CARD
// ============================================================
@Composable
private fun MockTestCard(
    item: MockTestWithQuestions,
    onViewAnalysis: () -> Unit,
    onDelete: (MockTestEntity) -> Unit
) {
    val test = item.mockTest
    val percent = test.percentage
    val percentColor = when {
        percent >= 75f -> StatColors.green()
        percent >= 50f -> StatColors.amber()
        else -> StatColors.red()
    }

    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // % circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(percentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "%.0f%%".format(percent),
                        style = MaterialTheme.typography.labelMedium,
                        color = percentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        test.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            test.subjectName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MahirColors.gold().copy(alpha = 0.12f)
                        ) {
                            Text(
                                MockCategory.label(test.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = MahirColors.gold(),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                IconButton(onClick = { onDelete(test) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetaPill(label = "Marks", value = "%.1f/%d".format(test.marksObtained, test.totalMarks))
                MetaPill(label = "Correct", value = test.correctCount.toString())
                MetaPill(label = "Wrong", value = test.wrongCount.toString())
                MetaPill(label = "Skipped", value = test.unattemptedCount.toString())
            }

            if (test.percentile > 0f || test.rank > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (test.percentile > 0f) {
                        MetaPill(label = "%ile", value = "%.1f".format(test.percentile))
                    }
                    if (test.rank > 0) {
                        MetaPill(label = "Rank", value = "#${test.rank}")
                    }
                    MetaPill(label = "Time", value = formatDuration(test.actualDurationSeconds))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    df.format(Date(test.attemptedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onViewAnalysis,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
                ) {
                    Icon(Icons.Rounded.Analytics, contentDescription = null, modifier = Modifier.size(16.dp), tint = MahirColors.goldForeground())
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Analysis", color = MahirColors.goldForeground())
                }
            }
        }
    }
}

@Composable
private fun MetaPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================
// ADD MOCK DIALOG (with Question Log Editor)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMockDialog(
    subjects: List<SubjectWithTopics>,
    onDismiss: () -> Unit,
    onCreate: (AddMockData) -> Unit
) {
    // Basic fields
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf<String?>(null) }
    var subjectName by remember { mutableStateOf("General") }
    var category by remember { mutableStateOf(MockCategory.FULL) }
    var totalQuestions by remember { mutableStateOf("30") }
    var durationMinutes by remember { mutableStateOf("60") }
    var totalMarks by remember { mutableStateOf("30") }
    var positiveMark by remember { mutableStateOf("1") }
    var negativeMark by remember { mutableStateOf("0") }
    var marksGot by remember { mutableStateOf("") }
    var correct by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf("") }
    var skipped by remember { mutableStateOf("") }
    var actualMinutes by remember { mutableStateOf("60") }
    var actualSeconds by remember { mutableStateOf("0") }
    var percentile by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("") }
    var candidates by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var attemptedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var subjectExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Question log editor state
    val questionLogs = remember { mutableStateListOf<QuestionLogInput>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AddCircle, contentDescription = null, tint = MahirColors.gold())
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Mock Test", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Physics Full Mock #1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                )

                // Subject dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("General") }, onClick = {
                            subjectName = "General"; subjectId = null; subjectExpanded = false
                        })
                        subjects.forEach { swt ->
                            DropdownMenuItem(text = { Text(swt.subject.name) }, onClick = {
                                subjectName = swt.subject.name
                                subjectId = swt.subject.id
                                subjectExpanded = false
                            })
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = MockCategory.label(category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        MockCategory.ALL.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(MockCategory.label(cat)) },
                                onClick = { category = cat; categoryExpanded = false }
                            )
                        }
                    }
                }

                // Date button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text("Date: ${df.format(Date(attemptedAt))}")
                }

                // Numbers row 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Total Q", totalQuestions, { totalQuestions = it }, Modifier.weight(1f))
                    NumField("Duration (m)", durationMinutes, { durationMinutes = it }, Modifier.weight(1f))
                    NumField("Marks", totalMarks, { totalMarks = it }, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("+ Mark", positiveMark, { positiveMark = it }, Modifier.weight(1f), decimal = true)
                    NumField("- Mark", negativeMark, { negativeMark = it }, Modifier.weight(1f), decimal = true)
                    NumField("Marks Got", marksGot, { marksGot = it }, Modifier.weight(1f), decimal = true)
                }

                // Score breakdown
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Correct", correct, { correct = it }, Modifier.weight(1f))
                    NumField("Wrong", wrong, { wrong = it }, Modifier.weight(1f))
                    NumField("Skipped", skipped, { skipped = it }, Modifier.weight(1f))
                }

                // Actual time + percentile/rank
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Min", actualMinutes, { actualMinutes = it }, Modifier.weight(1f))
                    NumField("Sec", actualSeconds, { actualSeconds = it }, Modifier.weight(1f))
                    NumField("%ile", percentile, { percentile = it }, Modifier.weight(1f), decimal = true)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumField("Rank", rank, { rank = it }, Modifier.weight(1f))
                    NumField("Candidates", candidates, { candidates = it }, Modifier.weight(1f))
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g. JEE, full-length") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                )

                // === QUESTION LOG EDITOR ===
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Question Log", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Add per-question tags to power error analysis. Use quick-add buttons below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick-add buttons
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ErrorCategory.ALL, key = { it }) { cat ->
                        val color = ErrorCategory.color(cat)
                        Surface(
                            onClick = {
                                questionLogs.add(
                                    QuestionLogInput(
                                        subjectName = subjectName,
                                        topicName = "",
                                        errorCategory = cat
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = color.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(10.dp).clip(CircleShape).background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(ErrorCategory.shortLabel(cat), style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Question list
                questionLogs.forEachIndexed { idx, q ->
                    QuestionLogRow(
                        index = idx + 1,
                        log = q,
                        subjects = subjects,
                        onTopicChange = { newTopic -> questionLogs[idx] = q.copy(topicName = newTopic) },
                        onSubjectChange = { newSub -> questionLogs[idx] = q.copy(subjectName = newSub) },
                        onRemove = { questionLogs.removeAt(idx) }
                    )
                }
                if (questionLogs.isNotEmpty()) {
                    Text(
                        "${questionLogs.size} question(s) logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val totalQ = totalQuestions.toIntOrNull() ?: 30
                    val correctInt = correct.toIntOrNull() ?: 0
                    val wrongInt = wrong.toIntOrNull() ?: 0
                    val skippedInt = skipped.toIntOrNull() ?: ((totalQ - correctInt - wrongInt).coerceAtLeast(0))
                    val actualSec = (actualMinutes.toIntOrNull() ?: 0) * 60 + (actualSeconds.toIntOrNull() ?: 0)
                    val marksObtained = marksGot.toFloatOrNull()
                        ?: (correctInt * (positiveMark.toFloatOrNull() ?: 1f) - wrongInt * (negativeMark.toFloatOrNull() ?: 0f))
                    onCreate(
                        AddMockData(
                            title = title.trim(),
                            category = category,
                            subjectId = subjectId,
                            subjectName = subjectName,
                            totalQuestions = totalQ,
                            durationMinutes = durationMinutes.toIntOrNull() ?: 60,
                            totalMarks = totalMarks.toIntOrNull() ?: 30,
                            positiveMark = positiveMark.toFloatOrNull() ?: 1f,
                            negativeMark = negativeMark.toFloatOrNull() ?: 0f,
                            marksObtained = marksObtained,
                            correctCount = correctInt,
                            wrongCount = wrongInt,
                            unattemptedCount = skippedInt,
                            actualDurationSeconds = actualSec,
                            percentile = percentile.toFloatOrNull() ?: 0f,
                            rank = rank.toIntOrNull() ?: 0,
                            totalCandidates = candidates.toIntOrNull() ?: 0,
                            attemptedAt = attemptedAt,
                            description = notes.trim(),
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            questions = questionLogs.toList()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
            ) {
                Text("Save Mock", color = MahirColors.goldForeground())
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = MahirColors.cardBackground(),
        shape = RoundedCornerShape(20.dp)
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = attemptedAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { attemptedAt = it }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun NumField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { c -> c.isDigit() || (decimal && c == '.') }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (decimal) androidx.compose.ui.text.input.KeyboardType.Decimal
            else androidx.compose.ui.text.input.KeyboardType.Number
        ),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionLogRow(
    index: Int,
    log: QuestionLogInput,
    subjects: List<SubjectWithTopics>,
    onSubjectChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var topicExpanded by remember { mutableStateOf(false) }
    val color = ErrorCategory.color(log.errorCategory)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Q$index",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp)
        )
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Subject picker (simple dropdown from available subjects)
            var subjExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subjExpanded,
                onExpandedChange = { subjExpanded = !subjExpanded }
            ) {
                OutlinedTextField(
                    value = log.subjectName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject", fontSize = 11.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    textStyle = MaterialTheme.typography.labelMedium
                )
                ExposedDropdownMenu(
                    expanded = subjExpanded,
                    onDismissRequest = { subjExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("General") }, onClick = { onSubjectChange("General"); subjExpanded = false })
                    subjects.forEach { swt ->
                        DropdownMenuItem(text = { Text(swt.subject.name) }, onClick = { onSubjectChange(swt.subject.name); subjExpanded = false })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Topic picker — finds topics from the selected subject
            val availableTopics = subjects.firstOrNull { it.subject.name == log.subjectName }?.topics?.map { it.topic.name } ?: emptyList()
            ExposedDropdownMenuBox(
                expanded = topicExpanded,
                onExpandedChange = { topicExpanded = !topicExpanded }
            ) {
                OutlinedTextField(
                    value = log.topicName,
                    onValueChange = onTopicChange,
                    label = { Text("Topic", fontSize = 11.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    textStyle = MaterialTheme.typography.labelMedium
                )
                if (availableTopics.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = topicExpanded,
                        onDismissRequest = { topicExpanded = false }
                    ) {
                        availableTopics.take(20).forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { onTopicChange(t); topicExpanded = false })
                        }
                    }
                }
            }
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class AddMockData(
    val title: String,
    val category: String,
    val subjectId: String?,
    val subjectName: String,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val totalMarks: Int,
    val positiveMark: Float,
    val negativeMark: Float,
    val marksObtained: Float,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val actualDurationSeconds: Int,
    val percentile: Float,
    val rank: Int,
    val totalCandidates: Int,
    val attemptedAt: Long,
    val description: String,
    val tags: List<String>,
    val questions: List<QuestionLogInput>
)

// ============================================================
// MOCK ANALYSIS BOTTOM SHEET
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MockAnalysisSheet(
    mockWithQuestions: MockTestWithQuestions,
    onDismiss: () -> Unit
) {
    val test = mockWithQuestions.mockTest
    val questions = mockWithQuestions.questions
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                test.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${test.subjectName}  •  ${MockCategory.label(test.category)}  •  ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(test.attemptedAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Score card
            val percent = test.percentage
            val percentColor = when {
                percent >= 75f -> StatColors.green()
                percent >= 50f -> StatColors.amber()
                else -> StatColors.red()
            }
            MahirCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            "%.1f / %d  •  %.0f%%".format(test.marksObtained, test.totalMarks, percent),
                            style = MaterialTheme.typography.titleMedium,
                            color = percentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Correct: ${test.correctCount}", style = MaterialTheme.typography.bodySmall, color = StatColors.green())
                        Text("Wrong: ${test.wrongCount}", style = MaterialTheme.typography.bodySmall, color = StatColors.red())
                        Text("Skipped: ${test.unattemptedCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (test.percentile > 0f || test.rank > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (test.percentile > 0f) {
                                Text("Percentile: %.1f".format(test.percentile), style = MaterialTheme.typography.bodySmall, color = StatColors.purple())
                            }
                            if (test.rank > 0) {
                                Text("Rank: #${test.rank} / ${test.totalCandidates}", style = MaterialTheme.typography.bodySmall, color = StatColors.blue())
                            }
                        }
                    }
                    Text("Time: ${formatDuration(test.actualDurationSeconds)} / ${test.durationMinutes}m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Question-wise breakdown
            if (questions.isNotEmpty()) {
                Text("Question Breakdown (${questions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                questions.forEach { q ->
                    val color = ErrorCategory.color(q.errorCategory)
                    MahirCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Q${q.questionNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(32.dp)
                            )
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${q.subjectName} • ${q.topicName.ifBlank { "—" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    ErrorCategory.label(q.errorCategory),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                            if (q.timeSpentSeconds > 0) {
                                Text(
                                    "${q.timeSpentSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (test.description.isNotBlank()) {
                Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(test.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
