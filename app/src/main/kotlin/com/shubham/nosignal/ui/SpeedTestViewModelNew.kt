package com.shubham.nosignal.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.nosignal.data.database.AppDatabase
import com.shubham.nosignal.data.mapper.toDomain
import com.shubham.nosignal.data.mapper.toEntity
import com.shubham.nosignal.data.repository.SpeedTestRepository
import com.shubham.nosignal.domain.model.SpeedTestResult
import com.shubham.nosignal.utils.NetworkConnectionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for managing speed test results using Clean Architecture
 */
class SpeedTestViewModelNew(application: Application) : AndroidViewModel(application) {
    
    private val dao = AppDatabase.getInstance(application).speedTestDao()
    private val repo = SpeedTestRepository(dao)
    private val networkConnectionManager = NetworkConnectionManager(application)
    
    // Network connection state
    private val _networkType = MutableStateFlow("Checking...")
    val networkType: StateFlow<String> = _networkType.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    // Speed test results using domain models
    val recentResults: Flow<List<SpeedTestResult>> = repo.getRecentResults().map { entities ->
        entities.toDomain()
    }
    
    val latestResult: Flow<SpeedTestResult?> = repo.getLatestResult().map { entity ->
        entity?.toDomain()
    }
    
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
     * Store a new speed test result using the repository
     */
    fun storeResult(result: SpeedTestResult) = viewModelScope.launch {
        repo.saveResult(result.toEntity())
    }
    
    /**
     * Create a sample speed test result for testing
     */
    fun createSampleResult() {
        val sampleResult = SpeedTestResult(
            timestamp = System.currentTimeMillis(),
            networkType = _networkType.value,
            ispName = "Sample ISP", // Add ISP name
            downloadBps = (10_000_000..100_000_000).random().toDouble(), // 10-100 Mbps
            uploadBps = (5_000_000..50_000_000).random().toDouble(),     // 5-50 Mbps
            unloadedLatencyMs = (10..50).random().toDouble(),
            loadedDownloadLatencyMs = (20..80).random().toDouble(),
            loadedUploadLatencyMs = (25..90).random().toDouble(),
            unloadedJitterMs = (1..10).random().toDouble(),
            loadedDownloadJitterMs = (2..15).random().toDouble(),
            loadedUploadJitterMs = (3..20).random().toDouble(),
            packetLossPercent = (0..5).random().toDouble(), // 0-5%
            aimScoreStreaming = "Excellent", // String format for AIM scores
            aimScoreGaming = "Good",
            aimScoreRTC = "Fair"
        )
        storeResult(sampleResult)
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
                    ispName = "Demo ISP", // Will be replaced with actual ISP detection
                    downloadBps = (20_000_000..150_000_000).random().toDouble(),
                    uploadBps = (10_000_000..75_000_000).random().toDouble(),
                    unloadedLatencyMs = (5..40).random().toDouble(),
                    loadedDownloadLatencyMs = (15..70).random().toDouble(),
                    loadedUploadLatencyMs = (20..85).random().toDouble(),
                    unloadedJitterMs = (0.5 + (8.0 - 0.5) * Math.random()),
                    loadedDownloadJitterMs = (1.0 + (12.0 - 1.0) * Math.random()),
                    loadedUploadJitterMs = (1.5 + (18.0 - 1.5) * Math.random()),
                    packetLossPercent = (0..3).random().toDouble(),
                    aimScoreStreaming = listOf("Excellent", "Good", "Fair").random(),
                    aimScoreGaming = listOf("Excellent", "Good", "Fair").random(),
                    aimScoreRTC = listOf("Excellent", "Good", "Fair").random()
                )
                
                storeResult(result)
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
            repo.clearAllResults()
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
    
    /**
     * Get result count for analytics
     */
    suspend fun getResultCount(): Int = repo.getResultCount()
} 