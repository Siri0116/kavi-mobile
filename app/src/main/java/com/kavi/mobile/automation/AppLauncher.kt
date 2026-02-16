package com.kavi.mobile.automation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

/**
 * App Launcher - Launches apps by name
 */
class AppLauncher(private val context: Context) {

    companion object {
        private const val TAG = "AppLauncher"
        
        // Common app package mappings
        private val APP_PACKAGES = mapOf(
            "instagram" to "com.instagram.android",
            "whatsapp" to "com.whatsapp",
            "facebook" to "com.facebook.katana",
            "messenger" to "com.facebook.orca",
            "twitter" to "com.twitter.android",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "camera" to "com.android.camera",
            "gallery" to "com.google.android.apps.photos",
            "photos" to "com.google.android.apps.photos",
            "settings" to "com.android.settings",
            "calculator" to "com.android.calculator2",
            "calendar" to "com.google.android.calendar",
            "contacts" to "com.android.contacts",
            "phone" to "com.android.dialer",
            "messages" to "com.google.android.apps.messaging",
            "play store" to "com.android.vending",
            "spotify" to "com.spotify.music",
            "netflix" to "com.netflix.mediaclient",
            "amazon" to "com.amazon.mShop.android.shopping",
            "telegram" to "org.telegram.messenger",
            "snapchat" to "com.snapchat.android",
            "tiktok" to "com.zhiliaoapp.musically",
            "linkedin" to "com.linkedin.android"
        )
    }

    /**
     * Launch an app by name
     */
    fun launchApp(appName: String) {
        val normalizedName = appName.lowercase().trim()
        Log.d(TAG, "Attempting to launch: $normalizedName")

        // Try direct package mapping first
        val packageName = APP_PACKAGES[normalizedName]
        
        if (packageName != null) {
            launchByPackage(packageName, appName)
        } else {
            // Try fuzzy search through installed apps
            searchAndLaunch(normalizedName)
        }
    }

    private fun launchByPackage(packageName: String, appName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Toast.makeText(context, "Opening $appName", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Launched: $packageName")
            } else {
                Toast.makeText(context, "$appName is not installed", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "App not installed: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: $packageName", e)
            Toast.makeText(context, "Could not open $appName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchAndLaunch(appName: String) {
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            
            val apps = pm.queryIntentActivities(mainIntent, 0)
            
            // Search for matching app
            for (app in apps) {
                val label = app.loadLabel(pm).toString().lowercase()
                
                if (label.contains(appName) || appName.contains(label)) {
                    val packageName = app.activityInfo.packageName
                    launchByPackage(packageName, label)
                    return
                }
            }
            
            // No match found
            Toast.makeText(context, "App '$appName' not found", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "No matching app found for: $appName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching for app", e)
            Toast.makeText(context, "Could not find app", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Get list of all installed apps
     */
    fun getInstalledApps(): List<String> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val apps = pm.queryIntentActivities(mainIntent, 0)
        return apps.map { it.loadLabel(pm).toString() }
    }
}
