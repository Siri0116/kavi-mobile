package com.kavi.mobile.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

/**
 * VoiceResponseEngine - Handles all text-to-speech functionality for Kavi
 * Provides personality-driven voice responses with emotional variations
 */
class VoiceResponseEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onSpeechCompleteCallback: (() -> Unit)? = null

    companion object {
        private const val TAG = "VoiceResponseEngine"
        private const val DEFAULT_PITCH = 1.0f
        private const val DEFAULT_SPEED = 1.0f
    }

    /**
     * Emotion types that affect voice characteristics
     */
    enum class Emotion(val pitch: Float, val speed: Float) {
        NEUTRAL(1.0f, 1.0f),
        HAPPY(1.2f, 1.1f),
        CONCERNED(0.9f, 0.9f),
        PLAYFUL(1.3f, 1.2f),
        SERIOUS(0.8f, 0.85f),
        EXCITED(1.4f, 1.3f),
        CALM(0.95f, 0.9f),
        URGENT(1.1f, 1.4f),
        CURIOUS(1.1f, 1.0f)
    }

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { textToSpeech ->
                val result = textToSpeech.setLanguage(Locale.US)
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                    isInitialized = false
                } else {
                    isInitialized = true
                    
                    // User Request: "make sure kavi voice is of female's"
                    // Try to find a female voice
                    val voices = textToSpeech.voices
                    if (voices != null) {
                        for (voice in voices) {
                            if (voice.name.contains("female", ignoreCase = true) || 
                                voice.name.contains("f00", ignoreCase = true)) {
                                textToSpeech.voice = voice
                                break
                            }
                        }
                    }
                    
                    // Fallback configuration (High pitch = Female-sounding)
                    textToSpeech.setPitch(1.3f) 
                    textToSpeech.setSpeechRate(1.2f)

                    setupProgressListener()
                    Log.d(TAG, "TTS initialized successfully")
                }
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
            isInitialized = false
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "Speech started: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "Speech completed: $utteranceId")
                onSpeechCompleteCallback?.invoke()
            }

            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Speech error: $utteranceId")
            }
        })
    }

    /**
     * Speak text with specified emotion
     */
    fun speak(text: String, emotion: Emotion = Emotion.NEUTRAL, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized")
            return
        }

        onSpeechCompleteCallback = onComplete

        tts?.let { textToSpeech ->
            // Set voice characteristics based on emotion
            textToSpeech.setPitch(emotion.pitch)
            textToSpeech.setSpeechRate(emotion.speed)

            // Speak with unique utterance ID
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            
            Log.d(TAG, "Speaking: $text (Emotion: $emotion)")
        }
    }

    /**
     * Speak text and add to queue (doesn't interrupt current speech)
     */
    fun speakQueued(text: String, emotion: Emotion = Emotion.NEUTRAL) {
        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized")
            return
        }

        tts?.let { textToSpeech ->
            textToSpeech.setPitch(emotion.pitch)
            textToSpeech.setSpeechRate(emotion.speed)

            val utteranceId = UUID.randomUUID().toString()
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
            
            Log.d(TAG, "Queued: $text")
        }
    }

    /**
     * Interrupt current speech
     */
    fun interrupt() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            Log.d(TAG, "Speech interrupted")
        }
    }

    /**
     * Ask a question and wait for response
     */
    fun askQuestion(question: String, emotion: Emotion = Emotion.NEUTRAL, callback: (String) -> Unit) {
        speak(question, emotion) {
            // Question asked, ready for response
            // Callback will be triggered when user responds
            Log.d(TAG, "Question asked, waiting for response")
        }
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    /**
     * Set voice pitch (0.5 to 2.0)
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * Set speech rate (0.5 to 2.0)
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * Get available voices
     */
    fun getAvailableVoices(): Set<android.speech.tts.Voice>? {
        return tts?.voices
    }

    /**
     * Set specific voice
     */
    fun setVoice(voice: android.speech.tts.Voice) {
        tts?.voice = voice
    }

    /**
     * Cleanup resources
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        Log.d(TAG, "TTS shutdown")
    }
}
