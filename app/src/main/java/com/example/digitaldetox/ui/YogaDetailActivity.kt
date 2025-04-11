package com.example.digitaldetox

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class YogaDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.yoga_details)

        val referralInput = findViewById<EditText>(R.id.referralInput)
        val btnEnter = findViewById<Button>(R.id.btnEnter)

        btnEnter.setOnClickListener {
            val code = referralInput.text.toString()
            if (code.isNotEmpty()) {
                // Show form popup here (you can replace this with a custom dialog if needed)
                Toast.makeText(this, "Referral code '$code' submitted!", Toast.LENGTH_LONG).show()
                // You can also navigate to a form screen here
            } else {
                Toast.makeText(this, "Please enter referral code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
