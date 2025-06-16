package com.shubham.nosignal.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.shubham.nosignal.MainActivity
import com.shubham.nosignal.R
import com.shubham.nosignal.data.database.SpeedDatabase
import com.shubham.nosignal.data.repository.SpeedSnapshotRepositoryImpl
import com.shubham.nosignal.domain.repository.SpeedSnapshotRepository
import com.shubham.nosignal.model.SpeedGraphModel
import com.shubham.nosignal.utils.Settings
import com.shubham.nosignal.utils.UnitFormatter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeedMonitorService : Service() {
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "speed_monitor_channel"
        const val ACTION_START_MONITORING = "action_start_monitoring"
        
        // Shared state for the app
        private val _downloadSpeed = MutableStateFlow(0f)
        val downloadSpeed: StateFlow<Float> = _downloadSpeed
        
        private val _uploadSpeed = MutableStateFlow(0f)
        val uploadSpeed: StateFlow<Float> = _uploadSpeed
        
        private val _isMonitoring = MutableStateFlow(false)
        val isMonitoring: StateFlow<Boolean> = _isMonitoring
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitoringJob: Job? = null
    
    private lateinit var notificationManager: NotificationManager
    private lateinit var settings: Settings
    private lateinit var repository: SpeedSnapshotRepository
    
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTimestamp = 0L
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        settings = Settings(this)
        
        // Initialize Room database and repository
        val database = SpeedDatabase.getDatabase(this)
        repository = SpeedSnapshotRepositoryImpl(database.speedSnapshotDao())
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Always start monitoring when service is started
        startMonitoring()
        return START_STICKY // Restart service if killed by system
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        serviceScope.cancel()
    }
    
    private fun startMonitoring() {
        if (_isMonitoring.value) return
        
        _isMonitoring.value = true
        
        // Initialize baseline values
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTimestamp = System.currentTimeMillis()
        
        startForeground(NOTIFICATION_ID, createNotification(0f, 0f))
        
        monitoringJob = serviceScope.launch {
            while (isActive && _isMonitoring.value) {
                try {
                    updateSpeeds()
                    delay(500) // Poll every 500ms
                } catch (e: Exception) {
                    // Handle any exceptions gracefully
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun stopMonitoring() {
        _isMonitoring.value = false
        
        monitoringJob?.cancel()
        monitoringJob = null
        
        _downloadSpeed.value = 0f
        _uploadSpeed.value = 0f
        
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    
    private suspend fun updateSpeeds() {
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentTimestamp = System.currentTimeMillis()
        
        if (lastRxBytes > 0 && lastTxBytes > 0) {
            val timeDiff = (currentTimestamp - lastTimestamp) / 1000f // Convert to seconds
            
            if (timeDiff > 0) {
                val downloadBytesPerSec = (currentRxBytes - lastRxBytes) / timeDiff
                val uploadBytesPerSec = (currentTxBytes - lastTxBytes) / timeDiff
                
                // Update speeds (in bytes per second)
                _downloadSpeed.value = downloadBytesPerSec
                _uploadSpeed.value = uploadBytesPerSec
                
                // Store data in Room database for persistence
                try {
                    repository.insertSnapshot(
                        timestamp = currentTimestamp,
                        downloadSpeed = downloadBytesPerSec,
                        uploadSpeed = uploadBytesPerSec
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Update notification
                withContext(Dispatchers.Main) {
                    updateNotification(downloadBytesPerSec, uploadBytesPerSec)
                }
            }
        }
        
        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastTimestamp = currentTimestamp
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Speed Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time network speed"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(downloadSpeed: Float, uploadSpeed: Float): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val downloadText = UnitFormatter.formatSpeed(downloadSpeed, settings.isUsingBits())
        val uploadText = UnitFormatter.formatSpeed(uploadSpeed, settings.isUsingBits())
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Network Speed Monitor")
            .setContentText("⬇ $downloadText ⬆ $uploadText")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    private fun updateNotification(downloadSpeed: Float, uploadSpeed: Float) {
        val notification = createNotification(downloadSpeed, uploadSpeed)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
