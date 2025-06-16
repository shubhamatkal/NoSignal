package com.shubham.nosignal.data.repository

import com.shubham.nosignal.data.database.dao.SpeedSnapshotDao
import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity
import com.shubham.nosignal.domain.repository.SpeedSnapshotRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of SpeedSnapshotRepository using Room database
 */
class SpeedSnapshotRepositoryImpl(
    private val dao: SpeedSnapshotDao
) : SpeedSnapshotRepository {
    
    override suspend fun insertSnapshot(timestamp: Long, downloadSpeed: Float, uploadSpeed: Float) {
        // Insert the new snapshot
        val snapshot = SpeedSnapshotEntity(
            timestamp = timestamp,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed
        )
        dao.insertSnapshot(snapshot)
        
        // Maintain the 20-entry limit by trimming old entries
        dao.trimToLimit(20)
    }
    
    override fun getLatestSnapshots(): Flow<List<SpeedSnapshotEntity>> {
        return dao.getLatestSnapshots()
    }
    
    override suspend fun clearAllSnapshots() {
        dao.clearAllSnapshots()
    }
}
