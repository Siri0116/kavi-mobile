package com.kavi.mobile.wakeword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.*

/**
 * WakeWordDetector - Continuously listens for "Kavi" wake word
 * Uses text-based detection with Android SpeechRecognizer
 */
class WakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: () -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isEnabled = false

    companion object {
        private const val TAG = "WakeWordDetector"
        private val WAKE_WORDS = listOf("kavi", "hey kavi", "ok kavi", "kavi assistant")
        private const val RESTART_DELAY_MS = 1000L
    }

    init {
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
            Log.d(TAG, "Wake word detector initialized")
        } else {
            Log.e(TAG, "Speech recognition not available")
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for wake word")
            }

            override fun onBeginningOfSpeech() {
                // User started speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                // User stopped speaking
            }

            override fun onError(error: Int) {
                Log.e(TAG, "Recognition error: $error")
                
                // Restart listening if still enabled
                if (isEnabled) {
                    restartListening()
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0].lowercase(Locale.getDefault())
                    Log.d(TAG, "Heard: $spokenText")
                    
                    if (containsWakeWord(spokenText)) {
                        Log.d(TAG, "Wake word detected!")
                        onWakeWordDetected()
                    }
                }
                
                // Continue listening if still enabled
                if (isEnabled) {
                    restartListening()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0].lowercase(Locale.getDefault())
                    
                    // Check partial results for faster detection
                    if (containsWakeWord(spokenText)) {
                        Log.d(TAG, "Wake word detected (partial)!")
                        speechRecognizer?.stopListening()
                        onWakeWordDetected()
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Reserved for future use
            }
        }
    }

    private fun containsWakeWord(text: String): Boolean {
        return WAKE_WORDS.any { wakeWord ->
            text.contains(wakeWord)
        }
    }

    /**
     * Start listening for wake word
     */
    fun startListening() {
        if (!isEnabled) {
            isEnabled = true
            startRecognition()
            Log.d(TAG, "Wake word detection started")
        }
    }

    /**
     * Stop listening for wake word
     */
    fun stopListening() {
        isEnabled = false
        isListening = false
        speechRecognizer?.stopListening()
        Log.d(TAG, "Wake word detection stopped")
    }

    private fun startRecognition() {
        if (isListening) {
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recognition", e)
            isListening = false
        }
    }

    private fun restartListening() {
        isListening = false
        
        // Small delay before restarting to avoid rapid restarts
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isEnabled) {
                startRecognition()
            }
        }, RESTART_DELAY_MS)
    }

    /**
     * Check if currently listening
     */
    fun isActive(): Boolean {
        return isEnabled && isListening
    }

    /**
     * Cleanup resources
     */
    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Wake word detector destroyed")
    }
}
