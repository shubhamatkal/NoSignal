package com.shubham.nosignal.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for speed snapshots
 */
@Dao
interface SpeedSnapshotDao {
    
    /**
     * Insert a new speed snapshot
     */
    @Insert
    suspend fun insertSnapshot(snapshot: SpeedSnapshotEntity)
    
    /**
     * Get the latest 20 snapshots (for minimalistic chart display)
     * Ordered by timestamp in ascending order for proper chart display
     */
    @Query("SELECT * FROM speed_snapshots ORDER BY timestamp ASC LIMIT 20")
    fun getLatestSnapshots(): Flow<List<SpeedSnapshotEntity>>
    
    /**
     * Get the count of snapshots
     */
    @Query("SELECT COUNT(*) FROM speed_snapshots")
    suspend fun getSnapshotCount(): Int
    
    /**
     * Delete the oldest snapshot (used when we exceed the 20 limit)
     */
    @Query("DELETE FROM speed_snapshots WHERE id = (SELECT MIN(id) FROM speed_snapshots)")
    suspend fun deleteOldestSnapshot()
    
    /**
     * Delete old snapshots when we have more than the specified limit
     */
    @Query("DELETE FROM speed_snapshots WHERE id NOT IN (SELECT id FROM speed_snapshots ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun trimToLimit(limit: Int = 20)
    
    /**
     * Clear all snapshots (for testing or reset purposes)
     */
    @Query("DELETE FROM speed_snapshots")
    suspend fun clearAllSnapshots()
}
