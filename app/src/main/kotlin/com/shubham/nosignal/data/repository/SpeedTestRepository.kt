package com.shubham.nosignal.data.repository

import com.shubham.nosignal.data.database.dao.SpeedTestDao
import com.shubham.nosignal.data.database.entity.SpeedTestResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing speed test results
 * Provides clean abstraction layer between UI and database
 */
class SpeedTestRepository(private val dao: SpeedTestDao) {

    /**
     * Get the latest 10 speed test results
     */
    fun getRecentResults(): Flow<List<SpeedTestResultEntity>> = dao.getLast10Results()

    /**
     * Get the most recent speed test result
     */
    fun getLatestResult(): Flow<SpeedTestResultEntity?> = dao.getLatestResultFlow()

    /**
     * Save a new speed test result and maintain the 10-result limit
     */
    suspend fun saveResult(result: SpeedTestResultEntity) {
        dao.insert(result)
        dao.deleteOldResults()
    }

    /**
     * Get the count of all results
     */
    suspend fun getResultCount(): Int = dao.getResultCount()

    /**
     * Clear all speed test results
     */
    suspend fun clearAllResults() {
        dao.clearAllResults()
    }

    /**
     * Get results by network type
     */
    fun getResultsByNetworkType(networkType: String, limit: Int = 10): Flow<List<SpeedTestResultEntity>> =
        dao.getResultsByNetworkType(networkType, limit)
} 