package com.example.digitaldetox

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TimetableActivity : AppCompatActivity() {

    private lateinit var editDay: EditText
    private lateinit var editSubject: EditText
    private lateinit var editTime: EditText
    private lateinit var timetableList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        editDay = findViewById(R.id.editDay)
        editSubject = findViewById(R.id.editSubject)
        editTime = findViewById(R.id.editTime)
        timetableList = findViewById(R.id.timetableList)

        findViewById<Button>(R.id.btnAddToTimetable).setOnClickListener {
            val day = editDay.text.toString().trim()
            val subject = editSubject.text.toString().trim()
            val time = editTime.text.toString().trim()

            if (day.isNotEmpty() && subject.isNotEmpty() && time.isNotEmpty()) {
                val entry = TextView(this).apply {
                    text = "$day - $subject at $time"
                    setTextColor(resources.getColor(android.R.color.white))
                    textSize = 14f
                    setPadding(4, 4, 4, 4)
                }
                timetableList.addView(entry)

                editDay.text.clear()
                editSubject.text.clear()
                editTime.text.clear()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
