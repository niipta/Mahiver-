package com.example.data.ai

import com.example.data.SyllabusJson
import com.example.data.SyllabusImporter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Result of an AI syllabus generation request.
 */
sealed class AiSyllabusResult {
    data class Success(val syllabus: SyllabusJson) : AiSyllabusResult()
    data class Error(val message: String, val retryable: Boolean) : AiSyllabusResult()
    object NoApiKey : AiSyllabusResult()
}

/**
 * AI engine that generates a full syllabus (subjects → topics → subtopics)
 * from a free-text prompt like "JEE Main Physics" or "UPSC GS Paper 1".
 *
 * The generated JSON matches the [SyllabusJson] schema so it can be imported
 * directly via [SyllabusImporter.import].
 */
class AiSyllabusEngine(private val getApiKey: () -> String) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(SyllabusJson::class.java)

    suspend fun generateSyllabus(prompt: String): AiSyllabusResult {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) return AiSyllabusResult.NoApiKey

        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank()) {
            return AiSyllabusResult.Error("Please enter a syllabus description", retryable = false)
        }

        val systemPrompt = """
            You are a syllabus generator for a study management app called MahirVerse.
            The user will describe what exam, course, or subject they want a syllabus for.
            Generate a complete, well-structured syllabus as JSON.

            Output ONLY valid JSON matching this exact structure (no markdown, no explanation):
            {
              "subjects": [
                {
                  "name": "Subject Name",
                  "topics": [
                    {
                      "name": "Topic Name",
                      "estimatedMinutes": 60,
                      "subtopics": ["Subtopic 1", "Subtopic 2", "Subtopic 3"]
                    }
                  ]
                }
              ]
            }

            Rules:
            1. Break down the syllabus into logical subjects (usually 2-6 subjects).
            2. Each subject should have 3-8 major topics.
            3. Each topic should have 2-5 subtopics.
            4. estimatedMinutes: 30 for easy topics, 60 for medium, 120 for hard topics.
            5. Use real, accurate topic names — do NOT invent fake topics.
            6. If the prompt is vague, make reasonable assumptions.
            7. Keep names concise (no long sentences).
            8. Do NOT include color or icon fields — the app will assign defaults.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "Generate syllabus for: $cleanPrompt")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.3f, responseMimeType = "application/json")
        )

        return try {
            val response = try {
                RetrofitClient.service.generateContent(apiKey, "gemini-2.0-flash", request)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404 || e.code() == 400) {
                    RetrofitClient.service.generateContent(apiKey, "gemini-1.5-flash", request)
                } else throw e
            }

            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText.isNullOrBlank()) {
                return AiSyllabusResult.Error("AI returned empty response", retryable = true)
            }

            // The AI may wrap JSON in markdown fences — strip them.
            val cleaned = jsonText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val syllabus = adapter.fromJson(cleaned)
                ?: return AiSyllabusResult.Error("Could not parse AI response", retryable = true)

            if (syllabus.subjects.isEmpty()) {
                return AiSyllabusResult.Error("AI generated no subjects", retryable = true)
            }

            AiSyllabusResult.Success(syllabus)
        } catch (e: retrofit2.HttpException) {
            AiSyllabusResult.Error("AI service error (${e.code()})", retryable = e.code() in 500..599)
        } catch (e: java.io.IOException) {
            AiSyllabusResult.Error("Network error — check your internet", retryable = true)
        } catch (e: Throwable) {
            AiSyllabusResult.Error("Unexpected error: ${e.message ?: "unknown"}", retryable = false)
        }
    }
}
