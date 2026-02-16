package com.kavi.mobile.security.selfdefense

import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * Debug Detector - Detects if device represents a hostile environment
 * - USB Debugging Enabled (ADB)
 * - Developer Options Enabled
 * - Install from Unknown Sources
 */
class DebugDetector(private val context: Context) {

    companion object {
        private const val TAG = "DebugDetector"
    }

    fun isUsbDebuggingEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver, 
                Settings.Global.ADB_ENABLED, 0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    fun isUnknownSourcesEnabled(): Boolean {
        return try {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0
            ) == 1
        } catch (e: Exception) {
            // Android O+ handles this per-app, global setting is less relevant/accessible
            // Assuming false if we can't check
            false
        }
    }
    
    fun getSecurityMessage(): String? {
        if (isUsbDebuggingEnabled()) {
            return "Warning: USB Debugging is enabled. This makes your device vulnerable to physical attacks."
        }
        if (isUnknownSourcesEnabled()) {
            return "Warning: Installation from unknown sources is enabled globally."
        }
        return null
    }
}
