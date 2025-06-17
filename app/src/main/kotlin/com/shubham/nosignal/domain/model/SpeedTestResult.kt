package com.shubham.nosignal.domain.model

/**
 * Domain model for speed test results
 * This is used in the UI layer and business logic
 */
data class SpeedTestResult(
    val timestamp: Long,
    val networkType: String,
    val ispName: String,
    val downloadBps: Double,
    val uploadBps: Double,
    val unloadedLatencyMs: Double,
    val loadedDownloadLatencyMs: Double?,
    val loadedUploadLatencyMs: Double?,
    val unloadedJitterMs: Double?,
    val loadedDownloadJitterMs: Double?,
    val loadedUploadJitterMs: Double?,
    val packetLossPercent: Double?,
    val aimScoreStreaming: String?,
    val aimScoreGaming: String?,
    val aimScoreRTC: String?
) {
    // Helper properties for UI display
    val downloadMbps: Double get() = downloadBps / 1_000_000.0
    val uploadMbps: Double get() = uploadBps / 1_000_000.0
    val latencyMs: Double get() = unloadedLatencyMs
} 