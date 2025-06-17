package com.shubham.nosignal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Comprehensive speed test result entity storing all Cloudflare metrics
 */
@Entity(tableName = "speed_test_results")
data class SpeedTestResult(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
    val timestamp: Long,
    val networkType: String, // Wi-Fi: SSID, SIM1/SIM2: operatorName, Offline
    
    // Core bandwidth measurements (in bits per second)
    val downloadBps: Double,
    val uploadBps: Double,
    
    // Latency measurements (in milliseconds)
    val unloadedLatencyMs: Double,
    val loadedDownLatencyMs: Double,
    val loadedUpLatencyMs: Double,
    
    // Jitter measurements (in milliseconds)
    val unloadedJitterMs: Double,
    val loadedDownJitterMs: Double,
    val loadedUpJitterMs: Double,
    
    // Optional advanced metrics
    val packetLossRatio: Double? = null,        // nullable if not tested
    val aimScoreStreaming: Double? = null,      // AIM score for streaming
    val aimScoreGaming: Double? = null,         // AIM score for gaming
    val aimScoreRTC: Double? = null             // AIM score for real-time communication
) {
    // Helper properties for backward compatibility and UI display
    val downloadMbps: Double get() = downloadBps / 1_000_000.0
    val uploadMbps: Double get() = uploadBps / 1_000_000.0
    val latencyMs: Double get() = unloadedLatencyMs // Use unloaded latency as primary
} 