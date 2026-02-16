package com.kavi.mobile.automation

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager

/**
 * SystemController - Controls Android system settings
 * Handles silent mode, volume, brightness, notifications, etc.
 */
class SystemController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val TAG = "SystemController"
    }

    /**
     * Battery information
     */
    data class BatteryInfo(
        val level: Int,
        val isCharging: Boolean,
        val temperature: Float,
        val voltage: Int
    )

    // ==================== AUDIO CONTROLS ====================

    /**
     * Set silent mode
     */
    fun setSilentMode(enabled: Boolean): Boolean {
        return try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val ringerMode = if (enabled) {
                    AudioManager.RINGER_MODE_SILENT
                } else {
                    AudioManager.RINGER_MODE_NORMAL
                }
                audioManager.ringerMode = ringerMode
                Log.d(TAG, "Silent mode ${if (enabled) "enabled" else "disabled"}")
                true
            } else {
                Log.w(TAG, "Do Not Disturb permission not granted")
                openDoNotDisturbSettings()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting silent mode", e)
            false
        }
    }

    /**
     * Set vibrate mode
     */
    fun setVibrateMode(enabled: Boolean): Boolean {
        return try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val ringerMode = if (enabled) {
                    AudioManager.RINGER_MODE_VIBRATE
                } else {
                    AudioManager.RINGER_MODE_NORMAL
                }
                audioManager.ringerMode = ringerMode
                Log.d(TAG, "Vibrate mode ${if (enabled) "enabled" else "disabled"}")
                true
            } else {
                openDoNotDisturbSettings()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting vibrate mode", e)
            false
        }
    }

    /**
     * Set volume level (0-100)
     */
    fun setVolume(level: Int, streamType: Int = AudioManager.STREAM_MUSIC): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val targetVolume = (level.coerceIn(0, 100) * maxVolume) / 100
            audioManager.setStreamVolume(streamType, targetVolume, 0)
            Log.d(TAG, "Volume set to $level%")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
            false
        }
    }

    /**
     * Get current volume level (0-100)
     */
    fun getVolume(streamType: Int = AudioManager.STREAM_MUSIC): Int {
        val currentVolume = audioManager.getStreamVolume(streamType)
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        return (currentVolume * 100) / maxVolume
    }

    /**
     * Mute/unmute
     */
    fun setMute(muted: Boolean, streamType: Int = AudioManager.STREAM_MUSIC) {
        audioManager.adjustStreamVolume(
            streamType,
            if (muted) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
            0
        )
    }

    // ==================== DISPLAY CONTROLS ====================

    /**
     * Set brightness (0-100)
     * Note: Requires WRITE_SETTINGS permission
     */
    fun setBrightness(level: Int): Boolean {
        return try {
            if (Settings.System.canWrite(context)) {
                val brightness = level.coerceIn(0, 100) * 255 / 100
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness
                )
                Log.d(TAG, "Brightness set to $level%")
                true
            } else {
                Log.w(TAG, "WRITE_SETTINGS permission not granted")
                openWriteSettingsPermission()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting brightness", e)
            false
        }
    }

    /**
     * Get current brightness (0-100)
     */
    fun getBrightness(): Int {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            (brightness * 100) / 255
        } catch (e: Exception) {
            Log.e(TAG, "Error getting brightness", e)
            50 // Default
        }
    }

    // ==================== CONNECTIVITY ====================

    /**
     * Open WiFi settings (can't toggle directly without root)
     */
    fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Log.d(TAG, "Opened WiFi settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WiFi settings", e)
        }
    }

    /**
     * Open Bluetooth settings (can't toggle directly without root)
     */
    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Log.d(TAG, "Opened Bluetooth settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Bluetooth settings", e)
        }
    }

    /**
     * Open airplane mode settings
     */
    fun openAirplaneModeSettings() {
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Log.d(TAG, "Opened airplane mode settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening airplane mode settings", e)
        }
    }

    // ==================== NOTIFICATIONS ====================

    /**
     * Clear all notifications (requires notification listener service)
     */
    fun clearAllNotifications(): Boolean {
        return try {
            // This requires NotificationListenerService
            // For now, just open notification settings
            openNotificationSettings()
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing notifications", e)
            false
        }
    }

    // ==================== BATTERY ====================

    /**
     * Get battery status
     */
    fun getBatteryStatus(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        val temperature = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_TEMPERATURE) / 10f
        val voltage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE)

        return BatteryInfo(level, isCharging, temperature, voltage)
    }

    /**
     * Enable battery saver mode (opens settings)
     */
    fun enableBatterySaver() {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Log.d(TAG, "Opened battery saver settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening battery saver", e)
        }
    }

    // ==================== HELPER METHODS ====================

    private fun openDoNotDisturbSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening DND settings", e)
        }
    }

    private fun openWriteSettingsPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening write settings permission", e)
        }
    }

    private fun openNotificationSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening notification settings", e)
        }
    }

    /**
     * Check if has Do Not Disturb permission
     */
    fun hasDoNotDisturbPermission(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Check if has write settings permission
     */
    fun hasWriteSettingsPermission(): Boolean {
        return Settings.System.canWrite(context)
    }
}
