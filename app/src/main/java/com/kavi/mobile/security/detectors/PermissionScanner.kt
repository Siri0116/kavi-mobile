package com.kavi.mobile.security.detectors

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Permission Scanner - Analyzes app permissions for dangerous combinations
 * Detects:
 * - Microphone access
 * - Camera access
 * - Location access
 * - Overlay permission
 * - Accessibility Service usage
 */
class PermissionScanner(private val context: Context) {

    companion object {
        private const val TAG = "PermissionScanner"
        
        // Critical permissions to watch
        private const val PERM_MIC = "android.permission.RECORD_AUDIO"
        private const val PERM_CAMERA = "android.permission.CAMERA"
        private const val PERM_LOCATION = "android.permission.ACCESS_FINE_LOCATION"
        private const val PERM_OVERLAY = "android.permission.SYSTEM_ALERT_WINDOW"
        private const val PERM_READ_SMS = "android.permission.READ_SMS"
        private const val PERM_READ_CONTACTS = "android.permission.READ_CONTACTS"
    }

    data class AppPermissions(
        val packageName: String,
        val hasMic: Boolean,
        val hasCamera: Boolean,
        val hasLocation: Boolean,
        val hasOverlay: Boolean,
        val hasSms: Boolean,
        val hasContacts: Boolean
    )

    /**
     * Get permissions for a specific app
     */
    fun getAppPermissions(packageName: String): AppPermissions? {
        return try {
            val pm = context.packageManager
            val pkg = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            
            val requestedPermissions = pkg.requestedPermissions?.toList() ?: emptyList()
            
            // Note: requestedPermissions includes denied ones too. 
            // Ideally should check active permissions, but requested shows intent.
            
            AppPermissions(
                packageName = packageName,
                hasMic = requestedPermissions.contains(PERM_MIC),
                hasCamera = requestedPermissions.contains(PERM_CAMERA),
                hasLocation = requestedPermissions.contains(PERM_LOCATION),
                hasOverlay = requestedPermissions.contains(PERM_OVERLAY),
                hasSms = requestedPermissions.contains(PERM_READ_SMS),
                hasContacts = requestedPermissions.contains(PERM_READ_CONTACTS)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions for $packageName", e)
            null
        }
    }

    /**
     * Scan all apps for dangerous permission combinations
     */
    fun scanForDangerousCombos(): List<Pair<String, String>> {
        val dangerousApps = mutableListOf<Pair<String, String>>()
        val pm = context.packageManager
        
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        for (pkg in packages) {
            if (pkg.packageName == context.packageName) continue
            // Skip system apps usually, but maybe flag if they act weird? Keeping simple for now.
            if ((pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            val permissions = pkg.requestedPermissions?.toList() ?: continue
            
            val hasMic = permissions.contains(PERM_MIC)
            val hasOverlay = permissions.contains(PERM_OVERLAY)
            val hasCamera = permissions.contains(PERM_CAMERA)
            val hasLocation = permissions.contains(PERM_LOCATION)

            // Spyware Pattern 1: Mic + Overlay (Record while hiding)
            if (hasMic && hasOverlay) {
                dangerousApps.add(pkg.packageName to "High Risk: Can record audio and draw over other apps.")
            }

            // Spyware Pattern 2: Mic + Camera + Location (Full surveillance)
            if (hasMic && hasCamera && hasLocation) {
                dangerousApps.add(pkg.packageName to "High Risk: Full surveillance access (Mic, Camera, Location).")
            }
        }
        
        return dangerousApps
    }
}
