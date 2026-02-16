package com.kavi.mobile.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay

/**
 * Enhanced Kavi Accessibility Service
 * Provides advanced UI automation capabilities including:
 * - App closing
 * - App switching
 * - Notification reading
 * - Screen text extraction
 * - Button finding by text
 */
class KaviAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "KaviAccessibilityService"
        private var instance: KaviAccessibilityService? = null

        fun getInstance(): KaviAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Enhanced Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    handleNotification(it)
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    Log.d(TAG, "Window changed: ${it.packageName}")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    // ==================== APP CONTROL ====================

    /**
     * Close current app (go to home)
     */
    fun closeCurrentApp(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_HOME)
            Log.d(TAG, "Closed current app")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error closing app", e)
            false
        }
    }

    /**
     * Open recent apps (task switcher)
     */
    fun openRecentApps(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_RECENTS)
            Log.d(TAG, "Opened recent apps")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening recent apps", e)
            false
        }
    }

    /**
     * Go back
     */
    fun goBack(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_BACK)
            Log.d(TAG, "Went back")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error going back", e)
            false
        }
    }

    // ==================== NOTIFICATION HANDLING ====================

    private var lastNotification: String? = null
    private var lastNotificationTime: Long = 0

    private fun handleNotification(event: AccessibilityEvent) {
        try {
            val notification = event.text.joinToString(" ")
            if (notification.isNotEmpty()) {
                lastNotification = notification
                lastNotificationTime = System.currentTimeMillis()
                Log.d(TAG, "Notification: $notification")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling notification", e)
        }
    }

    /**
     * Get last notification text
     */
    fun getLastNotification(): String? {
        val timeSince = System.currentTimeMillis() - lastNotificationTime
        return if (timeSince < 60000) { // Within last minute
            lastNotification
        } else {
            null
        }
    }

    /**
     * Open notification shade
     */
    fun openNotifications(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            Log.d(TAG, "Opened notifications")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening notifications", e)
            false
        }
    }

    /**
     * Clear all notifications (requires notification listener)
     */
    fun clearAllNotifications(): Boolean {
        return try {
            // This requires NotificationListenerService
            // For now, just open notifications
            openNotifications()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing notifications", e)
            false
        }
    }

    // ==================== SCREEN TEXT EXTRACTION ====================

    /**
     * Extract all text from current screen
     */
    fun extractScreenText(): String {
        val rootNode = rootInActiveWindow ?: return ""
        val textBuilder = StringBuilder()
        
        extractTextRecursive(rootNode, textBuilder)
        rootNode.recycle()
        
        return textBuilder.toString()
    }

    private fun extractTextRecursive(node: AccessibilityNodeInfo, builder: StringBuilder) {
        // Add node text
        node.text?.let { text ->
            if (text.isNotEmpty()) {
                builder.append(text).append("\n")
            }
        }

        // Add content description
        node.contentDescription?.let { desc ->
            if (desc.isNotEmpty()) {
                builder.append(desc).append("\n")
            }
        }

        // Recursively process children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                extractTextRecursive(child, builder)
                child.recycle()
            }
        }
    }

    /**
     * Read screen aloud (for accessibility)
     */
    fun readScreen(): String {
        return extractScreenText()
    }

    // ==================== BUTTON FINDING ====================

    /**
     * Find button by text
     */
    fun findButtonByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val button = findNodeByText(rootNode, text, "android.widget.Button")
        rootNode.recycle()
        return button
    }

    /**
     * Find any node by text
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        val node = findNodeByText(rootNode, text, null)
        rootNode.recycle()
        return node
    }

    private fun findNodeByText(
        node: AccessibilityNodeInfo,
        text: String,
        className: String?
    ): AccessibilityNodeInfo? {
        // Check if this node matches
        val nodeText = node.text?.toString() ?: ""
        val nodeDesc = node.contentDescription?.toString() ?: ""
        val nodeClass = node.className?.toString() ?: ""

        val textMatches = nodeText.contains(text, ignoreCase = true) ||
                         nodeDesc.contains(text, ignoreCase = true)
        val classMatches = className == null || nodeClass == className

        if (textMatches && classMatches) {
            return node
        }

        // Search children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val found = findNodeByText(child, text, className)
                if (found != null) {
                    child.recycle()
                    return found
                }
                child.recycle()
            }
        }

        return null
    }

    /**
     * Click button by text
     */
    fun clickButtonByText(text: String): Boolean {
        val button = findButtonByText(text)
        return if (button != null) {
            val clicked = button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            button.recycle()
            Log.d(TAG, "Clicked button: $text - Success: $clicked")
            clicked
        } else {
            Log.w(TAG, "Button not found: $text")
            false
        }
    }

    /**
     * Click any element by text
     */
    fun clickByText(text: String): Boolean {
        val node = findNodeByText(text)
        return if (node != null) {
            val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            node.recycle()
            Log.d(TAG, "Clicked element: $text - Success: $clicked")
            clicked
        } else {
            Log.w(TAG, "Element not found: $text")
            false
        }
    }

    // ==================== ADVANCED GESTURES ====================

    /**
     * Perform swipe gesture
     */
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        return try {
            val path = Path()
            path.moveTo(startX, startY)
            path.lineTo(endX, endY)

            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))

            val gesture = gestureBuilder.build()
            dispatchGesture(gesture, null, null)
            
            Log.d(TAG, "Performed swipe gesture")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe", e)
            false
        }
    }

    /**
     * Scroll down
     */
    fun scrollDown(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val scrolled = rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        rootNode.recycle()
        return scrolled
    }

    /**
     * Scroll up
     */
    fun scrollUp(): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val scrolled = rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        rootNode.recycle()
        return scrolled
    }

    // ==================== TEXT INPUT ====================

    /**
     * Type text into focused field
     */
    fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focused = findFocusedNode(rootNode)
        
        return if (focused != null) {
            val typed = focused.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
            )
            focused.recycle()
            rootNode.recycle()
            Log.d(TAG, "Typed text: $text - Success: $typed")
            typed
        } else {
            rootNode.recycle()
            Log.w(TAG, "No focused field found")
            false
        }
    }

    private fun findFocusedNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused) {
            return node
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val focused = findFocusedNode(child)
                if (focused != null) {
                    child.recycle()
                    return focused
                }
                child.recycle()
            }
        }

        return null
    }

    // ==================== QUICK ACTIONS ====================

    /**
     * Open quick settings
     */
    fun openQuickSettings(): Boolean {
        return try {
            performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            Log.d(TAG, "Opened quick settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening quick settings", e)
            false
        }
    }

    /**
     * Take screenshot (Android 9+)
     */
    fun takeScreenshot(): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                Log.d(TAG, "Took screenshot")
                true
            } else {
                Log.w(TAG, "Screenshot not supported on this Android version")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error taking screenshot", e)
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Enhanced Accessibility Service Destroyed")
    }
}
