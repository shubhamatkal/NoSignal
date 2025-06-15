package com.shubham.nosignal.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.nosignal.service.SpeedMonitorService
import com.shubham.nosignal.utils.Settings
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that manages UI state for network statistics
 */
class NetworkStatsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settings = Settings(application.applicationContext)
    
    // StateFlows from the service
    val downloadSpeed: StateFlow<Float> = SpeedMonitorService.downloadSpeed
    val uploadSpeed: StateFlow<Float> = SpeedMonitorService.uploadSpeed
    val isMonitoring: StateFlow<Boolean> = SpeedMonitorService.isMonitoring
    
    // Graph data from service (we'll need to expose this from service)
    val downloadData: StateFlow<List<Float>> get() = _downloadData
    val uploadData: StateFlow<List<Float>> get() = _uploadData
    val isUsingBits: StateFlow<Boolean> get() = _isUsingBits
    
    // Local StateFlows for graph data and unit preference
    private val _downloadData = kotlinx.coroutines.flow.MutableStateFlow<List<Float>>(emptyList())
    private val _uploadData = kotlinx.coroutines.flow.MutableStateFlow<List<Float>>(emptyList())
    private val _isUsingBits = kotlinx.coroutines.flow.MutableStateFlow(settings.isUsingBits())
    
    init {
        // Update local StateFlows when service data changes
        viewModelScope.launch {
            // For now, we'll create mock graph data
            // In a real implementation, you'd get this from the service's graph model
            downloadSpeed.collect { speed ->
                val currentData = _downloadData.value.toMutableList()
                currentData.add(speed)
                if (currentData.size > 120) { // Keep last 60 seconds
                    currentData.removeAt(0)
                }
                _downloadData.value = currentData
            }
        }
        
        viewModelScope.launch {
            uploadSpeed.collect { speed ->
                val currentData = _uploadData.value.toMutableList()
                currentData.add(speed)
                if (currentData.size > 120) { // Keep last 60 seconds
                    currentData.removeAt(0)
                }
                _uploadData.value = currentData
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
}
