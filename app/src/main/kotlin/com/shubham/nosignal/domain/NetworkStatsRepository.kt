package com.shubham.nosignal.domain

import android.content.Context
import com.shubham.nosignal.data.NetworkStatsManager
import com.shubham.nosignal.data.NetworkTrafficData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository that provides network statistics following domain layer principles
 */
class NetworkStatsRepository(context: Context) {
    
    private val networkStatsManager = NetworkStatsManager(context)
    
    /**
     * Exposes a Flow of NetworkStats for the UI layer
     */
    fun getNetworkStats(): Flow<NetworkStats> {
        return networkStatsManager.getNetworkStats().map { trafficData ->
            NetworkStats(
                downloadSpeed = trafficData.downloadSpeedKBps,
                uploadSpeed = trafficData.uploadSpeedKBps,
                dailyDataUsed = trafficData.dailyDataUsedMB
            )
        }
    }
}

/**
 * Domain model representing network statistics
 */
data class NetworkStats(
    val downloadSpeed: Double, // KB/s
    val uploadSpeed: Double,   // KB/s
    val dailyDataUsed: Double  // MB
)
