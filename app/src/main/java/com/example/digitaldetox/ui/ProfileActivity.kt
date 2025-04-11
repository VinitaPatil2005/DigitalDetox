package com.example.digitaldetox.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R
import com.example.digitaldetox.ui.Coupon
import com.example.digitaldetox.utils.CouponUtils
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvRewardsToggle: TextView
    private lateinit var llRewardsSection: LinearLayout
    private lateinit var tvCoins: TextView
    private var rewardsVisible = false
    private var totalCoins = 120 // Example initial coins
    private lateinit var coinUpdateReceiver: BroadcastReceiver

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

        tvCoins = findViewById(R.id.tvTotalCoins)
        tvRewardsToggle = findViewById(R.id.tvRewardsToggle)
        llRewardsSection = findViewById(R.id.llRewardsSection)

        findViewById<TextView>(R.id.tvUserName).text = "Vinita Patil"
        findViewById<TextView>(R.id.tvUserEmail).text = "vinita@email.com"

        val blink = AnimationUtils.loadAnimation(this, R.anim.blink_anim)
        findViewById<TextView>(R.id.tvYogaDiscount).startAnimation(blink)
        findViewById<TextView>(R.id.tvDanceDiscount).startAnimation(blink)
        findViewById<TextView>(R.id.tvGymDiscount).startAnimation(blink)
        findViewById<TextView>(R.id.tvCoachingDiscount).startAnimation(blink)

        updateCoinDisplay()

        tvRewardsToggle.setOnClickListener {
            toggleRewardsVisibility()
        }

        val tvViewCoupons = findViewById<TextView>(R.id.tvViewCoupons)
        val llCouponsSection = findViewById<LinearLayout>(R.id.llCouponsSection)

        tvViewCoupons.setOnClickListener {
            if (llCouponsSection.visibility == View.GONE) {
                llCouponsSection.visibility = View.VISIBLE
                tvViewCoupons.text = "Hide My Coupons ▲"
                showSavedCoupons(llCouponsSection)
            } else {
                llCouponsSection.visibility = View.GONE
                tvViewCoupons.text = "View My Coupons ▶"
            }
        }

        // Register BroadcastReceiver for coin updates
        coinUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // Refresh the coin display
                updateCoinDisplay()
            }
        }
        val intentFilter = IntentFilter("COINS_UPDATED")
        registerReceiver(coinUpdateReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        // Ensure coins are updated when the activity is resumed
        updateCoinDisplay()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver to prevent memory leaks
        unregisterReceiver(coinUpdateReceiver)
    }

    private fun toggleRewardsVisibility() {
        rewardsVisible = !rewardsVisible
        llRewardsSection.visibility = if (rewardsVisible) View.VISIBLE else View.GONE
        tvRewardsToggle.text = if (rewardsVisible) "Hide My Rewards ▲" else "View My Rewards ▼"
    }

    private fun updateCoinDisplay() {
        val prefs = getSharedPreferences("digital_detox_prefs", Context.MODE_PRIVATE)
        totalCoins = prefs.getInt("user_coins", 0)
        tvCoins.text = "Total Coins: $totalCoins"
    }

    private fun showSavedCoupons(container: LinearLayout) {
        val coupons = CouponUtils.loadCoupons(this)
        container.removeAllViews()

        if (coupons.isEmpty()) {
            val noCouponView = TextView(this).apply {
                text = "No coupons available yet."
                setTextColor(resources.getColor(android.R.color.white))
                textSize = 14f
                setPadding(12, 12, 12, 12)
            }
            container.addView(noCouponView)
            return
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (coupon in coupons) {
            val view = layoutInflater.inflate(R.layout.coupon_item, null)

            val title = view.findViewById<TextView>(R.id.tvCouponTitle)
            val code = view.findViewById<TextView>(R.id.tvCouponCode)
            val validity = view.findViewById<TextView>(R.id.tvCouponValidity)

            title.text = coupon.title
            code.text = "Code: ${coupon.code}"
            validity.text = "Valid: ${dateFormat.format(Date(coupon.issuedDate))} - ${dateFormat.format(Date(coupon.expiryDate))}"

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24) // bottom margin between cards
            }

            view.layoutParams = params
            container.addView(view)
        }
    }
}