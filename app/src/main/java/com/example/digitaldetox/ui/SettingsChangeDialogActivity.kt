package com.example.digitaldetox.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsChangeDialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this)
            .setTitle("Focus Mode Started")
            .setMessage("Your scheduled Focus Mode has started. Tap OK to review settings.")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                // Open settings without finishing this activity immediately
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                // Optional: delay finish slightly to avoid auto return
                window.decorView.postDelayed({ finish() }, 500)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
}
