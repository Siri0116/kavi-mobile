package com.kavi.mobile.engine

import android.util.Log

/**
 * Intent Engine - Classifies voice commands into actionable intents
 */
class IntentEngine {

    companion object {
        private const val TAG = "IntentEngine"
    }

    enum class Intent {
        // App Control
        OPEN_APP,
        CLOSE_APP,
        SWITCH_APP,
        GO_BACK,
        
        // Communication
        MAKE_CALL,
        SEND_MESSAGE,
        
        // Media
        TAKE_PHOTO,
        TAKE_VIDEO,
        PLAY_MUSIC,
        
        // Navigation & Time
        NAVIGATE,
        SET_ALARM,
        SET_REMINDER,
        
        // Search
        SEARCH_WEB,
        
        // System Control
        SILENT_MODE,
        VIBRATE_MODE,
        VOLUME_CONTROL,
        BRIGHTNESS_CONTROL,
        WIFI_SETTINGS,
        BLUETOOTH_SETTINGS,
        AIRPLANE_MODE,
        
        // Information
        WEATHER,
        TIME,
        DATE,
        BATTERY_STATUS,
        READ_NOTIFICATIONS,
        READ_SCREEN,
        
        // Conversational
        CASUAL_CHAT,
        QUESTION,
        GREETING,
        THANKS,
        
        // Accessibility Actions
        CLICK_BUTTON,
        SCROLL_DOWN,
        SCROLL_UP,
        TAKE_SCREENSHOT,
        
        // Security Actions
        SECURITY_CHECK,
        
        UNKNOWN
    }

    data class CommandResult(
        val intent: Intent,
        val parameters: Map<String, String>,
        val confidence: Float
    )

    /**
     * Classify a voice command into an intent
     */
    fun classify(command: String): CommandResult {
        val lowerCommand = command.lowercase().trim()
        Log.d(TAG, "Classifying command: $lowerCommand")

        // Open app commands
        if (lowerCommand.contains("open") || lowerCommand.contains("launch") || lowerCommand.contains("start")) {
            val appName = extractAppName(lowerCommand)
            return CommandResult(
                Intent.OPEN_APP,
                mapOf("app_name" to appName),
                0.9f
            )
        }

        // Call commands
        if (lowerCommand.contains("call") || lowerCommand.contains("phone") || lowerCommand.contains("dial")) {
            val contactName = extractContactName(lowerCommand)
            return CommandResult(
                Intent.MAKE_CALL,
                mapOf("contact_name" to contactName),
                0.9f
            )
        }

        // Message commands
        if (lowerCommand.contains("message") || lowerCommand.contains("text") || lowerCommand.contains("whatsapp")) {
            val contactName = extractContactName(lowerCommand)
            val message = extractMessage(lowerCommand)
            return CommandResult(
                Intent.SEND_MESSAGE,
                mapOf("contact_name" to contactName, "message" to message),
                0.85f
            )
        }

        // Camera commands
        if (lowerCommand.contains("photo") || lowerCommand.contains("picture") || lowerCommand.contains("camera")) {
            return CommandResult(
                Intent.TAKE_PHOTO,
                emptyMap(),
                0.95f
            )
        }

        if (lowerCommand.contains("video") || lowerCommand.contains("record")) {
            return CommandResult(
                Intent.TAKE_VIDEO,
                emptyMap(),
                0.95f
            )
        }

        // Navigation commands
        if (lowerCommand.contains("navigate") || lowerCommand.contains("directions") || lowerCommand.contains("map")) {
            val location = extractLocation(lowerCommand)
            return CommandResult(
                Intent.NAVIGATE,
                mapOf("location" to location),
                0.9f
            )
        }

        // Alarm commands
        if (lowerCommand.contains("alarm") || lowerCommand.contains("wake me")) {
            val time = extractTime(lowerCommand)
            return CommandResult(
                Intent.SET_ALARM,
                mapOf("time" to time),
                0.85f
            )
        }

        // Reminder commands
        if (lowerCommand.contains("remind") || lowerCommand.contains("reminder")) {
            val reminderText = extractReminderText(lowerCommand)
            return CommandResult(
                Intent.SET_REMINDER,
                mapOf("reminder" to reminderText),
                0.85f
            )
        }

        // Music commands
        if (lowerCommand.contains("play") || lowerCommand.contains("music") || lowerCommand.contains("song")) {
            val query = extractMusicQuery(lowerCommand)
            return CommandResult(
                Intent.PLAY_MUSIC,
                mapOf("query" to query),
                0.8f
            )
        }

        // System Control - Silent Mode
        if (lowerCommand.contains("silent") || lowerCommand.contains("mute phone") || lowerCommand.contains("quiet mode")) {
            val enable = !lowerCommand.contains("off") && !lowerCommand.contains("disable")
            return CommandResult(
                Intent.SILENT_MODE,
                mapOf("enable" to enable.toString()),
                0.95f
            )
        }

        // Vibrate Mode
        if (lowerCommand.contains("vibrate") || lowerCommand.contains("vibration")) {
            val enable = !lowerCommand.contains("off") && !lowerCommand.contains("disable")
            return CommandResult(
                Intent.VIBRATE_MODE,
                mapOf("enable" to enable.toString()),
                0.95f
            )
        }

        // Volume Control
        if (lowerCommand.contains("volume")) {
            val level = extractVolumeLevel(lowerCommand)
            return CommandResult(
                Intent.VOLUME_CONTROL,
                mapOf("level" to level.toString()),
                0.9f
            )
        }

        // Brightness Control
        if (lowerCommand.contains("brightness") || lowerCommand.contains("screen brightness")) {
            val level = extractBrightnessLevel(lowerCommand)
            return CommandResult(
                Intent.BRIGHTNESS_CONTROL,
                mapOf("level" to level.toString()),
                0.9f
            )
        }

        // WiFi Settings
        if (lowerCommand.contains("wifi") || lowerCommand.contains("wi-fi")) {
            return CommandResult(
                Intent.WIFI_SETTINGS,
                emptyMap(),
                0.95f
            )
        }

        // Bluetooth Settings
        if (lowerCommand.contains("bluetooth")) {
            return CommandResult(
                Intent.BLUETOOTH_SETTINGS,
                emptyMap(),
                0.95f
            )
        }

        // Airplane Mode
        if (lowerCommand.contains("airplane") || lowerCommand.contains("flight mode")) {
            return CommandResult(
                Intent.AIRPLANE_MODE,
                emptyMap(),
                0.95f
            )
        }

        // Weather
        if (lowerCommand.contains("weather") || lowerCommand.contains("temperature") || lowerCommand.contains("forecast")) {
            return CommandResult(
                Intent.WEATHER,
                emptyMap(),
                0.95f
            )
        }

        // Time
        if (lowerCommand.contains("what time") || lowerCommand.contains("current time") || lowerCommand.matches(Regex(".*time.*is.*"))) {
            return CommandResult(
                Intent.TIME,
                emptyMap(),
                0.95f
            )
        }

        // Date
        if (lowerCommand.contains("what date") || lowerCommand.contains("today's date") || lowerCommand.contains("what day")) {
            return CommandResult(
                Intent.DATE,
                emptyMap(),
                0.95f
            )
        }

        // Battery Status
        if (lowerCommand.contains("battery") || lowerCommand.contains("charge")) {
            return CommandResult(
                Intent.BATTERY_STATUS,
                emptyMap(),
                0.95f
            )
        }

        // Greetings
        if (lowerCommand.matches(Regex("^(hi|hello|hey|good morning|good evening|good afternoon).*"))) {
            return CommandResult(
                Intent.GREETING,
                emptyMap(),
                0.95f
            )
        }

        // Thanks
        if (lowerCommand.contains("thank") || lowerCommand.contains("thanks")) {
            return CommandResult(
                Intent.THANKS,
                emptyMap(),
                0.95f
            )
        }

        // Questions (starts with question words)
        if (lowerCommand.matches(Regex("^(what|why|how|when|where|who|can you|could you|would you).*"))) {
            return CommandResult(
                Intent.QUESTION,
                mapOf("question" to command),
                0.7f
            )
        }

        // Casual chat
        if (lowerCommand.matches(Regex("(how are you|what's up|hey there|sup|wassup)"))) {
            return CommandResult(Intent.CASUAL_CHAT, mapOf("message" to command), 0.8f)
        }

        // Close app
        if (lowerCommand.contains("close") && (lowerCommand.contains("app") || lowerCommand.contains("this"))) {
            return CommandResult(Intent.CLOSE_APP, emptyMap(), 0.9f)
        }

        // Switch app / Recent apps
        if (lowerCommand.contains("switch") || lowerCommand.contains("recent apps") || lowerCommand.contains("app switcher")) {
            return CommandResult(Intent.SWITCH_APP, emptyMap(), 0.9f)
        }

        // Go back
        if (lowerCommand.matches(Regex("(go back|back|previous)"))) {
            return CommandResult(Intent.GO_BACK, emptyMap(), 0.9f)
        }

        // Read notifications
        if (lowerCommand.contains("read") && lowerCommand.contains("notification")) {
            return CommandResult(Intent.READ_NOTIFICATIONS, emptyMap(), 0.9f)
        }

        // Read screen
        if (lowerCommand.contains("read") && (lowerCommand.contains("screen") || lowerCommand.contains("what's on"))) {
            return CommandResult(Intent.READ_SCREEN, emptyMap(), 0.9f)
        }

        // Click button
        if (lowerCommand.contains("click") || lowerCommand.contains("tap") || lowerCommand.contains("press")) {
            val buttonText = extractButtonText(lowerCommand)
            return CommandResult(Intent.CLICK_BUTTON, mapOf("button_text" to buttonText), 0.8f)
        }

        // Scroll
        if (lowerCommand.contains("scroll down") || lowerCommand.contains("swipe down")) {
            return CommandResult(Intent.SCROLL_DOWN, emptyMap(), 0.9f)
        }

        if (lowerCommand.contains("scroll up") || lowerCommand.contains("swipe up")) {
            return CommandResult(Intent.SCROLL_UP, emptyMap(), 0.9f)
        }

        // Screenshot
        if (lowerCommand.contains("screenshot") || lowerCommand.contains("screen shot") || lowerCommand.contains("capture screen")) {
            return CommandResult(Intent.TAKE_SCREENSHOT, emptyMap(), 0.9f)
        }

        // Security Check
        if (lowerCommand.contains("security") || lowerCommand.contains("scan") || lowerCommand.contains("threat")) {
            if (lowerCommand.contains("check") || lowerCommand.contains("scan") || lowerCommand.contains("analyze") || lowerCommand.contains("report")) {
                return CommandResult(Intent.SECURITY_CHECK, emptyMap(), 0.9f)
            }
        }

        // Priority STOP
        if (lowerCommand == "stop" || lowerCommand == "shut up" || lowerCommand.contains("stop now") || lowerCommand.contains("cancel")) {
            return CommandResult(Intent.STOP, emptyMap(), 1.0f)
        }

        // Default to web search: "get me information... from internet/google"
        return CommandResult(
            Intent.SEARCH_WEB,
            mapOf("query" to command),
            0.5f
        )
    }

    private fun extractAppName(command: String): String {
        val keywords = listOf("open", "launch", "start")
        var appName = command

        keywords.forEach { keyword ->
            appName = appName.replace(keyword, "").trim()
        }

        return appName
    }

    private fun extractContactName(command: String): String {
        val keywords = listOf("call", "phone", "dial", "message", "text", "whatsapp", "to")
        var contactName = command

        keywords.forEach { keyword ->
            contactName = contactName.replace(keyword, "").trim()
        }

        return contactName
    }

    private fun extractMessage(command: String): String {
        // Extract message after "saying" or similar keywords
        val patterns = listOf("saying", "say", "message")
        
        for (pattern in patterns) {
            if (command.contains(pattern)) {
                return command.substringAfter(pattern).trim()
            }
        }
        
        return ""
    }

    private fun extractLocation(command: String): String {
        val keywords = listOf("navigate to", "directions to", "map to", "navigate", "directions", "map")
        var location = command

        keywords.forEach { keyword ->
            location = location.replace(keyword, "").trim()
        }

        return location
    }

    private fun extractTime(command: String): String {
        // Simple time extraction - can be enhanced with regex
        val timePattern = Regex("\\d{1,2}(:\\d{2})?(\\s*(am|pm))?")
        val match = timePattern.find(command)
        return match?.value ?: "7:00 AM"
    }

    private fun extractReminderText(command: String): String {
        val keywords = listOf("remind me to", "reminder to", "remind", "reminder")
        var reminderText = command

        keywords.forEach { keyword ->
            reminderText = reminderText.replace(keyword, "").trim()
        }

        return reminderText
    }

    private fun extractMusicQuery(command: String): String {
        val keywords = listOf("play", "music", "song")
        var query = command

        keywords.forEach { keyword ->
            query = query.replace(keyword, "").trim()
        }

        return query
    }

    private fun extractVolumeLevel(command: String): Int {
        // Extract percentage or level
        val percentPattern = Regex("(\\d+)\\s*%")
        val percentMatch = percentPattern.find(command)
        if (percentMatch != null) {
            return percentMatch.groupValues[1].toIntOrNull() ?: 50
        }

        // Check for keywords
        return when {
            command.contains("max") || command.contains("full") || command.contains("loud") -> 100
            command.contains("min") || command.contains("low") || command.contains("quiet") -> 20
            command.contains("medium") || command.contains("half") -> 50
            command.contains("up") -> 75
            command.contains("down") -> 25
            else -> 50
        }
    }

    private fun extractBrightnessLevel(command: String): Int {
        // Extract percentage
        val percentPattern = Regex("(\\d+)\\s*%")
        val percentMatch = percentPattern.find(command)
        if (percentMatch != null) {
            return percentMatch.groupValues[1].toIntOrNull() ?: 50
        }

        // Check for keywords
        return when {
            command.contains("max") || command.contains("full") || command.contains("bright") -> 100
            command.contains("min") || command.contains("low") || command.contains("dim") -> 20
            command.contains("medium") || command.contains("half") -> 50
            else -> 50
        }
    }
}
