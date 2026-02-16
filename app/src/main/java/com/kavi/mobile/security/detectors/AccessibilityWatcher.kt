package com.kavi.mobile.security.detectors

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityManager

/**
 * Accessibility Watcher - Critical Component
 * Monitors enabled Accessibility Services.
 * Most Android malware exploits this to steal data/control UI.
 */
class AccessibilityWatcher(private val context: Context) {

    companion object {
        private const val TAG = "AccessibilityWatcher"
    }

    data class AccessibilityRisk(
        val packageName: String,
        val serviceId: String,
        val serviceName: String
    )

    /**
     * Check for unknown enabled accessibility services
     */
    fun checkEnabledServices(): List<AccessibilityRisk> {
        val risks = mutableListOf<AccessibilityRisk>()
        
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            
            for (service in enabledServices) {
                val componentName = service.id
                val packageName = service.resolveInfo.serviceInfo.packageName
                val serviceName = service.resolveInfo.serviceInfo.name || "Unknown Service"

                // Whitelist: Kavi + System Services + Known Safe (e.g. TalkBack, Switch Access)
                if (isWhitelisted(packageName)) {
                    continue
                }

                risks.add(AccessibilityRisk(
                    packageName = packageName,
                    serviceId = componentName,
                    serviceName = serviceName.toString()
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility services", e)
        }
        
        return risks
    }

    private fun isWhitelisted(packageName: String): Boolean {
        val systemWhitelist = listOf(
            context.packageName, // Self
            "com.google.android.marvin.talkback", // Google TalkBack
            "com.google.android.accessibility.soundamplifier",
            "com.google.audio.hearing.visualization.accessibility.scribe",
            "com.android.switchaccess"
        )
        
        return systemWhitelist.contains(packageName) || 
               packageName.startsWith("com.android.") || // Internal system
               packageName.startsWith("com.google.android.") // Google system services
    }
}
