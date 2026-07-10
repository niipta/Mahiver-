package com.example.data.ai

import kotlinx.coroutines.delay
import retrofit2.HttpException

/**
 * Helper for calling the Gemini API with automatic retry + model fallback.
 *
 * 429 (Too Many Requests) is the most common error on the free tier.
 * This helper handles it by:
 *   1. Retrying up to 3 times with exponential backoff (2s, 4s, 8s)
 *   2. Falling back to alternative models if the primary returns 429/404
 *   3. Returning a user-friendly error message for 429
 */
object AiHelper {

    /** Models to try in order. gemini-2.0-flash is primary; others are fallbacks. */
    private val MODEL_CHAIN = listOf(
        "gemini-2.0-flash",
        "gemini-2.0-flash-lite",
        "gemini-1.5-flash",
        "gemini-1.5-flash-8b"
    )

    /** Maximum retry attempts for 429/503 errors. */
    private const val MAX_RETRIES = 3

    /**
     * Calls the Gemini API with retry + model fallback.
     * Returns the response on success, or throws an [AiCallException] on failure.
     */
    suspend fun callWithRetry(
        apiKey: String,
        request: GenerateContentRequest
    ): GenerateContentResponse {
        var lastException: Exception? = null

        // Try each model in the chain
        for (model in MODEL_CHAIN) {
            // Try with retries for each model
            for (attempt in 0..MAX_RETRIES) {
                try {
                    return RetrofitClient.service.generateContent(apiKey, model, request)
                } catch (e: HttpException) {
                    lastException = e
                    when (e.code()) {
                        // Rate limited — wait and retry the same model
                        429 -> {
                            if (attempt < MAX_RETRIES) {
                                val backoffMs = (2000L * (1 shl attempt)) // 2s, 4s, 8s
                                delay(backoffMs)
                                continue
                            }
                            // Exhausted retries for this model — try next model
                            break
                        }
                        // Model not found / bad request — try next model immediately
                        400, 404 -> break
                        // Server errors — retry same model
                        in 500..599 -> {
                            if (attempt < MAX_RETRIES) {
                                delay(1000L * (attempt + 1))
                                continue
                            }
                            break
                        }
                        // Other errors (401, 403) — don't retry, don't try other models
                        else -> throw AiCallException(e.code(), friendlyMessage(e.code()))
                    }
                } catch (e: java.io.IOException) {
                    lastException = e
                    if (attempt < MAX_RETRIES) {
                        delay(1000L * (attempt + 1))
                        continue
                    }
                    break
                }
            }
        }

        // All models + retries exhausted
        val code = (lastException as? HttpException)?.code() ?: -1
        throw AiCallException(code, friendlyMessage(code))
    }

    /**
     * Maps an HTTP status code to a user-friendly message in Hinglish.
     */
    private fun friendlyMessage(code: Int): String = when (code) {
        429 -> "Rate limit reached (429). Free tier me bahut zyada requests ho gayi. Thodi der baad try karo, ya API key check karo."
        400 -> "Bad request (400). Model ko request samajh nahi aayi."
        401 -> "Invalid API key (401). Settings me sahi Gemini API key daalo."
        403 -> "Access forbidden (403). API key me permission nahi hai ya billing disabled hai."
        404 -> "Model not found (404). Ye model ab available nahi hai."
        in 500..599 -> "Gemini server error ($code). Thodi der baad try karo."
        -1 -> "Network error. Internet connection check karo."
        else -> "AI service error ($code)."
    }
}

/**
 * Exception thrown when an AI API call fails after all retries.
 * The [code] is -1 for network errors, otherwise the HTTP status code.
 */
class AiCallException(val code: Int, override val message: String) : Exception(message)
