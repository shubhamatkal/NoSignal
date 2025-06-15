package com.shubham.nosignal.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Model class for managing speed graph data with a time window buffer
 */
class SpeedGraphModel {
    companion object {
        private const val MAX_SAMPLES = 120 // 60 seconds at 500ms intervals
    }
    
    // Thread-safe queues for storing speed samples
    private val downloadSamples = ConcurrentLinkedQueue<Float>()
    private val uploadSamples = ConcurrentLinkedQueue<Float>()
    
    // StateFlow for UI updates
    private val _downloadData = MutableStateFlow<List<Float>>(emptyList())
    val downloadData: StateFlow<List<Float>> = _downloadData.asStateFlow()
    
    private val _uploadData = MutableStateFlow<List<Float>>(emptyList())
    val uploadData: StateFlow<List<Float>> = _uploadData.asStateFlow()
    
    private val _currentDownloadSpeed = MutableStateFlow(0f)
    val currentDownloadSpeed: StateFlow<Float> = _currentDownloadSpeed.asStateFlow()
    
    private val _currentUploadSpeed = MutableStateFlow(0f)
    val currentUploadSpeed: StateFlow<Float> = _currentUploadSpeed.asStateFlow()
    
    /**
     * Add new speed sample to the buffer
     * @param downloadSpeed Download speed in bytes/s
     * @param uploadSpeed Upload speed in bytes/s
     */
    fun addSample(downloadSpeed: Float, uploadSpeed: Float) {
        // Add new samples
        downloadSamples.offer(downloadSpeed)
        uploadSamples.offer(uploadSpeed)
        
        // Remove old samples if buffer is full
        while (downloadSamples.size > MAX_SAMPLES) {
            downloadSamples.poll()
        }
        while (uploadSamples.size > MAX_SAMPLES) {
            uploadSamples.poll()
        }
        
        // Update current speeds
        _currentDownloadSpeed.value = downloadSpeed
        _currentUploadSpeed.value = uploadSpeed
        
        // Update StateFlow with current data
        _downloadData.value = downloadSamples.toList()
        _uploadData.value = uploadSamples.toList()
    }
    
    /**
     * Clear all samples
     */
    fun clearSamples() {
        downloadSamples.clear()
        uploadSamples.clear()
        _downloadData.value = emptyList()
        _uploadData.value = emptyList()
        _currentDownloadSpeed.value = 0f
        _currentUploadSpeed.value = 0f
    }
    
    /**
     * Get current download samples as List
     */
    fun getDownloadSamples(): List<Float> = downloadSamples.toList()
    
    /**
     * Get current upload samples as List
     */
    fun getUploadSamples(): List<Float> = uploadSamples.toList()
    
    /**
     * Get the maximum value from all current samples for scaling graphs
     */
    fun getMaxValue(): Float {
        val maxDownload = downloadSamples.maxOrNull() ?: 0f
        val maxUpload = uploadSamples.maxOrNull() ?: 0f
        return maxOf(maxDownload, maxUpload, 1f) // Minimum 1 to avoid division by zero
    }
    
    /**
     * Get average download speed from current samples
     */
    fun getAverageDownloadSpeed(): Float {
        return if (downloadSamples.isEmpty()) 0f else downloadSamples.average().toFloat()
    }
    
    /**
     * Get average upload speed from current samples
     */
    fun getAverageUploadSpeed(): Float {
        return if (uploadSamples.isEmpty()) 0f else uploadSamples.average().toFloat()
    }
}
