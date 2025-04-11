package com.example.digitaldetox.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.digitaldetox.R
import com.example.digitaldetox.ui.TimetableActivity
import java.util.*
import kotlin.math.sqrt

class StepCounterService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var stepCounter: AccelerometerStepDetector = AccelerometerStepDetector()
    private var goalSteps: Int = 0
    private var timeLimit: Long = 0L
    private var startTime: Long = 0L
    private val CHANNEL_ID = "StepCounterChannel"
    private val NOTIFICATION_ID = 123

    // Timer for updating notification periodically
    private var notificationTimer: Timer? = null
    private var goalCheckTimer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // List all available sensors for debugging
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in allSensors) {
            Log.d("StepCounterService", "Available sensor: ${sensor.name}, type: ${sensor.type}")
        }

        // Try to get step counter sensor first
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            // Device has a step counter sensor, use it
            accelerometerSensor = stepSensor
            Log.d("StepCounterService", "Using dedicated step counter sensor: ${stepSensor.name}")
        } else {
            // Try step detector next
            val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            if (stepDetector != null) {
                accelerometerSensor = stepDetector
                Log.d("StepCounterService", "Using step detector sensor: ${stepDetector.name}")
            } else {
                // Use accelerometer as fallback
                accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                Log.d("StepCounterService", "Using accelerometer for step detection: ${accelerometerSensor?.name}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            goalSteps = intent.getIntExtra("GOAL_STEPS", 0)
            timeLimit = intent.getLongExtra("TIME_LIMIT_MS", 0L)
            startTime = System.currentTimeMillis()

            Log.d("StepCounterService", "Service started with goal: $goalSteps steps, time limit: ${timeLimit/1000} seconds")

            // Reset step counter
            stepCounter.reset()

            // Start as a foreground service with notification
            val notification = createNotification("Step Counter Active",
                "Goal: $goalSteps steps. Progress: 0 steps")
            startForeground(NOTIFICATION_ID, notification)

            // Register the sensor
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d("StepCounterService", "Sensor registered: ${it.name}")
            } ?: run {
                Log.e("StepCounterService", "Accelerometer sensor not available on this device")
                stopSelf()
            }

            // Start periodic notification updates (every 2 seconds)
            notificationTimer = Timer()
            notificationTimer?.schedule(object : TimerTask() {
                override fun run() {
                    updateNotification()
                }
            }, 2000, 2000)

            // Start periodic goal checks (every 5 seconds)
            goalCheckTimer = Timer()
            goalCheckTimer?.schedule(object : TimerTask() {
                override fun run() {
                    // Check if goal was reached
                    checkGoalStatus(false)
                }
            }, 5000, 5000)

            // Start countdown for time limit
            if (timeLimit > 0) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        // Time's up, check if goal was reached
                        Log.d("StepCounterService", "Time limit reached")
                        checkGoalStatus(true)
                    }
                }, timeLimit)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not used for bound services
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            Log.d("StepCounterService", "Sensor event: type=${event.sensor.type}")

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Process accelerometer data to detect steps
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                if (stepCounter.detectStep(x, y, z)) {
                    // A step was detected
                    Log.d("StepCounterService", "Steps counted: ${stepCounter.steps}")

                    // Check if goal is reached immediately on step detection
                    if (stepCounter.steps >= goalSteps) {
                        checkGoalStatus(false)
                    }
                }
            } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val current = event.values[0].toInt()

                // Set the baseline (initial steps) only once
                if (stepCounter.initialSteps == 0) {
                    stepCounter.initialSteps = current
                    Log.d("StepCounterService", "Initial step count set: ${stepCounter.initialSteps}")
                }

                val steps = current - stepCounter.initialSteps

                if (steps >= 0) {
                    stepCounter.steps = steps
                    Log.d("StepCounterService", "Hardware counted steps: ${stepCounter.steps}")

                    // Check if goal is reached immediately on step detection
                    if (stepCounter.steps >= goalSteps) {
                        checkGoalStatus(false)
                    }
                }
            } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                // For step detector sensor, each event.values[0] == 1.0 means one step
                if (event.values[0] == 1.0f) {
                    stepCounter.steps++
                    Log.d("StepCounterService", "Step detector counted: ${stepCounter.steps}")

                    // Check if goal is reached immediately on step detection
                    if (stepCounter.steps >= goalSteps) {
                        checkGoalStatus(false)
                    }
                }
            }
        }
    }

    private fun updateNotification() {
        val progressPercent = ((stepCounter.steps.toFloat() / goalSteps) * 100).toInt().coerceAtMost(100)
        val notification = createNotification(
            "Step Counter: ${stepCounter.steps}/${goalSteps}",
            "Progress: $progressPercent% complete"
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this implementation
    }

    private fun checkGoalStatus(timeUp: Boolean) {
        val elapsed = System.currentTimeMillis() - startTime

        // Log the status for debugging
        Log.d("StepCounterService", "Checking goal status: Steps=${stepCounter.steps}, Goal=$goalSteps, TimeUp=$timeUp")

        if (stepCounter.steps >= goalSteps) {
            // Goal achieved!
            val coins = calculateReward(elapsed)
            saveReward(coins)

            Log.d("StepCounterService", "GOAL COMPLETED! Coins earned: $coins")

            val intent = Intent("STEP_GOAL_COMPLETED")
            intent.putExtra("COINS_EARNED", coins)
            intent.putExtra("STEPS_COMPLETED", stepCounter.steps)
            sendBroadcast(intent)

            // Stop all timers before stopping service
            cleanupAndStop()
        } else if (timeUp) {
            // Time's up but goal not reached
            Log.d("StepCounterService", "GOAL FAILED! Time's up. Steps: ${stepCounter.steps}/${goalSteps}")
            val intent = Intent("STEP_GOAL_FAILED")
            intent.putExtra("STEPS_COMPLETED", stepCounter.steps)
            intent.putExtra("GOAL_STEPS", goalSteps)
            sendBroadcast(intent)

            // Still save partial reward if they completed at least 50% of steps
            if (stepCounter.steps >= goalSteps * 0.5) {
                val partialCoins = calculatePartialReward()
                saveReward(partialCoins)
                intent.putExtra("PARTIAL_COINS", partialCoins)
                Log.d("StepCounterService", "Partial reward: $partialCoins coins")
            }

            // Stop all timers before stopping service
            cleanupAndStop()
        }
    }

    private fun calculateReward(elapsedTime: Long): Int {
        // Enhanced reward calculation
        val baseCoins = 15
        val timeRatio = elapsedTime / timeLimit.toFloat()

        // More coins for completing faster (up to 2x bonus)
        val timeBonus = if (timeRatio < 0.6) {
            // Completed in less than 60% of allotted time - big bonus
            10
        } else if (timeRatio < 0.8) {
            // Completed in less than 80% of allotted time - medium bonus
            5
        } else {
            // Completed in time but slower - small bonus
            2
        }

        // Bonus for exceeding goal (up to 5 additional coins)
        val excessSteps = (stepCounter.steps - goalSteps).coerceAtLeast(0)
        val excessBonus = (excessSteps / (goalSteps * 0.1f)).toInt().coerceAtMost(5)

        return baseCoins + timeBonus + excessBonus
    }

    private fun calculatePartialReward(): Int {
        // Partial reward when time is up but made good progress
        val completionRatio = stepCounter.steps.toFloat() / goalSteps
        val baseCoins = 5

        return (baseCoins * completionRatio).toInt().coerceAtLeast(1)
    }

    private fun saveReward(coins: Int) {
        val prefs = getSharedPreferences("digital_detox_prefs", Context.MODE_PRIVATE)
        val currentCoins = prefs.getInt("user_coins", 0)
        prefs.edit().putInt("user_coins", currentCoins + coins).apply()
        Log.d("StepCounterService", "Saved reward: $coins coins. Total now: ${currentCoins + coins}")
    }

    private fun cleanupAndStop() {
        notificationTimer?.cancel()
        notificationTimer = null
        goalCheckTimer?.cancel()
        goalCheckTimer = null
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("StepCounterService", "Service being destroyed")
        sensorManager.unregisterListener(this)
        notificationTimer?.cancel()
        notificationTimer = null
        goalCheckTimer?.cancel()
        goalCheckTimer = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Counter Service"
            val descriptionText = "Tracks steps for Digital Detox challenges"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lightColor = Color.parseColor("#BB86FC")
                enableLights(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val notificationIntent = Intent(this, TimetableActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_footsteps) // Make sure to create this icon
            .setColor(Color.parseColor("#BB86FC"))
            .setContentIntent(pendingIntent)
            .setProgress(goalSteps, stepCounter.steps, false)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .build()
    }

    /**
     * Class to handle step detection using accelerometer data
     */
    inner class AccelerometerStepDetector {
        var steps: Int = 0
        var initialSteps: Int = 0

        // Parameters for step detection algorithm - more sensitive
        private val THRESHOLD = 8.0f  // Lower threshold for easier detection
        private val STEP_DELAY_NS = 200000000  // Shorter delay (200ms)

        private var accelRingX = FloatArray(10)
        private var accelRingY = FloatArray(10)
        private var accelRingZ = FloatArray(10)
        private var ringIndex = 0
        private var ringFilled = false

        private var lastStepTimeNs: Long = 0
        private var oldMagnitude = 0f

        fun reset() {
            steps = 0
            initialSteps = 0
            ringIndex = 0
            ringFilled = false
            lastStepTimeNs = 0
        }

        fun detectStep(x: Float, y: Float, z: Float): Boolean {
            // Add the latest accelerometer values to the ring buffer
            accelRingX[ringIndex] = x
            accelRingY[ringIndex] = y
            accelRingZ[ringIndex] = z

            ringIndex++
            if (ringIndex >= accelRingX.size) {
                ringIndex = 0
                ringFilled = true
            }

            if (!ringFilled) {
                // Need more data
                return false
            }

            // Calculate average magnitude from the ring buffer
            var sumX = 0f
            var sumY = 0f
            var sumZ = 0f

            for (i in accelRingX.indices) {
                sumX += accelRingX[i]
                sumY += accelRingY[i]
                sumZ += accelRingZ[i]
            }

            val avgX = sumX / accelRingX.size
            val avgY = sumY / accelRingY.size
            val avgZ = sumZ / accelRingZ.size

            // Calculate the current magnitude of acceleration
            val magnitude = sqrt((x - avgX) * (x - avgX) +
                    (y - avgY) * (y - avgY) +
                    (z - avgZ) * (z - avgZ))

            // Detect step pattern (peak in acceleration)
            val timeNs = System.nanoTime()

            if (magnitude > THRESHOLD && oldMagnitude <= THRESHOLD &&
                timeNs - lastStepTimeNs > STEP_DELAY_NS) {
                // We detected a step
                lastStepTimeNs = timeNs
                steps++
                oldMagnitude = magnitude
                return true
            }

            oldMagnitude = magnitude
            return false
        }
    }
}