package com.shubham.nosignal.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.nosignal.domain.NetworkStats
import com.shubham.nosignal.domain.NetworkStatsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel that manages UI state for network statistics
 */
class NetworkStatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = NetworkStatsRepository(application.applicationContext)
    
    private val _networkStats = mutableStateOf(
        NetworkStats(
            downloadSpeed = 0.0,
            uploadSpeed = 0.0,
            dailyDataUsed = 0.0
        )
    )
    val networkStats: State<NetworkStats> = _networkStats
    
    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading
    
    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error
    
    init {
        startNetworkMonitoring()
    }
    
    /**
     * Start collecting network statistics from the repository
     */
    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            repository.getNetworkStats()
                .catch { exception ->
                    _error.value = "Failed to monitor network: ${exception.message}"
                    _isLoading.value = false
                }
                .collect { stats ->
                    _networkStats.value = stats
                    _isLoading.value = false
                    _error.value = null
                }
        }
    }
    
    /**
     * Format speed value for display
     */
    fun formatSpeed(speedKBps: Double): String {
        return when {
            speedKBps >= 1024 -> String.format("%.1f MB/s", speedKBps / 1024)
            speedKBps >= 1 -> String.format("%.1f KB/s", speedKBps)
            else -> String.format("%.0f B/s", speedKBps * 1024)
        }
    }
    
    /**
     * Format data usage for display
     */
    fun formatDataUsage(dataMB: Double): String {
        return when {
            dataMB >= 1024 -> String.format("%.2f GB", dataMB / 1024)
            dataMB >= 1 -> String.format("%.1f MB", dataMB)
            else -> String.format("%.0f KB", dataMB * 1024)
        }
    }
}
