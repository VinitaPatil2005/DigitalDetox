package com.example.digitaldetox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudentOptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_options)

        // Timetable button
        findViewById<Button>(R.id.btnDesignTimetable).setOnClickListener {
            val intent = Intent(this, TimetableActivity::class.java)
            startActivity(intent)
        }

        // Focus Rituals button
        findViewById<Button>(R.id.btnFocusRituals).setOnClickListener {
            val intent = Intent(this, FocusRitualsActivity::class.java)
            startActivity(intent)
        }

        // Other buttons with toasts
        findViewById<Button>(R.id.btnPomodoro).setOnClickListener {
            Toast.makeText(this, "Pomodoro Focus Clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnMindfulBreaks).setOnClickListener {
            Toast.makeText(this, "Mindful Breaks Clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStudyJournal).setOnClickListener {
            Toast.makeText(this, "Study Journal Clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStudyTips).setOnClickListener {
            Toast.makeText(this, "Study Tips Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
