package com.shubham.nosignal.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shubham.nosignal.service.SpeedMonitorService
import com.shubham.nosignal.utils.Settings
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val settings = Settings(context)
            if (settings.isMonitoringEnabled()) {
                val serviceIntent = Intent(context, SpeedMonitorService::class.java).apply {
                    action = SpeedMonitorService.ACTION_START_MONITORING
                }
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}
