package com.example.digitaldetox.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R
import java.util.*
import android.widget.Button
import com.example.digitaldetox.receiver.StartFocusReceiver
class BedtimeModeActivity : AppCompatActivity() {

    private lateinit var startTimeText: TextView
    private lateinit var endTimeText: TextView

    private var startTimeMillis: Long = 0
    private var endTimeMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bedtime_mode) // make sure this layout exists

        startTimeText = findViewById(R.id.start_time)
        endTimeText = findViewById(R.id.end_time)

        startTimeText.setOnClickListener {
            checkExactAlarmPermission {
                pickTime(isStart = true)
            }
        }
        val turnOnNowButton = findViewById<Button>(R.id.turn_on_now)
        turnOnNowButton.setOnClickListener {
            // Show same settings change dialog as in schedule
            showSettingsDialog()
        }


        endTimeText.setOnClickListener {
            pickTime(isStart = false)
        }
    }

    private fun pickTime(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            if (isStart) {
                startTimeText.text = "Start Time: $formattedTime"
                startTimeMillis = selectedCalendar.timeInMillis
            } else {
                endTimeText.text = "End Time: $formattedTime"
                endTimeMillis = selectedCalendar.timeInMillis
            }

            if (startTimeMillis > 0 && endTimeMillis > 0) {
                scheduleBedtimeMode(startTimeMillis, endTimeMillis)
                Toast.makeText(this, "Bedtime Mode Scheduled!", Toast.LENGTH_SHORT).show()
            }
        }, hour, minute, false).show()
    }
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Allow permissions")
            .setMessage("Please allow required settings like grayscale and accessibility to enable Bedtime Mode.")
            .setPositiveButton("Go to settings") { dialog, _ ->
                // Navigate to accessibility settings
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                dialog.dismiss()

                // Optional: Also start bedtime logic immediately
                val intent = Intent(this, StartFocusReceiver::class.java)
                sendBroadcast(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun scheduleBedtimeMode(startMillis: Long, endMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startIntent = Intent(this, com.example.digitaldetox.receiver.StartFocusReceiver::class.java)
        val startPendingIntent = PendingIntent.getBroadcast(
            this, 1001, startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startMillis, startPendingIntent)

        val endIntent = Intent(this, com.example.digitaldetox.receiver.StopFocusReceiver::class.java)
        val endPendingIntent = PendingIntent.getBroadcast(
            this, 1002, endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, endMillis, endPendingIntent)
    }

    private fun checkExactAlarmPermission(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                onGranted()
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                Toast.makeText(this, "Please allow exact alarm permission", Toast.LENGTH_LONG).show()
            }
        } else {
            onGranted()
        }
    }
}
