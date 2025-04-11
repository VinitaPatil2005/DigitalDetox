package com.example.digitaldetox.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R
import com.example.digitaldetox.services.AppBlockerService
import com.example.digitaldetox.services.KeepAliveService

class FocusModeSettingActivity : AppCompatActivity() {
    private val TAG = "FocusModeSettings"

    private lateinit var lvAppList: ListView
    private lateinit var btnToggle: Button // Renamed from btnTurnOn to btnToggle

    private val selectedApps = mutableSetOf<String>() // Store selected package names
    private var isFocusModeActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.focus_mode_settings)

        lvAppList = findViewById(R.id.lvAppList)
        btnToggle = findViewById(R.id.btnTurnOn) // ID can remain the same in layout

        // Check if focus mode is currently active
        val prefs = getSharedPreferences("app_blocker_prefs", Context.MODE_PRIVATE)
        isFocusModeActive = prefs.getBoolean("monitoring_enabled", false)

        // Load previously selected apps
        selectedApps.addAll(prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet())
        Log.d(TAG, "Loaded previously selected apps: $selectedApps")

        // Check for accessibility service
        if (!isAccessibilityServiceEnabled(this, AppBlockerService::class.java)) {
            showAccessibilityInstructions()
        }

        loadInstalledApps()
        updateButtonState()
        setupToggleButton()
    }

    private fun showAccessibilityInstructions() {
        AlertDialog.Builder(this)
            .setTitle("Enable Accessibility Service")
            .setMessage("This app needs accessibility permissions to monitor and block other apps.\n\n" +
                    "1. Click 'Open Settings' below\n" +
                    "2. Find 'Digital Detox' in the list\n" +
                    "3. Toggle the switch to ON\n" +
                    "4. Click Allow/OK if prompted")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setCancelable(false)
            .show()
    }

    private fun setupToggleButton() {
        // Update button text based on current state
        updateButtonText()

        btnToggle.setOnClickListener {
            if (isFocusModeActive) {
                // Turn off focus mode
                turnOffFocusMode()
            } else {
                // Turn on focus mode
                if (!isAccessibilityServiceEnabled(this, AppBlockerService::class.java)) {
                    Toast.makeText(this, "Please enable the Accessibility Service first", Toast.LENGTH_LONG).show()
                    showAccessibilityInstructions()
                    return@setOnClickListener
                }

                if (!Settings.canDrawOverlays(this)) {
                    // Request overlay permission
                    AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to display over other apps.")
                        .setPositiveButton("Grant Permission") { _, _ ->
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
                        }
                        .show()
                } else {
                    turnOnFocusMode()
                }
            }
        }
    }

    private fun turnOnFocusMode() {
        Log.d(TAG, "Starting blocking with apps: $selectedApps")

        // Save selected apps to SharedPreferences
        val prefs = getSharedPreferences("app_blocker_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet("blocked_apps", selectedApps)
            putBoolean("monitoring_enabled", true)
            apply()
        }

        // Start the KeepAlive service first
        val serviceIntent = Intent(this, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Notify the service about updated apps list with a broadcast
        val updateIntent = Intent("com.example.digitaldetox.UPDATE_BLOCKED_APPS")
        updateIntent.setPackage(packageName)
        sendBroadcast(updateIntent)
        Log.d(TAG, "Broadcast sent: com.example.digitaldetox.UPDATE_BLOCKED_APPS")

        Toast.makeText(this, "Focus Mode Activated!", Toast.LENGTH_SHORT).show()

        // Update state and button
        isFocusModeActive = true
        updateButtonText()
    }

    private fun turnOffFocusMode() {
        Log.d(TAG, "Turning off focus mode")

        // Update SharedPreferences
        val prefs = getSharedPreferences("app_blocker_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("monitoring_enabled", false)
            apply()
        }

        // Stop the KeepAlive service
        val serviceIntent = Intent(this, KeepAliveService::class.java)
        stopService(serviceIntent)

        // Notify the service to stop blocking
        val updateIntent = Intent("com.example.digitaldetox.UPDATE_BLOCKED_APPS")
        updateIntent.setPackage(packageName)
        sendBroadcast(updateIntent)

        Toast.makeText(this, "Focus Mode Deactivated", Toast.LENGTH_SHORT).show()

        // Update state and button
        isFocusModeActive = false
        updateButtonText()
    }

    private fun updateButtonText() {
        if (isFocusModeActive) {
            btnToggle.text = "Turn Off Focus Mode"
            btnToggle.setBackgroundResource(R.drawable.rounded_button_red) // You'll need to create this
        } else {
            btnToggle.text = "Turn On Focus Mode"
            if (selectedApps.isNotEmpty()) {
                btnToggle.setBackgroundResource(R.drawable.rounded_button)
            } else {
                btnToggle.setBackgroundResource(R.drawable.rounded_button_disabled)
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, serviceClass)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val enabled = enabledServices.split(':').any {
            val component = ComponentName.unflattenFromString(it)
            component == expectedComponentName
        }

        Log.d(TAG, "Accessibility service enabled: $enabled")
        return enabled
    }

    // Helper method to normalize app icons to consistent size
    private fun normalizeAppIcon(context: Context, drawable: Drawable): Drawable {
        val size = context.resources.getDimensionPixelSize(R.dimen.app_icon_size)

        val bitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null } // only launchable apps
            .map {
                val icon = normalizeAppIcon(this, it.loadIcon(pm))
                AppInfo(
                    it.loadLabel(pm).toString(),
                    it.packageName,
                    icon
                )
            }
            .sortedBy { it.appName }

        Log.d(TAG, "Loaded ${apps.size} installed apps")
        val adapter = AppListAdapter(apps)
        lvAppList.adapter = adapter
    }

    inner class AppListAdapter(private val appList: List<AppInfo>) : BaseAdapter() {
        override fun getCount(): Int = appList.size
        override fun getItem(position: Int): Any = appList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val holder: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.item_app, parent, false)
                holder = ViewHolder()
                holder.appIcon = view.findViewById(R.id.app_icon)
                holder.appName = view.findViewById(R.id.app_name)
                holder.checkbox = view.findViewById(R.id.app_checkbox)
                view.tag = holder
            } else {
                view = convertView
                holder = view.tag as ViewHolder
            }

            val appInfo = appList[position]

            // Set app icon and name
            holder.appIcon.setImageDrawable(appInfo.icon)
            holder.appName.text = appInfo.appName

            // Set checkbox state
            holder.checkbox.isChecked = selectedApps.contains(appInfo.packageName)

            view.setOnClickListener {
                // Only allow changing selections when focus mode is off
                if (!isFocusModeActive) {
                    val newState = !holder.checkbox.isChecked
                    holder.checkbox.isChecked = newState

                    if (newState) {
                        selectedApps.add(appInfo.packageName)
                        Log.d(TAG, "Added app to block: ${appInfo.packageName}")
                    } else {
                        selectedApps.remove(appInfo.packageName)
                        Log.d(TAG, "Removed app from block list: ${appInfo.packageName}")
                    }
                    updateButtonState()
                } else {
                    Toast.makeText(applicationContext,
                        "Turn off Focus Mode first to change selections",
                        Toast.LENGTH_SHORT).show()
                }
            }

            return view
        }

        private inner class ViewHolder {
            lateinit var appIcon: ImageView
            lateinit var appName: TextView
            lateinit var checkbox: CheckBox
        }
    }

    private fun updateButtonState() {
        // Enable the button if focus mode is active or if we have apps selected
        btnToggle.isEnabled = isFocusModeActive || selectedApps.isNotEmpty()
        updateButtonText()
    }

    data class AppInfo(val appName: String, val packageName: String, val icon: Drawable)
}