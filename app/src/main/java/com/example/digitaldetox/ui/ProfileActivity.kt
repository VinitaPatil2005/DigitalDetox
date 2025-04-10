package com.example.digitaldetox.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvRewardsToggle: TextView
    private lateinit var llRewardsSection: LinearLayout
    private var rewardsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // TextViews and Section
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val tvCoins = findViewById<TextView>(R.id.tvCoinsEarned)
        val tvCoupons = findViewById<TextView>(R.id.tvCoupons)

        // Toggle Section
        tvRewardsToggle = findViewById(R.id.tvRewardsToggle)
        llRewardsSection = findViewById(R.id.llRewardsSection)

        // Load the blink animation
        val blink = AnimationUtils.loadAnimation(this, R.anim.blink_anim)

        // Start blinking for each discount TextView
        findViewById<TextView>(R.id.tvYogaDiscount).startAnimation(blink)
        findViewById<TextView>(R.id.tvDanceDiscount).startAnimation(blink)
        findViewById<TextView>(R.id.tvGymDiscount).startAnimation(blink)
        findViewById<TextView>(R.id.tvCoachingDiscount).startAnimation(blink)

        // Dummy data (can be dynamic later)
        tvUserName.text = "Vinita Patil"
        tvUserEmail.text = "vinita@email.com"
        tvCoins.text = "Coins Earned: 120"
        tvCoupons.text = "Coupons Received: 3"

        tvRewardsToggle.setOnClickListener {
            toggleRewardsVisibility()
        }
    }

    private fun toggleRewardsVisibility() {
        rewardsVisible = !rewardsVisible
        llRewardsSection.visibility = if (rewardsVisible) View.VISIBLE else View.GONE
        tvRewardsToggle.text = if (rewardsVisible) "Hide My Rewards ▲" else "View My Rewards ▼"
    }
}
