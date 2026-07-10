package com.example.data.ai

import com.example.data.FocusSessionEntity
import com.example.data.RevisionEntity
import com.example.data.SubjectWithTopics
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * AI-generated personal analysis result.
 */
@JsonClass(generateAdapter = true)
data class PersonalAnalysis(
    val summary: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val tonightTask: String,
    val motivationalMessage: String
)

sealed class AiAnalysisResult {
    data class Success(val analysis: PersonalAnalysis) : AiAnalysisResult()
    data class Error(val message: String, val retryable: Boolean) : AiAnalysisResult()
    object NoApiKey : AiAnalysisResult()
}

/**
 * Generates a personalized study analysis using Gemini AI.
 *
 * This is different from the Smart Plan (which suggests what to study) —
 * this analyses HOW you've been studying and gives personal feedback:
 * - What you're doing well (strengths)
 * - What needs improvement
 * - One specific task to do tonight
 * - A motivational message tailored to your data
 *
 * At night (after 8 PM), the tone shifts to a gentle "guilt trip" if the
 * user hasn't studied enough — not harsh, but enough to make them reflect.
 */
class AiAnalysisEngine(private val getApiKey: () -> String) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(PersonalAnalysis::class.java)

    suspend fun generateAnalysis(
        subjects: List<SubjectWithTopics>,
        revisions: List<RevisionEntity>,
        recentSessions: List<FocusSessionEntity>,
        currentStreak: Int,
        dailyGoalMinutes: Int
    ): AiAnalysisResult {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) return AiAnalysisResult.NoApiKey

        // Compute stats for the prompt
        val now = System.currentTimeMillis()
        val todayStart = run {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.clear(java.util.Calendar.MINUTE)
            cal.clear(java.util.Calendar.SECOND)
            cal.clear(java.util.Calendar.MILLISECOND)
            cal.timeInMillis
        }
        val weekAgo = now - (7L * 24 * 60 * 60 * 1000L)

        val todayMinutes = recentSessions
            .filter { it.timestamp >= todayStart && (it.sessionType == "Focus" || it.sessionType == "Study") }
            .sumOf { it.actualDurationSeconds / 60 }
        val weekMinutes = recentSessions
            .filter { it.timestamp >= weekAgo && (it.sessionType == "Focus" || it.sessionType == "Study") }
            .sumOf { it.actualDurationSeconds / 60 }
        val pendingRevisions = revisions.count { !it.isCompleted && it.isActive }
        val overdueRevisions = revisions.count { !it.isCompleted && it.isActive && it.scheduledDateMillis < now }
        val totalTopics = subjects.sumOf { it.totalTopics }
        val completedTopics = subjects.sumOf { it.completedTopics }

        // Determine time of day for tone
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val isNight = hour >= 20 // After 8 PM
        val toneInstruction = if (isNight) {
            "It's night time (${hour}:00). The user studied $todayMinutes minutes today (goal: $dailyGoalMinutes). " +
            if (todayMinutes < dailyGoalMinutes) {
                "They're below their daily goal. Be honest but not harsh — a gentle guilt trip to make them reflect. " +
                "In the motivationalMessage, acknowledge the missed goal but encourage them to do better tomorrow. " +
                "Do NOT say 'it's okay'. Say it like a caring but strict mentor."
            } else {
                "They met their goal today! Be warm and encouraging in the motivationalMessage."
            }
        } else {
            "It's daytime. Be encouraging and motivational in tone."
        }

        val prompt = """
            You are MahirVerse's personal study coach. Analyse the user's study data and give honest, personal feedback.

            Output ONLY valid JSON (no markdown, no explanation):
            {
              "summary": "One sentence overview of their study situation",
              "strengths": ["What they're doing well 1", "What they're doing well 2"],
              "improvements": ["What needs work 1", "What needs work 2"],
              "tonightTask": "One specific, actionable task to do tonight (max 15 words)",
              "motivationalMessage": "Personal motivational message based on their data (2-3 sentences)"
            }

            User Data:
            - Today: $todayMinutes minutes studied (daily goal: $dailyGoalMinutes min)
            - This week: $weekMinutes minutes total
            - Current streak: $currentStreak days
            - Topics completed: $completedTopics / $totalTopics
            - Pending revisions: $pendingRevisions ($overdueRevisions overdue)
            - Subjects: ${subjects.joinToString { "${it.subject.name} (${it.completedTopics}/${it.totalTopics})" }}

            Rules:
            1. Be personal — reference their actual numbers and subjects.
            2. $toneInstruction
            3. Keep strengths and improvements to 1-2 items each.
            4. tonightTask must be specific (e.g. "Revise Calculus formulas for 20 min").
            5. If data is empty, say so honestly — don't make up fake progress.
            6. Write in simple English with occasional Hindi words if natural.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.4f, responseMimeType = "application/json")
        )

        return try {
            val response = AiHelper.callWithRetry(apiKey, request)

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText.isNullOrBlank()) {
                return AiAnalysisResult.Error("AI returned empty response", retryable = true)
            }

            val cleaned = jsonText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val analysis = adapter.fromJson(cleaned)
                ?: return AiAnalysisResult.Error("Could not parse AI response", retryable = true)

            AiAnalysisResult.Success(analysis)
        } catch (e: AiCallException) {
            AiAnalysisResult.Error(e.message, retryable = e.code == 429 || e.code == -1 || e.code in 500..599)
        } catch (e: Throwable) {
            AiAnalysisResult.Error("Unexpected error: ${e.message ?: "unknown"}", retryable = false)
        }
    }
}
