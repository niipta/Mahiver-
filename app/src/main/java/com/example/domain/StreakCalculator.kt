package com.example.domain

import com.example.data.FocusSessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val missedYesterday: Boolean
)

/**
 * Computes streak based on either:
 *   - meeting the daily minutes goal (default 120 min), OR
 *   - completing the daily topics goal (passed in via goalMetDates)
 *
 * Fixed in this version:
 *  - Configurable daily minutes threshold (no more hardcoded 150f)
 *  - Caches longest streak by day to avoid 4×/sec recompute in HomeViewModel
 *  - Timezone-safe via ZoneId.systemDefault()
 */
object StreakCalculator {

    /** Per-day cache of longest streak to avoid recomputation. Keyed by today's date string. */
    @Volatile
    private var cachedLongest: Pair<String, Int>? = null

    fun compute(
        sessions: List<FocusSessionEntity>,
        goalMetDates: Set<String>,
        todayStr: String,
        dailyGoalMinutes: Int = 120
    ): StreakResult {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val zone = ZoneId.systemDefault()
        val validSessions = sessions.filter { it.actualDurationSeconds >= 60 }

        val dailyMinutes = validSessions.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate().format(dateFormatter)
        }.mapValues { e -> e.value.sumOf { it.actualDurationSeconds } / 60f }

        val goalThreshold = dailyGoalMinutes.toFloat().coerceAtLeast(1f)

        var streak = 0
        var date = LocalDate.now(zone)
        var missedYesterday = false
        val yesterdayStr = LocalDate.now(zone).minusDays(1).format(dateFormatter)

        while (true) {
            val dateStr = date.format(dateFormatter)
            val minutes = dailyMinutes[dateStr] ?: 0f
            val goalMet = goalMetDates.contains(dateStr)

            if (minutes >= goalThreshold || goalMet) {
                streak++
                date = date.minusDays(1)
            } else if (dateStr == todayStr) {
                // today not met yet, doesn't break streak
                date = date.minusDays(1)
            } else {
                if (dateStr == yesterdayStr) {
                    missedYesterday = true
                }
                break
            }
        }

        val longest = computeLongest(dailyMinutes, goalMetDates, dateFormatter, goalThreshold, todayStr)
        return StreakResult(
            currentStreak = streak,
            longestStreak = longest,
            missedYesterday = missedYesterday
        )
    }

    private fun computeLongest(
        dailyMinutes: Map<String, Float>,
        goalMetDates: Set<String>,
        dateFormatter: DateTimeFormatter,
        goalThreshold: Float,
        todayStr: String
    ): Int {
        // Return cached value if same day
        cachedLongest?.let { (date, value) ->
            if (date == todayStr) return value
        }

        var longest = 0
        var current = 0

        val allDatesStr = (dailyMinutes.keys + goalMetDates).toSet()
        val allDates = allDatesStr.mapNotNull {
            try { LocalDate.parse(it, dateFormatter) } catch (e: Exception) { null }
        }.sorted()

        if (allDates.isEmpty()) {
            cachedLongest = todayStr to 0
            return 0
        }

        var date = allDates.first()
        val endDate = LocalDate.now(ZoneId.systemDefault())

        while (!date.isAfter(endDate)) {
            val dateStr = date.format(dateFormatter)
            val minutes = dailyMinutes[dateStr] ?: 0f
            val goalMet = goalMetDates.contains(dateStr)

            if (minutes >= goalThreshold || goalMet) {
                current++
                if (current > longest) longest = current
            } else {
                current = 0
            }
            date = date.plusDays(1)
        }

        cachedLongest = todayStr to longest
        return longest
    }
}
