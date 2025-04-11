package com.example.digitaldetox.ui
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.digitaldetox.MainActivity
import com.example.digitaldetox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var roleGroup: RadioGroup
    private lateinit var btnNext: Button
    private var isSignupFlow = true  // Pass this as intent extra

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        roleGroup = findViewById(R.id.roleGroup)
        btnNext = findViewById(R.id.btnNext)

        isSignupFlow = intent.getBooleanExtra("isSignup", true)

        btnNext.setOnClickListener {
            val selectedId = roleGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRole = when (selectedId) {
                R.id.rbParent -> "parent"
                R.id.rbChild -> "child"
                else -> ""
            }

            // Save role to Firebase Realtime DB
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val dbRef = FirebaseDatabase.getInstance().getReference("Users")
            uid?.let {
                dbRef.child(it).child("role").setValue(selectedRole).addOnSuccessListener {
                    if (isSignupFlow) {
                        // Redirect to profile page
                        val intent = if (selectedRole == "parent") {
                            Intent(this, ParentProfileOnboardingActivity::class.java)
                        } else {
                            Intent(this, ProfileOnboardingActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // Login flow → redirect to home
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}
