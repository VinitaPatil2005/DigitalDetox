package com.example.digitaldetox

data class TimetableEntry(
    val day: String,
    val type: String,
    val subject: String,
    val time: String,
    var stepsCompleted: Boolean = false
)
