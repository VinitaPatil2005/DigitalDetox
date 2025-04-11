package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.digitaldetox.ui.BlockScreenActivity
import com.example.digitaldetox.util.FocusModeManager

class FocusModeAccessibilityService : AccessibilityService() {

    private val allowedApps = listOf(
        "com.android.incallui", // Phone
        "com.google.android.dialer"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!FocusModeManager.isFocusModeEnabled(this)) return

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d("FocusService", "Opened: $packageName")

            if (!allowedApps.contains(packageName)) {
                val intent = Intent(this, BlockScreenActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
