package com.example.digitaldetox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Toast.makeText(context, "Time for Bed!", Toast.LENGTH_LONG).show()

        // Optional: Play alarm sound
        val mediaPlayer = MediaPlayer.create(context, R.raw.bedtime_alarm)
        mediaPlayer?.start()
    }
}
