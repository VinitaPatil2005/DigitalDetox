package com.example.digitaldetox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.digitaldetox.util.FocusModeManager

class StopFocusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        FocusModeManager.setFocusModeEnabled(context, false)

        Toast.makeText(context, "Bedtime Mode Ended!", Toast.LENGTH_SHORT).show()
    }
}
