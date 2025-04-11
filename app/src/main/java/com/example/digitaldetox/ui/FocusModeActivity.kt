package com.example.digitaldetox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.ui.FocusModeSettingActivity

class FocusModeActivity : AppCompatActivity() {

    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.focus_mode_screen)

        btnStart = findViewById(R.id.btnStart)

        btnStart.setOnClickListener {
            // Go to Focus Settings screen
            val intent = Intent(this, FocusModeSettingActivity::class.java)
            startActivity(intent)
        }
    }
}
