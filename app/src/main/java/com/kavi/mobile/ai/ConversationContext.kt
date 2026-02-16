package com.kavi.mobile.ai

import android.util.Log
import com.kavi.mobile.data.KaviDatabase
import com.kavi.mobile.data.entities.CommandHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Conversation Context Manager - Maintains conversation history and context
 * Enables pronoun resolution, topic continuity, and follow-up questions
 */
class ConversationContext(private val database: KaviDatabase) {

    companion object {
        private const val TAG = "ConversationContext"
        private const val MAX_HISTORY_SIZE = 10
        private const val CONTEXT_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    data class ConversationTurn(
        val userInput: String,
        val intent: String,
        val entities: Map<String, String>,
        val response: String,
        val timestamp: Long
    )

    private val conversationHistory = mutableListOf<ConversationTurn>()
    private var currentTopic: String? = null
    private var lastMentionedPerson: String? = null
    private var lastMentionedApp: String? = null
    private var lastMentionedLocation: String? = null
    private var lastInteractionTime: Long = 0

    /**
     * Add a turn to conversation history
     */
    fun addTurn(
        userInput: String,
        intent: String,
        entities: Map<String, String>,
        response: String
    ) {
        val turn = ConversationTurn(
            userInput = userInput,
            intent = intent,
            entities = entities,
            response = response,
            timestamp = System.currentTimeMillis()
        )

        conversationHistory.add(turn)
        
        // Keep only recent history
        if (conversationHistory.size > MAX_HISTORY_SIZE) {
            conversationHistory.removeAt(0)
        }

        // Update context
        updateContext(entities, intent)
        lastInteractionTime = System.currentTimeMillis()

        Log.d(TAG, "Added conversation turn: $intent")
    }

    /**
     * Update context based on entities
     */
    private fun updateContext(entities: Map<String, String>, intent: String) {
        // Update mentioned entities
        entities["contact_name"]?.let { lastMentionedPerson = it }
        entities["app_name"]?.let { lastMentionedApp = it }
        entities["location"]?.let { lastMentionedLocation = it }

        // Update topic based on intent
        currentTopic = when {
            intent.contains("CALL") || intent.contains("MESSAGE") -> "communication"
            intent.contains("APP") -> "apps"
            intent.contains("NAVIGATE") -> "navigation"
            intent.contains("WEATHER") -> "weather"
            intent.contains("QUESTION") -> "information"
            else -> currentTopic
        }
    }

    /**
     * Resolve pronouns in user input
     */
    fun resolvePronoun(input: String): String {
        if (!isContextValid()) {
            return input
        }

        var resolved = input

        // Resolve "him" or "her" to last mentioned person
        if (resolved.contains(Regex("\\b(him|her)\\b", RegexOption.IGNORE_CASE))) {
            lastMentionedPerson?.let { person ->
                resolved = resolved.replace(Regex("\\b(him|her)\\b", RegexOption.IGNORE_CASE), person)
                Log.d(TAG, "Resolved pronoun to: $person")
            }
        }

        // Resolve "it" to last mentioned app
        if (resolved.contains(Regex("\\bit\\b", RegexOption.IGNORE_CASE))) {
            lastMentionedApp?.let { app ->
                resolved = resolved.replace(Regex("\\bit\\b", RegexOption.IGNORE_CASE), app)
                Log.d(TAG, "Resolved 'it' to: $app")
            }
        }

        // Resolve "there" to last mentioned location
        if (resolved.contains(Regex("\\bthere\\b", RegexOption.IGNORE_CASE))) {
            lastMentionedLocation?.let { location ->
                resolved = resolved.replace(Regex("\\bthere\\b", RegexOption.IGNORE_CASE), location)
                Log.d(TAG, "Resolved 'there' to: $location")
            }
        }

        return resolved
    }

    /**
     * Get context for follow-up questions
     */
    fun getFollowUpContext(): String {
        if (!isContextValid() || conversationHistory.isEmpty()) {
            return ""
        }

        val recentTurns = conversationHistory.takeLast(3)
        return buildString {
            append("Recent conversation:\n")
            recentTurns.forEach { turn ->
                append("User: ${turn.userInput}\n")
                append("Kavi: ${turn.response}\n")
            }
        }
    }

    /**
     * Check if current context is still valid
     */
    private fun isContextValid(): Boolean {
        val timeSinceLastInteraction = System.currentTimeMillis() - lastInteractionTime
        return timeSinceLastInteraction < CONTEXT_TIMEOUT_MS
    }

    /**
     * Detect if input is a follow-up question
     */
    fun isFollowUpQuestion(input: String): Boolean {
        if (!isContextValid() || conversationHistory.isEmpty()) {
            return false
        }

        val followUpIndicators = listOf(
            "and", "also", "what about", "how about", "tell me more",
            "explain", "why", "how", "when", "where", "who"
        )

        val lowerInput = input.lowercase()
        
        // Check if it starts with a follow-up indicator
        return followUpIndicators.any { lowerInput.startsWith(it) } ||
               // Or if it's a short question (likely follow-up)
               (lowerInput.split(" ").size <= 5 && lowerInput.contains("?"))
    }

    /**
     * Get topic continuity suggestion
     */
    fun getTopicSuggestion(): String? {
        if (!isContextValid()) {
            return null
        }

        return when (currentTopic) {
            "communication" -> lastMentionedPerson?.let { "Would you like to contact $it again?" }
            "apps" -> lastMentionedApp?.let { "Want to open $it again?" }
            "navigation" -> lastMentionedLocation?.let { "Need directions to $it?" }
            "weather" -> "Want an updated weather report?"
            else -> null
        }
    }

    /**
     * Clear conversation context
     */
    fun clearContext() {
        conversationHistory.clear()
        currentTopic = null
        lastMentionedPerson = null
        lastMentionedApp = null
        lastMentionedLocation = null
        lastInteractionTime = 0
        Log.d(TAG, "Context cleared")
    }

    /**
     * Get conversation summary
     */
    fun getSummary(): String {
        return buildString {
            append("Conversation turns: ${conversationHistory.size}\n")
            append("Current topic: ${currentTopic ?: "none"}\n")
            append("Last person: ${lastMentionedPerson ?: "none"}\n")
            append("Last app: ${lastMentionedApp ?: "none"}\n")
            append("Last location: ${lastMentionedLocation ?: "none"}\n")
            append("Context valid: ${isContextValid()}")
        }
    }

    /**
     * Load recent conversation from database
     */
    suspend fun loadRecentHistory(limit: Int = 5) = withContext(Dispatchers.IO) {
        try {
            val recentCommands = database.commandHistoryDao().getRecentCommands(limit)
            
            recentCommands.forEach { command ->
                // Reconstruct conversation turns from command history
                addTurn(
                    userInput = command.command,
                    intent = command.intent,
                    entities = emptyMap(), // Would need to store entities in DB
                    response = "" // Would need to store responses in DB
                )
            }
            
            Log.d(TAG, "Loaded $limit recent commands from database")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recent history", e)
        }
    }
}
