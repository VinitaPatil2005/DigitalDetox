package com.example.digitaldetox

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TimetableActivity : AppCompatActivity() {

    private lateinit var editDay: EditText
    private lateinit var editType: EditText
    private lateinit var editSubject: EditText
    private lateinit var editTime: EditText
    private lateinit var timetableList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        editDay = findViewById(R.id.editDay)
        editType = findViewById(R.id.editType)
        editSubject = findViewById(R.id.editSubject)
        editTime = findViewById(R.id.editTime)
        timetableList = findViewById(R.id.timetableList)

        findViewById<Button>(R.id.btnAddToTimetable).setOnClickListener {
            val day = editDay.text.toString().trim()
            val type = editType.text.toString().trim().lowercase()
            val subject = editSubject.text.toString().trim()
            val time = editTime.text.toString().trim()

            if (day.isNotEmpty() && type.isNotEmpty() && subject.isNotEmpty() && time.isNotEmpty()) {
                val entryText = "$day - [$type] $subject at $time"

                val entry = TextView(this).apply {
                    text = entryText
                    setTextColor(resources.getColor(android.R.color.white, null))
                    textSize = 14f
                    setPadding(8, 8, 8, 8)
                }

                timetableList.addView(entry)

                // Placeholder for app blocking logic
                if (type != "break") {
                    Toast.makeText(this, "🔒 Apps will be blocked during this session!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "📱 Break time! Phone is allowed.", Toast.LENGTH_SHORT).show()
                }

                // Clear inputs
                editDay.text.clear()
                editType.text.clear()
                editSubject.text.clear()
                editTime.text.clear()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
