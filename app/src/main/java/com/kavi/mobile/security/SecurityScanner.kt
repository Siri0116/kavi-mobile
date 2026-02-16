package com.kavi.mobile.security

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager

/**
 * Security Scanner - Detects potential threats and security risks
 * Monitors:
 * 1. Risky Apps (Dangerous permission combos)
 * 2. Accessibility Abuse
 * 3. Screen Overlays
 * 4. System Settings (USB Debugging, Unknown Sources)
 */
class SecurityScanner(private val context: Context) {

    companion object {
        private const val TAG = "SecurityScanner"
        
        // Dangerous permissions to watch for
        private const val PERM_ACCESSIBILITY = "android.permission.BIND_ACCESSIBILITY_SERVICE"
        private const val PERM_OVERLAY = "android.permission.SYSTEM_ALERT_WINDOW"
        private const val PERM_MIC = "android.permission.RECORD_AUDIO"
        private const val PERM_CAMERA = "android.permission.CAMERA"
        private const val PERM_LOCATION = "android.permission.ACCESS_FINE_LOCATION"
    }

    data class SecurityRisk(
        val type: RiskType,
        val severity: RiskSeverity,
        val description: String,
        val actionablePackage: String? = null
    )

    enum class RiskType {
        DANGEROUS_APP,
        ACCESSIBILITY_ABUSE,
        UNSECURE_SETTING,
        OVERLAY_ATTACK,
        DEV_MODE_ENABLED
    }

    enum class RiskSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Run full security scan
     */
    fun runFullScan(): List<SecurityRisk> {
        val risks = mutableListOf<SecurityRisk>()
        
        risks.addAll(scanSystemSettings())
        risks.addAll(scanAccessibilityServices())
        risks.addAll(scanInstalledApps())
        
        return risks.sortedByDescending { it.severity }
    }

    /**
     * Scan system settings for vulnerabilities
     */
    private fun scanSystemSettings(): List<SecurityRisk> {
        val risks = mutableListOf<SecurityRisk>()

        // Check USB Debugging
        try {
            val adbEnabled = Settings.Global.getInt(
                context.contentResolver, 
                Settings.Global.ADB_ENABLED, 0
            ) == 1
            
            if (adbEnabled) {
                risks.add(SecurityRisk(
                    RiskType.DEV_MODE_ENABLED,
                    RiskSeverity.MEDIUM,
                    "USB Debugging is enabled. This exposes your device to external attacks via USB."
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking ADB", e)
        }

        // Check Unknown Sources (Install from unknown apps)
        try {
            val unknownSources = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0
            ) == 1

            if (unknownSources) {
                risks.add(SecurityRisk(
                    RiskType.UNSECURE_SETTING,
                    RiskSeverity.HIGH,
                    "Installation from Unknown Sources is enabled. This allows malicious apps to be installed."
                ))
            }
        } catch (e: Exception) {
            // Might be deprecated/different in newer Android versions, safe fallback
        }

        return risks
    }

    /**
     * Check for suspicious Accessibility Services
     */
    private fun scanAccessibilityServices(): List<SecurityRisk> {
        val risks = mutableListOf<SecurityRisk>()
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        for (service in enabledServices) {
            val componentName = service.resolveInfo.serviceInfo.packageName
            
            // Skip Kavi itself and known system services
            if (componentName == context.packageName || 
                componentName.startsWith("com.google") ||
                componentName.startsWith("com.android")) {
                continue
            }

            risks.add(SecurityRisk(
                RiskType.ACCESSIBILITY_ABUSE,
                RiskSeverity.HIGH,
                "Unknown app '${getAppName(componentName)}' has Accessibility access. This can be used to spy on your screen inputs.",
                componentName
            ))
        }

        return risks
    }

    /**
     * Scan installed apps for dangerous permission combinations
     */
    private fun scanInstalledApps(): List<SecurityRisk> {
        val risks = mutableListOf<SecurityRisk>()
        val pm = context.packageManager
        
        // Get all installed apps with their permissions
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        
        for (pkg in packages) {
            // Skip system apps
            if ((pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
                continue
            }
            
            // Skip Kavi
            if (pkg.packageName == context.packageName) {
                continue
            }

            val permissions = pkg.requestedPermissions ?: continue
            val permList = permissions.toList()

            val hasMic = permList.contains(PERM_MIC)
            val hasCamera = permList.contains(PERM_CAMERA)
            val hasOverlay = permList.contains(PERM_OVERLAY)
            val hasLocation = permList.contains(PERM_LOCATION)
            // Note checking for BIND_ACCESSIBILITY_SERVICE in requested permissions isn't enough as it must be granted in settings
            // but we check for the capability request here
            
            // Heuristic 1: Overlay + Mic (Spyware pattern)
            if (hasOverlay && hasMic) {
                risks.add(SecurityRisk(
                    RiskType.DANGEROUS_APP,
                    RiskSeverity.HIGH,
                    "App '${getAppName(pkg.packageName)}' can record audio AND draw over other apps. This is a common spyware pattern.",
                    pkg.packageName
                ))
            }

            // Heuristic 2: Mic + Camera + Background Location (Tracking pattern)
            if (hasMic && hasCamera && hasLocation) {
                risks.add(SecurityRisk(
                    RiskType.DANGEROUS_APP,
                    RiskSeverity.MEDIUM,
                    "App '${getAppName(pkg.packageName)}' has full surveillance access (Mic, Camera, Location). Review if this is necessary.",
                    pkg.packageName
                ))
            }
        }

        return risks
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
