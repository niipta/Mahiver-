package com.example.ui.focus

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.service.SessionType
import com.example.service.TimerState
import com.example.data.FocusSessionEntity
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val haptics = com.example.util.rememberMahirHaptics()
    val timeRemaining by viewModel.timeRemaining.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val sessionType by viewModel.sessionType.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val subjectsWithTopics by viewModel.subjectsWithTopics.collectAsStateWithLifecycle()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsStateWithLifecycle()
    val selectedTopicId by viewModel.selectedTopicId.collectAsStateWithLifecycle()
    val selectedSubtopicId by viewModel.selectedSubtopicId.collectAsStateWithLifecycle()
    val customTaskTitle by viewModel.customTaskTitle.collectAsStateWithLifecycle()
    val isSessionCompleted by viewModel.isSessionCompleted.collectAsStateWithLifecycle()
    val todayPlan by viewModel.todayPlan.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { com.example.data.SettingsRepository.getInstance(context) }
    val autoEnableDnd by settingsRepository.autoEnableDnd.collectAsStateWithLifecycle()
    val hasPromptedForDnd by settingsRepository.hasPromptedForDnd.collectAsStateWithLifecycle()

    var showTopicSelector by remember { mutableStateOf(false) }
    var showMissingTopicPrompt by remember { mutableStateOf(false) }
    var showDndPrompt by remember { mutableStateOf(false) }

    if (isSessionCompleted) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                MahirBottomNavigation(navController = navController)
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                SessionCompletedScreen(
                    sessionType = sessionType,
                    onStartBreak = {
                        haptics.confirm()
                        viewModel.dismissCompletion()
                        viewModel.setSessionType(if (sessionType == SessionType.FOCUS) SessionType.SHORT_BREAK else SessionType.FOCUS)
                        viewModel.startTimer()
                    },
                    onContinue = {
                        haptics.tap()
                        viewModel.dismissCompletion()
                        if (sessionType != SessionType.FOCUS) {
                            viewModel.setSessionType(SessionType.FOCUS)
                        }
                    }
                )
            }
        }
        return
    }

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
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AnimatedEntry(0) {
                    HeaderSection(timerState = timerState)
                }
            }

            item {
                AnimatedEntry(1) {
                    FocusTimerSection(
                        timeRemaining = timeRemaining,
                        totalTime = com.example.service.TimerManager.getDurationMinutes(sessionType) * 60L,
                        timerState = timerState,
                        sessionType = sessionType,
                        autoEnableDnd = autoEnableDnd,
                        onStart = {
                            if (timerState == TimerState.PAUSED) {
                                haptics.confirm()
                                viewModel.resumeTimer()
                                return@FocusTimerSection
                            }
                            if (selectedTopicId == null && customTaskTitle == null && sessionType == SessionType.FOCUS) {
                                haptics.reject()
                                showMissingTopicPrompt = true
                            } else {
                                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                if (sessionType == SessionType.FOCUS && !hasPromptedForDnd && !notificationManager.isNotificationPolicyAccessGranted) {
                                    showDndPrompt = true
                                    return@FocusTimerSection
                                }
                                haptics.confirm()
                                viewModel.startTimer()
                            }
                        },
                        onPause = {
                            haptics.tap()
                            viewModel.pauseTimer()
                        },
                        onStop = { viewModel.stopTimer() },
                        onReset = { viewModel.resetTimer() },
                        onAdjustDuration = { delta ->
                            haptics.tap()
                            viewModel.adjustDuration(delta)
                        }
                    )
                }
            }

            item {
                AnimatedEntry(2) {
                    SessionTypeSelector(
                        currentType = sessionType,
                        onTypeSelected = { viewModel.setSessionType(it) },
                        timerState = timerState
                    )
                }
            }

            item {
                AnimatedEntry(3) {
                    SubjectSelectorDropdown(
                        subjectsWithTopics = subjectsWithTopics,
                        selectedSubjectId = selectedSubjectId,
                        selectedTopicId = selectedTopicId,
                        selectedSubtopicId = selectedSubtopicId,
                        customTaskTitle = customTaskTitle,
                        onSelectClick = { showTopicSelector = true },
                        onClearClick = { viewModel.setTargetTopic(null, null, null, null) }
                    )
                }
            }

            item {
                AnimatedEntry(4) {
                    BottomStatsRow(recentSessions)
                }
            }
        }
        
        if (showTopicSelector) {
            // Topic AND subtopic are both selectable.
            // Pass today's planned topic/subtopic IDs so the sheet can show
            // them first in a "Today's Plan" section.
            val plannedTopicIds = todayPlan?.plannedTopicIds
                ?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            val plannedSubtopicIds = todayPlan?.plannedSubtopicIds
                ?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            TopicSelectionSheet(
                subjectsWithTopics = subjectsWithTopics,
                plannedTopicIds = plannedTopicIds,
                plannedSubtopicIds = plannedSubtopicIds,
                onDismiss = { showTopicSelector = false },
                onTopicSelected = { subjectId, topicId, subtopicId ->
                    viewModel.setTargetTopic(subjectId, topicId, subtopicId, null)
                    showTopicSelector = false
                },
                onCustomTask = { title ->
                    viewModel.setTargetTopic(null, null, null, title)
                    showTopicSelector = false
                }
            )
        }

        if (showMissingTopicPrompt) {
            AlertDialog(
                onDismissRequest = { showMissingTopicPrompt = false },
                title = { Text("Select a Topic", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
                text = { Text("Please select a topic or add a custom task to focus on.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    TextButton(onClick = { 
                        showMissingTopicPrompt = false
                        showTopicSelector = true 
                    }) {
                        Text("Select Topic", color = MahirColors.gold())
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showMissingTopicPrompt = false
                        viewModel.startTimer() // start anyway
                    }) {
                        Text("Start Anyway", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MahirColors.cardBackground(),
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showDndPrompt) {
            AlertDialog(
                onDismissRequest = { showDndPrompt = false },
                title = { Text("Auto-enable DND", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
                text = { Text("To automatically enable Do Not Disturb during focus sessions, we need permission to modify your Do Not Disturb settings.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            showDndPrompt = false
                            val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
                    ) {
                        Text("Grant Permission", color = Color.Black)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDndPrompt = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                containerColor = MahirColors.cardBackground(),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun HeaderSection(timerState: TimerState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Focus",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Crossfade(targetState = timerState, label = "subtitle") { state ->
            Text(
                text = when (state) {
                    TimerState.RUNNING -> "Stay in the zone"
                    TimerState.PAUSED -> "Timer paused"
                    else -> "Ready when you are"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FocusTimerSection(
    timeRemaining: Long,
    totalTime: Long,
    timerState: TimerState,
    sessionType: SessionType,
    autoEnableDnd: Boolean = false,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onAdjustDuration: (Int) -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val progress = if (totalTime > 0) 1f - (timeRemaining.toFloat() / totalTime.toFloat()) else 0f
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(1000, easing = LinearEasing),
            label = "timerProgress"
        )

        val strokeColor = MahirColors.gold()
        val trackColor = MaterialTheme.colorScheme.outlineVariant
        val canAdjust = timerState == TimerState.STOPPED

        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()

                if (timerState == TimerState.RUNNING) {
                    drawIntoCanvas { canvas ->
                        val paint = androidx.compose.ui.graphics.Paint().apply {
                            color = strokeColor.copy(alpha = 0.3f)
                            style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidth
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(40f, android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }
                        val rect = androidx.compose.ui.geometry.Rect(
                            left = strokeWidth / 2f,
                            top = strokeWidth / 2f,
                            right = size.width - strokeWidth / 2f,
                            bottom = size.height - strokeWidth / 2f
                        )
                        canvas.drawArc(rect, -90f, 360f * animatedProgress, false, paint)
                    }
                }

                val radius = size.minDimension / 2 - strokeWidth
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = strokeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (autoEnableDnd && timerState == TimerState.RUNNING && sessionType == SessionType.FOCUS) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Rounded.DoNotDisturbOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DND ON", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace // tabular-nums equivalent
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(MahirColors.gold(), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (sessionType) {
                            SessionType.FOCUS -> "Deep Focus"
                            SessionType.SHORT_BREAK -> "Short Break"
                            SessionType.LONG_BREAK -> "Long Break"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canAdjust) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap − / + below to change duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // +/- duration controls (visible only when STOPPED so user can adjust before starting)
        if (canAdjust) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DurationButton(
                    text = "− 5m",
                    onClick = { onAdjustDuration(-5) }
                )
                DurationButton(
                    text = "− 1m",
                    onClick = { onAdjustDuration(-1) }
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
                DurationButton(
                    text = "+ 1m",
                    isPrimary = true,
                    onClick = { onAdjustDuration(1) }
                )
                DurationButton(
                    text = "+ 5m",
                    isPrimary = true,
                    onClick = { onAdjustDuration(5) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MahirColors.subtleBackground())
            ) {
                Icon(Icons.Rounded.Replay, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onBackground)
            }

            // Play/Pause
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MahirColors.gold())
                    .clickable {
                        if (timerState == TimerState.RUNNING) onPause() else onStart()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (timerState == TimerState.RUNNING) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (timerState == TimerState.RUNNING) "Pause" else "Play",
                    tint = MahirColors.goldForeground(),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Stop
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MahirColors.subtleBackground())
            ) {
                Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun DurationButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    val bg = if (isPrimary) MahirColors.gold().copy(alpha = 0.12f) else MahirColors.subtleBackground()
    val fg = if (isPrimary) MahirColors.gold() else MaterialTheme.colorScheme.onBackground
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrimary) MahirColors.gold().copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp).fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SessionTypeSelector(
    currentType: SessionType,
    onTypeSelected: (SessionType) -> Unit,
    timerState: TimerState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val types = listOf(
            Triple(SessionType.FOCUS, "Focus", "${com.example.service.TimerManager.getDurationMinutes(SessionType.FOCUS)}m"),
            Triple(SessionType.SHORT_BREAK, "Break", "${com.example.service.TimerManager.getDurationMinutes(SessionType.SHORT_BREAK)}m"),
            Triple(SessionType.LONG_BREAK, "Long Break", "${com.example.service.TimerManager.getDurationMinutes(SessionType.LONG_BREAK)}m")
        )
        
        types.forEach { (type, label, time) ->
            val isSelected = currentType == type
            Surface(
                onClick = { if (timerState == TimerState.STOPPED) onTypeSelected(type) },
                shape = RoundedCornerShape(999.dp),
                color = if (isSelected) MahirColors.gold().copy(alpha = 0.1f) else MahirColors.subtleBackground(),
                border = if (isSelected) BorderStroke(1.dp, MahirColors.gold()) else null,
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$label $time",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) MahirColors.gold() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectSelectorDropdown(
    subjectsWithTopics: List<com.example.data.SubjectWithTopics>,
    selectedSubjectId: String?,
    selectedTopicId: String?,
    selectedSubtopicId: String?,
    customTaskTitle: String?,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val selectedSubject = subjectsWithTopics.find { it.subject.id == selectedSubjectId }
    val selectedTopic = selectedSubject?.topics?.find { it.topic.id == selectedTopicId }
    val selectedSubtopic = selectedTopic?.subtopics?.find { it.id == selectedSubtopicId }

    val targetName = when {
        customTaskTitle != null -> customTaskTitle
        selectedSubtopic != null -> selectedSubtopic.name
        selectedTopic != null -> selectedTopic.topic.name
        else -> "Select a topic to focus on"
    }
    val targetSubtitle = when {
        customTaskTitle != null -> "Custom task"
        selectedSubtopic != null && selectedTopic != null -> "${selectedTopic.topic.name} • ${selectedSubject?.subject?.name ?: ""}".trim().trimEnd('•').trim()
        selectedTopic != null && selectedSubject != null -> "${selectedSubject.subject.name} • Entire topic"
        else -> null
    }
    
    var isHovered by remember { mutableStateOf(false) } // just for animated chevron if we wanted
    val chevronRotation by animateFloatAsState(targetValue = if (isHovered) 180f else 0f, label = "chevron")

    MahirCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MahirColors.subtleBackground(), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selectedTopic != null || customTaskTitle != null) Icons.Rounded.Task else Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = if (selectedTopic != null || customTaskTitle != null) MahirColors.gold() else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = targetName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedTopic != null || customTaskTitle != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (targetSubtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = targetSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (selectedTopic != null || customTaskTitle != null) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation)
                )
            }
        }
    }
}

@Composable
fun BottomStatsRow(recentSessions: List<FocusSessionEntity>) {
    val focusSessions = recentSessions.filter { it.sessionType == "Study" || it.sessionType == "Focus" }
    val breakSessions = recentSessions.filter { it.sessionType == "Break" || it.sessionType == "Short Break" || it.sessionType == "Long Break" }
    
    val totalFocusMinutes = focusSessions.sumOf { it.actualDurationSeconds } / 60
    val totalBreakMinutes = breakSessions.sumOf { it.actualDurationSeconds } / 60
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = focusSessions.size.toString(),
            label = "Sessions",
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
        StatItem(
            value = "${totalFocusMinutes / 60}h ${totalFocusMinutes % 60}m",
            label = "Focus",
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
        StatItem(
            value = "${totalBreakMinutes / 60}h ${totalBreakMinutes % 60}m",
            label = "Break",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicSelectionSheet(
    subjectsWithTopics: List<com.example.data.SubjectWithTopics>,
    plannedTopicIds: Set<String> = emptySet(),
    plannedSubtopicIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onTopicSelected: (subjectId: String, topicId: String, subtopicId: String?) -> Unit,
    onCustomTask: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)
        ) {
            Text(
                "Select what to focus on",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Today's planned topics appear first. Tap a topic to focus on it, or expand to pick a subtopic.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            var custom by remember { mutableStateOf("") }
            var query by remember { mutableStateOf("") }
            var expandedTopicId by remember { mutableStateOf<String?>(null) }
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it },
                placeholder = { Text("Or enter a custom task") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { if (custom.isNotBlank()) onCustomTask(custom) }) {
                        Icon(Icons.Rounded.Check, contentDescription = "Add", tint = MahirColors.gold())
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MahirColors.gold(),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search topics") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MahirColors.gold(),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            // CRITICAL FIX: bounded height to avoid nested-LazyColumn infinity crash
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)
            ) {
                // === TODAY'S PLAN SECTION (shown first) ===
                // Collect all topics/subtopics that are in today's plan and show
                // them at the top so the user can quickly start a focus session
                // on something they already committed to today.
                val plannedItems = mutableListOf<Triple<String, String, String?>>() // (subjectId, topicId, subtopicId?)
                val plannedLabels = mutableListOf<String>() // display labels
                subjectsWithTopics.forEach { subject ->
                    subject.topics.forEach { topic ->
                        // Whole topic planned
                        if (plannedTopicIds.contains(topic.topic.id)) {
                            plannedItems.add(Triple(subject.subject.id, topic.topic.id, null))
                            plannedLabels.add("${topic.topic.name} (${subject.subject.name})")
                        }
                        // Individual subtopics planned
                        topic.subtopics.forEach { subtopic ->
                            if (plannedSubtopicIds.contains(subtopic.id)) {
                                plannedItems.add(Triple(subject.subject.id, topic.topic.id, subtopic.id))
                                plannedLabels.add("${subtopic.name} (${subject.subject.name})")
                            }
                        }
                    }
                }

                if (plannedItems.isNotEmpty() && query.isBlank()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Today,
                                contentDescription = null,
                                tint = MahirColors.gold(),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Today's Plan (${plannedItems.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MahirColors.gold(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    items(plannedItems.size, key = { "planned_${plannedItems[it].second}_${plannedItems[it].third ?: ""}" }) { index ->
                        val (subjId, topId, subId) = plannedItems[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MahirColors.gold().copy(alpha = 0.08f))
                                .clickable { onTopicSelected(subjId, topId, subId) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MahirColors.gold(), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = plannedLabels[index],
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "All Topics",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // === ALL TOPICS SECTION ===
                subjectsWithTopics.forEach { subject ->
                    val filtered = subject.topics.filter { topicWithSubtopics ->
                        query.isBlank() ||
                            topicWithSubtopics.topic.name.contains(query, ignoreCase = true) ||
                            topicWithSubtopics.subtopics.any { it.name.contains(query, ignoreCase = true) } ||
                            subject.subject.name.contains(query, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) return@forEach
                    item {
                        Text(
                            subject.subject.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filtered.size, key = { filtered[it].topic.id }) { index ->
                        val topic = filtered[index]
                        val isExpanded = expandedTopicId == topic.topic.id
                        val chevronRotation by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            animationSpec = tween(200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            label = "chevron"
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            // Topic row — clickable to select whole topic OR expand subtopics
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Select entire topic
                                        onTopicSelected(subject.subject.id, topic.topic.id, null)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = topic.topic.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                if (topic.subtopics.isNotEmpty()) {
                                    // Expand toggle (does NOT trigger selection)
                                    IconButton(
                                        onClick = {
                                            expandedTopicId = if (isExpanded) null else topic.topic.id
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand subtopics",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp).rotate(chevronRotation)
                                        )
                                    }
                                }
                            }
                            // Expanded subtopics
                            if (topic.subtopics.isNotEmpty() && isExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)) {
                                    topic.subtopics.forEach { subtopic ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onTopicSelected(subject.subject.id, topic.topic.id, subtopic.id)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(MahirColors.gold(), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = subtopic.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
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
}

@Composable
fun SessionCompletedScreen(
    sessionType: SessionType,
    onStartBreak: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MahirColors.gold(), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Session Completed", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onStartBreak,
            colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
        ) {
            Text("Start Break", color = MahirColors.goldForeground())
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onContinue) {
            Text("Skip Break", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DeepFocusModeScreen(
    timeRemaining: Long,
    totalTime: Long,
    timerState: TimerState,
    onExit: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    val haptics = com.example.util.rememberMahirHaptics()
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontSize = 96.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(64.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            IconButton(onClick = onExit) {
                Icon(Icons.Rounded.Close, contentDescription = "Exit", tint = Color.White)
            }
            IconButton(onClick = { 
                if (timerState == TimerState.RUNNING) {
                    haptics.tap()
                    onPause()
                } else {
                    haptics.confirm()
                    onResume()
                }
            }) {
                Icon(if (timerState == TimerState.RUNNING) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = "Play/Pause", tint = Color.White)
            }
        }
    }
}
