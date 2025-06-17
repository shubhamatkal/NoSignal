package com.shubham.nosignal.data.mapper

import com.shubham.nosignal.data.database.entity.SpeedTestResultEntity
import com.shubham.nosignal.domain.model.SpeedTestResult

/**
 * Mapper functions to convert between domain model and database entity
 */

/**
 * Convert domain model to database entity
 */
fun SpeedTestResult.toEntity(): SpeedTestResultEntity = SpeedTestResultEntity(
    timestamp = timestamp,
    networkType = networkType,
    ispName = ispName,
    downloadBps = downloadBps,
    uploadBps = uploadBps,
    unloadedLatencyMs = unloadedLatencyMs,
    loadedDownloadLatencyMs = loadedDownloadLatencyMs,
    loadedUploadLatencyMs = loadedUploadLatencyMs,
    unloadedJitterMs = unloadedJitterMs,
    loadedDownloadJitterMs = loadedDownloadJitterMs,
    loadedUploadJitterMs = loadedUploadJitterMs,
    packetLossPercent = packetLossPercent,
    aimScoreStreaming = aimScoreStreaming,
    aimScoreGaming = aimScoreGaming,
    aimScoreRTC = aimScoreRTC
)

/**
 * Convert database entity to domain model
 */
fun SpeedTestResultEntity.toDomain(): SpeedTestResult = SpeedTestResult(
    timestamp = timestamp,
    networkType = networkType,
    ispName = ispName,
    downloadBps = downloadBps,
    uploadBps = uploadBps,
    unloadedLatencyMs = unloadedLatencyMs,
    loadedDownloadLatencyMs = loadedDownloadLatencyMs,
    loadedUploadLatencyMs = loadedUploadLatencyMs,
    unloadedJitterMs = unloadedJitterMs,
    loadedDownloadJitterMs = loadedDownloadJitterMs,
    loadedUploadJitterMs = loadedUploadJitterMs,
    packetLossPercent = packetLossPercent,
    aimScoreStreaming = aimScoreStreaming,
    aimScoreGaming = aimScoreGaming,
    aimScoreRTC = aimScoreRTC
)

/**
 * Convert list of entities to list of domain models
 */
fun List<SpeedTestResultEntity>.toDomain(): List<SpeedTestResult> = map { it.toDomain() }

/**
 * Convert list of domain models to list of entities
 */
fun List<SpeedTestResult>.toEntity(): List<SpeedTestResultEntity> = map { it.toEntity() } 