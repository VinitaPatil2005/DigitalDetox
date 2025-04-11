package com.example.digitaldetox

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.ui.PomodoroActivity
import com.example.digitaldetox.ui.TimetableActivity

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
        // Pomodoro Focus button
        findViewById<Button>(R.id.btnPomodoro).setOnClickListener {
            // Navigate to Pomodoro activity
            val intent = Intent(this, PomodoroActivity::class.java)
            startActivity(intent)
        }


        // Study Tips button
        findViewById<Button>(R.id.btnStudyTips).setOnClickListener {
            // Show a dialog with study tips
            showStudyTipsDialog()
        }
    }

    // Method to show a dialog for Mindful Breaks
    private fun showMindfulBreaksDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Mindful Breaks")
            .setMessage("Take a few minutes to relax, breathe, and reset your mind.")
            .setPositiveButton("Start Break") { _, _ ->
                Toast.makeText(this, "Break Started!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    // Method to show a dialog with study tips
    private fun showStudyTipsDialog() {
        val tips = listOf(
            "Break your study sessions into focused intervals (Pomodoro technique).",
            "Stay hydrated and take regular breaks.",
            "Use active recall and spaced repetition for better retention.",
            "Organize your study materials and environment for productivity."
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("Study Tips")
            .setItems(tips.toTypedArray()) { _, which ->
                Toast.makeText(this, "Tip: ${tips[which]}", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}
