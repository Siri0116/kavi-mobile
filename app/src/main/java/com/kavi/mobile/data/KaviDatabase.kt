package com.kavi.mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kavi.mobile.data.dao.*
import com.kavi.mobile.data.entities.*

/**
 * KaviDatabase - Main Room database for Kavi's memory system
 * Stores all user interactions, patterns, and behavioral data
 */
@Database(
    entities = [
        CommandHistory::class,
        BehaviorPattern::class,
        UserMood::class,
        QuestionCategory::class,
        ProactiveInteraction::class,
        AppUsagePattern::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KaviDatabase : RoomDatabase() {

    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun behaviorPatternDao(): BehaviorPatternDao
    abstract fun userMoodDao(): UserMoodDao
    abstract fun questionCategoryDao(): QuestionCategoryDao
    abstract fun proactiveInteractionDao(): ProactiveInteractionDao
    abstract fun appUsagePatternDao(): AppUsagePatternDao

    companion object {
        @Volatile
        private var INSTANCE: KaviDatabase? = null

        fun getDatabase(context: Context): KaviDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaviDatabase::class.java,
                    "kavi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
