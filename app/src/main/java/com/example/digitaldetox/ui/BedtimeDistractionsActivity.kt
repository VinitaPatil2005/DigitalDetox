package com.example.digitaldetox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R

import com.example.digitaldetox.ui.BedtimeModeActivity
// ✅ Make sure this line is added

class BedtimeDistractionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bedtime_distractions)

        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this, BedtimeModeActivity::class.java)
            startActivity(intent)
        }
    }
}
