package com.example.ui.more

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.components.MahirBottomNavigation
import com.example.ui.components.MahirCard
import com.example.ui.components.SectionTitle
import com.example.ui.theme.Dimens
import com.example.ui.theme.MahirColors
import com.example.ui.theme.StatColors
import com.example.util.rememberMahirHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController, viewModel: MoreViewModel = hiltViewModel()) {
    val haptics = rememberMahirHaptics()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val amoledMode by viewModel.amoledMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val autoEnableDnd by viewModel.autoEnableDnd.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsStateWithLifecycle()
    val dailyGoalMinutes by viewModel.dailyGoalMinutes.collectAsStateWithLifecycle()
    val ambientSoundEnabled by viewModel.ambientSoundEnabled.collectAsStateWithLifecycle()
    val geminiApiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showNameDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showDndPrompt by remember { mutableStateOf(false) }
    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Firebase auth state
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    ThemeDialog(
        showDialog = showThemeDialog,
        currentTheme = themeMode,
        onThemeSelected = {
            haptics.confirm()
            viewModel.updateThemeMode(it)
            showThemeDialog = false
        },
        onDismiss = { showThemeDialog = false }
    )
    NameDialog(
        showDialog = showNameDialog,
        currentName = userName,
        onNameSaved = {
            haptics.confirm()
            viewModel.updateUserName(it)
            showNameDialog = false
        },
        onDismiss = { showNameDialog = false }
    )
    DailyGoalDialog(
        showDialog = showDailyGoalDialog,
        currentMinutes = dailyGoalMinutes,
        onSave = {
            haptics.confirm()
            viewModel.updateDailyGoalMinutes(it)
            showDailyGoalDialog = false
        },
        onDismiss = { showDailyGoalDialog = false }
    )
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
    if (showResetDialog) {
        ResetAppDialog(
            onConfirm = {
                haptics.reject()
                viewModel.requestReset()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = geminiApiKey,
            onSave = {
                haptics.confirm()
                viewModel.updateApiKey(it)
                showApiKeyDialog = false
            },
            onClear = {
                haptics.reject()
                viewModel.clearApiKey()
                showApiKeyDialog = false
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out?", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("Aapka data cloud pe sync rahega. Wapas login karke access kar sakte ho.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.confirm()
                        auth.signOut()
                        showSignOutDialog = false
                        // Restart to auth screen
                        val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sign Out", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MahirColors.cardBackground(),
            shape = RoundedCornerShape(Dimens.cardRadius)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MahirBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPaddingHorizontal),
            contentPadding = PaddingValues(top = Dimens.screenPaddingTop, bottom = Dimens.screenPaddingBottom),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingXxl)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                MahirCard(modifier = Modifier.fillMaxWidth(), onClick = {
                    haptics.tap()
                    showNameDialog = true
                }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingSm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MahirColors.gold().copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = MahirColors.gold(),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(Dimens.spacingLg))
                        Column {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacingXs))
                            Text(
                                text = currentUser?.email ?: "Guest user",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // === ACCOUNT SECTION ===
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    SectionTitle("Account")
                    MoreMenuItem(
                        icon = Icons.Rounded.WorkspacePremium,
                        title = "Subscription",
                        subtitle = "Manage your subscription",
                        onClick = {
                            haptics.tap()
                            navController.navigate("subscription")
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.AdminPanelSettings,
                        title = "Admin Panel",
                        subtitle = "Admin access only",
                        onClick = {
                            haptics.tap()
                            navController.navigate("admin")
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.Logout,
                        title = "Sign Out",
                        subtitle = "Cloud data safe rahega",
                        onClick = {
                            haptics.tap()
                            showSignOutDialog = true
                        },
                        isDanger = true
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    SectionTitle("App Features")
                    MoreMenuItem(
                        icon = Icons.Rounded.EmojiEvents,
                        title = "Achievements",
                        subtitle = "View your badges and progress",
                        onClick = {
                            haptics.tap()
                            navController.navigate("achievements")
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.DateRange,
                        title = "Planner",
                        subtitle = "Manage your study plan",
                        onClick = {
                            haptics.tap()
                            navController.navigate("planner")
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.Quiz,
                        title = "Mock Tests",
                        subtitle = "Analysis planner & test logs",
                        onClick = {
                            haptics.tap()
                            navController.navigate("mocks")
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.History,
                        title = "History",
                        subtitle = "View past focus sessions",
                        onClick = {
                            haptics.tap()
                            navController.navigate("history")
                        }
                    )
                }
            }

            // === AI & Intelligence ===
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    SectionTitle("AI & Intelligence")
                    MoreMenuItem(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Gemini API Key",
                        subtitle = if (geminiApiKey.isBlank()) "Not configured — AI features disabled" else "Configured (${geminiApiKey.take(8)}…)",
                        onClick = {
                            haptics.tap()
                            showApiKeyDialog = true
                        }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    SectionTitle("Daily Goal")
                    MoreMenuItem(
                        icon = Icons.Rounded.Flag,
                        title = "Daily Study Goal",
                        subtitle = "${dailyGoalMinutes / 60}h ${dailyGoalMinutes % 60}m per day",
                        onClick = {
                            haptics.tap()
                            showDailyGoalDialog = true
                        }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    SectionTitle("Data & Privacy")
                    MoreMenuItem(
                        icon = Icons.Rounded.CloudUpload,
                        title = "Backup / Restore",
                        subtitle = "Export or import your data",
                        onClick = {
                            haptics.tap()
                            navController.navigate("backup")
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.Sync,
                        title = "Cloud Sync",
                        subtitle = "Sync data to Firestore (cloud backup)",
                        onClick = {
                            haptics.tap()
                            // Trigger sync via WorkManager
                            val syncRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.data.sync.SyncWorker>().build()
                            androidx.work.WorkManager.getInstance(context).enqueue(syncRequest)
                            android.widget.Toast.makeText(context, "Cloud sync started — check back in a minute", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.CloudDownload,
                        title = "Restore from Cloud",
                        subtitle = "Pull data from Firestore to this device",
                        onClick = {
                            haptics.tap()
                            val restoreRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.data.sync.RestoreWorker>().build()
                            androidx.work.WorkManager.getInstance(context).enqueue(restoreRequest)
                            android.widget.Toast.makeText(context, "Cloud restore started — check back in a minute", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.LockReset,
                        title = "Reset App Data",
                        subtitle = "Clear all study data permanently",
                        onClick = {
                            haptics.tap()
                            showResetDialog = true
                        },
                        isDanger = true
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    SectionTitle("General")
                    MoreMenuItem(
                        icon = Icons.Rounded.Palette,
                        title = "Appearance",
                        subtitle = themeMode.toFriendlyLabel(),
                        onClick = {
                            haptics.tap()
                            showThemeDialog = true
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.DarkMode,
                        title = "True Black (AMOLED)",
                        subtitle = if (amoledMode) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = amoledMode,
                                onCheckedChange = {
                                    haptics.tap()
                                    viewModel.updateAmoledMode(it)
                                }
                            )
                        },
                        onClick = {}
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.Notifications,
                        title = "Notifications",
                        subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    haptics.tap()
                                    viewModel.updateNotificationsEnabled(it)
                                }
                            )
                        },
                        onClick = {}
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.DoNotDisturbOn,
                        title = "Auto-enable DND during focus",
                        subtitle = if (autoEnableDnd) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = autoEnableDnd,
                                onCheckedChange = { newValue ->
                                    haptics.tap()
                                    if (newValue) {
                                        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                        if (!notificationManager.isNotificationPolicyAccessGranted) {
                                            showDndPrompt = true
                                        } else {
                                            viewModel.updateAutoEnableDnd(true)
                                        }
                                    } else {
                                        viewModel.updateAutoEnableDnd(false)
                                    }
                                }
                            )
                        },
                        onClick = {}
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.VolumeUp,
                        title = "Sound Effects",
                        subtitle = if (soundEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    haptics.tap()
                                    viewModel.updateSoundEnabled(it)
                                }
                            )
                        },
                        onClick = {}
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.Vibration,
                        title = "Haptic Feedback",
                        subtitle = if (hapticsEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = {
                                    viewModel.updateHapticsEnabled(it)
                                }
                            )
                        },
                        onClick = {}
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Ambient Sounds",
                        subtitle = if (ambientSoundEnabled) "Enabled" else "Disabled",
                        trailing = {
                            Switch(
                                checked = ambientSoundEnabled,
                                onCheckedChange = {
                                    haptics.tap()
                                    viewModel.updateAmbientSoundEnabled(it)
                                }
                            )
                        },
                        onClick = {}
                    )
                    MoreMenuItem(
                        icon = Icons.AutoMirrored.Rounded.Help,
                        title = "Help & Support",
                        subtitle = "Get assistance",
                        onClick = {
                            haptics.tap()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ai.studio/apps/edf84881-a20e-407f-87d0-60564237de9c"))
                            context.startActivity(intent)
                        }
                    )
                    MoreMenuItem(
                        icon = Icons.Rounded.Info,
                        title = "About MahirVerse",
                        subtitle = "Version ${BuildConfig.VERSION_NAME}",
                        onClick = {
                            haptics.tap()
                            showAboutDialog = true
                        }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.spacingLg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MahirVerse v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Text(
                        text = "Crafted with care • Master your study",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    com.example.ui.components.MahirWatermark()
                }
            }
        }
    }

    // Wire the previously dead DND dialog
    DndPromptDialog(
        showDialog = showDndPrompt,
        onDismiss = { showDndPrompt = false }
    )
}

fun String.toFriendlyLabel(): String = when (this) {
    "SYSTEM" -> "Follow System"
    "LIGHT" -> "Light"
    "DARK" -> "Dark"
    "DYNAMIC" -> "Dynamic (Material You)"
    else -> this
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    isDanger: Boolean = false
) {
    val accentColor = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    MahirCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (trailing == null) onClick else ({})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacingLg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
fun ThemeDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Choose Theme") },
            text = {
                Column {
                    val options = listOf("SYSTEM", "LIGHT", "DARK", "DYNAMIC")
                    options.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeSelected(mode)
                                }
                                .padding(Dimens.spacingLg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == mode,
                                onClick = { onThemeSelected(mode) }
                            )
                            Spacer(modifier = Modifier.width(Dimens.spacingLg))
                            Text(text = mode.toFriendlyLabel(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            },
            containerColor = MahirColors.cardBackground(),
            shape = RoundedCornerShape(Dimens.cardRadius)
        )
    }
}

@Composable
fun NameDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentName: String,
    onNameSaved: (String) -> Unit
) {
    if (showDialog) {
        var tempName by remember(currentName) { mutableStateOf(currentName) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Update Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(Dimens.cardRadiusSm),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onNameSaved(tempName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold()),
                    shape = RoundedCornerShape(Dimens.cardRadiusSm)
                ) {
                    Text("Save", color = MahirColors.goldForeground())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MahirColors.cardBackground(),
            shape = RoundedCornerShape(Dimens.cardRadius)
        )
    }
}

@Composable
fun DailyGoalDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentMinutes: Int,
    onSave: (Int) -> Unit
) {
    if (showDialog) {
        var hours by remember(currentMinutes) { mutableStateOf((currentMinutes / 60).coerceAtLeast(0)) }
        var minutes by remember(currentMinutes) { mutableStateOf(currentMinutes % 60) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set Daily Study Goal") },
            text = {
                Column {
                    Text(
                        "How long do you want to study each day?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                        NumberStepper(
                            label = "Hours",
                            value = hours,
                            range = 0..12,
                            onValueChange = { hours = it }
                        )
                        NumberStepper(
                            label = "Minutes",
                            value = minutes,
                            range = 0..59 step 5,
                            onValueChange = { minutes = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    Text(
                        "Total: ${hours * 60 + minutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MahirColors.gold(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onSave(hours * 60 + minutes) },
                    colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold()),
                    shape = RoundedCornerShape(Dimens.cardRadiusSm)
                ) {
                    Text("Save", color = MahirColors.goldForeground())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MahirColors.cardBackground(),
            shape = RoundedCornerShape(Dimens.cardRadius)
        )
    }
}

@Composable
fun NumberStepper(label: String, value: Int, range: IntProgression, onValueChange: (Int) -> Unit) {
    val step = (range.step).coerceAtLeast(1)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val next = value - step
                if (next >= range.first) onValueChange(next)
            }) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = {
                val next = value + step
                if (next <= range.last) onValueChange(next)
            }) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase")
            }
        }
    }
}

@Composable
fun DndPromptDialog(showDialog: Boolean, onDismiss: () -> Unit) {
    if (showDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Auto-enable DND", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("To automatically enable Do Not Disturb during focus sessions, we need permission to modify your Do Not Disturb settings.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        context.startActivity(intent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold()),
                    shape = RoundedCornerShape(Dimens.cardRadiusSm)
                ) {
                    Text("Grant Permission", color = MahirColors.goldForeground())
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            },
            containerColor = MahirColors.cardBackground(),
            shape = RoundedCornerShape(Dimens.cardRadius)
        )
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MahirColors.gold().copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.School, contentDescription = null, tint = MahirColors.gold())
                }
                Spacer(modifier = Modifier.width(Dimens.spacingMd))
                Text("MahirVerse", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column {
                Text(
                    "A clean, minimal, and premium study management app. Track your syllabus, master spaced repetition, focus with the built-in Pomodoro timer, and let AI plan your day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacingLg))
                Text("Version: ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Master your study, master your life.", style = MaterialTheme.typography.bodySmall, color = MahirColors.gold(), fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MahirColors.gold()),
                shape = RoundedCornerShape(Dimens.cardRadiusSm)
            ) {
                Text("Close", color = MahirColors.goldForeground())
            }
        },
        containerColor = MahirColors.cardBackground(),
        shape = RoundedCornerShape(Dimens.cardRadius)
    )
}

@Composable
fun ResetAppDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset App Data?", color = MaterialTheme.colorScheme.error) },
        text = {
            Text(
                "This will permanently delete all subjects, topics, focus sessions, revisions, plans, and exams. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(Dimens.cardRadiusSm)
            ) {
                Text("Reset Everything", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
            }
        },
        containerColor = MahirColors.cardBackground(),
        shape = RoundedCornerShape(Dimens.cardRadius)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var keyInput by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StatColors.purple())
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gemini API Key", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Paste your Google Gemini API key to enable AI features (Smart Plan, Syllabus Auto-Generate).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("API Key") },
                    placeholder = { Text("AIza…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatColors.purple(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Get a free key at: aistudio.google.com/apikey",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(keyInput.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = StatColors.purple()),
                shape = RoundedCornerShape(Dimens.cardRadiusSm),
                enabled = keyInput.isNotBlank()
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Row {
                if (currentKey.isNotBlank()) {
                    TextButton(onClick = onClear) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        containerColor = MahirColors.cardBackground(),
        shape = RoundedCornerShape(Dimens.cardRadius)
    )
}

// Backwards-compat helper used elsewhere
