package com.example.ui.syllabus

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.data.SubjectWithTopics
import com.example.data.TopicWithSubtopics
import com.example.data.SubtopicEntity
import com.example.data.SubjectEntity
import com.example.data.TopicEntity
import com.example.ui.components.AnimatedEntry
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: SyllabusViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val haptics = com.example.util.rememberMahirHaptics()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<SubjectEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pending revision-prompt request (for both topic & subtopic completion flows)
    var pendingTopicRevision by remember { mutableStateOf<TopicEntity?>(null) }
    var pendingTopicSubjectName by remember { mutableStateOf("Subject") }
    var pendingSubtopicRevision by remember { mutableStateOf<SubtopicEntity?>(null) }
    var pendingSubtopicSubjectName by remember { mutableStateOf("Subject") }

    // Listen to UI events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is SyllabusUiEvent.ShowRevisionPrompt -> {
                    // Find latest topic snapshot (so isCompleted reflects DB)
                    val refreshed = subjects.asSequence()
                        .flatMap { it.topics }
                        .map { it.topic }
                        .firstOrNull { it.id == event.topicId }
                    refreshed?.let {
                        pendingTopicRevision = it
                        pendingTopicSubjectName = event.subjectName
                    }
                }
                is SyllabusUiEvent.ShowRevisionPromptSubtopic -> {
                    val refreshed = subjects.asSequence()
                        .flatMap { it.topics }
                        .flatMap { it.subtopics }
                        .firstOrNull { it.id == event.subtopicId }
                    refreshed?.let {
                        pendingSubtopicRevision = it
                        pendingSubtopicSubjectName = event.subjectName
                    }
                }
                is SyllabusUiEvent.ImportSuccess -> {
                    haptics.confirm()
                    snackbarHostState.showSnackbar(
                        "Imported ${event.subjects} subjects, ${event.topics} topics, ${event.subtopics} subtopics"
                    )
                }
                is SyllabusUiEvent.TemplateImportSuccess -> {
                    haptics.confirm()
                    snackbarHostState.showSnackbar(
                        "${event.templateName}: ${event.subjects} subjects, ${event.topics} topics imported"
                    )
                }
                is SyllabusUiEvent.ImportError -> {
                    haptics.reject()
                    snackbarHostState.showSnackbar("Import failed: ${event.message}")
                }
                is SyllabusUiEvent.AiGenerateSuccess -> {
                    haptics.confirm()
                    snackbarHostState.showSnackbar(
                        "AI generated ${event.subjects} subjects, ${event.topics} topics, ${event.subtopics} subtopics"
                    )
                }
                is SyllabusUiEvent.AiGenerateError -> {
                    haptics.reject()
                    snackbarHostState.showSnackbar("AI error: ${event.message}")
                }
            }
        }
    }

    // SAF launcher for JSON import
    val jsonPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importSyllabusFromUri(context, uri)
        }
    }

    // Performance: derivedStateOf to avoid recomputation on every recomposition
    val filteredSubjects by remember(subjects, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) subjects
            else subjects.mapNotNull { subject ->
                val filteredTopics = subject.topics.filter {
                    it.topic.name.contains(searchQuery, ignoreCase = true) ||
                    it.subtopics.any { sub -> sub.name.contains(searchQuery, ignoreCase = true) }
                }
                if (filteredTopics.isNotEmpty() || subject.subject.name.contains(searchQuery, ignoreCase = true)) {
                    subject.copy(topics = filteredTopics.ifEmpty { subject.topics })
                } else null
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // === HEADER + ACTIONS (fixed overlap) ===
            item(key = "header") {
                AnimatedEntry(0) {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search subjects & topics...") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MahirColors.subtleBackground(),
                                unfocusedContainerColor = MahirColors.subtleBackground(),
                                focusedBorderColor = MahirColors.gold(),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        SyllabusHeader(
                            onAddSubject = { showAddSubjectDialog = true },
                            onImportJson = { jsonPickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            onPickTemplate = { showTemplatesSheet = true },
                            onAiGenerate = { showAiDialog = true },
                            onSearchClick = { isSearchActive = true }
                        )
                    }
                }
            }

            // === EXAMS SECTION — visually separated from Add Subject ===
            item(key = "exams") {
                AnimatedEntry(1) {
                    ExamsSection(viewModel, snackbarHostState)
                }
            }

            // === SUBJECT LIST ===
            if (filteredSubjects.isEmpty()) {
                item(key = "empty") {
                    if (searchQuery.isBlank()) {
                        com.example.ui.components.EmptyState(
                            icon = Icons.Rounded.MenuBook,
                            title = "No subjects yet",
                            subtitle = "Add manually, import JSON, or pick a built-in template",
                            actionText = "Browse Templates",
                            onAction = { showTemplatesSheet = true },
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    } else {
                        com.example.ui.components.EmptyState(
                            icon = Icons.Rounded.SearchOff,
                            title = "No results found",
                            subtitle = "Try adjusting your search query",
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(filteredSubjects, key = { _, item -> item.subject.id }) { index, subjectWithTopics ->
                    AnimatedEntry(index + 2) {
                        SubjectCard(
                            item = subjectWithTopics,
                            onAddTopic = { name -> viewModel.addTopic(subjectWithTopics.subject.id, name) },
                            onDeleteSubject = {
                                subjectToDelete = it
                                showDeleteDialog = true
                            },
                            onEditSubject = { viewModel.updateSubject(subjectWithTopics.subject, it) },
                            onDeleteTopic = { viewModel.deleteTopic(it) },
                            onEditTopic = { topic, newName -> viewModel.updateTopic(topic, newName) },
                            onToggleTopic = {
                                haptics.confirm()
                                viewModel.toggleTopicCompletion(it, subjectWithTopics.subject.name)
                            },
                            onAddSubtopic = { topicId, name -> viewModel.addSubtopic(topicId, name) },
                            onDeleteSubtopic = { viewModel.deleteSubtopic(it) },
                            onEditSubtopic = { subtopic, newName -> viewModel.updateSubtopic(subtopic, newName) },
                            onToggleSubtopic = {
                                haptics.confirm()
                                viewModel.toggleSubtopic(it, subjectWithTopics.subject.name)
                            }
                        )
                    }
                }
            }
        }

        // === Delete-Subject confirmation ===
        if (showDeleteDialog && subjectToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete ${subjectToDelete!!.name}?") },
                text = { Text("This will delete all topics and subtopics. This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteSubject(subjectToDelete!!)
                        showDeleteDialog = false
                        subjectToDelete = null
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                }
            )
        }

        // === Add-Subject dialog ===
        if (showAddSubjectDialog) {
            AddSubjectDialog(
                onDismiss = { showAddSubjectDialog = false },
                onConfirm = { name, colorLong ->
                    viewModel.addSubject(name, "book", colorLong)
                    showAddSubjectDialog = false
                }
            )
        }

        // === Templates bottom-sheet ===
        if (showTemplatesSheet) {
            TemplatesSheet(
                onDismiss = { showTemplatesSheet = false },
                onTemplateSelected = { template ->
                    showTemplatesSheet = false
                    viewModel.importTemplate(context, template)
                },
                onPickJsonFile = {
                    showTemplatesSheet = false
                    jsonPickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            )
        }

        // === AI Generate dialog ===
        if (showAiDialog) {
            AiSyllabusDialog(
                loading = aiLoading,
                onDismiss = { if (!aiLoading) showAiDialog = false },
                onGenerate = { prompt ->
                    viewModel.generateSyllabusWithAi(context, prompt)
                }
            )
        }

        // === Revision prompt (Topic) ===
        pendingTopicRevision?.let { topic ->
            AlertDialog(
                onDismissRequest = {
                    pendingTopicRevision = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Repeat, contentDescription = null, tint = MahirColors.gold())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to revision?", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column {
                        Text(
                            "\"${topic.name}\" marked complete.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Schedule this for spaced repetition (1 → 3 → 7 → 15 → 30 → 60 → 120 days)? You can remove it later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.confirmAddTopicToRevision(topic, pendingTopicSubjectName)
                            haptics.confirm()
                            pendingTopicRevision = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
                    ) {
                        Text("Add to Revision", color = MahirColors.goldForeground())
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        haptics.tap()
                        pendingTopicRevision = null
                    }) {
                        Text("Not Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MahirColors.cardBackground(),
                shape = RoundedCornerShape(20.dp)
            )
        }

        // === Revision prompt (Subtopic) ===
        pendingSubtopicRevision?.let { subtopic ->
            AlertDialog(
                onDismissRequest = { pendingSubtopicRevision = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Repeat, contentDescription = null, tint = MahirColors.gold())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to revision?", style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Text(
                        "\"${subtopic.name}\" marked complete. Add it to your revision schedule?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.confirmAddSubtopicToRevision(subtopic, pendingSubtopicSubjectName)
                            haptics.confirm()
                            pendingSubtopicRevision = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold())
                    ) {
                        Text("Add", color = MahirColors.goldForeground())
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        haptics.tap()
                        pendingSubtopicRevision = null
                    }) {
                        Text("Not Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MahirColors.cardBackground(),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ============================================================
// HEADER (FIXED — no more overlap between Add Subject / Add Exam)
// ============================================================
@Composable
private fun SyllabusHeader(
    onAddSubject: () -> Unit,
    onImportJson: () -> Unit,
    onPickTemplate: () -> Unit,
    onAiGenerate: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Syllabus",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track your subjects and progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MahirColors.subtleBackground())
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Four primary action buttons in their own Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeaderActionButton(
                text = "Add",
                icon = Icons.Rounded.Add,
                isPrimary = true,
                onClick = onAddSubject,
                modifier = Modifier.weight(1f)
            )
            HeaderActionButton(
                text = "Templates",
                icon = Icons.Rounded.Dashboard,
                onClick = onPickTemplate,
                modifier = Modifier.weight(1f)
            )
            HeaderActionButton(
                text = "AI Gen",
                icon = Icons.Rounded.AutoAwesome,
                onClick = onAiGenerate,
                modifier = Modifier.weight(1f)
            )
            HeaderActionButton(
                text = "Import",
                icon = Icons.Rounded.FileUpload,
                onClick = onImportJson,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeaderActionButton(
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
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// ============================================================
// ADD SUBJECT DIALOG
// ============================================================
@Composable
private fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableStateOf(3) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AddCircle, contentDescription = null, tint = MahirColors.gold())
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Subject", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Mathematics") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahirColors.gold(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Pick a color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SubjectPalette.forEachIndexed { idx, (light, _) ->
                        val isSelected = selectedColorIndex == idx
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 32.dp else 28.dp)
                                .clip(CircleShape)
                                .background(light)
                                .clickable { selectedColorIndex = idx }
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val colorLong = SubjectPaletteLongs[selectedColorIndex]
                    onConfirm(name, colorLong)
                }
            }) {
                Text("Add", color = MahirColors.gold(), fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MahirColors.cardBackground(),
        shape = RoundedCornerShape(20.dp)
    )
}

// ============================================================
// TEMPLATES SHEET
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatesSheet(
    onDismiss: () -> Unit,
    onTemplateSelected: (com.example.data.SyllabusTemplate) -> Unit,
    onPickJsonFile: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Text(
                "Quick-start templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap a card to instantly import its full syllabus.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(com.example.data.SyllabusImporter.templates, key = { it.id }) { template ->
                    Surface(
                        onClick = { onTemplateSelected(template) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(template.accentColor).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    template.emoji,
                                    color = Color(template.accentColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    template.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                onClick = onPickJsonFile,
                shape = RoundedCornerShape(16.dp),
                color = MahirColors.gold().copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MahirColors.gold().copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = MahirColors.gold())
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Import from JSON file",
                            style = MaterialTheme.typography.titleSmall,
                            color = MahirColors.gold(),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Pick a .json file from storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// SUBJECT CARD
// ============================================================
@Composable
fun SubjectCard(
    item: SubjectWithTopics,
    onAddTopic: (String) -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit,
    onEditSubject: (String) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit,
    onEditTopic: (TopicEntity, String) -> Unit,
    onToggleTopic: (TopicEntity) -> Unit,
    onAddSubtopic: (String, String) -> Unit,
    onDeleteSubtopic: (SubtopicEntity) -> Unit,
    onEditSubtopic: (SubtopicEntity, String) -> Unit,
    onToggleSubtopic: (SubtopicEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddTopic by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }
    var showSubjectMenu by remember { mutableStateOf(false) }
    var editSubjectName by remember { mutableStateOf("") }

    val progress = item.progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress"
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "chevron"
    )

    val subjectColor = Color(item.subject.color)

    MahirCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(subjectColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(subjectColor, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${item.completedTopics}/${item.totalTopics} topics",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = subjectColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(subjectColor)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
                exit = shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (item.topics.isEmpty()) {
                        Text(
                            text = "No topics yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        item.topics.forEach { topicWithSubtopics ->
                            TopicRow(
                                item = topicWithSubtopics,
                                subjectColor = subjectColor,
                                onToggle = { onToggleTopic(it) },
                                onDelete = { onDeleteTopic(it) },
                                onEdit = { topic, newName -> onEditTopic(topic, newName) },
                                onAddSubtopic = { onAddSubtopic(topicWithSubtopics.topic.id, it) },
                                onDeleteSubtopic = { onDeleteSubtopic(it) },
                                onEditSubtopic = { sub, newName -> onEditSubtopic(sub, newName) },
                                onToggleSubtopic = { onToggleSubtopic(it) }
                            )
                        }
                    }

                    if (showAddTopic) {
                        OutlinedTextField(
                            value = newTopicName,
                            onValueChange = { newTopicName = it },
                            placeholder = { Text("New Topic Name", style = MaterialTheme.typography.bodyMedium) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (newTopicName.isNotBlank()) onAddTopic(newTopicName)
                                    showAddTopic = false; newTopicName = ""
                                }) {
                                    Icon(Icons.Rounded.Check, contentDescription = "Add", tint = subjectColor)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = subjectColor,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        TextButton(
                            onClick = { showAddTopic = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = subjectColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Topic", style = MaterialTheme.typography.labelMedium, color = subjectColor)
                        }
                    }

                    // Subject edit/delete
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editSubjectName = item.subject.name
                            showSubjectMenu = true
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rename", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onDeleteSubject(item.subject) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showSubjectMenu) {
        AlertDialog(
            onDismissRequest = { showSubjectMenu = false },
            title = { Text("Rename Subject") },
            text = {
                OutlinedTextField(
                    value = editSubjectName,
                    onValueChange = { editSubjectName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editSubjectName.isNotBlank()) onEditSubject(editSubjectName)
                    showSubjectMenu = false
                }) { Text("Save", color = MahirColors.gold()) }
            },
            dismissButton = {
                TextButton(onClick = { showSubjectMenu = false }) { Text("Cancel") }
            }
        )
    }
}

// ============================================================
// TOPIC ROW
// ============================================================
@Composable
fun TopicRow(
    item: TopicWithSubtopics,
    subjectColor: Color,
    onToggle: (TopicEntity) -> Unit,
    onDelete: (TopicEntity) -> Unit,
    onEdit: (TopicEntity, String) -> Unit,
    onAddSubtopic: (String) -> Unit,
    onDeleteSubtopic: (SubtopicEntity) -> Unit,
    onEditSubtopic: (SubtopicEntity, String) -> Unit,
    onToggleSubtopic: (SubtopicEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddSub by remember { mutableStateOf(false) }
    var newSubName by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(item.topic.name) { mutableStateOf(item.topic.name) }

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onToggle(item.topic) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (item.isFullyCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Toggle",
                    tint = if (item.isFullyCompleted) subjectColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (editName.isNotBlank() && editName != item.topic.name) onEdit(item.topic, editName)
                            isEditing = false
                        }) {
                            Icon(Icons.Rounded.Check, contentDescription = "Save", tint = subjectColor)
                        }
                    }
                )
            } else {
                Text(
                    text = item.topic.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.isFullyCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (item.isFullyCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(onClick = { isEditing = !isEditing }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { onDelete(item.topic) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 36.dp, top = 4.dp)) {
                item.subtopics.forEach { subtopic ->
                    SubtopicRow(
                        subtopic = subtopic,
                        subjectColor = subjectColor,
                        onToggle = { onToggleSubtopic(it) },
                        onDelete = { onDeleteSubtopic(it) },
                        onEdit = { sub, newName -> onEditSubtopic(sub, newName) }
                    )
                }
                if (showAddSub) {
                    OutlinedTextField(
                        value = newSubName,
                        onValueChange = { newSubName = it },
                        placeholder = { Text("New Subtopic", style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newSubName.isNotBlank()) onAddSubtopic(newSubName)
                                showAddSub = false; newSubName = ""
                            }) {
                                Icon(Icons.Rounded.Check, contentDescription = "Add", tint = subjectColor)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = subjectColor,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                } else {
                    TextButton(
                        onClick = { showAddSub = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("+ Add Subtopic", style = MaterialTheme.typography.labelMedium, color = subjectColor)
                    }
                }
            }
        }
    }
}

@Composable
fun SubtopicRow(
    subtopic: SubtopicEntity,
    subjectColor: Color,
    onToggle: (SubtopicEntity) -> Unit,
    onDelete: (SubtopicEntity) -> Unit,
    onEdit: (SubtopicEntity, String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(subtopic.name) { mutableStateOf(subtopic.name) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onToggle(subtopic) },
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = if (subtopic.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = "Toggle",
                tint = if (subtopic.isCompleted) subjectColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isEditing) {
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                singleLine = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = {
                        if (editName.isNotBlank() && editName != subtopic.name) onEdit(subtopic, editName)
                        isEditing = false
                    }) {
                        Icon(Icons.Rounded.Check, contentDescription = "Save", tint = subjectColor)
                    }
                }
            )
        } else {
            Text(
                text = subtopic.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (subtopic.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (subtopic.isCompleted) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )
        }
        IconButton(onClick = { isEditing = !isEditing }, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        }
        IconButton(onClick = { onDelete(subtopic) }, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        }
    }
}

// ============================================================
// EXAMS SECTION (kept separate from Add Subject)
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsSection(viewModel: SyllabusViewModel, snackbarHostState: SnackbarHostState) {
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    var showExamDialog by remember { mutableStateOf(false) }
    var examToEdit by remember { mutableStateOf<com.example.data.ExamEntity?>(null) }
    var examToDelete by remember { mutableStateOf<com.example.data.ExamEntity?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Target Exams",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    if (exams.isEmpty()) "No exams scheduled" else "${exams.size} exam${if (exams.size == 1) "" else "s"} tracked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                onClick = { showExamDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = MahirColors.subtleBackground()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Exam", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (exams.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(exams, key = { it.id }) { exam ->
                    val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    val daysLeft = ((exam.dateMillis - System.currentTimeMillis()) / 86400000L).coerceAtLeast(0)
                    MahirCard(
                        modifier = Modifier
                            .width(180.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = { examToEdit = exam })
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(exam.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(format.format(java.util.Date(exam.dateMillis)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MahirColors.gold())
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$daysLeft days left", fontSize = 12.sp, color = MahirColors.gold(), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExamDialog || examToEdit != null) {
        var name by remember(examToEdit) { mutableStateOf(examToEdit?.name ?: "") }
        var dateMillis by remember(examToEdit) { mutableStateOf(examToEdit?.dateMillis ?: (System.currentTimeMillis() + 86400000L * 7)) }
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

        AlertDialog(
            onDismissRequest = { showExamDialog = false; examToEdit = null },
            title = { Text(if (examToEdit != null) "Edit Exam" else "Add Exam") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Exam Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        Text(format.format(java.util.Date(dateMillis)))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        if (examToEdit != null) {
                            viewModel.updateExam(examToEdit!!.copy(name = name, dateMillis = dateMillis))
                        } else {
                            viewModel.addExam(name, dateMillis)
                        }
                        showExamDialog = false; examToEdit = null
                    }
                }) { Text("Save", color = MahirColors.gold()) }
            },
            dismissButton = {
                Row {
                    if (examToEdit != null) {
                        TextButton(onClick = {
                            examToDelete = examToEdit
                            showExamDialog = false; examToEdit = null
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                    TextButton(onClick = { showExamDialog = false; examToEdit = null }) { Text("Cancel") }
                }
            }
        )
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }

    if (examToDelete != null) {
        val exam = examToDelete!!
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("Delete Exam") },
            text = { Text("Are you sure you want to delete '${exam.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExam(exam)
                    examToDelete = null
                    scope.launch {
                        val result = snackbarHostState.showSnackbar("Exam deleted", "Undo", duration = SnackbarDuration.Short)
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteExam(exam)
                        }
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { examToDelete = null }) { Text("Cancel") } }
        )
    }
}

// ============================================================
// AI SYLLABUS GENERATION DIALOG
// ============================================================
@Composable
private fun AiSyllabusDialog(
    loading: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StatColors.purple())
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Syllabus Generator", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Describe what you want a syllabus for. AI will generate subjects, topics & subtopics automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("e.g. JEE Main Physics, UPSC GS, Class 12 Biology") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatColors.purple(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                if (loading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = StatColors.purple()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI is generating your syllabus…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(prompt) },
                colors = ButtonDefaults.buttonColors(containerColor = StatColors.purple()),
                enabled = !loading && prompt.isNotBlank()
            ) {
                Text("Generate", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) {
                Text("Cancel")
            }
        },
        containerColor = MahirColors.cardBackground()
    )
}
