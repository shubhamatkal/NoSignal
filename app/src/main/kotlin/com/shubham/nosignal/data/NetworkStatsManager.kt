package com.shubham.nosignal.data

import android.content.Context
import android.content.SharedPreferences
import android.net.TrafficStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages network statistics by polling TrafficStats API and handling daily reset logic
 */
class NetworkStatsManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("network_stats", Context.MODE_PRIVATE)
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTimestamp = 0L
    
    companion object {
        private const val POLL_INTERVAL_MS = 1000L // 1 second
        private const val BYTES_TO_KB = 1024.0
    }
    
    /**
     * Returns a Flow that emits NetworkTrafficData every second
     */
    fun getNetworkStats(): Flow<NetworkTrafficData> = flow {
        initializeDailyCounters()
        
        while (true) {
            val currentTimestamp = System.currentTimeMillis()
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()
            
            // Skip first reading to establish baseline
            if (lastTimestamp > 0 && lastRxBytes > 0 && lastTxBytes > 0) {
                val timeDiffSeconds = (currentTimestamp - lastTimestamp) / 1000.0
                
                if (timeDiffSeconds > 0) {
                    val rxDiff = currentRxBytes - lastRxBytes
                    val txDiff = currentTxBytes - lastTxBytes
                    
                    val downloadSpeedKBps = (rxDiff / timeDiffSeconds) / BYTES_TO_KB
                    val uploadSpeedKBps = (txDiff / timeDiffSeconds) / BYTES_TO_KB
                    
                    val dailyDataUsedMB = getDailyDataUsage(currentRxBytes, currentTxBytes)
                    
                    emit(
                        NetworkTrafficData(
                            downloadSpeedKBps = maxOf(0.0, downloadSpeedKBps),
                            uploadSpeedKBps = maxOf(0.0, uploadSpeedKBps),
                            dailyDataUsedMB = dailyDataUsedMB
                        )
                    )
                }
            }
            
            // Update for next iteration
            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
            lastTimestamp = currentTimestamp
            
            delay(POLL_INTERVAL_MS)
        }
    }
    
    /**
     * Initialize or reset daily counters based on current date
     */
    private fun initializeDailyCounters() {
        val today = dateFormat.format(Date())
        val storedDate = sharedPreferences.getString("last_date", "")
        
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        
        // If it's a new day or first run, reset counters
        if (storedDate != today) {
            sharedPreferences.edit()
                .putString("last_date", today)
                .putLong("usage_start_rx_$today", currentRxBytes)
                .putLong("usage_start_tx_$today", currentTxBytes)
                .apply()
        }
    }
    
    /**
     * Calculate daily data usage in MB
     */
    private fun getDailyDataUsage(currentRxBytes: Long, currentTxBytes: Long): Double {
        val today = dateFormat.format(Date())
        val startRxBytes = sharedPreferences.getLong("usage_start_rx_$today", currentRxBytes)
        val startTxBytes = sharedPreferences.getLong("usage_start_tx_$today", currentTxBytes)
        
        val dailyRxBytes = currentRxBytes - startRxBytes
        val dailyTxBytes = currentTxBytes - startTxBytes
        val totalDailyBytes = dailyRxBytes + dailyTxBytes
        
        return maxOf(0.0, totalDailyBytes / (1024.0 * 1024.0)) // Convert to MB
    }
}

/**
 * Data class representing network traffic statistics
 */
data class NetworkTrafficData(
    val downloadSpeedKBps: Double,
    val uploadSpeedKBps: Double,
    val dailyDataUsedMB: Double
)
