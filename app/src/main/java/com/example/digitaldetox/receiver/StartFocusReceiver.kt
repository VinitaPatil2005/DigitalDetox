package com.example.digitaldetox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.digitaldetox.ui.SettingsChangeDialogActivity
import com.example.digitaldetox.util.FocusModeManager

class StartFocusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Enable Focus Mode
        FocusModeManager.setFocusModeEnabled(context, true)

        // Launch the dialog activity
        val dialogIntent = Intent(context, SettingsChangeDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(dialogIntent)

        Toast.makeText(context, "Focus Mode schedule started", Toast.LENGTH_SHORT).show()
    }
}