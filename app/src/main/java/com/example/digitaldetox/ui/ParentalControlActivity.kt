package com.example.digitaldetox.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.digitaldetox.R

class ParentalControlActivity : AppCompatActivity() {

    private lateinit var allowedTimeEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var firebaseDatabase: DatabaseReference
    private val parentUid: String by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: "default_uid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parental_control)

        allowedTimeEditText = findViewById(R.id.etAllowedTime)
        saveButton = findViewById(R.id.btnSaveParentalControl)

        // Initialize Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("ParentalControl")

        saveButton.setOnClickListener {
            val allowedTime = allowedTimeEditText.text.toString().trim()
            if (allowedTime.isNotEmpty()) {
                val controlData = hashMapOf("allowedTime" to allowedTime)

                firebaseDatabase.child(parentUid).setValue(controlData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Parental Control Saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter allowed time", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
