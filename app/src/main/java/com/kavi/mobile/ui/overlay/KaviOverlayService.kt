package com.kavi.mobile.ui.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.kavi.mobile.R

class KaviOverlayService : Service() {

    private var expandedView: View? = null
    private var isExpanded = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_SHOW_NOTIFICATION") {
            val title = intent.getStringExtra("TITLE") ?: "Kavi"
            val message = intent.getStringExtra("MESSAGE") ?: "You have a new suggestion."
            showNotification(title, message)
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showOrb()
    }
    
    // ==================== NOTIFICATIONS ====================
    
    private var notificationView: View? = null

    private fun showNotification(title: String, message: String) {
        if (notificationView != null) {
            windowManager?.removeView(notificationView)
            notificationView = null
        }

        notificationView = LayoutInflater.from(this).inflate(R.layout.overlay_notification, null)
        
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP
        layoutParams.y = 100 // Top offset
        
        // Setup Views
        val titleText = notificationView?.findViewById<android.widget.TextView>(R.id.notificationTitle)
        val messageText = notificationView?.findViewById<android.widget.TextView>(R.id.notificationMessage)
        val btnDismiss = notificationView?.findViewById<View>(R.id.btnDismiss)
        val btnAction = notificationView?.findViewById<View>(R.id.btnAction)
        
        titleText?.text = title
        messageText?.text = message
        
        btnDismiss?.setOnClickListener {
            closeNotification()
        }
        
        btnAction?.setOnClickListener {
            // TODO: Handle 'Do It' action (e.g., Open App, Set Alarm)
            closeNotification()
            expandOverlay() // Open main AI to confirm
        }
        
        // Auto-dismiss after 10 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            closeNotification()
        }, 10000)

        try {
            windowManager?.addView(notificationView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun closeNotification() {
        if (notificationView != null) {
            windowManager?.removeView(notificationView)
            notificationView = null
        }
    }

    private fun showOrb() {
        if (overlayView != null) return
        if (expandedView != null) {
            windowManager?.removeView(expandedView)
            expandedView = null
        }
        isExpanded = false

        // Inflate the layout
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_orb, null)
        orbImage = overlayView?.findViewById(R.id.orbImage)

        // Window Layout Params
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // Initial Position (Bottom Right, floating)
        layoutParams.gravity = Gravity.BOTTOM or Gravity.END
        layoutParams.x = 50
        layoutParams.y = 200

        // Drag to move listener
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (Math.abs(event.rawX - initialTouchX) > 10 || Math.abs(event.rawY - initialTouchY) > 10) {
                            isClick = false // It's a drag
                        }
                        layoutParams.x = initialX - (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY - (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(overlayView, layoutParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            expandOverlay()
                        }
                        return true
                    }
                }
                return false
            }
        })

        // Add to Window
        try {
            windowManager?.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun expandOverlay() {
        if (isExpanded) return
        
        // Remove Orb
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }

        // Inflate Expanded Layout
        expandedView = LayoutInflater.from(this).inflate(R.layout.overlay_expanded, null)
        isExpanded = true

        // Full Screen Layout Params
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.dimAmount = 0.5f // Dim background

        // Setup Interactivity
        val btnClose = expandedView?.findViewById<View>(R.id.btnClose)
        btnClose?.setOnClickListener {
            collapseToOrb()
        }
        
        // Microphone Button Interaction
        val btnMic = expandedView?.findViewById<View>(R.id.btnMic)
        btnMic?.setOnClickListener {
            toggleListeningState(btnMic)
        }
        
        // Security Button
        val btnSecurity = expandedView?.findViewById<View>(R.id.btnSecurity)
        btnSecurity?.setOnClickListener {
            showSecurityDashboard()
        }
        
        // Close if tapping outside card (scrim)
        expandedView?.setOnClickListener {
             collapseToOrb()
        }

        // Add to Window
        try {
            windowManager?.addView(expandedView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // ==================== SECURITY DASHBOARD ====================
    
    private var securityView: View? = null
    
    private fun showSecurityDashboard() {
        // Remove conversational view temporarily or just stack on top?
        // Let's stack on top for "Pop up" feel, but first hide expanded to avoid clutter
        if (expandedView != null) {
            windowManager?.removeView(expandedView)
            expandedView = null
            isExpanded = false
        }
        
        securityView = LayoutInflater.from(this).inflate(R.layout.overlay_security, null)
        
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.dimAmount = 0.7f
        
        // Setup Logic
        val btnRunScan = securityView?.findViewById<View>(R.id.btnRunScan)
        val progress = securityView?.findViewById<android.widget.ProgressBar>(R.id.riskMeterProgress)
        val riskText = securityView?.findViewById<android.widget.TextView>(R.id.riskLevelText)
        
        btnRunScan?.setOnClickListener {
            // Mock Scan Animation
            riskText?.text = "SCANNING..."
            progress?.indeterminateTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.YELLOW)
            progress?.isIndeterminate = true
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                progress?.isIndeterminate = false
                progress?.progress = 0
                progress?.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GREEN)
                riskText?.text = "SECURE"
                riskText?.setTextColor(android.graphics.Color.GREEN)
            }, 2000)
        }
        
        val btnClose = securityView?.findViewById<View>(R.id.btnCloseSecurity)
        btnClose?.setOnClickListener {
            closeSecurityDashboard()
        }
        
        try {
            windowManager?.addView(securityView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun closeSecurityDashboard() {
        if (securityView != null) {
            windowManager?.removeView(securityView)
            securityView = null
        }
        // Return to Orb
        showOrb()
    }

    private var isListening = false

    private fun toggleListeningState(micButton: View) {
        val agentText = expandedView?.findViewById<android.widget.TextView>(R.id.agentResponseText)
        val userText = expandedView?.findViewById<android.widget.TextView>(R.id.userInputText)
        
        if (!isListening) {
            // Start Listening Animation
            isListening = true
            val pulseAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim_pulse)
            micButton.startAnimation(pulseAnim)
            
            agentText?.text = "Listening..."
            userText?.visibility = View.GONE
            
            // Simulating speech input ending after 3 seconds (Mock for now)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                stopListeningState(micButton)
            }, 3000)
            
        } else {
            // Stop manually
            stopListeningState(micButton)
        }
    }

    private fun stopListeningState(micButton: View) {
        isListening = false
        micButton.clearAnimation()
        
        val agentText = expandedView?.findViewById<android.widget.TextView>(R.id.agentResponseText)
        val userText = expandedView?.findViewById<android.widget.TextView>(R.id.userInputText)
        
        agentText?.text = "Processing..."
        
        // Mock processing delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
             agentText?.text = "Here is what I found."
             userText?.text = "Open YouTube"
             userText?.visibility = View.VISIBLE
        }, 1500)
    }

    private fun collapseToOrb() {
        if (!isExpanded) return
        
        // Remove Expanded
        if (expandedView != null) {
            windowManager?.removeView(expandedView)
            expandedView = null
        }
        isExpanded = false
        
        // Show Orb again
        showOrb()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) windowManager?.removeView(overlayView)
        if (expandedView != null) windowManager?.removeView(expandedView)
    }
}
