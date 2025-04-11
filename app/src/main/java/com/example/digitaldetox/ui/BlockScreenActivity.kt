package com.example.digitaldetox.ui

import android.app.Activity
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

    override fun onBackPressed() {
        // Disable back press
    }
}
