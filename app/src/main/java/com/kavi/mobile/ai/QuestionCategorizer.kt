package com.kavi.mobile.ai

import android.util.Log

/**
 * QuestionCategorizer - Categorizes user questions for pattern analysis
 * Helps Kavi understand what types of help the user needs most
 */
class QuestionCategorizer {

    companion object {
        private const val TAG = "QuestionCategorizer"
    }

    /**
     * Question categories
     */
    enum class Category(val keywords: List<String>) {
        TECHNICAL(listOf(
            "how to", "code", "program", "bug", "error", "install", "setup",
            "configure", "debug", "compile", "syntax", "algorithm", "api"
        )),
        
        EMOTIONAL(listOf(
            "feel", "feeling", "sad", "happy", "stressed", "worried", "anxious",
            "depressed", "lonely", "angry", "frustrated", "tired", "exhausted"
        )),
        
        ROUTINE(listOf(
            "eat", "food", "meal", "breakfast", "lunch", "dinner", "cook",
            "sleep", "wake", "morning", "evening", "daily", "schedule"
        )),
        
        WORK(listOf(
            "work", "job", "project", "deadline", "meeting", "boss", "colleague",
            "task", "assignment", "presentation", "email", "office"
        )),
        
        STUDY(listOf(
            "study", "learn", "exam", "test", "homework", "assignment", "class",
            "lecture", "course", "subject", "grade", "university", "college"
        )),
        
        HEALTH(listOf(
            "health", "exercise", "workout", "gym", "run", "walk", "medicine",
            "doctor", "sick", "pain", "headache", "fever", "diet", "weight"
        )),
        
        PHILOSOPHICAL(listOf(
            "why", "meaning", "purpose", "life", "existence", "believe", "think",
            "opinion", "philosophy", "ethics", "morality", "right", "wrong"
        )),
        
        CASUAL(listOf(
            "joke", "funny", "fun", "game", "play", "movie", "music", "song",
            "weather", "time", "date", "news", "story", "chat"
        ))
    }

    /**
     * Categorize a question based on keywords
     */
    fun categorize(question: String): Category {
        val lowerQuestion = question.lowercase()
        
        // Count matches for each category
        val categoryScores = Category.values().associateWith { category ->
            category.keywords.count { keyword ->
                lowerQuestion.contains(keyword)
            }
        }

        // Return category with highest score
        val bestMatch = categoryScores.maxByOrNull { it.value }
        
        return if (bestMatch != null && bestMatch.value > 0) {
            Log.d(TAG, "Categorized as ${bestMatch.key} (score: ${bestMatch.value})")
            bestMatch.key
        } else {
            Log.d(TAG, "Categorized as CASUAL (default)")
            Category.CASUAL
        }
    }

    /**
     * Get suggested response based on category
     */
    fun getSuggestedResponse(category: Category, question: String): String {
        return when (category) {
            Category.TECHNICAL -> {
                "Let me help you with that technical question."
            }
            Category.EMOTIONAL -> {
                "I'm here to listen. Tell me more about how you're feeling."
            }
            Category.ROUTINE -> {
                "Let me help you with your daily routine."
            }
            Category.WORK -> {
                "Work-related question. I'll do my best to help."
            }
            Category.STUDY -> {
                "Study question. Let's figure this out together."
            }
            Category.HEALTH -> {
                "Health is important. What do you need to know?"
            }
            Category.PHILOSOPHICAL -> {
                "That's a deep question. Let me think about it."
            }
            Category.CASUAL -> {
                "Sure, I can help with that."
            }
        }
    }

    /**
     * Determine if question needs AI backend
     */
    fun needsAIBackend(category: Category): Boolean {
        return when (category) {
            Category.TECHNICAL,
            Category.PHILOSOPHICAL,
            Category.EMOTIONAL -> true
            else -> false
        }
    }
}
