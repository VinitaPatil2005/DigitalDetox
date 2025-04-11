package com.example.digitaldetox

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.example.digitaldetox.ui.ChallengesActivity
import com.example.digitaldetox.ui.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import com.example.digitaldetox.ui.BedtimeDistractionsActivity


class MainActivity : AppCompatActivity() {

    private lateinit var tvScreenTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvInstagramTime: TextView
    private lateinit var tvYouTubeTime: TextView
    private lateinit var tvWhatsAppTime: TextView
    private lateinit var motivationalQuote: TextView
    private lateinit var usageBarContainer: LinearLayout
    private lateinit var usageDetailsContainer: LinearLayout
    private lateinit var btnShowMore: Button
    private var showingAllApps = false
    private var appUsageStats: List<Pair<String, Long>> = emptyList()
    private lateinit var gamificationCard: LinearLayout

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

        // Set sample screen times for specific apps
        tvScreenTime.text = "Today's Screen Time: 4 hr 30 min"
        tvInstagramTime.text = "Instagram: 2 hr"
        tvYouTubeTime.text = "YouTube: 1 hr 30 min"
        tvWhatsAppTime.text = "WhatsApp: 1 hr"
    }

    private fun initializeViews() {
        try {
            tvScreenTime = findViewById(R.id.tvScreenTime)
            tvTotalTime = findViewById(R.id.tvTotalTime)
            tvInstagramTime = findViewById(R.id.tvInstagramTime)
            tvYouTubeTime = findViewById(R.id.tvYouTubeTime)
            tvWhatsAppTime = findViewById(R.id.tvWhatsAppTime)
            motivationalQuote = findViewById(R.id.tvMotivationalQuote)
            usageBarContainer = findViewById(R.id.usageBarContainer)
            usageDetailsContainer = findViewById(R.id.usageDetailsContainer)
            btnShowMore = findViewById(R.id.btnShowMore)
            gamificationCard = findViewById(R.id.gamificationCard)
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

            // Get app usage stats
            appUsageStats = getAppUsageStats()

            // Populate usage statistics with only top 2 apps initially
            populateUsageBarAndDetails(2)
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
            findViewById<TextView>(R.id.tvParentalControl)?.setOnClickListener {
                val intent = Intent(this@MainActivity, BedtimeDistractionsActivity::class.java)
                startActivity(intent)
            }

            findViewById<Button>(R.id.btnWeeklyTracking)?.setOnClickListener {
                Toast.makeText(this, "Weekly Tracking clicked", Toast.LENGTH_SHORT).show()
            }

            // Show More button
            btnShowMore.setOnClickListener {
                showingAllApps = !showingAllApps
                if (showingAllApps) {
                    populateUsageBarAndDetails(5)
                    btnShowMore.text = "Show Less"
                } else {
                    populateUsageBarAndDetails(2)
                    btnShowMore.text = "Show More"
                }
            }

            // Motivational quote
            motivationalQuote.setOnClickListener {
                val intent = Intent(this, StudentOptionsActivity::class.java)
                startActivity(intent)
            }

            // Gamification card
            gamificationCard.setOnClickListener {
                val intent = Intent(this, ChallengesActivity::class.java)
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
                        val intent = Intent(this, FocusModeActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_set_limits -> {
                        val intent = Intent(this, SetLimitsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.parental_control -> {
                        Toast.makeText(this, "Parental Control clicked", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.nav_profile -> {
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
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

    private fun populateUsageBarAndDetails(numAppsToShow: Int) {
        try {
            val totalScreenTime = appUsageStats.sumOf { it.second }

            // Clear existing views
            usageBarContainer.removeAllViews()
            usageDetailsContainer.removeAllViews()

            // Split into top N and others
            val topApps = appUsageStats.take(numAppsToShow)
            val otherApps = appUsageStats.drop(numAppsToShow)

            // Calculate total usage time for "Others" category
            val othersUsageTime = otherApps.sumOf { it.second }

            // Combined list with top N apps and "Others" category
            val displayList = if (othersUsageTime > 0) {
                topApps + Pair("Others", othersUsageTime)
            } else {
                topApps
            }

            // Add progress bars for each app in the display list
            displayList.forEach { (appName, usageTime) ->
                // Create progress bar segment
                val weight = usageTime.toFloat() / totalScreenTime.toFloat()
                val progressBarSegment = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                    setBackgroundColor(getColorForApp(appName))
                }
                usageBarContainer.addView(progressBarSegment)

                // Create app item layout
                createAppUsageItem(appName, usageTime, weight, totalScreenTime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error populating usage statistics", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAppUsageItem(appName: String, usageTime: Long, weight: Float, totalScreenTime: Long) {
        // Create the container layout
        val appItemLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#2A2D31"))
            setPadding(12)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        // App icon (colored circle)
        val appIcon = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(36, 36).apply {
                marginEnd = 12
            }
            setBackgroundColor(getColorForApp(appName))
        }
        appItemLayout.addView(appIcon)

        // App info container
        val appInfoLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }

        // App name
        val appNameTextView = TextView(this).apply {
            text = appName
            setTextColor(Color.WHITE)
            textSize = 16f
        }
        appInfoLayout.addView(appNameTextView)

        // App usage time
        val appTimeTextView = TextView(this).apply {
            text = formatUsageTime(usageTime)
            setTextColor(Color.parseColor("#99AAB5"))
            textSize = 14f
        }
        appInfoLayout.addView(appTimeTextView)

        appItemLayout.addView(appInfoLayout)

        // Usage percentage
        val percentageTextView = TextView(this).apply {
            text = "${(weight * 100).toInt()}%"
            setTextColor(getColorForApp(appName))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        appItemLayout.addView(percentageTextView)

        // Add the item to the container
        usageDetailsContainer.addView(appItemLayout)
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