package com.kavi.mobile.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * CommandHistory - Stores all voice commands executed by the user
 */
@Entity(tableName = "command_history")
data class CommandHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val timestamp: Long,
    val intent: String,
    val parameters: String? = null,  // JSON string
    val success: Boolean,
    val executionTimeMs: Long = 0
)

/**
 * BehaviorPattern - Tracks recurring user behaviors
 */
@Entity(tableName = "behavior_patterns")
data class BehaviorPattern(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,              // e.g., "open_instagram"
    val timeOfDay: Int,              // Hour (0-23)
    val dayOfWeek: Int,              // 1-7 (Monday-Sunday)
    val frequency: Int,              // Count
    val lastOccurrence: Long,
    val averageInterval: Long = 0    // Average time between occurrences
)

/**
 * UserMood - Tracks user emotional state indicators
 */
@Entity(tableName = "user_moods")
data class UserMood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val mood: String,                // "stressed", "happy", "tired", "focused"
    val indicators: String,          // JSON of indicators (voice tone, word choice, etc.)
    val confidence: Float            // 0.0 to 1.0
)

/**
 * QuestionCategory - Categorizes user questions for pattern analysis
 */
@Entity(tableName = "question_categories")
data class QuestionCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String,
    val category: String,            // "technical", "emotional", "routine", "work", "study", "health"
    val timestamp: Long,
    val response: String? = null,
    val wasHelpful: Boolean? = null
)

/**
 * ProactiveInteraction - Logs Kavi's proactive messages
 */
@Entity(tableName = "proactive_interactions")
data class ProactiveInteraction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val message: String,
    val trigger: String,             // "time_based", "pattern_based", "context_based"
    val userResponse: String? = null,
    val wasPositive: Boolean? = null
)

/**
 * AppUsagePattern - Tracks which apps user opens and when
 */
@Entity(tableName = "app_usage_patterns")
data class AppUsagePattern(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val timeOfDay: Int,              // Hour (0-23)
    val dayOfWeek: Int,              // 1-7
    val duration: Long = 0           // How long app was used (if trackable)
)
