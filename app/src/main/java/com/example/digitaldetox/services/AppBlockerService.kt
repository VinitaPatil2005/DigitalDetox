package com.example.digitaldetox.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AppBlockerService : AccessibilityService() {
    private val TAG = "AppBlockerService"
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

        // Register for updates with LocalBroadcastManager (more reliable)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            updateReceiver,
            IntentFilter("com.example.digitaldetox.UPDATE_BLOCKED_APPS")
        )

        // Also register for global broadcasts as a fallback
        registerReceiver(updateReceiver, IntentFilter("com.example.digitaldetox.UPDATE_BLOCKED_APPS"))

        Log.d(TAG, "AppBlockerService created and receiver registered")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Unregister both receivers
            LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

    private fun updateBlockedApps() {
        val prefs = getSharedPreferences("app_blocker_prefs", MODE_PRIVATE)
        val newBlockedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
        blockedApps = newBlockedApps
        Log.d(TAG, "Updated blocked apps: ${blockedApps.joinToString()}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val currentApp = event.packageName?.toString() ?: return

            // Check if monitoring is enabled
            val prefs = getSharedPreferences("app_blocker_prefs", MODE_PRIVATE)
            val monitoringEnabled = prefs.getBoolean("monitoring_enabled", false)

            if (!monitoringEnabled) {
                Log.d(TAG, "Monitoring disabled, ignoring events")
                return
            }

            if (blockedApps.contains(currentApp)) {
                Log.d(TAG, "Blocking app: $currentApp")
                // Force redirect to home screen
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
        // Re-load blocked apps when service connects
        updateBlockedApps()
    }
}