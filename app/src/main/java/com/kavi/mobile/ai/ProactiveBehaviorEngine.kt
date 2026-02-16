package com.kavi.mobile.ai

import android.content.Context
import android.util.Log
import com.kavi.mobile.data.KaviDatabase
import com.kavi.mobile.data.entities.ProactiveInteraction
import com.kavi.mobile.data.entities.BehaviorPattern
import com.kavi.mobile.voice.VoiceResponseEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ProactiveBehaviorEngine - Makes Kavi proactive and caring
 * Analyzes patterns and initiates conversations
 */
class ProactiveBehaviorEngine(
    private val context: Context,
    private val database: KaviDatabase,
    private val voiceEngine: VoiceResponseEngine,
    private val personalityEngine: PersonalityEngine
) {

    private val scope = CoroutineScope(Dispatchers.Main)
    
    companion object {
        private const val TAG = "ProactiveBehavior"
        private const val MIN_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes between proactive messages
    }

    private var lastProactiveMessageTime = 0L

    /**
     * Trigger types for proactive behavior
     */
    enum class TriggerType {
        TIME_BASED,      // Based on time of day
        PATTERN_BASED,   // Based on user patterns
        CONTEXT_BASED,   // Based on current context
        ANOMALY_BASED    // Based on unusual behavior
    }

    /**
     * Check if should send proactive message
     */
    fun shouldSendProactiveMessage(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastProactiveMessageTime) > MIN_INTERVAL_MS
    }

    /**
     * Analyze patterns and generate proactive message
     */
    suspend fun generateProactiveMessage(): String? {
        if (!shouldSendProactiveMessage()) {
            return null
        }

        // Try different trigger types
        val message = checkTimeBasedTriggers()
            ?: checkPatternBasedTriggers()
            ?: checkAnomalyBasedTriggers()
            ?: checkContextBasedTriggers()

        if (message != null) {
            lastProactiveMessageTime = System.currentTimeMillis()
            logProactiveInteraction(message, determineTriggerType(message))
        }

        return message
    }

    /**
     * Time-based proactive messages
     */
    private fun checkTimeBasedTriggers(): String? {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return when {
            hour == 6 && minute < 30 -> {
                listOf(
                    "Good morning! Ready to start the day?",
                    "Morning! How did you sleep?",
                    "Rise and shine! What's on the agenda today?"
                ).random()
            }
            hour == 12 && minute < 30 -> {
                listOf(
                    "It's lunchtime. Have you eaten yet?",
                    "Lunch break! Don't skip it.",
                    "Time for lunch. What are you having?"
                ).random()
            }
            hour == 22 && minute < 30 -> {
                listOf(
                    "It's getting late. Time to wind down?",
                    "Almost bedtime. Ready to call it a day?",
                    "Late evening. How was your day?"
                ).random()
            }
            hour >= 23 || hour < 6 -> {
                listOf(
                    "You're still up? You should probably sleep.",
                    "Late night again? This is becoming a habit.",
                    "It's really late. Don't you have work tomorrow?"
                ).random()
            }
            else -> null
        }
    }

    /**
     * Pattern-based proactive messages
     */
    private suspend fun checkPatternBasedTriggers(): String? {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val patterns = database.behaviorPatternDao().getPatternsByHour(hour)

        if (patterns.isEmpty()) {
            return null
        }

        // Check for missing usual patterns
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)

        for (pattern in patterns) {
            if (pattern.lastOccurrence < oneHourAgo && pattern.frequency > 10) {
                return when (pattern.action) {
                    "open_email" -> "You usually check email by now. Everything okay?"
                    "open_workout" -> "No workout today? Feeling alright?"
                    "make_coffee" -> "Haven't made coffee yet? That's unusual for you."
                    else -> "You usually ${pattern.action} around this time. Forgot?"
                }
            }
        }

        // Check for excessive repetition
        val recentCommands = database.commandHistoryDao().getRecentCommands(20)
        val recentCounts = recentCommands.groupingBy { it.intent }.eachCount()
        
        recentCounts.forEach { (intent, count) ->
            if (count > 5) {
                return when (intent) {
                    "OPEN_APP" -> {
                        val appName = recentCommands.first { it.intent == intent }.parameters
                        "You've opened $appName $count times recently. Everything alright?"
                    }
                    "SEARCH_WEB" -> "That's a lot of searching. Looking for something specific?"
                    else -> "You're doing that a lot. Is something wrong?"
                }
            }
        }

        return null
    }

    /**
     * Anomaly-based proactive messages
     */
    private suspend fun checkAnomalyBasedTriggers(): String? {
        val recentCommands = database.commandHistoryDao().getRecentCommands(100)
        
        if (recentCommands.isEmpty()) {
            return null
        }

        // Check for unusual activity levels
        val last24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        val commandsToday = database.commandHistoryDao().getCommandCountSince(last24Hours)

        return when {
            commandsToday > 100 -> {
                "You've been very active today. Are you okay?"
            }
            commandsToday < 5 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 18 -> {
                "You've been quiet today. Everything alright?"
            }
            else -> null
        }
    }

    /**
     * Context-based proactive messages
     */
    private fun checkContextBasedTriggers(): String? {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when {
            dayOfWeek == Calendar.MONDAY && hour == 9 -> {
                "Monday morning! How are you feeling about the week?"
            }
            dayOfWeek == Calendar.FRIDAY && hour == 17 -> {
                "Friday evening! Any plans for the weekend?"
            }
            dayOfWeek == Calendar.SUNDAY && hour == 20 -> {
                "Sunday night. Ready for the week ahead?"
            }
            else -> null
        }
    }

    /**
     * Send proactive message with voice
     */
    fun sendProactiveMessage(message: String, emotion: VoiceResponseEngine.Emotion = VoiceResponseEngine.Emotion.NEUTRAL) {
        scope.launch {
            voiceEngine.speak(message, emotion)
            Log.d(TAG, "Proactive message sent: $message")
        }
    }

    /**
     * Log proactive interaction to database
     */
    private fun logProactiveInteraction(message: String, trigger: String) {
        scope.launch(Dispatchers.IO) {
            val interaction = ProactiveInteraction(
                timestamp = System.currentTimeMillis(),
                message = message,
                trigger = trigger
            )
            database.proactiveInteractionDao().insert(interaction)
        }
    }

    private fun determineTriggerType(message: String): String {
        return when {
            message.contains("morning", ignoreCase = true) || 
            message.contains("lunch", ignoreCase = true) ||
            message.contains("late", ignoreCase = true) -> "time_based"
            
            message.contains("usually", ignoreCase = true) ||
            message.contains("again", ignoreCase = true) -> "pattern_based"
            
            message.contains("active", ignoreCase = true) ||
            message.contains("quiet", ignoreCase = true) -> "anomaly_based"
            
            else -> "context_based"
        }
    }

    /**
     * Start proactive behavior monitoring
     */
    fun startMonitoring() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(15 * 60 * 1000) // Check every 15 minutes
                
                val message = generateProactiveMessage()
                if (message != null) {
                    val emotion = if (message.contains("?")) {
                        VoiceResponseEngine.Emotion.CURIOUS
                    } else {
                        VoiceResponseEngine.Emotion.NEUTRAL
                    }
                    sendProactiveMessage(message, emotion)
                }
            }
        }
    }
}
