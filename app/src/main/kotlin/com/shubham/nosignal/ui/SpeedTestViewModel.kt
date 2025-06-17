package com.shubham.nosignal.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.nosignal.data.database.SpeedDatabase
import com.shubham.nosignal.model.SpeedTestResult
import com.shubham.nosignal.utils.NetworkConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel for managing speed test results and network connection state
 */
class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = SpeedDatabase.getDatabase(application)
    private val speedTestResultDao = database.speedTestResultDao()
    private val networkConnectionManager = NetworkConnectionManager(application)
    
    // Network connection state
    private val _networkType = MutableStateFlow("Checking...")
    val networkType: StateFlow<String> = _networkType.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    // Speed test results
    val latestResults: Flow<List<SpeedTestResult>> = speedTestResultDao.getLatestResults()
    val latestResult: Flow<SpeedTestResult?> = speedTestResultDao.getLatestResultFlow()
    
    // UI state for speed test execution
    private val _isRunningTest = MutableStateFlow(false)
    val isRunningTest: StateFlow<Boolean> = _isRunningTest.asStateFlow()
    
    init {
        observeNetworkChanges()
    }
    
    /**
     * Observe network changes and update state
     */
    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkConnectionManager.getNetworkTypeFlow().collect { networkType ->
                _networkType.value = networkType
                _isConnected.value = networkConnectionManager.isConnected()
            }
        }
    }
    
    /**
     * Insert a new speed test result
     */
    fun insertSpeedTestResult(result: SpeedTestResult) {
        viewModelScope.launch {
            speedTestResultDao.insertResult(result)
            // Trim to keep only the latest 10 results
            speedTestResultDao.trimToLimit(10)
        }
    }
    
    /**
     * Create a sample speed test result for testing
     */
    fun createSampleResult() {
        val sampleResult = SpeedTestResult(
            timestamp = System.currentTimeMillis(),
            networkType = _networkType.value,
            downloadBps = (10_000_000..100_000_000).random().toDouble(), // 10-100 Mbps
            uploadBps = (5_000_000..50_000_000).random().toDouble(),     // 5-50 Mbps
            unloadedLatencyMs = (10..50).random().toDouble(),
            loadedDownLatencyMs = (20..80).random().toDouble(),
            loadedUpLatencyMs = (25..90).random().toDouble(),
            unloadedJitterMs = (1..10).random().toDouble(),
            loadedDownJitterMs = (2..15).random().toDouble(),
            loadedUpJitterMs = (3..20).random().toDouble(),
            packetLossRatio = (0..5).random().toDouble() / 100.0, // 0-5%
            aimScoreStreaming = (50..100).random().toDouble(),
            aimScoreGaming = (40..95).random().toDouble(),
            aimScoreRTC = (45..90).random().toDouble()
        )
        insertSpeedTestResult(sampleResult)
    }
    
    /**
     * Start a speed test (placeholder for actual implementation)
     */
    fun startSpeedTest() {
        if (_isRunningTest.value || !_isConnected.value) return
        
        viewModelScope.launch {
            _isRunningTest.value = true
            
            try {
                // TODO: Implement actual Cloudflare speed test logic here
                // For now, simulate a test with delay
                kotlinx.coroutines.delay(3000) // 3-second simulation
                
                // Create a result with current network type
                val result = SpeedTestResult(
                    timestamp = System.currentTimeMillis(),
                    networkType = _networkType.value,
                    downloadBps = (20_000_000..150_000_000).random().toDouble(),
                    uploadBps = (10_000_000..75_000_000).random().toDouble(),
                    unloadedLatencyMs = (5..40).random().toDouble(),
                    loadedDownLatencyMs = (15..70).random().toDouble(),
                    loadedUpLatencyMs = (20..85).random().toDouble(),
                                unloadedJitterMs = (0.5 + (8.0 - 0.5) * Math.random()),
            loadedDownJitterMs = (1.0 + (12.0 - 1.0) * Math.random()),
            loadedUpJitterMs = (1.5 + (18.0 - 1.5) * Math.random()),
                    packetLossRatio = (0..3).random().toDouble() / 100.0,
                    aimScoreStreaming = (60..100).random().toDouble(),
                    aimScoreGaming = (50..98).random().toDouble(),
                    aimScoreRTC = (55..95).random().toDouble()
                )
                
                insertSpeedTestResult(result)
            } finally {
                _isRunningTest.value = false
            }
        }
    }
    
    /**
     * Clear all speed test results
     */
    fun clearAllResults() {
        viewModelScope.launch {
            speedTestResultDao.clearAllResults()
        }
    }
    
    /**
     * Get current network type immediately (synchronous)
     */
    fun getCurrentNetworkType(): String {
        return networkConnectionManager.getCurrentNetworkType()
    }
    
    /**
     * Refresh network state manually
     */
    fun refreshNetworkState() {
        _networkType.value = networkConnectionManager.getCurrentNetworkType()
        _isConnected.value = networkConnectionManager.isConnected()
    }
} 