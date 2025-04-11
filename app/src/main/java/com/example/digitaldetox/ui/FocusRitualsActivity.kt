package com.example.digitaldetox

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton

class FocusRitualsActivity : AppCompatActivity() {

    private var ambientPlayer: MediaPlayer? = null
    private var isSoundPlaying = false
    private lateinit var breathingText: TextView
    private lateinit var checklistLayout: LinearLayout
    private lateinit var btnBreathing: MaterialButton
    private lateinit var btnAmbient: MaterialButton
    private var isBreathingActive = false
    private var breathingAnimatorSet: AnimatorSet? = null
    private var checkedCount = 0
    private val totalTasks = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FocusRituals", "onCreate called")
        try {
            setContentView(R.layout.activity_focus_rituals)
        } catch (e: Exception) {
            Log.e("FocusRituals", "Failed to set content view: ${e.message}", e)
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            btnBreathing = findViewById(R.id.btnBreathing)
            btnAmbient = findViewById(R.id.btnAmbient)
            val btnAffirmations: MaterialButton = findViewById(R.id.btnAffirmations)
            val btnChecklist: MaterialButton = findViewById(R.id.btnChecklist)
            breathingText = findViewById(R.id.breathingText)
            checklistLayout = findViewById(R.id.checklistLayout)
            checklistLayout.isVisible = false

            btnBreathing.setOnClickListener { toggleBreathingExercise() }
            btnAmbient.setOnClickListener {
                if (isSoundPlaying) stopAmbientSound() else playAmbientSound()
            }
            btnAffirmations.setOnClickListener { showAffirmation() }
            btnChecklist.setOnClickListener { showChecklist() }
        } catch (e: Exception) {
            Log.e("FocusRituals", "Error initializing views: ${e.message}", e)
            Toast.makeText(this, "Error setting up UI", Toast.LENGTH_LONG).show()
            finish()
            return
        }
    }

    private fun toggleBreathingExercise() {
        animateButtonClick(btnBreathing)
        if (isBreathingActive) {
            stopBreathingExercise()
        } else {
            startBreathingExercise()
        }
    }

    private fun startBreathingExercise() {
        Log.d("FocusRituals", "Starting breathing exercise")
        val container = findViewById<View>(R.id.breathingContainer)
        container.visibility = View.VISIBLE
        breathingText.visibility = View.VISIBLE
        container.alpha = 0f
        container.pivotX = container.width / 2f
        container.pivotY = container.height / 2f
        container.animate().alpha(1f).setDuration(300).start()

        btnBreathing.text = "Stop Breathing Ritual"
        isBreathingActive = true

        val animationDuration = 4000L
        val scaleUpX = ObjectAnimator.ofFloat(container, "scaleX", 1f, 1.3f)
        val scaleUpY = ObjectAnimator.ofFloat(container, "scaleY", 1f, 1.3f)
        val scaleDownX = ObjectAnimator.ofFloat(container, "scaleX", 1.3f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(container, "scaleY", 1.3f, 1f)

        scaleUpX.duration = animationDuration / 2
        scaleUpY.duration = animationDuration / 2
        scaleDownX.duration = animationDuration / 2
        scaleDownY.duration = animationDuration / 2

        val inhaleSet = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            interpolator = OvershootInterpolator()
        }

        val exhaleSet = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            interpolator = OvershootInterpolator()
        }

        breathingAnimatorSet = AnimatorSet().apply {
            playSequentially(inhaleSet, exhaleSet)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    breathingText.text = "Inhale"
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (isBreathingActive) {
                        breathingText.text = "Inhale"
                        start()
                    }
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        try {
            breathingAnimatorSet?.start()
        } catch (e: Exception) {
            Log.e("FocusRituals", "Breathing animation failed: ${e.message}", e)
            stopBreathingExercise()
        }
    }

    private fun stopBreathingExercise() {
        Log.d("FocusRituals", "Stopping breathing exercise")
        btnBreathing.text = "Start Breathing Ritual"
        isBreathingActive = false

        breathingAnimatorSet?.cancel()
        breathingAnimatorSet = null

        val container = findViewById<View>(R.id.breathingContainer)
        container.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                breathingText.visibility = View.GONE
                container.visibility = View.GONE
                container.scaleX = 1f
                container.scaleY = 1f
            }
            .start()
    }

    private fun playAmbientSound() {
        animateButtonClick(btnAmbient)
        try {
            ambientPlayer = MediaPlayer.create(this, R.raw.ambient_forest)?.apply {
                isLooping = true
                start()
            }
            isSoundPlaying = true
            btnAmbient.text = "Stop Ambient Sound"
            Toast.makeText(this, "Ambient Sound Started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FocusRituals", "Failed to play ambient sound: ${e.message}", e)
            Toast.makeText(this, "Sound not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAmbientSound() {
        animateButtonClick(btnAmbient)
        try {
            ambientPlayer?.let {
                it.stop()
                it.release()
            }
            ambientPlayer = null
            isSoundPlaying = false
            btnAmbient.text = "Play Ambient Sound"
            Toast.makeText(this, "Ambient Sound Stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FocusRituals", "Failed to stop ambient sound: ${e.message}", e)
        }
    }

    private fun showAffirmation() {
        animateButtonClick(findViewById(R.id.btnAffirmations))
        val affirmations = arrayOf(
            "Your mind is a powerhouse of focus and clarity.",
            "Today, you conquer distractions with unshakable resolve.",
            "Every breath fuels your unstoppable productivity.",
            "You are the master of your attention and destiny.",
            "With every moment, your focus sharpens and shines."
        )

        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.affirmation_dialog, null)
        val affirmationText = dialogView.findViewById<TextView>(R.id.affirmationText)
        val btnFeelThePower = dialogView.findViewById<MaterialButton>(R.id.btnFeelThePower)

        // Set random affirmation
        affirmationText.text = affirmations.random()

        // Create and style dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Animation for dialog entry
        dialogView.alpha = 0f
        dialogView.scaleX = 0.8f
        dialogView.scaleY = 0.8f
        val fadeIn = ObjectAnimator.ofFloat(dialogView, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(dialogView, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(dialogView, "scaleY", 0.8f, 1f)
        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = 300
            interpolator = OvershootInterpolator()
            start()
        }

        // Button click with haptic feedback
        btnFeelThePower.setOnClickListener {
            animateButtonClick(btnFeelThePower)
            dialog.dismiss()
        }

        // Show dialog
        try {
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background
        } catch (e: Exception) {
            Log.e("FocusRituals", "Failed to show affirmation dialog: ${e.message}", e)
            Toast.makeText(this, "Error showing affirmation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChecklist() {
        animateButtonClick(findViewById(R.id.btnChecklist))
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
        checkedCount = 0
        Log.d("Checklist", "Showing checklist, tasks: ${tasks.size}, checkedCount reset to $checkedCount")

        tasks.forEachIndexed { index, task ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 24, 32, 24)
                background = ContextCompat.getDrawable(this@FocusRitualsActivity, R.drawable.rounded_card)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 16, 0, 0) }
                gravity = Gravity.CENTER_VERTICAL
                alpha = 0f
                translationY = 50f
            }

            val checkBox = CheckBox(this).apply {
                text = task
                setTextColor(ContextCompat.getColor(this@FocusRitualsActivity, android.R.color.white))
            }

            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay((index * 100).toLong())
                .start()

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                checkedCount += if (isChecked) 1 else -1
                Log.d("Checklist", "Task: $task, isChecked: $isChecked, checkedCount: $checkedCount")
                if (isChecked) {
                    checkBox.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
                        checkBox.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    }.start()
                }
                if (checkedCount == totalTasks && isChecked) {
                    Log.d("Checklist", "All tasks completed, showing dialog")
                    showChecklistCompleteDialog()
                }
            }

            card.addView(checkBox)
            checklistLayout.addView(card)
        }
    }

    private fun showChecklistCompleteDialog() {
        try {
            AlertDialog.Builder(this)
                .setTitle("Great Job!")
                .setMessage("You've completed all tasks! You're ready to focus.")
                .setPositiveButton("Awesome") { _, _ ->
                    Toast.makeText(this, "Let's get started!", Toast.LENGTH_SHORT).show()
                }
                .show()
        } catch (e: Exception) {
            Log.e("FocusRituals", "Failed to show checklist dialog: ${e.message}", e)
            Toast.makeText(this, "Error showing completion message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateButtonClick(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 100
            start()
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.w("FocusRituals", "Haptic feedback failed: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        ambientPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (isSoundPlaying) {
            ambientPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ambientPlayer?.release()
        breathingAnimatorSet?.cancel()
    }
}