package com.shubham.nosignal.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shubham.nosignal.model.SpeedTestResult
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for speed test results
 */
@Dao
interface SpeedTestResultDao {
    
    /**
     * Insert a new speed test result
     */
    @Insert
    suspend fun insertResult(result: SpeedTestResult)
    
    /**
     * Get the latest 10 speed test results ordered by timestamp descending
     */
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 10")
    fun getLatestResults(): Flow<List<SpeedTestResult>>
    
    /**
     * Get the most recent speed test result
     */
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestResult(): SpeedTestResult?
    
    /**
     * Get the most recent speed test result as Flow for reactive UI updates
     */
    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 1")
    fun getLatestResultFlow(): Flow<SpeedTestResult?>
    
    /**
     * Get the count of all speed test results
     */
    @Query("SELECT COUNT(*) FROM speed_test_results")
    suspend fun getResultCount(): Int
    
    /**
     * Delete old results when we have more than the specified limit
     */
    @Query("DELETE FROM speed_test_results WHERE id NOT IN (SELECT id FROM speed_test_results ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trimToLimit(limit: Int = 10)
    
    /**
     * Clear all speed test results (for testing or reset purposes)
     */
    @Query("DELETE FROM speed_test_results")
    suspend fun clearAllResults()
    
    /**
     * Get results by network type for analytics
     */
    @Query("SELECT * FROM speed_test_results WHERE networkType = :networkType ORDER BY timestamp DESC LIMIT :limit")
    fun getResultsByNetworkType(networkType: String, limit: Int = 10): Flow<List<SpeedTestResult>>
} 