package com.example.data.ai

import com.example.data.SubjectWithTopics
import com.example.data.RevisionEntity
import com.example.data.FocusSessionEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class DailyPlanResponse(
    val suggestedTopics: List<String>,
    val recommendedFocusSessions: Int,
    val weakTopicsDetected: List<String>,
    val priorities: String
)


sealed class AiPlanResult {
    data class Success(val plan: DailyPlanResponse) : AiPlanResult()
    data class Error(val message: String, val retryable: Boolean) : AiPlanResult()
    object NoApiKey : AiPlanResult()
}
class AiPlannerEngine(private val getApiKey: () -> String) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(DailyPlanResponse::class.java)

    suspend fun generateDailyPlan(
        subjects: List<SubjectWithTopics>,
        revisions: List<RevisionEntity>,
        recentSessions: List<FocusSessionEntity>,
        studyHours: String,
        examsCount: Int
    ): AiPlanResult {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) return AiPlanResult.NoApiKey

        val pendingTopics = subjects.flatMap { it.topics }.filter { !it.isFullyCompleted }.map { it.topic.name }.take(10)
        val pendingRevisions = revisions.filter { !it.isCompleted && it.isActive }.map { it.title }.take(5)

        val prompt = """
            You are the "MahirVerse" intelligent productivity engine.
            Goal: Generate a minimal daily study plan based on user data.
            Do not provide conversational text. Output ONLY valid JSON matching this structure:
            {
              "suggestedTopics": ["Topic 1", "Topic 2"],
              "recommendedFocusSessions": 3,
              "weakTopicsDetected": ["Topic causing trouble"],
              "priorities": "Focus on revision today"
            }

            Current Data:
            - Pending syllabus topics: ${pendingTopics.joinToString()}
            - Pending revisions: ${pendingRevisions.joinToString()}
            - Total study hours recently: $studyHours
            - Upcoming exams: $examsCount

            Based on this, prioritize weak topics if revisions are piling up, and suggest 1-2 topics.
            CRITICAL RULES:
            1. ONLY use exact topic names provided in the Current Data.
            2. If Current Data is empty, do NOT make up fake subjects or fake topics. Instead, return empty lists [] for topics and a generic welcoming message for priorities (e.g. "Add topics to get started").
            3. Do not invent any placeholder data.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.2f, responseMimeType = "application/json")
        )

        return try {
            val response = AiHelper.callWithRetry(apiKey, request)

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            val plan = jsonText?.let { adapter.fromJson(it) }

            if (plan != null) {
                AiPlanResult.Success(plan)
            } else {
                AiPlanResult.Error("Empty AI response", retryable = true)
            }
        } catch (e: AiCallException) {
            AiPlanResult.Error(e.message, retryable = e.code == 429 || e.code == -1 || e.code in 500..599)
        } catch (e: Throwable) {
            AiPlanResult.Error("Unexpected error: ${e.message ?: "unknown"}", retryable = false)
        }
    }
}
