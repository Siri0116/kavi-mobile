package com.kavi.mobile.security.detectors

import android.content.Context
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.util.Log

/**
 * Mic & Camera Monitor
 * Detects if microphone is currently in use by other apps.
 * (Camera usage is harder to detect without callbacks on older APIs, focusing on Mic first)
 */
class MicCameraMonitor(private val context: Context) {

    companion object {
        private const val TAG = "MicCameraMonitor"
    }

    data class MicUsage(
        val packageName: String?,
        val isActive: Boolean
    )

    /**
     * Check if microphone is currently recording
     */
    fun isMicActive(): MicUsage {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val configs = audioManager.activeRecordingConfigurations

            for (config in configs) {
                // Return immediately if any app is recording
                // We can try to get client info (uid) but mapping to package is probabilistic if multiple
                // apps share UID, though typically 1-to-1.
                
                // Note: ClientAudioSource 1999 usually means Hotword (Assistant)
                if (config.clientAudioSource == 1999) continue
                
                // Flag it
                return MicUsage(packageName = "UID:${config.clientUid}", isActive = true)
            }
        } else {
            // Pre-N fallback: checking mode is unreliable but AudioManager.MODE_IN_COMMUNICATION might hint
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audioManager.mode == AudioManager.MODE_IN_COMMUNICATION || 
                audioManager.mode == AudioManager.MODE_IN_CALL) {
                return MicUsage(packageName = "Unknown (Call/VoIP)", isActive = true)
            }
        }
        
        return MicUsage(null, false)
    }
}
