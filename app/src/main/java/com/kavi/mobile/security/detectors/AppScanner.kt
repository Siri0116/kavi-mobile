package com.kavi.mobile.security.detectors

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log

/**
 * App Scanner - Monitors installed applications
 * Tracks:
 * - Newly installed apps
 * - Sideloaded apps (installer verification)
 * - App updates
 */
class AppScanner(private val context: Context) {

    companion object {
        private const val TAG = "AppScanner"
    }

    data class AppInfo(
        val packageName: String,
        val appName: String,
        val version: String,
        val isSystemApp: Boolean,
        val installSource: String?,
        val installTime: Long
    )

    /**
     * Get all installed apps
     */
    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()
        
        try {
            val packages = pm.getInstalledPackages(0)
            
            for (pkg in packages) {
                // Skip Kavi itself
                if (pkg.packageName == context.packageName) continue
                
                val isSystem = (pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                val installSource = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    try {
                        pm.getInstallSourceInfo(pkg.packageName).installingPackageName
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(pkg.packageName)
                }

                apps.add(AppInfo(
                    packageName = pkg.packageName,
                    appName = pm.getApplicationLabel(pkg.applicationInfo).toString(),
                    version = pkg.versionName ?: "unknown",
                    isSystemApp = isSystem,
                    installSource = installSource,
                    installTime = pkg.firstInstallTime
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning apps", e)
        }
        
        return apps
    }

    /**
     * Check if an app is sideloaded (not from Play Store)
     */
    fun isSideloaded(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val installer = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
            
            // Known safe installers
            val safeInstallers = listOf(
                "com.android.vending", // Google Play
                "com.google.android.packageinstaller", 
                "com.amazon.venezia"   // Amazon Appstore
            )
            
            installer == null || !safeInstallers.contains(installer)
        } catch (e: Exception) {
            true // Assume sideloaded on error
        }
    }
}
