    // ==================== ACCESSIBILITY ACTIONS ====================

    private fun closeCurrentApp() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.closeCurrentApp()
            if (success) {
                speak("Closing app", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not close app", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled. Please enable it in settings.", 
                com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            Toast.makeText(context, "Enable Accessibility Service", Toast.LENGTH_LONG).show()
        }
    }

    private fun switchApp() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.openRecentApps()
            if (success) {
                speak("Opening app switcher", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not open app switcher", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun goBack() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.goBack()
            if (success) {
                speak("Going back", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not go back", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
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
                // Read first few lines to avoid overwhelming
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
        if (accessibilityService != null) {
            val success = accessibilityService.scrollDown()
            if (success) {
                speak("Scrolling down", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not scroll", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun scrollUp() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.scrollUp()
            if (success) {
                speak("Scrolling up", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not scroll", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    private fun takeScreenshot() {
        val accessibilityService = com.kavi.mobile.services.KaviAccessibilityService.getInstance()
        if (accessibilityService != null) {
            val success = accessibilityService.takeScreenshot()
            if (success) {
                speak("Taking screenshot", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL)
            } else {
                speak("Could not take screenshot. This feature requires Android 9 or higher.", 
                    com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
            }
        } else {
            speak("Accessibility service not enabled", com.kavi.mobile.voice.VoiceResponseEngine.Emotion.CONCERNED)
        }
    }

    // ==================== HELPER METHODS ====================

    private fun speak(text: String, emotion: com.kavi.mobile.voice.VoiceResponseEngine.Emotion = com.kavi.mobile.voice.VoiceResponseEngine.Emotion.NEUTRAL) {
        voiceEngine?.speak(text, emotion)
    }
}
