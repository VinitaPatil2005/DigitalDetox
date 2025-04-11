package com.example.digitaldetox.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.util.Log
import com.example.digitaldetox.ui.BlockScreenActivity

class AppBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        val prefs = getSharedPreferences("app_blocker_prefs", MODE_PRIVATE)
        val monitoring = prefs.getBoolean("monitoring_enabled", false)
        if (!monitoring) return

        // List of allowed calls apps (update these package names as needed)
        val allowedCallsApps = listOf(
            "com.android.incallui",
            "com.google.android.dialer",
            "com.android.dialer"
        )

        Log.d("AppBlockerService", "Foreground app: $packageName")

        if (!allowedCallsApps.contains(packageName)) {
            val blockIntent = Intent(this, BlockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(blockIntent)
        }
    }

    override fun onInterrupt() {
        // This method must be implemented. You can log or leave empty.
        Log.d("AppBlockerService", "Accessibility service interrupted")
    }
}
