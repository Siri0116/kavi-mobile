package com.kavi.mobile.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.kavi.mobile.voice.VoiceResponseEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Security Reporter - Handles user interaction for security events
 * - Voice Alerts
 * - Manual Reports
 * - Guided Removal (Settings Jumps)
 */
class SecurityReporter(
    private val context: Context,
    private val voiceEngine: VoiceResponseEngine?
) {

    private val threatAnalyzer = ThreatAnalyzer(context)

    /**
     * Run a manual security check and report findings verbally
     */
    fun runManualScan() {
        voiceEngine?.speak("Running security diagnostics...", VoiceResponseEngine.Emotion.SERIOUS)

        CoroutineScope(Dispatchers.Default).launch {
            val report = threatAnalyzer.analyze()
            
            delay(1500) // Simulate scanning feel
            
            CoroutineScope(Dispatchers.Main).launch {
                handleReport(report)
            }
        }
    }

    private suspend fun handleReport(report: ThreatAnalyzer.SecurityReport) {
        if (report.threatLevel == ThreatAnalyzer.ThreatLevel.SAFE) {
            voiceEngine?.speak(
                "System secure. No threats detected.", 
                VoiceResponseEngine.Emotion.HAPPY
            )
            return
        }

        // Announce Level
        val emotion = if (report.threatLevel == ThreatAnalyzer.ThreatLevel.CRITICAL) 
            VoiceResponseEngine.Emotion.CONCERNED 
        else 
            VoiceResponseEngine.Emotion.SERIOUS
            
        voiceEngine?.speak(
            "Siri, Security Alert! Threat level is ${report.threatLevel}. I found ${report.threats.size} issues.",
            emotion
        )
        
        delay(3000)

        // Detail Top 3 Threats
        report.threats.take(3).forEach { threat ->
            voiceEngine?.speak(threat.description, VoiceResponseEngine.Emotion.SERIOUS)
            delay(4000)
            
            // Offer Action if finding has package name
            if (threat.packageName != null) {
                // We could prompt here, but for now just inform.
                // "Say 'open settings' to fix this."
            }
        }
    }

    /**
     * Open Accessibility Settings (Direct Jump)
     */
    fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            voiceEngine?.speak("Opening Accessibility settings.", VoiceResponseEngine.Emotion.NEUTRAL)
        } catch (e: Exception) {
            voiceEngine?.speak("I couldn't open settings directly.", VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    /**
     * Open App Details for Uninstall/Permission Revocation
     */
    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            voiceEngine?.speak("Opening app settings.", VoiceResponseEngine.Emotion.NEUTRAL)
        } catch (e: Exception) {
            voiceEngine?.speak("I couldn't find that app.", VoiceResponseEngine.Emotion.CONCERNED)
        }
    }
}
