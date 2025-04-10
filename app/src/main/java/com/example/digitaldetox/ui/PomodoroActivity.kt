package com.example.digitaldetox.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R

class PomodoroActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var startButton: Button
    private var isTimerRunning = false
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 25 * 60 * 1000  // Default: 25 minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        // Find views
        timerText = findViewById(R.id.timerText)
        startButton = findViewById(R.id.startButton)

        // Set the initial time on the timer
        updateTimerText()

        // Start button click listener
        startButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
    }

    // Function to start the timer
    private fun startTimer() {
        isTimerRunning = true
        startButton.text = "Pause"

        // Start a countdown timer
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                // When timer finishes, reset the timer for next Pomodoro session
                startButton.text = "Start"
                isTimerRunning = false
                timeLeftInMillis = 25 * 60 * 1000  // Reset to 25 minutes
                updateTimerText()
            }
        }.start()
    }

    // Function to pause the timer
    private fun pauseTimer() {
        isTimerRunning = false
        startButton.text = "Resume"
        countDownTimer?.cancel()
    }

    // Function to update the time display
    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60

        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerText.text = timeFormatted
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()  // Clean up the timer if the activity is destroyed
    }
}
