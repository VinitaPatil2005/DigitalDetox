package com.example.digitaldetox

import android.app.usage.UsageEvents
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
import com.example.digitaldetox.ui.SetLimitsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity() {

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

    private var currentRange = TimeRange.DAILY

    enum class TimeRange {
        DAILY,
        WEEKLY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        setupBottomNavigation()
        setupUsageStats()

    }

    private fun initializeViews() {
        try {
            tvTotalTime = findViewById(R.id.tvTotalTime)
            motivationalQuote = findViewById(R.id.tvMotivationalQuote)
            usageBarContainer = findViewById(R.id.usageBarContainer)
            usageDetailsContainer = findViewById(R.id.usageDetailsContainer)
            btnShowMore = findViewById(R.id.btnShowMore)
            gamificationCard = findViewById(R.id.gamificationCard)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUsageStats() {
        try {
            val screenTime = getScreenTimeToday(currentRange)
            tvTotalTime.text = screenTime
            appUsageStats = getAppUsageStats(currentRange)
            populateUsageBarAndDetails()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error setting up usage stats", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        try {
            findViewById<Button>(R.id.btnDailyTracking)?.setOnClickListener {
                currentRange = TimeRange.DAILY
                setupUsageStats()
                Toast.makeText(this, "Showing Daily Stats", Toast.LENGTH_SHORT).show()
            }

            findViewById<Button>(R.id.btnWeeklyTracking)?.setOnClickListener {
                currentRange = TimeRange.WEEKLY
                setupUsageStats()
                Toast.makeText(this, "Showing Weekly Stats", Toast.LENGTH_SHORT).show()
            }

            btnShowMore.setOnClickListener {
                showingAllApps = !showingAllApps
                populateUsageBarAndDetails()
                btnShowMore.text = if (showingAllApps) "Show Less" else "Show More"
            }

            motivationalQuote.setOnClickListener {
                val intent = Intent(this, StudentOptionsActivity::class.java)
                startActivity(intent)
            }

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

    private fun getScreenTimeToday(range: TimeRange): String {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = when (range) {
                TimeRange.DAILY -> {
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                TimeRange.WEEKLY -> endTime - (1000 * 60 * 60 * 24 * 7)
            }

            val usageEvents = usm.queryEvents(startTime, endTime)
            var totalTime = 0L
            val event = UsageEvents.Event()
            val foregroundAppTimes = mutableMapOf<String, Long>()
            val lastAppStartTimes = mutableMapOf<String, Long>()

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val packageName = event.packageName

                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastAppStartTimes[packageName] = event.timeStamp
                } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    val startTimeStamp = lastAppStartTimes[packageName]
                    if (startTimeStamp != null) {
                        val usageTime = event.timeStamp - startTimeStamp
                        foregroundAppTimes[packageName] = foregroundAppTimes.getOrDefault(packageName, 0L) + usageTime
                    }
                }
            }

            totalTime = foregroundAppTimes.values.sum()

            val hours = totalTime / (1000 * 60 * 60)
            val minutes = (totalTime / (1000 * 60)) % 60
            "${hours}h ${minutes}m"
        } catch (e: Exception) {
            e.printStackTrace()
            "0h 0m"
        }
    }


    private fun getAppUsageStats(range: TimeRange): List<Pair<String, Long>> {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = when (range) {
                TimeRange.DAILY -> endTime - (1000 * 60 * 60 * 24)
                TimeRange.WEEKLY -> endTime - (1000 * 60 * 60 * 24 * 7)
            }

            val usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            usageStats
                .filter { it.totalTimeInForeground > 0 }
                .map { Pair(getAppNameFromPackage(it.packageName), it.totalTimeInForeground) }
                .groupBy { it.first }
                .mapValues { entry -> entry.value.sumOf { it.second } }
                .toList()
                .sortedByDescending { it.second }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun populateUsageBarAndDetails() {
        try {
            val totalScreenTime = appUsageStats.sumOf { it.second }

            // Clear existing views
            usageBarContainer.removeAllViews()
            usageDetailsContainer.removeAllViews()

            val topApps = appUsageStats.take(5)
            val otherApps = appUsageStats.drop(5)

            val othersUsageTime = otherApps.sumOf { it.second }

            val displayBarList = if (othersUsageTime > 0) {
                topApps + Pair("Others", othersUsageTime)
            } else topApps

            displayBarList.forEach { (appName, usageTime) ->
                val weight = usageTime.toFloat() / totalScreenTime.toFloat()
                val progressBarSegment = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                    setBackgroundColor(getColorForApp(appName))
                }
                usageBarContainer.addView(progressBarSegment)
            }

            // Show more or less in the details
            val displayDetailsList = if (showingAllApps) {
                appUsageStats
            } else {
                topApps
            }

            displayDetailsList.forEach { (appName, usageTime) ->
                val weight = usageTime.toFloat() / totalScreenTime.toFloat()
                createAppUsageItem(appName, usageTime, weight, totalScreenTime)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error populating usage statistics", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAppUsageItem(appName: String, usageTime: Long, weight: Float, totalScreenTime: Long) {
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

        val appIcon = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(36, 36).apply { marginEnd = 12 }
            setBackgroundColor(getColorForApp(appName))
        }
        appItemLayout.addView(appIcon)

        val appInfoLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val appNameTextView = TextView(this).apply {
            text = appName
            setTextColor(Color.WHITE)
            textSize = 16f
        }
        appInfoLayout.addView(appNameTextView)

        val appTimeTextView = TextView(this).apply {
            text = formatUsageTime(usageTime)
            setTextColor(Color.parseColor("#99AAB5"))
            textSize = 14f
        }
        appInfoLayout.addView(appTimeTextView)

        appItemLayout.addView(appInfoLayout)

        val percentageTextView = TextView(this).apply {
            text = "${(weight * 100).toInt()}%"
            setTextColor(getColorForApp(appName))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        appItemLayout.addView(percentageTextView)

        usageDetailsContainer.addView(appItemLayout)
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
            when (appName) {
                "Instagram" -> Color.parseColor("#C13584")
                "YouTube" -> Color.parseColor("#FF0000")
                "WhatsApp" -> Color.parseColor("#25D366")
                "Facebook" -> Color.parseColor("#1877F2")
                "TikTok" -> Color.parseColor("#000000")
                "Twitter" -> Color.parseColor("#1DA1F2")
                "Snapchat" -> Color.parseColor("#FFFC00")
                "Chrome" -> Color.parseColor("#4285F4")
                "Others" -> Color.parseColor("#888888")
                else -> getRandomColor()
            }
        } catch (e: Exception) {
            getRandomColor()
        }
    }

    private fun getRandomColor(): Int {
        return try {
            val random = Random()
            Color.rgb(
                100 + random.nextInt(156),
                100 + random.nextInt(156),
                100 + random.nextInt(156)
            )
        } catch (e: Exception) {
            Color.GRAY
        }
    }
}
