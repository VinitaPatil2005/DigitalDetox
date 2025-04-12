package com.example.digitaldetox.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.example.digitaldetox.R

class BlockScreenActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_block_screen)
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            val intent = Intent(this, BlockScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Disable back press
    }
}
