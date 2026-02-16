package com.kavi.mobile.workers

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kavi.mobile.ui.overlay.KaviOverlayService

class WindDownWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Trigger the Overlay Service to show the notification
        val intent = Intent(applicationContext, KaviOverlayService::class.java)
        intent.action = "ACTION_SHOW_NOTIFICATION"
        intent.putExtra("TITLE", "Wind Down Time")
        intent.putExtra("MESSAGE", "It's getting late. Would you like to set an alarm or enable Do Not Disturb?")
        applicationContext.startService(intent)
        
        return Result.success()
    }
}
