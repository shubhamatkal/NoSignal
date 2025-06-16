package com.shubham.nosignal.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.nosignal.data.database.SpeedDatabase
import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity
import com.shubham.nosignal.data.repository.SpeedSnapshotRepositoryImpl
import com.shubham.nosignal.domain.repository.SpeedSnapshotRepository
import com.shubham.nosignal.service.SpeedMonitorService
import com.shubham.nosignal.utils.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that manages UI state for network statistics
 */
class NetworkStatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settings = Settings(application.applicationContext)
    private val repository: SpeedSnapshotRepository
    
    // StateFlows from the service
    val downloadSpeed: StateFlow<Float> = SpeedMonitorService.downloadSpeed
    val uploadSpeed: StateFlow<Float> = SpeedMonitorService.uploadSpeed
    val isMonitoring: StateFlow<Boolean> = SpeedMonitorService.isMonitoring
    
    // StateFlow for persistent speed data from Room
    val speedSnapshots: StateFlow<List<SpeedSnapshotEntity>> get() = _speedSnapshots.asStateFlow()
    private val _speedSnapshots = MutableStateFlow<List<SpeedSnapshotEntity>>(emptyList())
    
    // Unit preference
    val isUsingBits: StateFlow<Boolean> get() = _isUsingBits.asStateFlow()
    private val _isUsingBits = MutableStateFlow(settings.isUsingBits())
    
    init {
        // Initialize Room database and repository
        val database = SpeedDatabase.getDatabase(application.applicationContext)
        repository = SpeedSnapshotRepositoryImpl(database.speedSnapshotDao())
        
        // Collect persistent data from Room
        viewModelScope.launch {
            repository.getLatestSnapshots().collect { snapshots ->
                _speedSnapshots.value = snapshots
            }
        }
    }
    
    /**
     * Toggle between bits and bytes display
     */
    fun toggleUnit() {
        val newValue = !_isUsingBits.value
        _isUsingBits.value = newValue
        settings.setUsingBits(newValue)
    }
    
    /**
     * Get download data as List<Float> for charts
     */
    fun getDownloadData(): List<Float> {
        return _speedSnapshots.value.map { it.downloadSpeed }
    }
    
    /**
     * Get upload data as List<Float> for charts
     */
    fun getUploadData(): List<Float> {
        return _speedSnapshots.value.map { it.uploadSpeed }
    }
}
