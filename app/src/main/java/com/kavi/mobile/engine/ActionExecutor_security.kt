
    // ==================== SECURITY ACTIONS ====================

    private fun runSecurityCheck() {
        speak("Starting security scan...", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.SERIOUS)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Run scan in background
                val risks = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    securityScanner.runFullScan()
                }
                
                if (risks.isEmpty()) {
                    speak("Security check complete. No threats detected. Your device is secure.", 
                        com.kavi.mobile.voice.VoiceResponseEngine.Emotion.HAPPY)
                } else {
                    val criticalCount = risks.count { it.severity == com.kavi.mobile.security.SecurityScanner.RiskSeverity.CRITICAL || it.severity == com.kavi.mobile.security.SecurityScanner.RiskSeverity.HIGH }
                    val totalCount = risks.size
                    
                    speak("Scan complete. I found $totalCount potential issues, including $criticalCount high risk items.", 
                        com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
                    
                    // Read top 3 risks
                    risks.take(3).forEach { risk ->
                        // Pause slightly
                        kotlinx.coroutines.delay(500)
                        speak(risk.description, com.kavi.mobile.voice.VoiceResponseEngine.Emotion.SERIOUS)
                    }
                    
                    if (risks.size > 3) {
                        speak("Please check the security log for more details.", 
                            com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error running security scan", e)
                speak("I encountered an error while scanning. Please try again.", 
                    com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        }
    }
}
