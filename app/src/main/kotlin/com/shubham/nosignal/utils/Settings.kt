package com.shubham.nosignal.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing app settings using SharedPreferences
 */
class Settings(private val context: Context) {
    companion object {
        private const val PREF_NAME = "nosignal_settings"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        private const val KEY_USE_BITS = "use_bits"
    }
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Check if speed monitoring is enabled
     */
    fun isMonitoringEnabled(): Boolean {
        return preferences.getBoolean(KEY_MONITORING_ENABLED, false)
    }
    
    /**
     * Set monitoring enabled state
     */
    fun setMonitoringEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_MONITORING_ENABLED, enabled)
            .apply()
    }
    
    /**
     * Check if using bits instead of bytes
     */
    fun isUsingBits(): Boolean {
        return preferences.getBoolean(KEY_USE_BITS, false)
    }
    
    /**
     * Set whether to use bits instead of bytes
     */
    fun setUsingBits(useBits: Boolean) {
        preferences.edit()
            .putBoolean(KEY_USE_BITS, useBits)
            .apply()
    }
}
