//package com.example.digitaldetox.ui
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import com.example.digitaldetox.R
//
//class ProfileOnboardingActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_profile_onboarding)
//
//        val btnContinue = findViewById<Button>(R.id.btnContinue)
//        val skip = findViewById<TextView>(R.id.skip)
//
//        btnContinue.setOnClickListener {
//            // Handle input validation and save logic here
//
//            startActivity(Intent(this, OnboardingActivity::class.java))
//            finish()
//        }
//
//        skip.setOnClickListener {
//            startActivity(Intent(this, OnboardingActivity::class.java))
//            finish()
//        }
//    }
//}

package com.example.digitaldetox.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.R
import com.example.digitaldetox.model.ChildProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileOnboardingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_onboarding)



        auth = FirebaseAuth.getInstance()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etBirthdate = findViewById<EditText>(R.id.etBirthdate)
        val genderGroup = findViewById<RadioGroup>(R.id.genderGroup)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val skip = findViewById<TextView>(R.id.skip)

        etBirthdate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePicker = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                etBirthdate.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }

        btnContinue.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val birthdate = etBirthdate.text.toString().trim()
            val selectedGenderId = genderGroup.checkedRadioButtonId

            if (username.isEmpty() || birthdate.isEmpty() || selectedGenderId == -1) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = findViewById<RadioButton>(selectedGenderId).text.toString()

            val profile = ChildProfile(username, birthdate, gender)

            val currentUser = auth.currentUser
            currentUser?.let {
                val userId = it.uid
                val databaseRef = FirebaseDatabase.getInstance().getReference("childProfiles").child(userId)

                databaseRef.setValue(profile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, OnboardingActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
                    }
            } ?: run {
                Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            }
        }

        skip.setOnClickListener {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }
}


//val ref = FirebaseDatabase.getInstance().getReference("childProfiles").child(userId)
