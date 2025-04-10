package com.example.digitaldetox

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvScreenTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvInstagramTime: TextView
    private lateinit var tvYouTubeTime: TextView
    private lateinit var tvWhatsAppTime: TextView
    private lateinit var motivationalQuote: TextView
    private lateinit var usageBarContainer: LinearLayout
    private lateinit var usageDetailsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views first
        initializeViews()

        // Then set up everything else
        setupClickListeners()
        setupBottomNavigation()

        // Setup usage stats last since it depends on views being initialized
        setupUsageStats()
    }

    private fun initializeViews() {
        try {
//            tvScreenTime = findViewById(R.id.tvScreenTime)
            tvTotalTime = findViewById(R.id.tvTotalTime)
            tvInstagramTime = findViewById(R.id.tvInstagramTime)
//            tvYouTubeTime = findViewById(R.id.tvYouTubeTime)
//            tvWhatsAppTime = findViewById(R.id.tvWhatsAppTime)
            motivationalQuote = findViewById(R.id.tvMotivationalQuote)
            usageBarContainer = findViewById(R.id.usageBarContainer)
            usageDetailsContainer = findViewById(R.id.usageDetailsContainer)
        } catch (e: Exception) {
            // Log any view initialization errors
            e.printStackTrace()
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUsageStats() {
        try {
            // Get and display total screen time
            val screenTime = getScreenTimeToday()
            tvTotalTime.text = screenTime
//            tvScreenTime.text = "Today's Screen Time:"

            // Populate usage statistics
            populateUsageBarAndDetails()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up usage stats", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        try {
            // Tracking buttons
            findViewById<Button>(R.id.btnDailyTracking)?.setOnClickListener {
                Toast.makeText(this, "Daily Tracking clicked", Toast.LENGTH_SHORT).show()
            }

            findViewById<Button>(R.id.btnWeeklyTracking)?.setOnClickListener {
                Toast.makeText(this, "Weekly Tracking clicked", Toast.LENGTH_SHORT).show()
            }

            // Motivational quote
            motivationalQuote.setOnClickListener {
                val intent = Intent(this, StudentOptionsActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up click listeners", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        try {
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_focus_mode -> {
                        Toast.makeText(this, "Focus Mode clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_set_limits -> {
                        Toast.makeText(this, "Set Limits clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_profile -> {
                        Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_chatbot -> {
                        val intent = Intent(this, ChatbotActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up bottom navigation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getScreenTimeToday(): String {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (1000 * 60 * 60 * 24) // Last 24 hours

            val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            val totalTime = usageStats.sumOf { it.totalTimeInForeground }

            val hours = (totalTime / (1000 * 60 * 60))
            val minutes = (totalTime / (1000 * 60)) % 60
            "${hours}h ${minutes}m"
        } catch (e: Exception) {
            e.printStackTrace()
            "0h 0m"
        }
    }

    private fun populateUsageBarAndDetails() {
        try {
            val appUsageStats = getAppUsageStats()
            val totalScreenTime = appUsageStats.sumOf { it.second }

            // Clear existing views
            usageBarContainer.removeAllViews()
            usageDetailsContainer.removeAllViews()

            // Split into top 10 and others
            val top10Apps = appUsageStats.take(10)
            val otherApps = appUsageStats.drop(10)

            // Calculate total usage time for "Others" category
            val othersUsageTime = otherApps.sumOf { it.second }

            // Combined list with top 10 apps and "Others" category
            val displayList = if (othersUsageTime > 0) {
                top10Apps + Pair("Others", othersUsageTime)
            } else {
                top10Apps
            }

//            // Update specific app TextViews with real data if available
//            displayList.take(3).forEachIndexed { index, (appName, usageTime) ->
//                when (index) {
//                    0 -> tvInstagramTime.text = "$appName: ${formatUsageTime(usageTime)}"
//                    1 -> tvYouTubeTime.text = "$appName: ${formatUsageTime(usageTime)}"
//                    2 -> tvWhatsAppTime.text = "$appName: ${formatUsageTime(usageTime)}"
//                }
//            }

            // Add progress bars for each app in the display list
            displayList.forEach { (appName, usageTime) ->
                // Create progress bar segment
                val weight = usageTime.toFloat() / totalScreenTime.toFloat()
                val progressBarSegment = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                    setBackgroundColor(getColorForApp(appName))
                }
                usageBarContainer.addView(progressBarSegment)

                // Add detailed usage stats
                val appUsageText = TextView(this).apply {
                    text = "$appName: ${formatUsageTime(usageTime)} (${(weight * 100).toInt()}%)"
                    setPadding(8)
                    setTextColor(Color.WHITE)
                }
                usageDetailsContainer.addView(appUsageText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error populating usage statistics", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAppUsageStats(): List<Pair<String, Long>> {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (1000 * 60 * 60 * 24)

            val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            usageStats
                .filter { it.totalTimeInForeground > 0 }
                .map { Pair(getAppNameFromPackage(it.packageName), it.totalTimeInForeground) }
                .sortedByDescending { it.second }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun formatUsageTime(usageTime: Long): String {
        val hours = usageTime / (1000 * 60 * 60)
        val minutes = (usageTime / (1000 * 60)) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private fun getColorForApp(appName: String): Int {
        return try {
            // Use consistent colors for common apps
            when (appName) {
                "Instagram" -> Color.parseColor("#C13584") // Instagram pink/purple
                "YouTube" -> Color.parseColor("#FF0000") // YouTube red
                "WhatsApp" -> Color.parseColor("#25D366") // WhatsApp green
                "Facebook" -> Color.parseColor("#1877F2") // Facebook blue
                "TikTok" -> Color.parseColor("#000000") // TikTok black
                "Twitter" -> Color.parseColor("#1DA1F2") // Twitter blue
                "Snapchat" -> Color.parseColor("#FFFC00") // Snapchat yellow
                "Chrome" -> Color.parseColor("#4285F4") // Chrome blue
                "Others" -> Color.parseColor("#888888") // Gray for others
                else -> getRandomColor() // Random color for other apps
            }
        } catch (e: Exception) {
            getRandomColor()
        }
    }

    private fun getRandomColor(): Int {
        return try {
            val random = Random()
            Color.rgb(
                100 + random.nextInt(156), // Avoid too dark colors
                100 + random.nextInt(156),
                100 + random.nextInt(156)
            )
        } catch (e: Exception) {
            Color.GRAY
        }
    }
}