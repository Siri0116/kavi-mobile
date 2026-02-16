package com.kavi.mobile.security.detectors

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log

/**
 * Overlay Watcher - Detects apps capable of drawing over others
 * This capability (SYSTEM_ALERT_WINDOW) is used for:
 * - Cloaking attacks (tapjacking)
 * - Fake login screens (phishing)
 * - Ransomware screens
 */
class OverlayWatcher(private val context: Context) {

    companion object {
        private const val TAG = "OverlayWatcher"
    }

    /**
     * Get list of apps with active overlay permission
     */
    fun getAppsWithOverlayPermission(): List<String> {
        val riskyApps = mutableListOf<String>()
        val pm = context.packageManager
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val packages = pm.getInstalledPackages(0)

        for (pkg in packages) {
            if (pkg.packageName == context.packageName) continue
            // Skip system apps? Often system apps have overlay, but some "system-like" malware might too.
            // Let's filter purely system signed for now to reduce noise.
            if ((pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            if (hasOverlayPermission(pkg.packageName, pkg.applicationInfo.uid)) {
                riskyApps.add(pkg.packageName)
            }
        }

        return riskyApps
    }

    private fun hasOverlayPermission(packageName: String, uid: Int): Boolean {
        return try {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW
            } else {
                "android:system_alert_window"
            }
            
            val result = context.getSystemService(AppOpsManager::class.java)
                .checkOpNoThrow(mode, uid, packageName)
                
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
}
