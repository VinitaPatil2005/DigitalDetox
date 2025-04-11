package com.example.digitaldetox.ui

import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldetox.R
import com.example.digitaldetox.TimetableAdapter
import com.example.digitaldetox.TimetableEntry
import com.example.digitaldetox.services.StepCounterService
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimetableActivity : AppCompatActivity() {

    private lateinit var editDay: EditText
    private lateinit var editType: EditText
    private lateinit var editSubject: EditText
    private lateinit var editStartTime: EditText
    private lateinit var editEndTime: EditText
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimetableAdapter
    private lateinit var textStepGoalDisplay: TextView
    private val entries = mutableListOf<TimetableEntry>()

    // Step counter components
    private var stepReceiver: BroadcastReceiver? = null

    // Track if we have an active step counter challenge
    private var activeStepChallenge = false

    // Constants for step goal calculation
    private val STEPS_PER_MINUTE = 25 // Average steps per minute
    private val MIN_STEP_GOAL = 500 // Minimum step goal
    private val MAX_STEP_GOAL = 5000 // Maximum step goal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        editDay = findViewById(R.id.editDay)
        editType = findViewById(R.id.editType)
        editSubject = findViewById(R.id.editSubject)
        editStartTime = findViewById(R.id.editStartTime)
        editEndTime = findViewById(R.id.editEndTime)
        btnAdd = findViewById(R.id.btnAddToTimetable)
        recyclerView = findViewById(R.id.recyclerView)
        textStepGoalDisplay = findViewById(R.id.textStepGoalDisplay)

        adapter = TimetableAdapter(entries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fun showTimePicker(targetEditText: EditText) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                { _, selectedHour, selectedMinute ->
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)

                    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val formattedTime = format.format(calendar.time)

                    targetEditText.setText(formattedTime)

                    // Update step goal display if both times are set
                    if (editStartTime.text.isNotEmpty() && editEndTime.text.isNotEmpty()) {
                        updateStepGoalDisplay()
                    }
                },
                hour, minute, false // false for 12-hour format
            )
            timePickerDialog.show()
        }

        editStartTime.setOnClickListener {
            showTimePicker(editStartTime)
        }

        editEndTime.setOnClickListener {
            showTimePicker(editEndTime)
        }

        // Update step goal calculation when activity type changes
        editType.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editStartTime.text.isNotEmpty() && editEndTime.text.isNotEmpty()) {
                updateStepGoalDisplay()
            }
        }

        btnAdd.setOnClickListener {
            val day = editDay.text.toString().trim()
            val type = editType.text.toString().trim()
            val subject = editSubject.text.toString().trim()
            val startTime = editStartTime.text.toString().trim()
            val endTime = editEndTime.text.toString().trim()

            if (day.isNotEmpty() && type.isNotEmpty() && subject.isNotEmpty() &&
                startTime.isNotEmpty() && endTime.isNotEmpty()) {

                val time = "$startTime - $endTime"
                val newEntry = TimetableEntry(day, type, subject, time)
                entries.add(newEntry)
                adapter.notifyItemInserted(entries.size - 1)

                if (type.lowercase() != "break") {
                    showStyledToast("🔒 Apps will be blocked during this session!")

                    // Calculate time limit between start and end time
                    val timeLimit = calculateTimeLimit(startTime, endTime)

                    // Calculate step goal based on time limit
                    val stepGoal = calculateStepGoal(timeLimit, type)

                    // Mark that we have an active challenge
                    activeStepChallenge = true

                    // Start the step counter service
                    startStepCounterService(stepGoal, timeLimit)

                    showStyledToast("👟 Step goal of $stepGoal steps set! Complete within ${formatTimeLimit(timeLimit)} to earn coins.")
                } else {
                    showStyledToast("📱 Break time! Phone is allowed.")
                }

                // Clear fields after adding
                editDay.text.clear()
                editType.text.clear()
                editSubject.text.clear()
                editStartTime.text.clear()
                editEndTime.text.clear()
                textStepGoalDisplay.text = "👟 Steps will be calculated automatically"
            } else {
                showStyledToast("Please fill all fields")
            }
        }

        // Register receivers for step counter service broadcasts
        registerStepReceivers()
    }

    private fun updateStepGoalDisplay() {
        val startTime = editStartTime.text.toString().trim()
        val endTime = editEndTime.text.toString().trim()
        val type = editType.text.toString().trim().lowercase()

        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            val timeLimit = calculateTimeLimit(startTime, endTime)

            if (type == "break") {
                textStepGoalDisplay.text = "📱 Break time - No steps required"
            } else {
                val stepGoal = calculateStepGoal(timeLimit, type)
                textStepGoalDisplay.text = "👟 Step Goal: $stepGoal steps"
            }
        }
    }

    private fun calculateStepGoal(timeLimit: Long, type: String): Int {
        // Convert time limit from milliseconds to minutes
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLimit)

        // Base calculation: steps per minute multiplied by session duration
        var stepGoal = (minutes * STEPS_PER_MINUTE).toInt()

        // Adjust based on activity type
        when (type.lowercase()) {
            "study" -> stepGoal = (stepGoal * 0.8).toInt() // Slightly fewer steps for study sessions
            "activity" -> stepGoal = (stepGoal * 1.2).toInt() // More steps for activity sessions
        }

        // Ensure within reasonable limits
        return stepGoal.coerceIn(MIN_STEP_GOAL, MAX_STEP_GOAL)
    }

    private fun formatTimeLimit(timeInMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60

        return when {
            hours > 0 -> "$hours hr ${if (minutes > 0) "$minutes min" else ""}"
            minutes > 0 -> "$minutes min"
            else -> "less than a minute"
        }
    }

    private fun showStyledToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun calculateTimeLimit(startTime: String, endTime: String): Long {
        try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val startDate = format.parse(startTime)
            val endDate = format.parse(endTime)

            var timeDiff = endDate.time - startDate.time
            if (timeDiff < 0) {
                // Handle case when end time is on the next day
                timeDiff += TimeUnit.DAYS.toMillis(1)
            }

            return timeDiff
        } catch (e: Exception) {
            // Default to 2 hours if there's any parsing error
            return TimeUnit.HOURS.toMillis(2)
        }
    }

    private fun startStepCounterService(stepGoal: Int, timeLimit: Long) {
        Log.d("TimetableActivity", "Starting step counter service with goal: $stepGoal, timeLimit: ${timeLimit/1000}s")

        val serviceIntent = Intent(this, StepCounterService::class.java).apply {
            putExtra("GOAL_STEPS", stepGoal)
            putExtra("TIME_LIMIT_MS", timeLimit)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun registerStepReceivers() {
        // Unregister previous receiver if it exists
        stepReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) { /* Ignore */ }
        }

        stepReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("TimetableActivity", "Broadcast received: ${intent.action}")

                when (intent.action) {
                    "STEP_GOAL_COMPLETED" -> {
                        val coinsEarned = intent.getIntExtra("COINS_EARNED", 0)
                        val stepsCompleted = intent.getIntExtra("STEPS_COMPLETED", 0)

                        Log.d("TimetableActivity", "Step goal completed! Coins: $coinsEarned, Steps: $stepsCompleted")

                        if (activeStepChallenge) {
                            showRewardDialog(coinsEarned, stepsCompleted)
                            activeStepChallenge = false // Reset flag
                        }
                    }
                    "STEP_GOAL_FAILED" -> {
                        val stepsCompleted = intent.getIntExtra("STEPS_COMPLETED", 0)
                        val goalSteps = intent.getIntExtra("GOAL_STEPS", 0)
                        val partialCoins = intent.getIntExtra("PARTIAL_COINS", 0)

                        Log.d("TimetableActivity", "Step goal failed! Steps: $stepsCompleted/$goalSteps")

                        if (activeStepChallenge) {
                            showFailureDialog(stepsCompleted, goalSteps, partialCoins)
                            activeStepChallenge = false // Reset flag
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction("STEP_GOAL_COMPLETED")
            addAction("STEP_GOAL_FAILED")
        }
        registerReceiver(stepReceiver, filter)
        Log.d("TimetableActivity", "Step goal receivers registered")
    }

    private fun showRewardDialog(coins: Int, stepsCompleted: Int) {
        val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Congratulations! 🎉")
            .setMessage("You've completed your step goal with $stepsCompleted steps and earned $coins coins!")
            .setPositiveButton("Awesome") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()

        // Style dialog buttons to match theme
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.purple_200))

        // Update UI to reflect reward
        updateCoinsDisplay()
    }

    private fun showFailureDialog(stepsCompleted: Int, goalSteps: Int, partialCoins: Int) {
        val message = if (partialCoins > 0) {
            "You completed $stepsCompleted of $goalSteps steps. " +
                    "You still earned $partialCoins coins for your effort!"
        } else {
            "You only completed $stepsCompleted of $goalSteps steps. " +
                    "Try again to earn coins!"
        }

        val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Time's up! ⏰")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()

        // Style dialog buttons to match theme
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(this, R.color.purple_200))

        // Update UI if there were partial coins
        if (partialCoins > 0) {
            updateCoinsDisplay()
        }
    }

    private fun updateCoinsDisplay() {
        // Read current coins from SharedPreferences
        val prefs = getSharedPreferences("digital_detox_prefs", Context.MODE_PRIVATE)
        val currentCoins = prefs.getInt("user_coins", 0)

        // You could update a TextView here if you have one that shows the coins
        // For now, just show a toast with the current coins
        showStyledToast("Your current coins: $currentCoins 🪙")
    }

    override fun onResume() {
        super.onResume()
        // Re-register receiver just to be safe
        registerStepReceivers()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister receivers to prevent leaks
        stepReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) { /* Ignore */ }
        }
    }
}