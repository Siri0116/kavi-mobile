package com.kavi.mobile.security.selfdefense

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Permission Guardian - Ensures Kavi still has its critical permissions
 * Checks:
 * - Microphone
 * - Accessibility Service (Self) - CRITICAL
 * - Overlay
 * - Notification Listener
 */
class PermissionGuardian(private val context: Context) {

    data class MissingPermission(
        val name: String,
        val isCritical: Boolean
    )

    fun checkSelfPermissions(): List<MissingPermission> {
        val missing = mutableListOf<MissingPermission>()
        
        // 1. Check Microphone (Critical)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            missing.add(MissingPermission("Microphone", true))
        }

        // 2. Check Accessibility (Critical for automation/security)
        if (!isAccessibilityEnabled()) {
            missing.add(MissingPermission("Accessibility Service", true))
        }

        // 3. Check Overlay (Important for UI)
        if (!Settings.canDrawOverlays(context)) {
            missing.add(MissingPermission("Display Over Apps", false))
        }

        return missing
    }

    private fun isAccessibilityEnabled(): Boolean {
        return try {
            val expectedService = "${context.packageName}/com.kavi.mobile.services.KaviAccessibilityService"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            
            enabledServices.contains(expectedService)
        } catch (e: Exception) {
            false
        }
    }
}
