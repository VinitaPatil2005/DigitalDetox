package com.example.digitaldetox.ui

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldetox.R

class ChallengeAdapter(
    private val challenges: List<ChallengesActivity.Challenge>,
    private val onToggleComplete: (ChallengesActivity.Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    inner class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvChallengeTitle)
        val tvCoins: TextView = view.findViewById(R.id.tvChallengeCoins)
        val tvStatus: TextView = view.findViewById(R.id.tvChallengeStatus)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarChallenge)
        var timer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.tvTitle.text = challenge.title
        holder.tvCoins.text = "${challenge.coinReward} coins"
        holder.tvStatus.text = when {
            challenge.completed -> "Completed"
            challenge.started -> "In Progress"
            else -> "Not Started"
        }

        val percent = (100 * (challenge.totalDurationMillis - challenge.timeRemainingMillis) / challenge.totalDurationMillis).toInt()
        holder.progressBar.progress = percent

        // Start countdown if challenge is started
        if (challenge.started && !challenge.completed) {
            holder.timer?.cancel()
            holder.timer = object : CountDownTimer(challenge.timeRemainingMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    challenge.timeRemainingMillis = millisUntilFinished
                    val progress = (100 * (challenge.totalDurationMillis - millisUntilFinished) / challenge.totalDurationMillis).toInt()
                    holder.progressBar.progress = progress
                    holder.tvStatus.text = "In Progress: ${millisUntilFinished / 1000}s left"
                }

                override fun onFinish() {
                    challenge.completed = true
                    holder.tvStatus.text = "Completed"
                    holder.progressBar.progress = 100
                }
            }.start()
        }

        holder.itemView.setOnClickListener {
            onToggleComplete(challenge)
        }
    }

    override fun getItemCount(): Int = challenges.size
}
