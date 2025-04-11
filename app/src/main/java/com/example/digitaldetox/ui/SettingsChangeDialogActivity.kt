package com.example.digitaldetox.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsChangeDialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show a dialog immediately with no layout.
        AlertDialog.Builder(this)
            .setTitle("Change Settings")
            .setMessage("Your scheduled Focus Mode has started. Please review and update your settings to ensure all apps get blocked. Tap OK to open Accessibility Settings.")
            .setCancelable(false)
            .setPositiveButton("Open Settings") { _, _ ->
                // Launch the system's accessibility settings (or any other settings)
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
}


