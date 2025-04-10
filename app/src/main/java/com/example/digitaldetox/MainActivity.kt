package com.example.digitaldetox

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var tvScreenTime: TextView
    private lateinit var tvInstagramTime: TextView
    private lateinit var tvYouTubeTime: TextView
    private lateinit var tvWhatsAppTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TextViews
        tvScreenTime = findViewById(R.id.tvScreenTime)
        tvInstagramTime = findViewById(R.id.tvInstagramTime)
        tvYouTubeTime = findViewById(R.id.tvYouTubeTime)
        tvWhatsAppTime = findViewById(R.id.tvWhatsAppTime)

        // Set screen time
        tvScreenTime.text = "Today's Screen Time: 4 hr 30 min"

        // Set onClickListeners for tracking buttons
        findViewById<Button>(R.id.btnDailyTracking).setOnClickListener {
            Toast.makeText(this, "Daily Tracking clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnWeeklyTracking).setOnClickListener {
            Toast.makeText(this, "Weekly Tracking clicked", Toast.LENGTH_SHORT).show()
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
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
                    Toast.makeText(this, "Chatbot clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}
