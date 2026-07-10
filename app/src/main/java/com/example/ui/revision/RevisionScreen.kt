package com.example.ui.revision

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.ui.components.AnimatedEntry
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.RevisionEntity
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: RevisionViewModel = hiltViewModel()
) {
    val haptics = com.example.util.rememberMahirHaptics()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val pendingRevisions = revisions.filter { !it.isCompleted && it.isActive }
    val completedRevisions = revisions.filter { it.isCompleted }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while(true) { kotlinx.coroutines.delay(60000L); currentTime = System.currentTimeMillis() } }
    val overdueRevisions = pendingRevisions.filter { it.scheduledDateMillis < currentTime - 86400000L }
    val dueTodayRevisions = pendingRevisions.filter { it.scheduledDateMillis >= currentTime - 86400000L && it.scheduledDateMillis <= currentTime + 86400000L }
    val upcomingRevisions = pendingRevisions.filter { it.scheduledDateMillis > currentTime + 86400000L }

    val highPriorityCount = pendingRevisions.count { it.priority == "High" }
    val medPriorityCount = pendingRevisions.count { it.priority == "Medium" }
    val lowPriorityCount = pendingRevisions.count { it.priority == "Low" }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCompletedSection by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    HeaderSection(onAddClick = { showAddDialog = true })
                }
            }

            item {
                AnimatedEntry(1) {
                    PriorityCardsRow(
                        high = highPriorityCount,
                        medium = medPriorityCount,
                        low = lowPriorityCount
                    )
                }
            }

            if (overdueRevisions.isNotEmpty()) {
                item {
                    AnimatedEntry(2) {
                        SectionTitle("Overdue", color = PriorityHigh)
                    }
                }
                items(overdueRevisions, key = { "overdue_${it.id}" }) { revision ->
                    AnimatedEntry(3) {
                        RevisionItemCard(
                            revision = revision,
                            onRevise = {
                                haptics.confirm()
                                viewModel.toggleRevisionCompletion(revision) { futureRev ->
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            "Revision completed. Next: ${formatDate(futureRev.scheduledDateMillis)}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoRevisionCompletion(revision, futureRev.id)
                                        }
                                    }
                                }
                            },
                            onUpdatePriority = { newPriority -> viewModel.updatePriority(revision, newPriority) },
                            onReschedule = { days -> viewModel.rescheduleRevision(revision, days) },
                            onUpdateConfidence = { conf -> viewModel.updateConfidence(revision, conf) },
                            onDelete = {
                                haptics.confirm()
                                viewModel.deleteRevision(revision)
                            }
                        )
                    }
                }
            }

            if (dueTodayRevisions.isNotEmpty()) {
                item {
                    AnimatedEntry(4) {
                        SectionTitle("Today", color = PriorityMedium)
                    }
                }
                items(dueTodayRevisions, key = { "today_${it.id}" }) { revision ->
                    AnimatedEntry(5) {
                        RevisionItemCard(
                            revision = revision,
                            onRevise = {
                                haptics.confirm()
                                viewModel.toggleRevisionCompletion(revision) { futureRev ->
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            "Revision completed. Next: ${formatDate(futureRev.scheduledDateMillis)}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoRevisionCompletion(revision, futureRev.id)
                                        }
                                    }
                                }
                            },
                            onUpdatePriority = { newPriority -> viewModel.updatePriority(revision, newPriority) },
                            onReschedule = { days -> viewModel.rescheduleRevision(revision, days) },
                            onUpdateConfidence = { conf -> viewModel.updateConfidence(revision, conf) },
                            onDelete = {
                                haptics.confirm()
                                viewModel.deleteRevision(revision)
                            }
                        )
                    }
                }
            }

            if (upcomingRevisions.isNotEmpty()) {
                item {
                    AnimatedEntry(6) {
                        SectionTitle("Upcoming", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(upcomingRevisions, key = { "upcoming_${it.id}" }) { revision ->
                    AnimatedEntry(7) {
                        RevisionItemCard(
                            revision = revision,
                            onRevise = {
                                haptics.confirm()
                                viewModel.toggleRevisionCompletion(revision) { futureRev ->
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            "Revision completed. Next: ${formatDate(futureRev.scheduledDateMillis)}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoRevisionCompletion(revision, futureRev.id)
                                        }
                                    }
                                }
                            },
                            onUpdatePriority = { newPriority -> viewModel.updatePriority(revision, newPriority) },
                            onReschedule = { days -> viewModel.rescheduleRevision(revision, days) },
                            onUpdateConfidence = { conf -> viewModel.updateConfidence(revision, conf) },
                            onDelete = {
                                haptics.confirm()
                                viewModel.deleteRevision(revision)
                            }
                        )
                    }
                }
            }

            // Completed section (collapsible)
            if (completedRevisions.isNotEmpty()) {
                item {
                    Surface(
                        onClick = { showCompletedSection = !showCompletedSection },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Completed (${completedRevisions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (showCompletedSection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showCompletedSection) {
                    items(completedRevisions.take(50), key = { "completed_${it.id}" }) { revision ->
                        CompletedRevisionItem(
                            revision = revision,
                            onDelete = {
                                haptics.confirm()
                                viewModel.deleteCompletedRevision(revision)
                            }
                        )
                    }
                }
            }

            if (overdueRevisions.isEmpty() && dueTodayRevisions.isEmpty() && upcomingRevisions.isEmpty()) {
                item {
                    AnimatedEntry(8) {
                        com.example.ui.components.EmptyState(
                            icon = Icons.Rounded.EventAvailable,
                            title = "All caught up!",
                            subtitle = "You have no pending revisions. Complete topics to generate new ones.",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddCustomRevisionDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, subject, priority ->
                    viewModel.addCustomRevision(title, subject, priority)
                    showAddDialog = false
                }
            )
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

@Composable
fun HeaderSection(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Revision",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stay on top of your learning",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MahirColors.subtleBackground())
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun PriorityCardsRow(high: Int, medium: Int, low: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PriorityCard(count = high, label = "High", color = PriorityHigh, modifier = Modifier.weight(1f))
        PriorityCard(count = medium, label = "Medium", color = PriorityMedium, modifier = Modifier.weight(1f))
        PriorityCard(count = low, label = "Low", color = PriorityLow, modifier = Modifier.weight(1f))
    }
}

@Composable
fun PriorityCard(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MahirColors.subtleBackground(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Revision item card with expandable details.
 * Fixes from previous version:
 * - "Revise" button and expand chevron no longer overlap (button is in the
 *   row, chevron is a separate trailing icon)
 * - Confidence uses correct 0-100 → 5-star math (/20)
 * - Added reschedule (+1d, +3d, +7d) and priority edit buttons
 * - Added delete button
 */
@Composable
fun RevisionItemCard(
    revision: RevisionEntity,
    onRevise: () -> Unit,
    onUpdatePriority: (String) -> Unit,
    onReschedule: (Int) -> Unit,
    onUpdateConfidence: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = when (revision.priority) {
        "High" -> PriorityHigh
        "Medium" -> PriorityMedium
        else -> PriorityLow
    }

    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron"
    )

    MahirCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority color bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .background(borderColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Title + subject (clickable to expand)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = revision.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${revision.subjectName} • Lvl ${revision.repetitionLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Revise button
                Surface(
                    onClick = onRevise,
                    shape = RoundedCornerShape(999.dp),
                    color = MahirColors.gold(),
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        Text("Revise", style = MaterialTheme.typography.labelMedium, color = MahirColors.goldForeground(), fontWeight = FontWeight.SemiBold)
                    }
                }

                // Expand toggle (separate from the title area to avoid overlap)
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(chevronRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                exit = shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Confidence row (0-100 stored, /20 for 5 stars)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Confidence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { index ->
                                    val starThreshold = (index + 1) * 20
                                    val isFilled = revision.confidence >= starThreshold
                                    Icon(
                                        imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                        contentDescription = null,
                                        tint = if (isFilled) MahirColors.gold() else MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { onUpdateConfidence(starThreshold) }
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Next", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDate(revision.scheduledDateMillis),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority selector
                    Text("Priority", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("High" to PriorityHigh, "Medium" to PriorityMedium, "Low" to PriorityLow).forEach { (label, color) ->
                            val isSelected = revision.priority == label
                            Surface(
                                onClick = { onUpdatePriority(label) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, if (isSelected) color else Color.Transparent)
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reschedule buttons
                    Text("Reschedule", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("+1 day" to 1, "+3 days" to 3, "+7 days" to 7, "+30 days" to 30).forEach { (label, days) ->
                            OutlinedButton(
                                onClick = { onReschedule(days) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Delete button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedRevisionItem(
    revision: RevisionEntity,
    onDelete: () -> Unit
) {
    MahirCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = revision.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
                Text(
                    text = "${revision.subjectName} • Lvl ${revision.repetitionLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomRevisionDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, subject: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Custom") }
    var priority by remember { mutableStateOf("Medium") }
    var subjectExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Revision", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Linear Algebra Chapter 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahirColors.gold(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = subject,
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
                        listOf("Custom", "Physics", "Chemistry", "Mathematics", "Biology").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = { subject = s; subjectExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahirColors.gold())
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        listOf("High", "Medium", "Low").forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = { priority = p; priorityExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), subject, priority)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold()),
                enabled = title.isNotBlank()
            ) {
                Text("Add", color = MahirColors.goldForeground(), fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MahirColors.cardBackground(),
        shape = RoundedCornerShape(16.dp)
    )
}
