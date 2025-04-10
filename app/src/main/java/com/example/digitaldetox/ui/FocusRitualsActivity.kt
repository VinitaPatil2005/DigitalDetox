package com.example.digitaldetox

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FocusRitualsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_rituals)

        val btnBreathing = findViewById<Button>(R.id.btnBreathing)
        val btnAmbient = findViewById<Button>(R.id.btnAmbient)
        val btnAffirmations = findViewById<Button>(R.id.btnAffirmations)
        val btnChecklist = findViewById<Button>(R.id.btnChecklist)

        btnBreathing.setOnClickListener {
            Toast.makeText(this, "Start a deep breathing session", Toast.LENGTH_SHORT).show()
        }

        btnAmbient.setOnClickListener {
            Toast.makeText(this, "Play ambient sounds", Toast.LENGTH_SHORT).show()
        }

        btnAffirmations.setOnClickListener {
            Toast.makeText(this, "Show positive affirmations", Toast.LENGTH_SHORT).show()
        }

        btnChecklist.setOnClickListener {
            Toast.makeText(this, "Open ritual preparation checklist", Toast.LENGTH_SHORT).show()
        }
    }
}
