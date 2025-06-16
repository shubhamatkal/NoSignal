package com.shubham.nosignal.domain.repository

import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for speed snapshots
 */
interface SpeedSnapshotRepository {
    
    /**
     * Insert a new speed snapshot and maintain the 20-entry limit
     */
    suspend fun insertSnapshot(timestamp: Long, downloadSpeed: Float, uploadSpeed: Float)
    
    /**
     * Get the latest 20 snapshots as a Flow for reactive UI updates
     */
    fun getLatestSnapshots(): Flow<List<SpeedSnapshotEntity>>
    
    /**
     * Clear all snapshots
     */
    suspend fun clearAllSnapshots()
}
