package com.example.digitaldetox

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SetLimitsActivity : AppCompatActivity() {

    private lateinit var appRecyclerView: RecyclerView
    private lateinit var timeInput: EditText
    private lateinit var setLimitButton: Button
    private lateinit var limitsContainer: LinearLayout

    private val appList = mutableListOf<AppItem>()
    private val appLimitMap = mutableMapOf<String, Int>() // Package -> Limit in minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_limits)

        appRecyclerView = findViewById(R.id.appRecyclerView)
        timeInput = findViewById(R.id.timeInput)
        setLimitButton = findViewById(R.id.setLimitButton)
        limitsContainer = findViewById(R.id.limitsContainer)

        // 🔔 Create Notification Channel
        createNotificationChannel()

        // 📱 Ask notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        checkUsagePermission()
        loadInstalledApps()
        loadLimitsFromPrefs()
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
                    Toast.makeText(this, "Limits set for ${selectedApps.size} app(s)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a valid time!", Toast.LENGTH_SHORT).show()
            }
        }

        // ⏱ Check usage every 10 minutes
        val handler = Handler(Looper.getMainLooper())
        val usageCheckRunnable = object : Runnable {
            override fun run() {
                if (hasUsageStatsPermission()) {
                    checkUsageAndNotify()
                } else {
                    Log.w("DigitalDetox", "Usage permission not granted.")
                }
                handler.postDelayed(this, 10 * 60 * 1000)
            }
        }
        handler.post(usageCheckRunnable)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "limit_channel",
                "App Limits",
                NotificationManager.IMPORTANCE_DEFAULT
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

    private fun checkUsageAndNotify() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 // Last 1 hour

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        for (stat in usageStats) {
            val limit = appLimitMap[stat.packageName]
            if (limit != null) {
                val usedMinutes = (stat.totalTimeInForeground / 1000) / 60
                Log.d("DigitalDetox", "${stat.packageName} used for $usedMinutes mins (limit: $limit)")
                if (usedMinutes >= limit) {
                    sendLimitNotification(stat.packageName)
                }
            }
        }
    }

    private fun sendLimitNotification(packageName: String) {
        val appName = appList.find { it.packageName == packageName }?.name ?: packageName

        val builder = Notification.Builder(this, "limit_channel")
            .setContentTitle("Limit Reached")
            .setContentText("You've used $appName more than your set limit.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(packageName.hashCode(), builder.build())

        Log.d("DigitalDetox", "Notification sent for $packageName")
    }

    data class AppItem(
        val name: String,
        val packageName: String,
        var isSelected: Boolean = false
    )
}
