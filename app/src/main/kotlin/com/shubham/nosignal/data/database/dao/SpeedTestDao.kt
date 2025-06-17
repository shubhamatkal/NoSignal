package com.shubham.nosignal.data.database.dao

import androidx.room.*
import com.shubham.nosignal.data.database.entity.SpeedTestResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for speed test results
 */
@Dao
interface SpeedTestDao {
    
    /**
     * Insert a new speed test result
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: SpeedTestResultEntity)
    
    /**
     * Get the latest 10 speed test results ordered by timestamp descending
     */
    @Query("SELECT * FROM speed_test_results_entity ORDER BY timestamp DESC LIMIT 10")
    fun getLast10Results(): Flow<List<SpeedTestResultEntity>>
    
    /**
     * Get the most recent speed test result
     */
    @Query("SELECT * FROM speed_test_results_entity ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestResult(): SpeedTestResultEntity?
    
    /**
     * Get the most recent speed test result as Flow for reactive UI updates
     */
    @Query("SELECT * FROM speed_test_results_entity ORDER BY timestamp DESC LIMIT 1")
    fun getLatestResultFlow(): Flow<SpeedTestResultEntity?>
    
    /**
     * Delete old results when we have more than the specified limit
     */
    @Query("DELETE FROM speed_test_results_entity WHERE id NOT IN (SELECT id FROM speed_test_results_entity ORDER BY timestamp DESC LIMIT 10)")
    suspend fun deleteOldResults()
    
    /**
     * Get the count of all speed test results
     */
    @Query("SELECT COUNT(*) FROM speed_test_results_entity")
    suspend fun getResultCount(): Int
    
    /**
     * Clear all speed test results
     */
    @Query("DELETE FROM speed_test_results_entity")
    suspend fun clearAllResults()
    
    /**
     * Get results by network type for analytics
     */
    @Query("SELECT * FROM speed_test_results_entity WHERE networkType = :networkType ORDER BY timestamp DESC LIMIT :limit")
    fun getResultsByNetworkType(networkType: String, limit: Int = 10): Flow<List<SpeedTestResultEntity>>
} 