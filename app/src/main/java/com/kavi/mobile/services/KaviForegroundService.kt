package com.kavi.mobile.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kavi.mobile.MainActivity
import com.kavi.mobile.R

class KaviForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "KaviServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var wakeWordDetector: com.kavi.mobile.wakeword.WakeWordDetector? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Acquire WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Kavi::VoiceLock").apply {
            acquire()
        }
        
        // Initialize Wake Word Detector
        wakeWordDetector = com.kavi.mobile.wakeword.WakeWordDetector(this) {
            onWakeWordDetected()
        }
        wakeWordDetector?.startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    
    private fun onWakeWordDetected() {
        // 1. Wake Screen
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Kavi::ScreenWake"
        )
        screenLock.acquire(3000) // Wake for 3 seconds
        
        // 2. Launch Main Activity (which handles UI and Speech Recognition)
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) 
            // Put extra to tell Activity to start listening immediately
            putExtra("AUTO_LISTEN", true)
        }
        startActivity(intent)
        
        // 3. Or trigger Overlay directly if preferred (The user guide mentions Activity showing up)
        // val overlayIntent = Intent(this, com.kavi.mobile.ui.overlay.KaviOverlayService::class.java)
        // overlayIntent.action = "EXPAND_OVERLAY"
        // startService(overlayIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordDetector?.stopListening()
        wakeLock?.release()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kavi Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Kavi running in the background"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.kavi_is_listening))
            .setContentText(getString(R.string.tap_to_open))
            .setSmallIcon(R.drawable.ic_kavi_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
