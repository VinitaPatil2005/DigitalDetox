package com.example.digitaldetox

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DanceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dance_details)

        val referralInput = findViewById<EditText>(R.id.referralInput)
        val btnEnter = findViewById<Button>(R.id.btnEnter)

        btnEnter.setOnClickListener {
            val code = referralInput.text.toString()
            if (code.isNotEmpty()) {
                Toast.makeText(this, "Referral code '$code' submitted!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please enter referral code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
