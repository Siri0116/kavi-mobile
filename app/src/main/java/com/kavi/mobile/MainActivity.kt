package com.kavi.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kavi.mobile.services.KaviForegroundService
import android.widget.TextView
import android.view.View
import android.widget.ProgressBar
import com.kavi.mobile.engine.IntentEngine
import com.kavi.mobile.engine.ActionExecutor
import com.kavi.mobile.voice.VoiceResponseEngine
import com.kavi.mobile.wakeword.WakeWordDetector
import com.kavi.mobile.data.KaviDatabase
import com.kavi.mobile.ai.PersonalityEngine
import com.kavi.mobile.ai.ProactiveBehaviorEngine
import com.kavi.mobile.ai.QuestionCategorizer
import com.kavi.mobile.data.entities.CommandHistory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context

class MainActivity : AppCompatActivity() {

    private lateinit var micButton: FloatingActionButton
    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var progressBar: ProgressBar
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    private lateinit var intentEngine: IntentEngine
    private lateinit var actionExecutor: ActionExecutor
    
    // AI Components
    private lateinit var voiceEngine: VoiceResponseEngine
    private lateinit var wakeWordDetector: WakeWordDetector
    private lateinit var database: KaviDatabase
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var proactiveBehavior: ProactiveBehaviorEngine
    private lateinit var questionCategorizer: QuestionCategorizer
    private lateinit var conversationContext: com.kavi.mobile.ai.ConversationContext
    
    private var wakeWordEnabled = false

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_RECORD_AUDIO = 100
        private const val REQUEST_CALL_PHONE = 101
        private const val REQUEST_CAMERA = 102
        private const val REQUEST_LOCATION = 103
        private const val REQUEST_CONTACTS = 104
        private const val REQUEST_OVERLAY_PERMISSION = 105
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        micButton = findViewById(R.id.micButton)
        statusText = findViewById(R.id.statusText)
        commandText = findViewById(R.id.commandText)
        progressBar = findViewById(R.id.progressBar)

        // Initialize intent engine and action executor
        intentEngine = IntentEngine()
        actionExecutor = ActionExecutor(this)
        
        // Initialize AI components
        initializeAIComponents()

        // Check and request permissions
        checkPermissions()

        // Initialize speech recognizer
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            setupSpeechRecognizer()
        } else {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_LONG).show()
        }

        // Set up mic button click listener
        micButton.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        // Start foreground service button (for testing)
        findViewById<View>(R.id.startServiceButton).setOnClickListener {
            startKaviService()
        }
        
        // Toggle wake word button
        findViewById<View>(R.id.toggleWakeWordButton)?.setOnClickListener {
            toggleWakeWord()
        }
        
        // Check for overlay permission (Required for Floating Orb)
        checkOverlayPermission()
        
        // Handle Lock Screen & Screen Wake
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        // Request Ignore Battery Optimizations
        checkBatteryOptimization()
        
        // Check if launched from Service with auto-listen
        if (intent.getBooleanExtra("AUTO_LISTEN", false)) {
            startListening()
        }
    }
    
    private fun checkBatteryOptimization() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    private fun checkOverlayPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
                Toast.makeText(this, "Please allow 'Display over other apps' for Kavi", Toast.LENGTH_LONG).show()
            } else {
                // Permission granted, start overlay service
                startOverlayService()
            }
        } else {
            startOverlayService()
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, com.kavi.mobile.ui.overlay.KaviOverlayService::class.java)
        startService(intent)
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
                runOnUiThread {
                    statusText.text = "Listening..."
                }
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could be used for visual feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
                runOnUiThread {
                    statusText.text = "Processing..."
                }
            }

            override fun onError(error: Int) {
                Log.e(TAG, "Speech recognition error: $error")
                runOnUiThread {
                    isListening = false
                    progressBar.visibility = View.GONE
                    statusText.text = "Ready"
                    
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    
                    if (error != SpeechRecognizer.ERROR_NO_MATCH && 
                        error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (matches != null && matches.isNotEmpty()) {
                    val command = matches[0]
                    Log.d(TAG, "Recognized: $command")
                    
                    // Display command
                    commandText.text = "You said: $command"
                    statusText.text = "Processing..."
                    
                    // Process command with personality
                    processCommandWithPersonality(command)
                    
                    // Update UI
                    statusText.text = "Ready"
                    progressBar.visibility = View.GONE
                    micButton.setImageResource(android.R.drawable.ic_btn_speak_now)
                }
                
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    Log.d(TAG, "Partial: ${matches[0]}")
                    runOnUiThread {
                        commandText.text = matches[0]
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Reserved for future use
            }
        })
    }

        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check each permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CALL_PHONE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }

        // Request all missing permissions at once
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_RECORD_AUDIO
            )
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }

        isListening = true
        statusText.text = "Listening..."
        progressBar.visibility = View.VISIBLE
        micButton.setImageResource(android.R.drawable.ic_btn_speak_now)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    private fun stopListening() {
        isListening = false
        statusText.text = "Ready"
        progressBar.visibility = View.GONE
        micButton.setImageResource(android.R.drawable.ic_btn_speak_now)
        speechRecognizer?.stopListening()
    }

    private fun startKaviService() {
        val serviceIntent = Intent(this, KaviForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        Toast.makeText(this, "Kavi service started", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permissions denied - some features may not work", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ==================== AI COMPONENT INITIALIZATION ====================

    private fun initializeAIComponents() {
        try {
            // Initialize voice engine
            voiceEngine = VoiceResponseEngine(this)
            Log.d(TAG, "VoiceResponseEngine initialized")
            
            // Get global state from Application
            val app = application as KaviApplication
            database = app.database
            conversationContext = app.conversationContext
            Log.d(TAG, "Database and ConversationContext retrieved from Application")
            
            // Initialize AI engines
            personalityEngine = PersonalityEngine(this, database, voiceEngine)
            questionCategorizer = QuestionCategorizer()
            proactiveBehavior = ProactiveBehaviorEngine(
                this, database, voiceEngine, personalityEngine
            )
            Log.d(TAG, "AI engines initialized")
            
            // Initialize AI client
            val aiClient = com.kavi.mobile.ai.KaviAIClient()
            Log.d(TAG, "KaviAIClient initialized")
            
            // Initialize wake word detector
            wakeWordDetector = WakeWordDetector(this) {
                onWakeWordDetected()
            }
            Log.d(TAG, "WakeWordDetector initialized")
            
            // Inject AI components into ActionExecutor
            actionExecutor.setVoiceEngine(voiceEngine)
            actionExecutor.setPersonalityEngine(personalityEngine)
            actionExecutor.setQuestionCategorizer(questionCategorizer)
            actionExecutor.setAIClient(aiClient)
            Log.d(TAG, "AI components injected into ActionExecutor")
            
            // Start proactive monitoring
            proactiveBehavior.startMonitoring()
            Log.d(TAG, "Proactive behavior monitoring started")
            
            // Welcome message
            voiceEngine.speak("Hello! I'm Kavi, your AI companion. Say 'Kavi' to activate me.", 
                VoiceResponseEngine.Emotion.HAPPY)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AI components", e)
            Toast.makeText(this, "Error initializing AI features", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== WAKE WORD HANDLING ====================

    private fun toggleWakeWord() {
        wakeWordEnabled = !wakeWordEnabled
        
        if (wakeWordEnabled) {
            wakeWordDetector.startListening()
            statusText.text = "Wake word active - say 'Kavi'"
            Toast.makeText(this, "Wake word detection enabled", Toast.LENGTH_SHORT).show()
            voiceEngine.speak("Wake word detection enabled. Say Kavi to activate me.", 
                VoiceResponseEngine.Emotion.NEUTRAL)
        } else {
            wakeWordDetector.stopListening()
            statusText.text = "Ready"
            Toast.makeText(this, "Wake word detection disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onWakeWordDetected() {
        Log.d(TAG, "Wake word detected!")
        
        // Vibrate to confirm activation
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(100)
            }
        }
        
        // Speak confirmation
        voiceEngine.speak("Yes?", VoiceResponseEngine.Emotion.CURIOUS)
        
        // Start listening for command
        runOnUiThread {
            startListening()
        }
    }

    // ==================== COMMAND LOGGING ====================

    private fun logCommand(command: String, intent: String, success: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val commandHistory = CommandHistory(
                    command = command,
                    timestamp = System.currentTimeMillis(),
                    intent = intent,
                    success = success,
                    executionTimeMs = 0
                )
                database.commandHistoryDao().insert(commandHistory)
                Log.d(TAG, "Command logged: $command -> $intent")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging command", e)
            }
        }
    }

    // ==================== ENHANCED COMMAND PROCESSING ====================

    private fun processCommandWithPersonality(command: String) {
        // 1. Resolve Pronouns using Context (Memory)
        val resolvedCommand = conversationContext.resolvePronoun(command)
        if (resolvedCommand != command) {
            Log.d(TAG, "Resolved '$command' to '$resolvedCommand'")
        }
        
        // 2. Classify the RESOLVED command
        val result = intentEngine.classify(resolvedCommand)
        
        // Log command to database
        logCommand(resolvedCommand, result.intent.toString(), true)
        
        // Get personality-driven response
        lifecycleScope.launch {
            try {
                val (response, emotion) = personalityEngine.generateResponse(
                    command = resolvedCommand,
                    intent = result.intent.toString(),
                    context = mapOf("frequency" to 1) // TODO: Get actual frequency from database
                )
                
                // Speak personality response
                voiceEngine.speak(response, emotion)
                
                // Execute action
                actionExecutor.execute(result)
                
                // 3. Update Short-Term Memory
                conversationContext.addTurn(
                    userInput = resolvedCommand,
                    intent = result.intent.toString(),
                    entities = result.parameters,
                    response = response
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing command with personality", e)
                // Fallback to regular execution
                actionExecutor.execute(result)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        
        // Cleanup AI components
        try {
            wakeWordDetector.destroy()
            voiceEngine.shutdown()
            Log.d(TAG, "AI components cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up AI components", e)
        }
    }
}
