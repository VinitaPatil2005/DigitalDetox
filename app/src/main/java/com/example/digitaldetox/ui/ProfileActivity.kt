package com.example.digitaldetox.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R
import com.example.digitaldetox.YogaDetailActivity
import com.example.digitaldetox.DanceDetailActivity


class ProfileActivity : AppCompatActivity() {

    private lateinit var tvRewardsToggle: TextView
    private lateinit var llRewardsSection: LinearLayout
    private lateinit var tvCoins: TextView
    private var rewardsVisible = false

    // Dummy coins data
    private var totalCoins = 120 // Assume the user starts with 120 coins

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        val yogaCard = findViewById<LinearLayout>(R.id.yogacard)
        val danceCard = findViewById<LinearLayout>(R.id.dance)

        yogaCard.setOnClickListener {
            val intent = Intent(this, YogaDetailActivity::class.java)
            intent.putExtra("category", "Yoga")
            startActivity(intent)
        }

        danceCard.setOnClickListener {
            val intent = Intent(this, DanceDetailActivity::class.java)
            intent.putExtra("category", "Dance")
            startActivity(intent)
        }


        // TextViews and Section
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        tvCoins = findViewById(R.id.tvTotalCoins)
//        val tvCoupons = findViewById<TextView>(R.id.tvCoupons)

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
//        tvCoupons.text = "Redeem Coins"

        // Update total coins on load
        updateCoinDisplay()

        // Toggle rewards visibility
        tvRewardsToggle.setOnClickListener {
            toggleRewardsVisibility()
        }

        // Redeem Coins Button
        val redeemBtn = findViewById<TextView>(R.id.btnRedeemCoins)
        redeemBtn.setOnClickListener {
            if (totalCoins >= 50) {
                AlertDialog.Builder(this)
                    .setTitle("Redeem Coins")
                    .setMessage(
                        "You have $totalCoins coins.\n\nYou can redeem them for discounts at:\n\n✔ Yoga Studios\n✔ Gyms\n✔ Coaching Classes\n✔ Dance Centers\n\nRedeem now?"
                    )
                    .setPositiveButton("Redeem") { dialog, _ ->
                        // Deduct coins and navigate to RedeemCoinActivity
                        totalCoins -= 50
                        updateCoinDisplay()
                        val intent = Intent(this, RedeemCoinActivity::class.java)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "You need at least 50 coins to redeem!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleRewardsVisibility() {
        rewardsVisible = !rewardsVisible
        llRewardsSection.visibility = if (rewardsVisible) View.VISIBLE else View.GONE
        tvRewardsToggle.text = if (rewardsVisible) "Hide My Rewards ▲" else "View My Rewards ▼"
    }

    private fun updateCoinDisplay() {
        tvCoins.text = "Total Coins: $totalCoins"
    }
}