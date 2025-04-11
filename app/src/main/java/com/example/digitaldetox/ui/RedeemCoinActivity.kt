package com.example.digitaldetox.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R
import java.util.UUID

class RedeemCoinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem_coin)

        val tvRedeemStatus = findViewById<TextView>(R.id.tvRedeemStatus)
        val tvRedeemCode = findViewById<TextView>(R.id.tvRedeemCode)

        val coins = 50 // Dummy amount of coins
        val redeemCode = generateUniqueRedeemCode()

        // Display success message
        tvRedeemStatus.text = "Congratulations! You have successfully redeemed $coins coins."

        // Display the unique redeem code
        tvRedeemCode.text = "Your Redeem Code: $redeemCode"
    }

    private fun generateUniqueRedeemCode(): String {
        // Generate a unique random alphanumeric code
        return UUID.randomUUID().toString().take(8).uppercase()
    }
}