package com.kavi.mobile.security.detectors

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Background Service Watcher
 * Lists apps with persistent foreground services or background processes.
 * Helps identify "unkillable" apps or hidden miners/monitors.
 */
class BackgroundServiceWatcher(private val context: Context) {

    companion object {
        private const val TAG = "BackgroundServiceWatcher"
    }

    /**
     * Get list of apps running foreground services
     * (These show a notification but might be hiding it or using a deceptive one)
     */
    fun getRunningServices(): List<String> {
        val runningApps = mutableListOf<String>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // getRunningServices is deprecated since API 26 (Oreo) and returns only self
        // effectively making "universal" service enumeration impossible for non-system apps
        // WITHOUT UsageStats permission or root.
        
        // HOWEVER, "getRunningAppProcesses" usually returns running apps (cached/foreground),
        // we can filter for IMPORTANCE_FOREGROUND_SERVICE.
        
        val processes = activityManager.runningAppProcesses ?: return emptyList()
        
        for (proc in processes) {
            if (proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE) {
                proc.pkgList.forEach { pkg ->
                    if (pkg != context.packageName && !isSystemPackage(pkg)) {
                        runningApps.add(pkg)
                    }
                }
            }
        }
        
        return runningApps.distinct()
    }

    private fun isSystemPackage(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            (info.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }
}
