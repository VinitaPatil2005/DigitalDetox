package com.example.digitaldetox.util

import android.content.Context
import android.content.SharedPreferences

object FocusModeManager {
    private const val PREFS_NAME = "focus_mode_prefs"
    private const val KEY_FOCUS_MODE = "focus_mode_enabled"

    fun isFocusModeEnabled(context: Context): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FOCUS_MODE, false)
    }

    fun setFocusModeEnabled(context: Context, enabled: Boolean) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FOCUS_MODE, enabled).apply()
    }
}
