package com.example

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.example.ui.home.HomeScreen
import com.example.ui.syllabus.SyllabusScreen
import com.example.ui.focus.FocusScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.data.sync.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
  private val requestPermissionLauncher = registerForActivityResult(
      androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
      // Permission handled
  }

    private fun maybeAskNotificationPermission() {
      if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return
      val granted = androidx.core.content.ContextCompat.checkSelfPermission(
          this, android.Manifest.permission.POST_NOTIFICATIONS
      ) == android.content.pm.PackageManager.PERMISSION_GRANTED
      
      if (!granted && !hasAskedBefore()) {
          markAskedBefore()
          requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
      }
  }

  private fun hasAskedBefore(): Boolean =
      getSharedPreferences("perm_prefs", MODE_PRIVATE)
          .getBoolean("asked_notif", false)

  private fun markAskedBefore() =
      getSharedPreferences("perm_prefs", MODE_PRIVATE).edit()
          .putBoolean("asked_notif", true).apply()
          
  
    private fun isOnboardingComplete(): Boolean =
        getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            .getBoolean("onboarding_complete", false)

  private fun hasSeededSyllabus(): Boolean =
      getSharedPreferences("seed_prefs", MODE_PRIVATE)
          .getBoolean("seeded", false)

  private fun markSeededSyllabus() =
      getSharedPreferences("seed_prefs", MODE_PRIVATE).edit()
          .putBoolean("seeded", true).apply()

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    com.example.service.TimerManager.init(this)
    
    // Restore DND if app crashed while session was active
    if (com.example.service.TimerManager.timerState.value != com.example.service.TimerState.RUNNING && 
        com.example.service.TimerManager.timerState.value != com.example.service.TimerState.PAUSED) {
        val prefs = getSharedPreferences("mahirverse_settings", android.content.Context.MODE_PRIVATE)
        if (prefs.contains("saved_dnd_filter")) {
            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val savedFilter = prefs.getInt("saved_dnd_filter", android.app.NotificationManager.INTERRUPTION_FILTER_ALL)
                notificationManager.setInterruptionFilter(savedFilter)
            }
            prefs.edit().remove("saved_dnd_filter").apply()
        }
    }

    if (com.example.service.TimerManager.timerState.value == com.example.service.TimerState.RUNNING) {
        val intent = android.content.Intent(this, com.example.service.FocusService::class.java).apply {
            action = com.example.service.FocusService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    maybeAskNotificationPermission()
    
    // Fetch FCM token for remote messaging
    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            android.util.Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
            return@addOnCompleteListener
        }
        val token = task.result
        android.util.Log.d("MainActivity", "FCM Token: $token")
    }
    


    // Cleanup duplicates on startup
    lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val db = com.example.data.AppDatabase.getDatabase(applicationContext)
        val allRevs = db.revisionDao().getAllRevisionsSync()
        val grouped = allRevs.groupBy { it.relatedId }
        val syncDao = db.syncDao()
        grouped.forEach { (relatedId, revList) ->
            if (revList.size > 1) {
                val sorted = revList.sortedWith(compareBy<com.example.data.RevisionEntity> { it.isCompleted }.thenByDescending { it.repetitionLevel })
                val keep = sorted.first()
                for (i in 1 until sorted.size) {
                    val dupe = sorted[i]
                    db.revisionDao().deleteRevision(dupe)
                    syncDao.insertSyncTask(com.example.data.SyncQueueEntity(operationType = "DELETE", entityType = "REVISION", entityId = dupe.id))
                }
            }
        }
        
        if (!hasSeededSyllabus()) {
            com.example.data.ComputerSyllabusSeeder.seed(applicationContext)
            markSeededSyllabus()
        }
    }

    // Schedule Firestore Sync
    val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "FirestoreSyncWork",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )

    // Schedule Smart Notification Engine
    val smartNotificationRequest = PeriodicWorkRequestBuilder<com.example.service.SmartNotificationWorker>(3, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "SmartNotificationWork",
        ExistingPeriodicWorkPolicy.KEEP,
        smartNotificationRequest
    )
    
    // Schedule Daily Streak Check
    val streakRequest = PeriodicWorkRequestBuilder<com.example.service.StreakWorker>(24, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "StreakEvaluationWork",
        ExistingPeriodicWorkPolicy.KEEP,
        streakRequest
    )

    // Schedule Auto Backup (Daily)
    val autoBackupRequest = PeriodicWorkRequestBuilder<com.example.service.AutoBackupWorker>(24, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "AutoBackupWork",
        ExistingPeriodicWorkPolicy.KEEP,
        autoBackupRequest
    )
    
    // Schedule Widget Updates (Every 15 mins)
    val widgetRequest = PeriodicWorkRequestBuilder<com.example.widget.MahirWidgetWorker>(15, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "WidgetUpdateWork",
        ExistingPeriodicWorkPolicy.KEEP,
        widgetRequest
    )

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        // Auth check: if not logged in → auth screen, if logged in but onboarding not done → onboarding, else → home
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val startDest = when {
            currentUser == null -> "auth"
            !isOnboardingComplete() -> "onboarding"
            else -> "home"
        }
        NavHost(
            navController = navController,
            startDestination = startDest,
            enterTransition = {
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + 
                androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(300), initialOffsetX = { it / 4 })
            },
            exitTransition = {
                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) + 
                androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(200), targetOffsetX = { -it / 4 })
            },
            popEnterTransition = {
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + 
                androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(300), initialOffsetX = { -it / 4 })
            },
            popExitTransition = {
                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) + 
                androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(200), targetOffsetX = { it / 4 })
            }
        ) {
            composable("auth") {
                com.example.ui.auth.AuthScreen(navController = navController)
            }
            composable("onboarding") {
                com.example.ui.onboarding.OnboardingScreen(navController = navController)
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("syllabus") {
                SyllabusScreen(navController = navController)
            }
            composable("mocks") {
                com.example.ui.mocks.MocksScreen(navController = navController)
            }
            composable("revision") {
                com.example.ui.revision.RevisionScreen(navController = navController)
            }
            composable(
                "focus",
                enterTransition = {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.scaleIn(initialScale = 0.92f, animationSpec = androidx.compose.animation.core.tween(300))
                },
                popEnterTransition = {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.scaleIn(initialScale = 0.92f, animationSpec = androidx.compose.animation.core.tween(300))
                }
            ) {
                FocusScreen(navController = navController)
            }
            composable("analytics") {
                com.example.ui.analytics.AnalyticsScreen(navController = navController)
            }
            composable("planner") {
                @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
                com.example.ui.planner.PlannerScreen(navController = navController)
            }
            composable("history") {
                com.example.ui.history.StudyHistoryScreen(navController = navController)
            }
            composable("more") {
                com.example.ui.more.MoreScreen(navController = navController)
            }
            composable("achievements") {
                val context = androidx.compose.ui.platform.LocalContext.current
                com.example.ui.achievements.AchievementsScreen(
                    navController = navController,
                    settingsRepository = com.example.data.SettingsRepository.getInstance(context)
                )
            }
            composable("backup") {
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.example.ui.backup.BackupRestoreViewModel>()
                com.example.ui.backup.BackupRestoreScreen(navController = navController, viewModel = viewModel)
            }
            composable("admin") {
                com.example.ui.admin.AdminScreen(navController = navController)
            }
            composable("subscription") {
                com.example.ui.subscription.SubscriptionScreen(navController = navController)
            }
        }
      }
    }
  }
}
