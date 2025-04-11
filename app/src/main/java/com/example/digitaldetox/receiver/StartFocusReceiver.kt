package com.example.digitaldetox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.digitaldetox.ui.SettingsChangeDialogActivity
import com.example.digitaldetox.util.FocusModeManager

class StartFocusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Enable Focus Mode (or Bedtime Mode) via your shared FocusModeManager
        FocusModeManager.setFocusModeEnabled(context, true)

        // Launch the settings dialog activity to prompt the user to change settings
        val dialogIntent = Intent(context, SettingsChangeDialogActivity::class.java)
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(dialogIntent)

        Toast.makeText(context, "Focus Mode schedule started", Toast.LENGTH_SHORT).show()
    }
}
