package com.example.digitaldetox

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var tvScreenTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvScreenTime = findViewById(R.id.tvScreenTime)
        val btnFocusMode = findViewById<Button>(R.id.btnFocusMode)
        val btnAppLimits = findViewById<Button>(R.id.btnAppLimits)

        tvScreenTime.text = "Today's Screen Time: ${getScreenTimeToday()}"

        btnFocusMode.setOnClickListener {
            // TODO: Start Focus Mode Activity
        }

        btnAppLimits.setOnClickListener {
            // TODO: Start App Limits Activity
        }
    }

    private fun getScreenTimeToday(): String {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24)

        val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        val totalTime = usageStats.sumOf { it.totalTimeInForeground }

        val hours = (totalTime / (1000 * 60 * 60))
        val minutes = (totalTime / (1000 * 60)) % 60
        return "$hours hr $minutes min"
    }
}
