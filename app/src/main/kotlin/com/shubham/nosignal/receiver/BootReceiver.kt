package com.shubham.nosignal.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shubham.nosignal.service.SpeedMonitorService
import androidx.core.content.ContextCompat

/**
 * Boot receiver that automatically starts the SpeedMonitorService when device boots up
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Always start the service on boot
            val serviceIntent = Intent(context, SpeedMonitorService::class.java).apply {
                action = SpeedMonitorService.ACTION_START_MONITORING
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
