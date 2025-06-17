package com.shubham.nosignal.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing speed test results in the database
 */
@Entity(tableName = "speed_test_results_entity")
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
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
) 