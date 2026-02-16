package com.kavi.mobile.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.kavi.mobile.automation.AppLauncher
import com.kavi.mobile.automation.PhoneController
import com.kavi.mobile.automation.SystemController
import com.kavi.mobile.services.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.kavi.mobile.security.SecurityScanner

/**
 * Action Executor - Executes actions based on classified intents
 */
class ActionExecutor(private val context: Context) {

    companion object {
        private const val TAG = "ActionExecutor"
    }

    private val appLauncher = AppLauncher(context)
    private val phoneController = PhoneController(context)
    private val systemController = SystemController(context)
    private val weatherService = WeatherService(context)
    // private val securityScanner = SecurityScanner(context) // Deprecated
    private var securityReporter: com.kavi.mobile.security.SecurityReporter? = null
    
    // These will be injected later for full AI features
    private var voiceEngine: com.kavi.mobile.voice.VoiceResponseEngine? = null
    private var personalityEngine: com.kavi.mobile.ai.PersonalityEngine? = null
    private var questionCategorizer: com.kavi.mobile.ai.QuestionCategorizer? = null
    private var aiClient: com.kavi.mobile.ai.KaviAIClient? = null
    
    fun setVoiceEngine(engine: com.kavi.mobile.voice.VoiceResponseEngine) {
        this.voiceEngine = engine
        this.securityReporter = com.kavi.mobile.security.SecurityReporter(context, engine)
    }
    
    fun setPersonalityEngine(engine: com.kavi.mobile.ai.PersonalityEngine) {
        this.personalityEngine = engine
    }
    
    fun setQuestionCategorizer(categorizer: com.kavi.mobile.ai.QuestionCategorizer) {
        this.questionCategorizer = categorizer
    }
    
    fun setAIClient(client: com.kavi.mobile.ai.KaviAIClient) {
        this.aiClient = client
    }

    /**
     * Execute an action based on the command result
     */
    fun execute(result: IntentEngine.CommandResult) {
        Log.d(TAG, "Executing intent: ${result.intent} with params: ${result.parameters}")

        when (result.intent) {
            IntentEngine.Intent.OPEN_APP -> {
                val appName = result.parameters["app_name"] ?: return
                appLauncher.launchApp(appName)
            }

            IntentEngine.Intent.MAKE_CALL -> {
                val contactName = result.parameters["contact_name"] ?: return
                phoneController.makeCall(contactName)
            }

            IntentEngine.Intent.SEND_MESSAGE -> {
                val contactName = result.parameters["contact_name"] ?: return
                val message = result.parameters["message"] ?: ""
                sendMessage(contactName, message)
            }

            IntentEngine.Intent.TAKE_PHOTO -> {
                openCamera()
            }

            IntentEngine.Intent.TAKE_VIDEO -> {
                openVideoCamera()
            }

            IntentEngine.Intent.NAVIGATE -> {
                val location = result.parameters["location"] ?: return
                navigate(location)
            }

            IntentEngine.Intent.SET_ALARM -> {
                val time = result.parameters["time"] ?: return
                setAlarm(time)
            }

            IntentEngine.Intent.SET_REMINDER -> {
                val reminder = result.parameters["reminder"] ?: return
                setReminder(reminder)
            }

            IntentEngine.Intent.PLAY_MUSIC -> {
                val query = result.parameters["query"] ?: ""
                playMusic(query)
            }

            IntentEngine.Intent.SEARCH_WEB -> {
                val query = result.parameters["query"] ?: return
                // User Request: "if i make it search... it shouldnt call my name"
                speak("Checking Google for $query.")
                searchWeb(query)
            }

            // System Control
            IntentEngine.Intent.SILENT_MODE -> {
                val enable = result.parameters["enable"]?.toBoolean() ?: true
                setSilentMode(enable)
            }

            IntentEngine.Intent.VIBRATE_MODE -> {
                val enable = result.parameters["enable"]?.toBoolean() ?: true
                setVibrateMode(enable)
            }

            IntentEngine.Intent.VOLUME_CONTROL -> {
                val level = result.parameters["level"]?.toIntOrNull() ?: 50
                setVolume(level)
            }

            IntentEngine.Intent.BRIGHTNESS_CONTROL -> {
                val level = result.parameters["level"]?.toIntOrNull() ?: 50
                setBrightness(level)
            }

            IntentEngine.Intent.WIFI_SETTINGS -> {
                systemController.openWifiSettings()
                speak("Opening WiFi settings")
            }

            IntentEngine.Intent.BLUETOOTH_SETTINGS -> {
                systemController.openBluetoothSettings()
                speak("Opening Bluetooth settings")
            }

            IntentEngine.Intent.AIRPLANE_MODE -> {
                systemController.openAirplaneModeSettings()
                speak("Opening airplane mode settings")
            }

            // Information
            IntentEngine.Intent.WEATHER -> {
                getWeather()
            }

            IntentEngine.Intent.TIME -> {
                tellTime()
            }

            IntentEngine.Intent.DATE -> {
                tellDate()
            }

            IntentEngine.Intent.BATTERY_STATUS -> {
                tellBatteryStatus()
            }

            // Conversational
            IntentEngine.Intent.GREETING -> {
                respondToGreeting()
            }

            IntentEngine.Intent.THANKS -> {
                respondToThanks()
            }

            IntentEngine.Intent.QUESTION -> {
                val question = result.parameters["question"] ?: ""
                handleQuestion(question)
            }

            IntentEngine.Intent.CASUAL_CHAT -> {
                val message = result.parameters["message"] ?: ""
                handleCasualChat(message)
            }

            // Accessibility Actions
            IntentEngine.Intent.CLOSE_APP -> {
                closeCurrentApp()
            }

            IntentEngine.Intent.SWITCH_APP -> {
                switchApp()
            }

            IntentEngine.Intent.GO_BACK -> {
                goBack()
            }

            IntentEngine.Intent.READ_NOTIFICATIONS -> {
                readNotifications()
            }

            IntentEngine.Intent.READ_SCREEN -> {
                readScreen()
            }

            IntentEngine.Intent.CLICK_BUTTON -> {
                val buttonText = result.parameters["button_text"] ?: ""
                clickButton(buttonText)
            }

            IntentEngine.Intent.SCROLL_DOWN -> {
                scrollDown()
            }

            IntentEngine.Intent.SCROLL_UP -> {
                scrollUp()
            }

            IntentEngine.Intent.TAKE_SCREENSHOT -> {
                takeScreenshot()
            }

            IntentEngine.Intent.SECURITY_CHECK -> {
                runSecurityCheck()
            }

            IntentEngine.Intent.STOP -> {
                stopAllActions()
            }

            IntentEngine.Intent.UNKNOWN -> {
                // Was: Toast.makeText...
                // Now: Fallback to Personality/Motherly response or Web Search if it looks like a question
                if (voiceEngine != null) {
                    // Treat unknown as potential conversation or search
                    // For now, let's just say we don't know but ask if they want to search
                    voiceEngine?.speak("I didn't quite catch that. Want me to search Google?", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
                }
            }
        }
    }

    private fun stopAllActions() {
        // Stop Voice
        voiceEngine?.stop()
        
        // Cancel all coroutines/jobs 
        // (ActionExecutor should likely manage its own scope for this to be effective, currently using companion or global scopes in some places?)
        // Assuming voiceEngine.stop() handles the speech.
        
        Toast.makeText(context, "Stopped.", Toast.LENGTH_SHORT).show()
    }

    // ==================== SYSTEM CONTROL ACTIONS ====================

    private fun setSilentMode(enable: Boolean) {
        if (systemController.setSilentMode(enable)) {
            speak(if (enable) "Silent mode enabled" else "Silent mode disabled")
        } else {
            speak("I need permission to control Do Not Disturb. Opening settings.")
        }
    }

    private fun setVibrateMode(enable: Boolean) {
        if (systemController.setVibrateMode(enable)) {
            speak(if (enable) "Vibrate mode enabled" else "Vibrate mode disabled")
        } else {
            speak("I need permission to control vibrate mode. Opening settings.")
        }
    }

    private fun setVolume(level: Int) {
        if (systemController.setVolume(level)) {
            speak("Volume set to $level percent")
        } else {
            speak("Could not set volume")
        }
    }

    private fun setBrightness(level: Int) {
        if (systemController.setBrightness(level)) {
            speak("Brightness set to $level percent")
        } else {
            speak("I need permission to control brightness. Opening settings.")
        }
    }

    // ==================== INFORMATION ACTIONS ====================

    private fun tellTime() {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val amPm = if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
        
        val timeString = String.format("%d:%02d %s", if (hour == 0) 12 else hour, minute, amPm)
        // User Request: Call my name for time
        speak("Siri, it's $timeString")
    }

    private fun tellDate() {
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        
        speak("Today is $dateString")
    }

    private fun tellBatteryStatus() {
        val batteryInfo = systemController.getBatteryStatus()
        val status = if (batteryInfo.isCharging) "charging" else "not charging"
        speak("Battery is at ${batteryInfo.level} percent and $status")
    }

    private fun getWeather() {
        speak("Let me check the weather for you", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val weather = weatherService.getCurrentWeather()
                
                if (weather != null) {
                    val response = weatherService.formatWeatherResponse(weather)
                    val simpleResponse = weatherService.getSimpleWeatherResponse(weather)
                    
                    speak(response, com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
                    
                    // Add personality comment
                    personalityEngine?.let {
                        if (weather.temperature > 35) {
                            speak("Stay indoors if possible, it's really hot out there!", 
                                com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
                        } else if (weather.temperature < 5) {
                            speak("Bundle up! It's freezing!", 
                                com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
                        }
                    }
                } else {
                    speak("I couldn't get the weather information. Let me search for it.", 
                        com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
                    searchWeb("weather")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting weather", e)
                speak("Sorry, I had trouble getting the weather. Let me search for it.", 
                    com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
                searchWeb("weather")
            }
        }
    }

    // ==================== CONVERSATIONAL ACTIONS ====================

    private fun respondToGreeting() {
        val responses = listOf(
            "Hello! How can I help you?",
            "Hi there! What can I do for you?",
            "Hey! Ready to assist!",
            "Hello! What do you need?"
        )
        speak(responses.random(), com.kavi.mobile.voice.VoiceResponseEngine.Emotion.HAPPY)
    }

    private fun respondToThanks() {
        val responses = listOf(
            "You're welcome!",
            "Happy to help!",
            "Anytime!",
            "My pleasure!",
            "No problem!"
        )
        speak(responses.random(), com.kavi.mobile.voice.VoiceResponseEngine.Emotion.HAPPY)
    }

    private fun handleQuestion(question: String) {
        questionCategorizer?.let { categorizer ->
            val category = categorizer.categorize(question)
            
            // Check if AI backend is available for complex questions
            if (categorizer.needsAIBackend(category) && aiClient?.isAvailable() == true) {
                speak("Let me think about that", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CURIOUS)
                
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val aiResponse = aiClient?.ask(question)
                        
                        if (aiResponse != null) {
                            speak(aiResponse.answer, com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
                        } else {
                            // Fallback to categorizer response
                            val response = categorizer.getSuggestedResponse(category, question)
                            speak(response)
                            searchWeb(question)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting AI response", e)
                        val response = categorizer.getSuggestedResponse(category, question)
                        speak(response)
                        searchWeb(question)
                    }
                }
            } else {
                // Use categorizer response
                val response = categorizer.getSuggestedResponse(category, question)
                speak(response)
                
                // Search web for additional information
                if (categorizer.needsAIBackend(category)) {
                    searchWeb(question)
                }
            }
        } ?: run {
            speak("Let me search for that")
            searchWeb(question)
        }
    }

    private fun handleCasualChat(message: String) {
        val responses = listOf(
            "I'm doing well, thanks for asking!",
            "I'm here and ready to help!",
            "All good! How about you?",
            "I'm great! What can I do for you?"
        )
        speak(responses.random(), com.kavi.mobile.voice.VoiceResponseEngine.Emotion.PLAYFUL)
    }

    // ==================== HELPER METHODS ====================

    private fun speak(text: String, emotion: com.kavi.mobile.voice.VoiceResponseEngine.Emotion = com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL) {
        voiceEngine?.speak(text, emotion) ?: run {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(contactName: String, message: String) {
        try {
            // Open WhatsApp with pre-filled message
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            
            Toast.makeText(context, "Opening WhatsApp", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "Opening camera", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera", e)
            Toast.makeText(context, "Could not open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openVideoCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "Opening video camera", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening video camera", e)
            Toast.makeText(context, "Could not open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigate(location: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(location)}"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "Navigating to $location", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error opening maps", e)
            Toast.makeText(context, "Could not open maps", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAlarm(time: String) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            // TODO: Parse time string and set hour/minute
            context.startActivity(intent)
            Toast.makeText(context, "Setting alarm", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting alarm", e)
            Toast.makeText(context, "Could not set alarm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setReminder(reminder: String) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, reminder)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "Setting reminder", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting reminder", e)
            Toast.makeText(context, "Could not set reminder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playMusic(query: String) {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
            intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, query)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "Playing music", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing music", e)
            Toast.makeText(context, "Could not play music", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchWeb(query: String) {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra("query", query)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching web", e)
            Toast.makeText(context, "Could not search", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== ACCESSIBILITY ACTIONS ====================

    private fun closeCurrentApp() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            accessibilityService.closeCurrentApp()
            speak("Closing app", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
        } else {
            speak("Accessibility service not enabled. Please enable it in settings.", 
                com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun switchApp() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            accessibilityService.openRecentApps()
            speak("Opening app switcher", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun goBack() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            accessibilityService.goBack()
            speak("Going back", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun readNotifications() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val notification = accessibilityService.getLastNotification()
            if (notification != null) {
                speak("Last notification: $notification", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("No recent notifications", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun readScreen() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val screenText = accessibilityService.extractScreenText()
            if (screenText.isNotEmpty()) {
                val lines = screenText.split("\n").take(5)
                val summary = lines.joinToString(". ")
                speak("Screen contains: $summary", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not read screen", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun clickButton(buttonText: String) {
        if (buttonText.isEmpty()) {
            speak("Please specify which button to click", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.QUESTIONING)
            return
        }

        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.clickByText(buttonText)
            if (success) {
                speak("Clicked $buttonText", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not find button: $buttonText", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun scrollDown() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        accessibilityService?.scrollDown()
        speak("Scrolling down", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
    }

    private fun scrollUp() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        accessibilityService?.scrollUp()
        speak("Scrolling up", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
    }

    private fun takeScreenshot() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.takeScreenshot()
            if (success) {
                speak("Taking screenshot", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Screenshot requires Android 9 or higher", 
                    com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun speak(text: String, emotion: com.kavi.mobile.voice.VoiceResponseEngine.Emotion = com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL) {
        voiceEngine?.speak(text, emotion)
    }

    // ==================== SECURITY ACTIONS ====================

    private fun runSecurityCheck() {
        if (securityReporter != null) {
            securityReporter?.runManualScan()
        } else {
            speak("Security module not initialized.", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }
}
