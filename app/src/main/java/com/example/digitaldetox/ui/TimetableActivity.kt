package com.example.digitaldetox

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TimetableActivity : AppCompatActivity() {

    private lateinit var editDay: EditText
    private lateinit var editType: EditText
    private lateinit var editSubject: EditText
    private lateinit var editStartTime: EditText
    private lateinit var editEndTime: EditText
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimetableAdapter
    private val entries = mutableListOf<TimetableEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        editDay = findViewById(R.id.editDay)
        editType = findViewById(R.id.editType)
        editSubject = findViewById(R.id.editSubject)
        editStartTime = findViewById(R.id.editStartTime)
        editEndTime = findViewById(R.id.editEndTime)
        btnAdd = findViewById(R.id.btnAddToTimetable)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = TimetableAdapter(entries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fun showTimePicker(targetEditText: EditText) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                { _, selectedHour, selectedMinute ->
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)

                    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val formattedTime = format.format(calendar.time)

                    targetEditText.setText(formattedTime)
                },
                hour, minute, false // false for 12-hour format
            )
            timePickerDialog.show()
        }

        editStartTime.setOnClickListener {
            showTimePicker(editStartTime)
        }

        editEndTime.setOnClickListener {
            showTimePicker(editEndTime)
        }

        btnAdd.setOnClickListener {
            val day = editDay.text.toString().trim()
            val type = editType.text.toString().trim()
            val subject = editSubject.text.toString().trim()
            val startTime = editStartTime.text.toString().trim()
            val endTime = editEndTime.text.toString().trim()

            if (day.isNotEmpty() && type.isNotEmpty() && subject.isNotEmpty() &&
                startTime.isNotEmpty() && endTime.isNotEmpty()) {

                val time = "$startTime - $endTime"
                val newEntry = TimetableEntry(day, type, subject, time)
                entries.add(newEntry)
                adapter.notifyItemInserted(entries.size - 1)

                if (type.lowercase() != "break") {
                    Toast.makeText(this, "🔒 Apps will be blocked during this session!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "📱 Break time! Phone is allowed.", Toast.LENGTH_SHORT).show()
                }

                // Clear fields after adding
                editDay.text.clear()
                editType.text.clear()
                editSubject.text.clear()
                editStartTime.text.clear()
                editEndTime.text.clear()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
