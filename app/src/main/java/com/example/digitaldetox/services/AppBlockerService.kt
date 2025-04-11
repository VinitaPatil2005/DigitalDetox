package com.example.digitaldetox.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent

class AppBlockerService : AccessibilityService() {

    private var blockedApps: Set<String> = emptySet()
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.digitaldetox.UPDATE_BLOCKED_APPS") {
                updateBlockedApps()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        updateBlockedApps()

        // Register for updates
//        registerReceiver(updateReceiver, IntentFilter("com.example.digitaldetox.UPDATE_BLOCKED_APPS"))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    private fun updateBlockedApps() {
        val prefs = getSharedPreferences("app_blocker_prefs", MODE_PRIVATE)
        blockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val currentApp = event.packageName?.toString() ?: return

            // Check if monitoring is enabled
            val prefs = getSharedPreferences("app_blocker_prefs", MODE_PRIVATE)
            val monitoringEnabled = prefs.getBoolean("monitoring_enabled", false)

            if (!monitoringEnabled) return

            if (blockedApps.contains(currentApp)) {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}