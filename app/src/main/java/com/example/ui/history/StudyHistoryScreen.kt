package com.example.ui.history

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.FocusSessionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHistoryScreen(
    navController: NavController,
    viewModel: StudyHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }

    val subjectsWithTopics by viewModel.subjectsWithTopics.collectAsStateWithLifecycle()
    var sessionToDelete by remember { mutableStateOf<FocusSessionEntity?>(null) }
    var sessionToEditDuration by remember { mutableStateOf<FocusSessionEntity?>(null) }
    var sessionToEditTopic by remember { mutableStateOf<FocusSessionEntity?>(null) }
    var showActionSheetForSession by remember { mutableStateOf<FocusSessionEntity?>(null) }
    var autoRevisionToDelete by remember { mutableStateOf<com.example.data.RevisionEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search sessions...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Study History", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchActive = !isSearchActive 
                        if (!isSearchActive) viewModel.setSearchQuery("")
                    }) {
                        Icon(if (isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Add Filter if needed later */ }) {
                        Icon(Icons.Rounded.FilterAlt, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabsRow(
                activeTab = state.activeTab,
                onTabSelected = { viewModel.setActiveTab(it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HeaderStatsGrid(state)
            
            Spacer(modifier = Modifier.height(16.dp))

            if (state.groupedSessions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.groupedSessions, key = { it.dateString }) { summary ->
                        DailySummaryCard(
                            summary = summary,
                            onActionClick = { session -> showActionSheetForSession = session },
                            onDeleteGesture = { session ->
                                scope.launch {
                                    val autoRev = viewModel.findAutoRevision(session)
                                    autoRevisionToDelete = autoRev
                                    sessionToDelete = session
                                }
                            }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Break time is not counted in Study Time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showActionSheetForSession != null) {
        val session = showActionSheetForSession!!
        ModalBottomSheet(onDismissRequest = { showActionSheetForSession = null }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Session Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
                ListItem(
                    headlineContent = { Text("Edit Duration") },
                    leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showActionSheetForSession = null
                        sessionToEditDuration = session
                    }
                )
                ListItem(
                    headlineContent = { Text("Edit Topic") },
                    leadingContent = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showActionSheetForSession = null
                        sessionToEditTopic = session
                    }
                )
                ListItem(
                    headlineContent = { Text("Delete Session", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        showActionSheetForSession = null
                        scope.launch {
                            autoRevisionToDelete = viewModel.findAutoRevision(session)
                            sessionToDelete = session
                        }
                    }
                )
            }
        }
    }

    if (sessionToDelete != null) {
        val session = sessionToDelete!!
        var deleteRevisionChecked by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session?") },
            text = {
                Column {
                    Text("This session will be permanently removed from your history.")
                    if (autoRevisionToDelete != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = deleteRevisionChecked, onCheckedChange = { deleteRevisionChecked = it })
                            Text("Also delete the revision task triggered by this session")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val toDeleteSession = session
                    val toDeleteAutoRevision = if (deleteRevisionChecked) autoRevisionToDelete else null
                    
                    viewModel.deleteSession(toDeleteSession, deleteRevisionChecked, toDeleteAutoRevision)
                    sessionToDelete = null
                    
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "Session deleted",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteSession(toDeleteSession, toDeleteAutoRevision)
                        }
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (sessionToEditDuration != null) {
        val session = sessionToEditDuration!!
        var durationMins by remember { mutableFloatStateOf((session.actualDurationSeconds / 60f).coerceIn(1f, 180f)) }
        AlertDialog(
            onDismissRequest = { sessionToEditDuration = null },
            title = { Text("Edit Duration") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${durationMins.toInt()} mins", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Slider(
                        value = durationMins,
                        onValueChange = { durationMins = it },
                        valueRange = 1f..180f,
                        steps = 179
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateSessionDuration(session, (durationMins * 60).toInt())
                    sessionToEditDuration = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToEditDuration = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (sessionToEditTopic != null) {
        val session = sessionToEditTopic!!
        com.example.ui.focus.TopicSelectionSheet(
            subjectsWithTopics = subjectsWithTopics,
            onDismiss = { sessionToEditTopic = null },
            onTopicSelected = { subjectId, topicId, subtopicId ->
                viewModel.updateSessionTopic(session, subjectId, topicId, subtopicId, null)
                sessionToEditTopic = null
            },
            onCustomTask = { title ->
                viewModel.updateSessionTopic(session, null, null, null, title)
                sessionToEditTopic = null
            }
        )
    }
}

@Composable
fun TabsRow(activeTab: HistoryTab, onTabSelected: (HistoryTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        val tabs = listOf(
            HistoryTab.TODAY to "Today",
            HistoryTab.THIS_WEEK to "This Week",
            HistoryTab.THIS_MONTH to "This Month",
            HistoryTab.ALL_TIME to "All Time"
        )
        
        tabs.forEach { (tab, label) ->
            val isSelected = tab == activeTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HeaderStatsGrid(state: HistoryUiState) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            ,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Main progress circle
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                val settingsRepository = remember { com.example.data.SettingsRepository.getInstance(context) }
                val dailyGoalMinutes by settingsRepository.dailyGoalMinutes.collectAsStateWithLifecycle()
                val goalFloat = dailyGoalMinutes.toFloat().coerceAtLeast(1f)
                val progress = (state.totalStudyMinutes / goalFloat).coerceIn(0f, 1f)
                
                CircularProgressIndicator(
                    progress = { 1f },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                CircularProgressIndicator(
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val h = state.totalStudyMinutes / 60
                    val m = state.totalStudyMinutes % 60
                    val hStr = if (h > 0) "${h}h" else ""
                    val mStr = if (m > 0 || h == 0) "${m}m" else ""
                    Text("$hStr $mStr".trim(), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Study Time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
            }
            
            // Sub stats
            StatItem(Icons.Rounded.Schedule, state.totalSessionCount.toString(), "Sessions", MaterialTheme.colorScheme.primary)
            StatItem(Icons.Rounded.MenuBook, state.totalTopicsLearned.toString(), "Topics", Color(0xFF4CAF50))
            StatItem(Icons.Rounded.LocalCafe, "${state.totalBreakMinutes}m", "Break Time", Color(0xFFFF9100))
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

@Composable
fun DailySummaryCard(
    summary: DailySummary,
    onActionClick: (FocusSessionEntity) -> Unit,
    onDeleteGesture: (FocusSessionEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val actualDate = format.format(Date(summary.dateMillis))

    val h = summary.totalStudyMinutes / 60
    val m = summary.totalStudyMinutes % 60
    val studyTimeStr = if (h > 0) "${h}h ${m}m" else "${m}m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column {
            // Header row — tap to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(summary.displayDate, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        if (summary.displayDate == "Today" || summary.displayDate == "Yesterday") {
                            Text(actualDate, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(studyTimeStr, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("·", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${summary.totalSessions}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                    summary.sessions.forEachIndexed { index, session ->
                        SessionHistoryItem(
                            session = session,
                            onActionClick = { onActionClick(session) }
                        )
                        if (index < summary.sessions.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionHistoryItem(session: FocusSessionEntity, onActionClick: () -> Unit) {
    val durationMin = session.actualDurationSeconds / 60
    val durationStr = if (durationMin >= 60) "${durationMin/60}h ${durationMin%60}m" else "${durationMin}m"

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(session.timestamp))

    val fullTopicStr = session.topicName ?: "Session"
    val parts = fullTopicStr.split(" - ")
    val mainTopic = parts[0]
    val subTopic = if (parts.size > 1) parts[1] else null

    val colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF4CAF50), Color(0xFFFF9100), Color(0xFFE91E63), Color(0xFF9C27B0))
    val subjectColor = colors[Math.floorMod(session.subjectName.hashCode(), colors.size)]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActionClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color dot indicating subject
        Box(
            modifier = Modifier.size(8.dp).background(subjectColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mainTopic,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.subjectName,
                    color = subjectColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (subTopic != null) {
                    Text(" · ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(subTopic, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(" · ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Text(timeStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(durationStr, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(4.dp))

        // 3-dot menu button — visible action trigger (replaces hidden long-press)
        IconButton(onClick = onActionClick, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = "No History",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("No Study History", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your completed focus sessions will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}
