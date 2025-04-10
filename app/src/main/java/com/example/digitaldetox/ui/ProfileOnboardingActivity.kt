package com.example.digitaldetox.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R

class ProfileOnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_onboarding)

        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val skip = findViewById<TextView>(R.id.skip)

        btnContinue.setOnClickListener {
            // Handle input validation and save logic here

            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }

        skip.setOnClickListener {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }
}
