package com.example.ui.achievements

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.domain.AchievementChecker
import com.example.domain.AchievementCategory
import com.example.ui.home.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    settingsRepository: com.example.data.SettingsRepository
) {
    val unlockedIds by settingsRepository.unlockedAchievements.collectAsStateWithLifecycle()
    val homeState by homeViewModel.fullUiState.collectAsStateWithLifecycle()
    
    val currentStreak = homeState.currentStreak
    val topicsCompleted = homeState.topicsCompletedOverview
    val revisionsCompleted = homeState.revisionsDoneOverview
    val focusMinutes = homeState.lifetimeFocusMinutes
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(AchievementChecker.ALL_ACHIEVEMENTS) { ach ->
                val isUnlocked = unlockedIds.contains(ach.id)
                val progress = when(ach.category) {
                    AchievementCategory.STREAK -> currentStreak
                    AchievementCategory.FOCUS -> focusMinutes
                    AchievementCategory.TOPICS -> topicsCompleted
                    AchievementCategory.REVISIONS -> revisionsCompleted
                }
                
                AchievementItem(
                    achievement = ach,
                    isUnlocked = isUnlocked,
                    progress = progress
                )
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: com.example.domain.Achievement, isUnlocked: Boolean, progress: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
        ) {
            Icon(
                imageVector = when(achievement.iconRes) {
                    "fire" -> Icons.Rounded.LocalFireDepartment
                    "timer" -> Icons.Rounded.Timer
                    "repeat" -> Icons.Rounded.Repeat
                    else -> Icons.Rounded.CheckCircle
                },
                contentDescription = null,
                tint = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )
            
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = achievement.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (!isUnlocked) {
            val displayProgress = progress.coerceAtMost(achievement.target)
            val formatStr = if (achievement.category == AchievementCategory.FOCUS) {
                if (achievement.target < 60) {
                    "$displayProgress / ${achievement.target}m"
                } else {
                    "${displayProgress / 60}h / ${achievement.target / 60}h"
                }
            } else if (achievement.category == AchievementCategory.STREAK) {
                "$displayProgress / ${achievement.target}d"
            } else {
                "$displayProgress / ${achievement.target}"
            }
            Text(
                text = formatStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Unlocked",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
