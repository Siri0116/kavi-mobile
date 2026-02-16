package com.kavi.mobile.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Kavi AI Client - Connects to backend AI service for advanced queries
 * Supports Groq API and custom Kavi server
 */
class KaviAIClient {

    companion object {
        private const val TAG = "KaviAIClient"
        
        // Groq API Configuration
        private const val GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
        private const val GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE" // TODO: Replace with actual API key
        private const val GROQ_MODEL = "mixtral-8x7b-32768" // Fast and capable model
        
        // Custom Kavi Server (if you have one)
        private const val KAVI_SERVER_URL = "YOUR_SERVER_URL_HERE"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class AIResponse(
        val answer: String,
        val confidence: Double,
        val sources: List<String> = emptyList()
    )

    /**
     * Send a question to Groq AI
     */
    suspend fun askGroq(question: String, context: String = ""): AIResponse? = withContext(Dispatchers.IO) {
        try {
            if (GROQ_API_KEY == "YOUR_GROQ_API_KEY_HERE") {
                Log.w(TAG, "Groq API key not configured")
                return@withContext null
            }

            val systemPrompt = """
                You are Kavi, a helpful AI assistant with a caring and slightly playful personality.
                You are running on a mobile device and helping the user with their daily tasks.
                Keep responses concise (2-3 sentences max) since they will be spoken aloud.
                Be friendly, encouraging, and occasionally show concern for the user's wellbeing.
            """.trimIndent()

            val userPrompt = if (context.isNotEmpty()) {
                "Context: $context\n\nQuestion: $question"
            } else {
                question
            }

            val jsonBody = JSONObject().apply {
                put("model", GROQ_MODEL)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 200)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(GROQ_API_URL)
                .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    parseGroqResponse(responseBody)
                } else {
                    null
                }
            } else {
                Log.e(TAG, "Groq API error: ${response.code} - ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Groq API", e)
            null
        }
    }

    /**
     * Parse Groq API response
     */
    private fun parseGroqResponse(jsonResponse: String): AIResponse {
        val json = JSONObject(jsonResponse)
        val choices = json.getJSONArray("choices")
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        val content = message.getString("content")
        
        return AIResponse(
            answer = content.trim(),
            confidence = 0.9, // Groq doesn't provide confidence, using default
            sources = emptyList()
        )
    }

    /**
     * Send question to custom Kavi server (if available)
     */
    suspend fun askKaviServer(question: String, userId: String = "default"): AIResponse? = withContext(Dispatchers.IO) {
        try {
            if (KAVI_SERVER_URL == "YOUR_SERVER_URL_HERE") {
                Log.w(TAG, "Kavi server URL not configured")
                return@withContext null
            }

            val jsonBody = JSONObject().apply {
                put("question", question)
                put("user_id", userId)
            }

            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$KAVI_SERVER_URL/ask")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    parseKaviServerResponse(responseBody)
                } else {
                    null
                }
            } else {
                Log.e(TAG, "Kavi server error: ${response.code} - ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Kavi server", e)
            null
        }
    }

    /**
     * Parse Kavi server response
     */
    private fun parseKaviServerResponse(jsonResponse: String): AIResponse {
        val json = JSONObject(jsonResponse)
        
        return AIResponse(
            answer = json.getString("answer"),
            confidence = json.optDouble("confidence", 0.8),
            sources = parseSourcesArray(json.optJSONArray("sources"))
        )
    }

    /**
     * Parse sources array from JSON
     */
    private fun parseSourcesArray(sourcesArray: JSONArray?): List<String> {
        if (sourcesArray == null) return emptyList()
        
        val sources = mutableListOf<String>()
        for (i in 0 until sourcesArray.length()) {
            sources.add(sourcesArray.getString(i))
        }
        return sources
    }

    /**
     * Ask AI with automatic fallback (Groq -> Kavi Server -> null)
     */
    suspend fun ask(question: String, context: String = ""): AIResponse? {
        // Try Groq first
        val groqResponse = askGroq(question, context)
        if (groqResponse != null) {
            return groqResponse
        }

        // Fallback to Kavi server
        val kaviResponse = askKaviServer(question)
        if (kaviResponse != null) {
            return kaviResponse
        }

        // No AI available
        return null
    }

    /**
     * Check if AI backend is available
     */
    fun isAvailable(): Boolean {
        return GROQ_API_KEY != "YOUR_GROQ_API_KEY_HERE" || 
               KAVI_SERVER_URL != "YOUR_SERVER_URL_HERE"
    }
}
