package com.kavi.mobile.data.dao

import androidx.room.*
import com.kavi.mobile.data.entities.*

/**
 * CommandHistoryDao - Data access for command history
 */
@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentCommands(limit: Int = 100): List<CommandHistory>
    
    @Query("SELECT * FROM command_history WHERE intent = :intent ORDER BY timestamp DESC")
    suspend fun getCommandsByIntent(intent: String): List<CommandHistory>
    
    @Query("SELECT COUNT(*) FROM command_history WHERE timestamp > :since")
    suspend fun getCommandCountSince(since: Long): Int
    
    @Insert
    suspend fun insert(command: CommandHistory)
    
    @Query("DELETE FROM command_history WHERE timestamp < :before")
    suspend fun deleteOldCommands(before: Long)
}

/**
 * BehaviorPatternDao - Data access for behavior patterns
 */
@Dao
interface BehaviorPatternDao {
    @Query("SELECT * FROM behavior_patterns WHERE action = :action")
    suspend fun getPatternForAction(action: String): BehaviorPattern?
    
    @Query("SELECT * FROM behavior_patterns WHERE timeOfDay = :hour ORDER BY frequency DESC")
    suspend fun getPatternsByHour(hour: Int): List<BehaviorPattern>
    
    @Query("SELECT * FROM behavior_patterns ORDER BY frequency DESC LIMIT :limit")
    suspend fun getTopPatterns(limit: Int = 10): List<BehaviorPattern>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pattern: BehaviorPattern)
    
    @Update
    suspend fun update(pattern: BehaviorPattern)
    
    @Query("DELETE FROM behavior_patterns WHERE lastOccurrence < :before")
    suspend fun deleteOldPatterns(before: Long)
}

/**
 * UserMoodDao - Data access for user moods
 */
@Dao
interface UserMoodDao {
    @Query("SELECT * FROM user_moods ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMoods(limit: Int = 50): List<UserMood>
    
    @Query("SELECT * FROM user_moods WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getMoodsSince(since: Long): List<UserMood>
    
    @Query("SELECT mood, COUNT(*) as count FROM user_moods WHERE timestamp > :since GROUP BY mood ORDER BY count DESC")
    suspend fun getMoodDistribution(since: Long): Map<String, Int>
    
    @Insert
    suspend fun insert(mood: UserMood)
    
    @Query("DELETE FROM user_moods WHERE timestamp < :before")
    suspend fun deleteOldMoods(before: Long)
}

/**
 * QuestionCategoryDao - Data access for question categories
 */
@Dao
interface QuestionCategoryDao {
    @Query("SELECT * FROM question_categories ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentQuestions(limit: Int = 100): List<QuestionCategory>
    
    @Query("SELECT * FROM question_categories WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getQuestionsByCategory(category: String): List<QuestionCategory>
    
    @Query("SELECT category, COUNT(*) as count FROM question_categories WHERE timestamp > :since GROUP BY category ORDER BY count DESC")
    suspend fun getCategoryDistribution(since: Long): Map<String, Int>
    
    @Insert
    suspend fun insert(question: QuestionCategory)
    
    @Update
    suspend fun update(question: QuestionCategory)
}

/**
 * ProactiveInteractionDao - Data access for proactive interactions
 */
@Dao
interface ProactiveInteractionDao {
    @Query("SELECT * FROM proactive_interactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentInteractions(limit: Int = 50): List<ProactiveInteraction>
    
    @Query("SELECT * FROM proactive_interactions WHERE trigger = :trigger ORDER BY timestamp DESC")
    suspend fun getInteractionsByTrigger(trigger: String): List<ProactiveInteraction>
    
    @Query("SELECT COUNT(*) FROM proactive_interactions WHERE wasPositive = 1 AND timestamp > :since")
    suspend fun getPositiveInteractionCount(since: Long): Int
    
    @Insert
    suspend fun insert(interaction: ProactiveInteraction)
    
    @Update
    suspend fun update(interaction: ProactiveInteraction)
}

/**
 * AppUsagePatternDao - Data access for app usage patterns
 */
@Dao
interface AppUsagePatternDao {
    @Query("SELECT * FROM app_usage_patterns WHERE appName = :appName ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUsageForApp(appName: String, limit: Int = 100): List<AppUsagePattern>
    
    @Query("SELECT appName, COUNT(*) as count FROM app_usage_patterns WHERE timestamp > :since GROUP BY appName ORDER BY count DESC LIMIT :limit")
    suspend fun getTopApps(since: Long, limit: Int = 10): Map<String, Int>
    
    @Query("SELECT * FROM app_usage_patterns WHERE timeOfDay = :hour ORDER BY timestamp DESC")
    suspend fun getUsageByHour(hour: Int): List<AppUsagePattern>
    
    @Insert
    suspend fun insert(usage: AppUsagePattern)
    
    @Query("DELETE FROM app_usage_patterns WHERE timestamp < :before")
    suspend fun deleteOldUsage(before: Long)
}
