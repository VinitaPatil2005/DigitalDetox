package com.example.digitaldetox.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R

class YogaDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showRedeemDialog()
    }

    private fun showRedeemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_redeem_offer, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnRedeem = dialogView.findViewById<Button>(R.id.btnRedeem)
        val codeLayout = dialogView.findViewById<LinearLayout>(R.id.codeLayout)
        val codeText = dialogView.findViewById<TextView>(R.id.redeemCodeText)
        val btnCopy = dialogView.findViewById<Button>(R.id.btnCopyCode)

        btnRedeem.setOnClickListener {
            val redeemCost = 50 // Specify the cost of redeeming the coupon
            val prefs = getSharedPreferences("digital_detox_prefs", Context.MODE_PRIVATE)
            val currentCoins = prefs.getInt("user_coins", 0)

            if (currentCoins >= redeemCost) {
                // Deduct coins
                prefs.edit().putInt("user_coins", currentCoins - redeemCost).apply()
                Toast.makeText(this, "$redeemCost coins deducted!", Toast.LENGTH_SHORT).show()

                // Notify other activities about coin update
                val intent = Intent("COINS_UPDATED")
                sendBroadcast(intent)

                // Show the code
                codeText.text = "GYM15OFF"
                codeLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Not enough coins to redeem!", Toast.LENGTH_SHORT).show()
            }
        }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Redeem Code", codeText.text.toString())
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Code Copied!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            loadYogaDetailContent()
        }

        dialog.show()
    }

    private fun loadYogaDetailContent() {
        setContentView(R.layout.yoga_details)

        val referralInput = findViewById<EditText>(R.id.referralInput)
        val btnEnter = findViewById<Button>(R.id.btnEnter)

        btnEnter.setOnClickListener {
            val code = referralInput.text.toString()
            if (code.isNotEmpty()) {
                val currentTime = System.currentTimeMillis()
                val expiryTime = currentTime + (15 * 24 * 60 * 60 * 1000L)

                val coupon = Coupon(
                    title = "Yoga Discount Offer",
                    code = code,
                    issuedDate = currentTime,
                    expiryDate = expiryTime
                )

                // ✅ Save using CouponUtils
                CouponUtils.saveCoupon(this, coupon)

                // Show dialog
                showCouponDialog(code)
            } else {
                Toast.makeText(this, "Please enter referral code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCouponDialog(code: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_coupon_generated, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val couponText = dialogView.findViewById<TextView>(R.id.tvCouponCode)
        val validityText = dialogView.findViewById<TextView>(R.id.tvValidity)
        val btnOkay = dialogView.findViewById<Button>(R.id.btnOkay)

        couponText.text = "Code: $code"
        validityText.text = "Valid for 15 days from today"

        btnOkay.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}