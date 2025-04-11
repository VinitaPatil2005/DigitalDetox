package com.example.digitaldetox.ui

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldetox.AppListAdapter
import com.example.digitaldetox.R
import com.example.digitaldetox.services.AppBlockerService
import com.example.digitaldetox.services.KeepAliveService

class SetLimitsActivity : AppCompatActivity() {
    private val TAG = "SetLimitsActivity"

    private lateinit var appRecyclerView: RecyclerView
    private lateinit var timeInput: EditText
    private lateinit var setLimitButton: Button
    private lateinit var limitsContainer: LinearLayout

    private val appList = mutableListOf<AppItem>()
    private val appLimitMap = mutableMapOf<String, Int>() // Package -> Limit in minutes
    private val appsToBlock = mutableSetOf<String>() // Apps that have exceeded limits

    // Check usage more frequently (every 10 seconds instead of 60)
    private val USAGE_CHECK_INTERVAL = 10 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_limits)

        appRecyclerView = findViewById(R.id.appRecyclerView)
        timeInput = findViewById(R.id.timeInput)
        setLimitButton = findViewById(R.id.setLimitButton)
        limitsContainer = findViewById(R.id.limitsContainer)

        // Create Notification Channel
        createNotificationChannel()

        // Ask notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        // Check for necessary permissions
        checkUsagePermission()
        checkAccessibilityPermission()

        loadInstalledApps()
        loadLimitsFromPrefs()
        loadBlockedAppsFromPrefs()
        updateLimitsView()

        appRecyclerView.layoutManager = LinearLayoutManager(this)
        appRecyclerView.adapter = AppListAdapter(appList)

        setLimitButton.setOnClickListener {
            val timeLimit = timeInput.text.toString().toIntOrNull()
            if (timeLimit != null) {
                val selectedApps = appList.filter { it.isSelected }
                if (selectedApps.isEmpty()) {
                    Toast.makeText(this, "Select at least one app!", Toast.LENGTH_SHORT).show()
                } else {
                    selectedApps.forEach { app ->
                        appLimitMap[app.packageName] = timeLimit
                    }
                    saveLimitsToPrefs()
                    updateLimitsView()

                    // Immediately apply zero-time limits
                    handleZeroTimeLimits()

                    // Force immediate check after setting limits
                    checkUsageAndBlockIfNeeded()

                    Toast.makeText(this, "Limits set for ${selectedApps.size} app(s)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid time!", Toast.LENGTH_SHORT).show()
            }
        }

        // Check usage at more frequent intervals
        val handler = Handler(Looper.getMainLooper())
        val usageCheckRunnable = object : Runnable {
            override fun run() {
                if (hasUsageStatsPermission()) {
                    checkUsageAndBlockIfNeeded()
                } else {
                    Log.w(TAG, "Usage permission not granted.")
                }
                handler.postDelayed(this, USAGE_CHECK_INTERVAL) // Check more frequently
            }
        }
        handler.post(usageCheckRunnable)
    }

    private fun handleZeroTimeLimits() {
        // Special handling for 0-minute limits - block immediately
        val immediateBlockApps = mutableSetOf<String>()

        for ((packageName, limitMinutes) in appLimitMap) {
            if (limitMinutes == 0) {
                Log.d(TAG, "Adding $packageName to immediate block (0 min limit)")
                immediateBlockApps.add(packageName)

                // Send notification for immediately blocked apps
                sendLimitNotification(packageName, true)
            }
        }

        if (immediateBlockApps.isNotEmpty()) {
            // Add these to our blocked apps list
            appsToBlock.addAll(immediateBlockApps)
            saveBlockedAppsToPrefs()
            updateBlockingService()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "limit_channel",
                "App Limits",
                NotificationManager.IMPORTANCE_HIGH // Changed to HIGH for more visibility
            ).apply {
                description = "Notifications for app usage limits"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateLimitsView() {
        limitsContainer.removeAllViews()
        for ((pkg, limit) in appLimitMap) {
            val appName = appList.find { it.packageName == pkg }?.name ?: pkg
            val textView = TextView(this).apply {
                text = "$appName: $limit min"
                setTextColor(resources.getColor(android.R.color.white))
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            limitsContainer.addView(textView)
        }
    }

    private fun checkUsagePermission() {
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Toast.makeText(this, "Please grant Usage Access Permission", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Please enable Accessibility Service to block apps", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceName = packageName + "/" + AppBlockerService::class.java.canonicalName
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").contains(expectedServiceName)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        apps.filter {
            pm.getLaunchIntentForPackage(it.packageName) != null &&
                    it.flags and ApplicationInfo.FLAG_SYSTEM == 0
        }.forEach {
            appList.add(AppItem(it.loadLabel(pm).toString(), it.packageName))
        }
    }

    private fun saveLimitsToPrefs() {
        val prefs = getSharedPreferences("AppLimitsPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        appLimitMap.forEach { (pkg, limit) ->
            editor.putInt(pkg, limit)
        }
        editor.apply()
    }

    private fun loadLimitsFromPrefs() {
        val prefs = getSharedPreferences("AppLimitsPrefs", MODE_PRIVATE)
        for (app in appList) {
            if (prefs.contains(app.packageName)) {
                val limit = prefs.getInt(app.packageName, 0)
                appLimitMap[app.packageName] = limit
            }
        }
    }

    private fun saveBlockedAppsToPrefs() {
        val prefs = getSharedPreferences("app_blocker_prefs", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("blocked_apps", appsToBlock).apply()

        // Set monitoring as enabled if we have apps to block
        prefs.edit().putBoolean("monitoring_enabled", appsToBlock.isNotEmpty()).apply()
    }

    private fun loadBlockedAppsFromPrefs() {
        val prefs = getSharedPreferences("app_blocker_prefs", Context.MODE_PRIVATE)
        appsToBlock.clear()
        appsToBlock.addAll(prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet())
    }

    private fun checkUsageAndBlockIfNeeded() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // Last 24 hours for daily limits

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        // Track which apps should be blocked or unblocked
        val newBlockedApps = mutableSetOf<String>()

        // First add all apps with 0 minute limits
        for ((packageName, limitMinutes) in appLimitMap) {
            if (limitMinutes == 0) {
                newBlockedApps.add(packageName)
            }
        }

        // Then check usage for the rest
        for ((packageName, limitMinutes) in appLimitMap) {
            if (limitMinutes == 0) continue // Already added above

            // Find usage for this app
            val stat = usageStats.find { it.packageName == packageName }
            if (stat != null) {
                val usedMinutes = (stat.totalTimeInForeground / 1000) / 60
                Log.d(TAG, "$packageName used for $usedMinutes mins (limit: $limitMinutes)")

                if (usedMinutes >= limitMinutes) {
                    // This app has exceeded its limit
                    newBlockedApps.add(packageName)
                    if (!appsToBlock.contains(packageName)) {
                        sendLimitNotification(packageName, false)
                    }
                }
            }
        }

        // Update our blocked apps list if there are changes
        if (newBlockedApps != appsToBlock) {
            appsToBlock.clear()
            appsToBlock.addAll(newBlockedApps)
            saveBlockedAppsToPrefs()

            // Start or update blocking service
            updateBlockingService()
        }
    }

    private fun updateBlockingService() {
        // If we have apps to block, make sure the service is running
        val serviceIntent = Intent(this, KeepAliveService::class.java)

        if (appsToBlock.isNotEmpty()) {
            // Start the KeepAlive service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            // Notify the service about updated apps list
            val updateIntent = Intent("com.example.digitaldetox.UPDATE_BLOCKED_APPS")
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)
            // Also send a direct broadcast for better reliability
            sendBroadcast(updateIntent)
            Log.d(TAG, "Blocking service updated with ${appsToBlock.size} apps: ${appsToBlock.joinToString()}")
        } else {
            // If no apps to block, we can stop the service
            stopService(serviceIntent)
            Log.d(TAG, "No apps to block, stopping service")
        }
    }

    private fun sendLimitNotification(packageName: String, isImmediate: Boolean = false) {
        val appName = appList.find { it.packageName == packageName }?.name ?: packageName

        val message = if (isImmediate) {
            "$appName has been blocked immediately (0 min limit)."
        } else {
            "$appName has been blocked because you've reached your usage limit."
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "limit_channel")
        } else {
            Notification.Builder(this)
        }

        builder.setContentTitle("App Blocked")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(packageName.hashCode(), builder.build())

        Log.d(TAG, "Notification sent for $packageName - app blocked")
    }

    override fun onResume() {
        super.onResume()
        // Force a check when activity is resumed
        if (hasUsageStatsPermission()) {
            checkUsageAndBlockIfNeeded()
        }
    }

    data class AppItem(
        val name: String,
        val packageName: String,
        var isSelected: Boolean = false
    )
}