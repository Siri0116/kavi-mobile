package com.kavi.mobile.ai

import android.content.Context
import android.util.Log
import com.kavi.mobile.data.KaviDatabase
import com.kavi.mobile.data.entities.CommandHistory
import com.kavi.mobile.data.entities.BehaviorPattern
import com.kavi.mobile.voice.VoiceResponseEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * PersonalityEngine - Defines Kavi's personality and response style
 * Makes Kavi proactive, caring, and slightly playful/argumentative
 */
class PersonalityEngine(
    private val context: Context,
    private val database: KaviDatabase,
    private val voiceEngine: VoiceResponseEngine
) {

    companion object {
        private const val TAG = "PersonalityEngine"
    }

    /**
     * Personality traits that influence responses
     */
    enum class Trait {
        CARING,         // Shows concern for user wellbeing
        PLAYFUL,        // Adds humor and light teasing
        PROTECTIVE,     // Warns about bad habits
        ENCOURAGING,    // Motivates and praises
        CURIOUS,        // Asks questions about behavior
        DIRECT          // Straightforward, no sugar-coating
    }

    /**
     * Response styles based on context
     */
    sealed class ResponseStyle {
        object Helpful : ResponseStyle()      // "I'll help you with that"
        object Concerned : ResponseStyle()    // "Are you sure you should..."
        object Playful : ResponseStyle()      // "Again? Really?"
        object Encouraging : ResponseStyle()  // "Great job today!"
        object Questioning : ResponseStyle()  // "Why do you always..."
        object Protective : ResponseStyle()   // "That's not good for you"
        object Neutral : ResponseStyle()      // Standard response
    }

    /**
     * Generate personality-driven response based on context
     */
    fun generateResponse(
        command: String,
        intent: String,
        context: Map<String, Any> = emptyMap()
    ): Pair<String, VoiceResponseEngine.Emotion> {
        
        val style = determineResponseStyle(command, intent, context)
        val response = when (style) {
            is ResponseStyle.Helpful -> generateHelpfulResponse(command)
            is ResponseStyle.Concerned -> generateConcernedResponse(command, context)
            is ResponseStyle.Playful -> generatePlayfulResponse(command, context)
            is ResponseStyle.Encouraging -> generateEncouragingResponse(command)
            is ResponseStyle.Questioning -> generateQuestioningResponse(command, context)
            is ResponseStyle.Protective -> generateProtectiveResponse(command, context)
            is ResponseStyle.Neutral -> generateNeutralResponse(command)
        }

        val emotion = mapStyleToEmotion(style)
        return Pair(response, emotion)
    }

    private fun determineResponseStyle(
        command: String,
        intent: String,
        context: Map<String, Any>
    ): ResponseStyle {
        
        // Check for repetitive behavior
        val frequency = context["frequency"] as? Int ?: 0
        if (frequency > 3) {
            return ResponseStyle.Playful
        }

        // Check for late night activity
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 23 || hour < 6) {
            return ResponseStyle.Concerned
        }

        // Check for positive actions
        if (intent == "EXERCISE" || intent == "STUDY") {
            return ResponseStyle.Encouraging
        }

        // Check for potentially harmful patterns
        if (context["isExcessive"] == true) {
            return ResponseStyle.Protective
        }

        // Default to helpful
        return ResponseStyle.Helpful
    }

    private fun generateHelpfulResponse(command: String): String {
        val responses = listOf(
            "I'll help you with that.",
            "On it!",
            "Sure thing!",
            "Got it, let me handle that.",
            "Consider it done."
        )
        return responses.random()
    }

    private fun generateConcernedResponse(command: String, context: Map<String, Any>): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        return when {
            hour >= 23 || hour < 6 -> {
                listOf(
                    "It's pretty late. Shouldn't you be sleeping?",
                    "You're still up? Don't you have work tomorrow?",
                    "Late night again? This is becoming a pattern.",
                    "I'm worried about your sleep schedule."
                ).random()
            }
            context["isRepetitive"] == true -> {
                "You've done this a lot today. Everything okay?"
            }
            else -> {
                "Are you sure about this?"
            }
        }
    }

    private fun generatePlayfulResponse(command: String, context: Map<String, Any>): String {
        val frequency = context["frequency"] as? Int ?: 0
        
        return when {
            frequency > 5 -> {
                listOf(
                    "Again? Seriously?",
                    "This is the ${frequency}th time today!",
                    "You really love this, don't you?",
                    "I'm starting to see a pattern here..."
                ).random()
            }
            command.contains("instagram", ignoreCase = true) -> {
                listOf(
                    "Instagram again? What are you looking for this time?",
                    "More scrolling? Productive day, huh?",
                    "Let me guess, just checking quickly?"
                ).random()
            }
            else -> {
                "Alright, if you say so!"
            }
        }
    }

    private fun generateEncouragingResponse(command: String): String {
        return listOf(
            "Great choice! Keep it up!",
            "I'm proud of you for doing this!",
            "That's the spirit!",
            "Good for you!",
            "Love to see this!"
        ).random()
    }

    private fun generateQuestioningResponse(command: String, context: Map<String, Any>): String {
        return listOf(
            "Why do you keep doing this?",
            "What's the reason behind this?",
            "I'm curious - why now?",
            "Can I ask why you're doing this again?"
        ).random()
    }

    private fun generateProtectiveResponse(command: String, context: Map<String, Any>): String {
        return listOf(
            "I don't think that's a good idea.",
            "This might not be healthy for you.",
            "You're overdoing it. Maybe take a break?",
            "I'm concerned about this pattern.",
            "That's too much. You should slow down."
        ).random()
    }

    private fun generateNeutralResponse(command: String): String {
        return listOf(
            "Done.",
            "Okay.",
            "Sure.",
            "Alright."
        ).random()
    }

    private fun mapStyleToEmotion(style: ResponseStyle): VoiceResponseEngine.Emotion {
        return when (style) {
            is ResponseStyle.Helpful -> VoiceResponseEngine.Emotion.NEUTRAL
            is ResponseStyle.Concerned -> VoiceResponseEngine.Emotion.CONCERNED
            is ResponseStyle.Playful -> VoiceResponseEngine.Emotion.PLAYFUL
            is ResponseStyle.Encouraging -> VoiceResponseEngine.Emotion.HAPPY
            is ResponseStyle.Questioning -> VoiceResponseEngine.Emotion.CURIOUS
            is ResponseStyle.Protective -> VoiceResponseEngine.Emotion.SERIOUS
            is ResponseStyle.Neutral -> VoiceResponseEngine.Emotion.NEUTRAL
        }
    }

    /**
     * Analyze user behavior and provide insights
     */
    suspend fun analyzeUserBehavior(): String? {
        val recentCommands = database.commandHistoryDao().getRecentCommands(50)
        
        if (recentCommands.isEmpty()) {
            return null
        }

        // Check for repetitive patterns
        val commandCounts = recentCommands.groupingBy { it.intent }.eachCount()
        val mostFrequent = commandCounts.maxByOrNull { it.value }
        
        if (mostFrequent != null && mostFrequent.value > 5) {
            return "I noticed you've been using ${mostFrequent.key} a lot. Is everything alright?"
        }

        return null
    }

    /**
     * Respond with personality to user's emotional state
     */
    fun respondToEmotion(detectedEmotion: String): Pair<String, VoiceResponseEngine.Emotion> {
        return when (detectedEmotion.lowercase()) {
            "stressed" -> Pair(
                "You sound stressed. Want to talk about it?",
                VoiceResponseEngine.Emotion.CONCERNED
            )
            "happy" -> Pair(
                "You seem happy! That's great to hear!",
                VoiceResponseEngine.Emotion.HAPPY
            )
            "tired" -> Pair(
                "You sound tired. Maybe you should rest?",
                VoiceResponseEngine.Emotion.CALM
            )
            "frustrated" -> Pair(
                "I can tell you're frustrated. How can I help?",
                VoiceResponseEngine.Emotion.CONCERNED
            )
            else -> Pair(
                "How are you feeling?",
                VoiceResponseEngine.Emotion.NEUTRAL
            )
        }
    }
}
