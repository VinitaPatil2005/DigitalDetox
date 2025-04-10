package com.example.digitaldetox.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldetox.R

class ChallengesActivity : AppCompatActivity() {

    data class Challenge(
        val title: String,
        val coinReward: Int,
        var started: Boolean = false,
        var completed: Boolean = false,
        val totalDurationMillis: Long = 600000L, // e.g., 10 mins
        var timeRemainingMillis: Long = 600000L
    )


    private val challenges = listOf(
        Challenge("Use phone < 2 hrs", 50, false),
        Challenge("No social media", 30, true),
        Challenge("Finish task before 8PM", 40, false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenges)


         fun updateProgress() {
            val total = challenges.size
            val completed = challenges.count { it.completed }
            val percent = (completed * 100) / total
            // Show percent in a TextView or ProgressBar if needed
        }


        val recyclerView = findViewById<RecyclerView>(R.id.rvChallenges)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChallengeAdapter(challenges) { challenge ->
            challenge.completed = !challenge.completed
            updateProgress()
        }
    }
}
