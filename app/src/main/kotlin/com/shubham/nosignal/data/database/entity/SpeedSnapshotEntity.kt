package com.shubham.nosignal.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a snapshot of network speed at a specific timestamp
 */
@Entity(tableName = "speed_snapshots")
data class SpeedSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val downloadSpeed: Float, // bytes per second
    val uploadSpeed: Float    // bytes per second
)
