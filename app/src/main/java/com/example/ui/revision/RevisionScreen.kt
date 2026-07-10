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
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while(true) { kotlinx.coroutines.delay(60000L); currentTime = System.currentTimeMillis() } }
    val overdueRevisions = pendingRevisions.filter { it.scheduledDateMillis < currentTime - 86400000L }
    val dueTodayRevisions = pendingRevisions.filter { it.scheduledDateMillis >= currentTime - 86400000L && it.scheduledDateMillis <= currentTime + 86400000L }
    val upcomingRevisions = pendingRevisions.filter { it.scheduledDateMillis > currentTime + 86400000L }

    val highPriorityCount = pendingRevisions.count { it.priority == "High" }
    val medPriorityCount = pendingRevisions.count { it.priority == "Medium" }
    val lowPriorityCount = pendingRevisions.count { it.priority == "Low" }

    var showAddDialog by remember { mutableStateOf(false) }

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
                items(overdueRevisions, key = { it.id }) { revision ->
                    AnimatedEntry(3) {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    haptics.confirm()
                                    viewModel.deleteRevision(revision)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Red, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Icon(Icons.Rounded.Delete, "Delete", tint = Color.White) }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            RevisionItemCard(
                                revision = revision,
                                onRevise = {
                                    viewModel.toggleRevisionCompletion(revision) { futureRev ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Revision marked as completed.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (dueTodayRevisions.isNotEmpty()) {
                item {
                    AnimatedEntry(4) {
                        SectionTitle("Today", color = PriorityMedium)
                    }
                }
                items(dueTodayRevisions, key = { it.id }) { revision ->
                    AnimatedEntry(5) {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    haptics.confirm()
                                    viewModel.deleteRevision(revision)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Red, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Icon(Icons.Rounded.Delete, "Delete", tint = Color.White) }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            RevisionItemCard(
                                revision = revision,
                                onRevise = {
                                    viewModel.toggleRevisionCompletion(revision) { futureRev ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Revision marked as completed.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (upcomingRevisions.isNotEmpty()) {
                item {
                    AnimatedEntry(6) {
                        SectionTitle("Upcoming", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(upcomingRevisions, key = { it.id }) { revision ->
                    AnimatedEntry(7) {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    haptics.confirm()
                                    viewModel.deleteRevision(revision)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Red, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) { Icon(Icons.Rounded.Delete, "Delete", tint = Color.White) }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            RevisionItemCard(
                                revision = revision,
                                onRevise = {
                                    viewModel.toggleRevisionCompletion(revision) { futureRev ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Revision marked as completed.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            if (overdueRevisions.isEmpty() && dueTodayRevisions.isEmpty() && upcomingRevisions.isEmpty()) {
                item {
                    AnimatedEntry(8) {
                        com.example.ui.components.EmptyState(
                            icon = Icons.Rounded.EventAvailable,
                            title = "All caught up!",
                            subtitle = "You have no pending revisions.",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            // Dummy add dialog for custom revision
            var title by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Custom Revision", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
                text = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Linear Algebra Chapter 3") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahirColors.gold(),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.addCustomRevision(title)
                        showAddDialog = false 
                    }) {
                        Text("Add", color = MahirColors.gold(), fontWeight = FontWeight.Medium)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MahirColors.cardBackground(),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
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

@Composable
fun RevisionItemCard(revision: RevisionEntity, onRevise: () -> Unit) {
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
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = revision.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = revision.subjectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    onClick = onRevise,
                    shape = RoundedCornerShape(999.dp),
                    color = MahirColors.gold(),
                    modifier = Modifier.height(32.dp).padding(end = 8.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        Text("Revise", style = MaterialTheme.typography.labelMedium, color = MahirColors.goldForeground())
                    }
                }
                
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation)
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                exit = shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Confidence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { index ->
                                    val isFilled = index < (revision.confidence / 20)
                                    Icon(
                                        imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                        contentDescription = null,
                                        tint = if (isFilled) MahirColors.gold() else MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Level", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lvl ${revision.repetitionLevel}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
