package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.digitaldetox.ui.BlockScreenActivity
import com.example.digitaldetox.util.FocusModeManager

class FocusModeAccessibilityService : AccessibilityService() {

    private val allowedApps = listOf(
        "com.android.incallui",
        "com.google.android.dialer"
    )

    private var isBlockScreenVisible = false
    private var lastBlockedPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!FocusModeManager.isFocusModeEnabled(this)) {
            isBlockScreenVisible = false
            return
        }

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Log.d("FocusService", "Opened: $packageName")

            // Block only if not in allowed apps
            if (!allowedApps.contains(packageName)) {
                // Prevent launching multiple times
                if (!isBlockScreenVisible || lastBlockedPackage != packageName) {
                    val intent = Intent(this, BlockScreenActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    isBlockScreenVisible = true
                    lastBlockedPackage = packageName
                }
            } else {
                // Reset if user opens an allowed app
                isBlockScreenVisible = false
            }
        }
    }

    override fun onInterrupt() {}
}
