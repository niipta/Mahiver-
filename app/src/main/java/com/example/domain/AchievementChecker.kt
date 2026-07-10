package com.example.domain

data class LifetimeStats(
    val streak: Int,
    val focusMinutes: Int,
    val topicsCompleted: Int,
    val revisionsCompleted: Int
)

data class Achievement(
    val id: String,
    val category: AchievementCategory,
    val title: String,
    val target: Int,
    val iconRes: String
)

enum class AchievementCategory {
    STREAK, FOCUS, TOPICS, REVISIONS
}

object AchievementChecker {
    val ALL_ACHIEVEMENTS = listOf(
        Achievement("streak_3", AchievementCategory.STREAK, "3-Day Streak", 3, "fire"),
        Achievement("streak_7", AchievementCategory.STREAK, "7-Day Streak", 7, "fire"),
        Achievement("streak_30", AchievementCategory.STREAK, "30-Day Streak", 30, "fire"),
        Achievement("streak_100", AchievementCategory.STREAK, "100-Day Streak", 100, "fire"),
        
        Achievement("focus_first", AchievementCategory.FOCUS, "First Focus", 25, "timer"),
        Achievement("focus_10h", AchievementCategory.FOCUS, "10 Hours Focus", 10 * 60, "timer"),
        Achievement("focus_50h", AchievementCategory.FOCUS, "50 Hours Focus", 50 * 60, "timer"),
        Achievement("focus_100h", AchievementCategory.FOCUS, "100 Hours Focus", 100 * 60, "timer"),
        
        Achievement("topics_10", AchievementCategory.TOPICS, "10 Topics Completed", 10, "done"),
        Achievement("topics_50", AchievementCategory.TOPICS, "50 Topics Completed", 50, "done"),
        Achievement("topics_200", AchievementCategory.TOPICS, "200 Topics Completed", 200, "done"),
        
        Achievement("revs_10", AchievementCategory.REVISIONS, "10 Revisions Completed", 10, "repeat"),
        Achievement("revs_50", AchievementCategory.REVISIONS, "50 Revisions Completed", 50, "repeat"),
        Achievement("revs_200", AchievementCategory.REVISIONS, "200 Revisions Completed", 200, "repeat")
    )

    fun check(stats: LifetimeStats, unlockedIds: Set<String>): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()

        for (ach in ALL_ACHIEVEMENTS) {
            if (unlockedIds.contains(ach.id)) continue
            
            val unlocked = when (ach.category) {
                AchievementCategory.STREAK -> stats.streak >= ach.target
                AchievementCategory.FOCUS -> stats.focusMinutes >= ach.target
                AchievementCategory.TOPICS -> stats.topicsCompleted >= ach.target
                AchievementCategory.REVISIONS -> stats.revisionsCompleted >= ach.target
            }
            if (unlocked) newlyUnlocked.add(ach)
        }
        return newlyUnlocked
    }
}
