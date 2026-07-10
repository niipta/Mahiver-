package com.example.ui.planner

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    navController: NavController,
    viewModel: PlannerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Study Planner", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                TabRow(
                    selectedTabIndex = state.activeTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[state.activeTabIndex]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = state.activeTabIndex == 0,
                        onClick = { viewModel.setTabIndex(0) },
                        text = { Text("Today", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.activeTabIndex == 1,
                        onClick = { viewModel.setTabIndex(1) },
                        text = { Text("Tomorrow", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.activeTabIndex == 2,
                        onClick = { viewModel.setTabIndex(2) },
                        text = { Text("Month", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (state.activeTabIndex == 0) {
                TodayExecutionTab(state, innerPadding, viewModel)
            } else if (state.activeTabIndex == 1) {
                ManagePlanTab(state, innerPadding, viewModel)
            } else {
                CalendarTab(state, innerPadding, viewModel)
            }
        }
    }
}

@Composable
fun TodayExecutionTab(state: PlannerUiState, innerPadding: PaddingValues, viewModel: PlannerViewModel) {
    // editMode toggles between the read-only execution view and the inline
    // plan editor. The button below is always visible so the user can enter
    // edit mode regardless of whether a plan exists.
    var editMode by remember { mutableStateOf(false) }

    // Add-to-revision prompt: shown when the user marks a subtopic/topic complete.
    // Holds the entity that was just tapped so the dialog can offer to add a revision.
    var pendingAddToRevision by remember { mutableStateOf<SubtopicEntity?>(null) }

    val plannedSubjects = state.availableSubjects.filter { swt ->
        swt.topics.any { t -> state.selectedTopicIds.contains(t.topic.id) || t.subtopics.any { s -> state.selectedSubtopicIds.contains(s.id) } }
    }
    val emptyPlan = plannedSubjects.isEmpty() && state.selectedRevisionIds.isEmpty()

    // If the user is in edit mode, render the inline editor instead of the
    // execution list. This lets them plan today's study without leaving the tab.
    if (editMode) {
        TodayManagePlanTab(state, innerPadding, viewModel, onDone = { editMode = false })
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Always-visible action button at the top: "Plan Today's Study" when
        // empty, "Edit Today's Plan" when a plan already exists.
        item {
            Button(
                onClick = { editMode = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    if (emptyPlan) Icons.Rounded.EditNote else Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (emptyPlan) "Plan Today's Study" else "Edit Today's Plan",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (emptyPlan) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.DateRange, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    val emptyTitle = if (state.activeTabIndex == 0) "No tasks planned for today yet." else "No tasks planned for ${state.selectedDate}."
                    Text(emptyTitle, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap \"Plan Today's Study\" above to pick topics and revisions you'll work on today.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            item {
                val title = if (state.activeTabIndex == 0) "Today's Tasks" else "Plan for ${state.selectedDate}"
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            plannedSubjects.forEach { swt ->
                item {
                    Text(text = swt.subject.name, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                }

                swt.topics.filter { state.selectedTopicIds.contains(it.topic.id) || it.subtopics.any { s -> state.selectedSubtopicIds.contains(s.id) } }.forEach { topicWithSub ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = topicWithSub.topic.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                val plannedSubtopics = topicWithSub.subtopics.filter { state.selectedSubtopicIds.contains(it.id) }

                                if (plannedSubtopics.isEmpty()) {
                                    Text("Entire topic selected.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                } else {
                                    plannedSubtopics.forEach { sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    val willComplete = !sub.isCompleted
                                                    viewModel.markSubtopicCompleted(sub, willComplete)
                                                    // Only prompt to add revision when marking complete (not when un-marking)
                                                    if (willComplete) {
                                                        pendingAddToRevision = sub
                                                    }
                                                }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (sub.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (sub.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = sub.name,
                                                color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                                textDecoration = if (sub.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.selectedRevisionIds.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Revisions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9800), modifier = Modifier.padding(bottom = 8.dp))
                }

                val plannedRevisions = state.availableRevisions.filter { state.selectedRevisionIds.contains(it.id) }
                items(plannedRevisions, key = { it.id }) { rev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.markRevisionCompleted(rev, !rev.isCompleted) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (rev.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (rev.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = rev.title,
                                color = if (rev.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                textDecoration = if (rev.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(text = rev.subjectName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Add-to-revision dialog: offered when a subtopic is marked complete.
    pendingAddToRevision?.let { sub ->
        AlertDialog(
            onDismissRequest = { pendingAddToRevision = null },
            title = { Text("Add to Revision?", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text("You completed \"${sub.name}\". Add it to your revision schedule for spaced repetition?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markSubtopicCompleted(sub, true, addToRevision = true)
                        pendingAddToRevision = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) { Text("Add to Revision", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { pendingAddToRevision = null }) { Text("Not now") }
            }
        )
    }
}

@Composable
fun ManagePlanTab(state: PlannerUiState, innerPadding: PaddingValues, viewModel: PlannerViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            val title = if (state.activeTabIndex == 1) "Plan Tomorrow's Study" else "Manage Plan for ${state.selectedDate}"
            Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Select topics and revisions you commit to completing tomorrow. No auto-assigned targets.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.plannedSubtopicsCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Planned", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.completedSubtopicsCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text(text = "Completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.pendingSubtopicsCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        Text(text = "Pending", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        item {
            Text(text = "Syllabus Topics (Recommended to tackle weak topics first)", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
            if (state.availableSubjects.isEmpty()) {
                Text("No subjects added.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        state.availableSubjects.forEach { subjectWithTopics ->
            item {
                Text(text = subjectWithTopics.subject.name, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
            }
            items(subjectWithTopics.topics.filter { !it.isFullyCompleted }) { topicWithSub ->
                val isSelected = state.selectedTopicIds.contains(topicWithSub.topic.id)
                var expanded by remember { mutableStateOf(false) }
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.toggleTopicSelection(topicWithSub.topic.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = topicWithSub.topic.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        
                        if (topicWithSub.subtopics.isNotEmpty()) {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown, contentDescription = "Expand")
                            }
                        }
                    }
                    
                    if (expanded && topicWithSub.subtopics.isNotEmpty()) {
                        Column(modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)) {
                            topicWithSub.subtopics.filter { !it.isCompleted }.forEach { subtopic ->
                                val subSelected = state.selectedSubtopicIds.contains(subtopic.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleSubtopicSelection(subtopic.id) }
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (subSelected) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (subSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = subtopic.name, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Pending Revisions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
            if (state.availableRevisions.filter { !it.isCompleted && it.isActive }.isEmpty()) {
                Text("No pending revisions.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        items(state.availableRevisions.filter { !it.isCompleted && it.isActive }) { rev ->
            val isSelected = state.selectedRevisionIds.contains(rev.id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                    .clickable { viewModel.toggleRevisionSelection(rev.id) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = rev.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    Text(text = rev.subjectName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Inline plan editor shown inside the Today tab when the user taps the
 * "Plan Today's Study" / "Edit Today's Plan" button.
 *
 * Uses a subject-first drill-down to avoid overwhelming the user with a long
 * flat list of topics: subjects are shown as cards → tap a subject to see its
 * topics → tap a topic to expand its subtopics. This keeps the initial list
 * short (one card per subject) and lets the user drill into exactly the area
 * they want to plan.
 */
@Composable
fun TodayManagePlanTab(
    state: PlannerUiState,
    innerPadding: PaddingValues,
    viewModel: PlannerViewModel,
    onDone: () -> Unit
) {
    // The currently expanded subject id. Only one subject is expanded at a time
    // so the list stays manageable. null = no subject expanded (show all subjects).
    var expandedSubjectId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title + Done button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Plan Today's Study",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Tap a subject, then pick topics to commit to today.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onDone,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Stats row: Planned / Completed / Pending
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.plannedSubtopicsCount.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Planned", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.completedSubtopicsCount.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text(text = "Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.pendingSubtopicsCount.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        Text(text = "Pending", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // === SUBJECT-FIRST DRILL-DOWN ===
        // When no subject is expanded, show the list of subjects as cards.
        // When a subject is expanded, show its topics (with expandable subtopics)
        // and a back button to return to the subject list.
        item {
            if (expandedSubjectId == null) {
                Text(
                    text = "Subjects",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (state.availableSubjects.isEmpty()) {
                    Text("No subjects added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { expandedSubjectId = null }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Subjects")
                    }
                }
                Text(
                    text = state.availableSubjects.firstOrNull { it.subject.id == expandedSubjectId }?.subject?.name ?: "Topics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (expandedSubjectId == null) {
            // Show subject cards
            items(state.availableSubjects, key = { it.subject.id }) { subjectWithTopics ->
                val topicsPlannedCount = subjectWithTopics.topics.count { t ->
                    state.selectedTopicIds.contains(t.topic.id) ||
                        t.subtopics.any { s -> state.selectedSubtopicIds.contains(s.id) }
                }
                val incompleteTopics = subjectWithTopics.topics.count { !it.isFullyCompleted }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { expandedSubjectId = subjectWithTopics.subject.id },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subjectWithTopics.subject.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$incompleteTopics topics available" +
                                    if (topicsPlannedCount > 0) " • $topicsPlannedCount planned" else "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Open",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Show topics for the expanded subject (with expandable subtopics)
            val expandedSubject = state.availableSubjects.firstOrNull { it.subject.id == expandedSubjectId }
            if (expandedSubject != null) {
                val topicsToShow = expandedSubject.topics.filter { !it.isFullyCompleted }
                if (topicsToShow.isEmpty()) {
                    item {
                        Text("All topics in this subject are completed.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                } else {
                    items(topicsToShow, key = { it.topic.id }) { topicWithSub ->
                        val isSelected = state.selectedTopicIds.contains(topicWithSub.topic.id)
                        var topicExpanded by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                    .clickable { viewModel.toggleTopicSelection(topicWithSub.topic.id) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = topicWithSub.topic.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                if (topicWithSub.subtopics.isNotEmpty()) {
                                    IconButton(onClick = { topicExpanded = !topicExpanded }) {
                                        Icon(if (topicExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown, contentDescription = "Expand")
                                    }
                                }
                            }
                            if (topicExpanded && topicWithSub.subtopics.isNotEmpty()) {
                                Column(modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)) {
                                    topicWithSub.subtopics.filter { !it.isCompleted }.forEach { subtopic ->
                                        val subSelected = state.selectedSubtopicIds.contains(subtopic.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.toggleSubtopicSelection(subtopic.id) }
                                                .padding(vertical = 8.dp, horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (subSelected) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                                contentDescription = null,
                                                tint = if (subSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = subtopic.name, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f), fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Revisions section (always visible at the bottom)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pending Revisions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (state.availableRevisions.filter { !it.isCompleted && it.isActive }.isEmpty()) {
                Text("No pending revisions.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }

        items(state.availableRevisions.filter { !it.isCompleted && it.isActive }, key = { it.id }) { rev ->
            val isSelected = state.selectedRevisionIds.contains(rev.id)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                    .clickable { viewModel.toggleRevisionSelection(rev.id) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = rev.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                    Text(text = rev.subjectName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CalendarTab(state: PlannerUiState, innerPadding: PaddingValues, viewModel: PlannerViewModel) {
    val visibleMonth by viewModel.visibleMonth.collectAsState()
    val monthlyPlans by viewModel.monthlyPlans.collectAsState()
    
    val today = java.time.LocalDate.now()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = "Previous Month")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${visibleMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${visibleMonth.year}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.currentMonth() }, modifier = Modifier.height(30.dp)) {
                    Text("Today", fontSize = 12.sp)
                }
            }
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
            daysOfWeek.forEach { day ->
                Text(day, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val firstDayOfMonth = visibleMonth.atDay(1)
        // Adjust for Sunday start. dayOfWeek value for Sunday is 7 in java.time
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 
        val daysInMonth = visibleMonth.lengthOfMonth()
        
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            var currentDay = 1
            for (row in 0..5) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (col in 0..6) {
                        if (row == 0 && col < startDayOfWeek || currentDay > daysInMonth) {
                            Box(modifier = Modifier.size(40.dp))
                        } else {
                            val date = visibleMonth.atDay(currentDay)
                            val dateStr = date.format(formatter)
                            val isToday = date == today
                            val plan = monthlyPlans[dateStr]
                            
                            val isSelected = state.selectedDate == dateStr
                            val isFuture = date.isAfter(today.plusDays(90))
                            
                            var dotColor = Color.Transparent
                            if (plan != null) {
                                val hasPlan = plan.plannedSubtopicIds.isNotBlank() || plan.plannedTopicIds.isNotBlank() || plan.plannedRevisionIds.isNotBlank()
                                if (hasPlan) {
                                    dotColor = if (plan.isCompleted) Color(0xFF4CAF50) else Color(0xFFFFD700)
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable(enabled = !isFuture) {
                                        viewModel.setSelectedMonthDate(dateStr)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = currentDay.toString(),
                                        color = if (isFuture) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) 
                                                else if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(dotColor))
                                }
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .border(
                                                1.dp, 
                                                Color(0xFFFFD700), 
                                                CircleShape
                                            )
                                    )
                                }
                            }
                            currentDay++
                        }
                    }
                }
                if (currentDay > daysInMonth) break
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        
        if (state.selectedDate.isNotEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                val selectedDate = try {
                    java.time.LocalDate.parse(state.selectedDate, formatter)
                } catch (e: Exception) {
                    today
                }
                if (selectedDate.isAfter(today)) {
                    ManagePlanTab(state, PaddingValues(0.dp), viewModel)
                } else {
                    TodayExecutionTab(state, PaddingValues(0.dp), viewModel)
                }
            }
        }
    }
}
