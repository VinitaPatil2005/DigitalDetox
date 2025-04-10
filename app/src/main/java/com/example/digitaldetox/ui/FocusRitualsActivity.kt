package com.example.digitaldetox

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class FocusRitualsActivity : AppCompatActivity() {

    private lateinit var ambientPlayer: MediaPlayer
    private var isSoundPlaying = false
    private lateinit var breathingCircle: ImageView
    private lateinit var breathingText: TextView
    private lateinit var checklistLayout: LinearLayout
    private lateinit var btnBreathing: Button

    private var isBreathingActive = false
    private var breathingTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_rituals)

        btnBreathing = findViewById(R.id.btnBreathing)
        val btnAmbient: Button = findViewById(R.id.btnAmbient)
        val btnAffirmations: Button = findViewById(R.id.btnAffirmations)
        val btnChecklist: Button = findViewById(R.id.btnChecklist)

        breathingCircle = findViewById(R.id.breathingCircle)
        breathingText = findViewById(R.id.breathingText)
        checklistLayout = findViewById(R.id.checklistLayout)
        checklistLayout.isVisible = false

        btnBreathing.setOnClickListener { toggleBreathingExercise() }
        btnAmbient.setOnClickListener {
            if (isSoundPlaying) stopAmbientSound() else playAmbientSound()
        }
        btnAffirmations.setOnClickListener { showAffirmation() }
        btnChecklist.setOnClickListener { showChecklist() }
    }

    private fun toggleBreathingExercise() {
        if (isBreathingActive) {
            stopBreathingExercise()
        } else {
            startBreathingExercise()
        }
    }

    private fun startBreathingExercise() {
        // Show views before starting animation
        breathingCircle.visibility = View.VISIBLE
        breathingText.visibility = View.VISIBLE
        findViewById<FrameLayout>(R.id.breathingContainer).visibility = View.VISIBLE

        // Change button text to Stop Breathing
        btnBreathing.text = "Stop Breathing Ritual"

        isBreathingActive = true

        val animationDuration = 4000L

        val scaleUpX = ObjectAnimator.ofFloat(breathingCircle, "scaleX", 1f, 1.2f)
        val scaleUpY = ObjectAnimator.ofFloat(breathingCircle, "scaleY", 1f, 1.2f)
        val scaleDownX = ObjectAnimator.ofFloat(breathingCircle, "scaleX", 1.2f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(breathingCircle, "scaleY", 1.2f, 1f)

        scaleUpX.duration = animationDuration / 2
        scaleUpY.duration = animationDuration / 2
        scaleDownX.duration = animationDuration / 2
        scaleDownY.duration = animationDuration / 2

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleUpX, scaleUpY)

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                breathingText.text = "Inhale"
            }

            override fun onAnimationEnd(animation: Animator) {
                breathingText.text = "Exhale"
                val scaleDownSet = AnimatorSet()
                scaleDownSet.playTogether(scaleDownX, scaleDownY)
                scaleDownSet.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animatorSet.start()

        // Loop the breathing effect until stopped
        breathingTimer = object : CountDownTimer(animationDuration, animationDuration) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (isBreathingActive) {
                    startBreathingExercise()  // restart the breathing cycle
                }
            }
        }
        breathingTimer?.start()
    }

    private fun stopBreathingExercise() {
        // Change button text back to Start Breathing
        btnBreathing.text = "Start Breathing Ritual"

        isBreathingActive = false

        // Stop the animation and timer
        breathingCircle.visibility = View.GONE
        breathingText.visibility = View.GONE
        findViewById<FrameLayout>(R.id.breathingContainer).visibility = View.GONE

        breathingTimer?.cancel()
        breathingTimer = null
    }

    private fun playAmbientSound() {
        ambientPlayer = MediaPlayer.create(this, R.raw.ambient_forest)
        ambientPlayer.isLooping = true
        ambientPlayer.start()
        isSoundPlaying = true
        Toast.makeText(this, "Ambient Sound Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopAmbientSound() {
        if (::ambientPlayer.isInitialized) {
            ambientPlayer.stop()
            ambientPlayer.release()
            isSoundPlaying = false
            Toast.makeText(this, "Ambient Sound Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAffirmation() {
        val affirmations = arrayOf(
            "You are capable of achieving greatness.",
            "Every day is a new opportunity.",
            "Believe in your inner strength.",
            "You are enough just as you are.",
            "Keep pushing, success is coming."
        )
        val dialog = AlertDialog.Builder(this)
            .setMessage(affirmations.random())
            .setPositiveButton("Okay", null)
            .create()
        dialog.show()
    }

    private fun showChecklist() {
        checklistLayout.isVisible = !checklistLayout.isVisible
        if (!checklistLayout.isVisible) return

        val tasks = listOf(
            "Set your intention for this session",
            "Gather your focus tools",
            "Prepare your study materials",
            "Sit comfortably",
            "Start the timer"
        )

        checklistLayout.removeAllViews()

        var checkedCount = 0
        val totalTasks = tasks.size

        // Adding checkboxes dynamically
        for (task in tasks) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 24, 32, 24)
                background = ContextCompat.getDrawable(this@FocusRitualsActivity, R.drawable.rounded_card)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 16, 0, 0)
                layoutParams = params
                gravity = Gravity.CENTER_VERTICAL
            }

            val checkBox = CheckBox(this).apply {
                text = task
                setTextColor(ContextCompat.getColor(this@FocusRitualsActivity, android.R.color.white))
            }

            // Update progress when checkbox is checked/unchecked
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                checkedCount += if (isChecked) 1 else -1
            }

            // Add the checkbox to the card and the card to the layout
            card.addView(checkBox)
            checklistLayout.addView(card)
        }
    }
}
