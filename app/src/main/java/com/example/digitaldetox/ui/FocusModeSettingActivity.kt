package com.example.digitaldetox

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class FocusModeSettingActivity : AppCompatActivity() {

    private lateinit var lvAppList: ListView
    private lateinit var btnTurnOn: Button

    private val selectedApps = mutableSetOf<String>() // Store selected package names

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.focus_mode_settings)

        lvAppList = findViewById(R.id.lvAppList)
        btnTurnOn = findViewById(R.id.btnTurnOn)

        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null } // only launchable apps
            .map {
                AppInfo(
                    it.loadLabel(pm).toString(),
                    it.packageName,
                    it.loadIcon(pm)
                )
            }

        val adapter = AppListAdapter(apps)
        lvAppList.adapter = adapter
    }

    inner class AppListAdapter(private val appList: List<AppInfo>) : BaseAdapter() {
        override fun getCount(): Int = appList.size
        override fun getItem(position: Int): Any = appList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup?): android.view.View {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)
            val appInfo = appList[position]

            val checkBox = view.findViewById<CheckedTextView>(android.R.id.text1)
            checkBox.text = appInfo.appName
            checkBox.isChecked = selectedApps.contains(appInfo.packageName)
            checkBox.setCompoundDrawablesWithIntrinsicBounds(appInfo.icon, null, null, null)
            checkBox.compoundDrawablePadding = 16

            lvAppList.setItemChecked(position, checkBox.isChecked)

            view.setOnClickListener {
                checkBox.toggle()
                if (checkBox.isChecked) {
                    selectedApps.add(appInfo.packageName)
                } else {
                    selectedApps.remove(appInfo.packageName)
                }
                updateButtonState()
            }

            return view
        }
    }

    private fun updateButtonState() {
        btnTurnOn.isEnabled = selectedApps.isNotEmpty()
        if (btnTurnOn.isEnabled) {
            btnTurnOn.setBackgroundResource(R.drawable.rounded_button)
        } else {
            btnTurnOn.setBackgroundResource(R.drawable.rounded_button_disabled)
        }
    }

    data class AppInfo(val appName: String, val packageName: String, val icon: Drawable)
}
