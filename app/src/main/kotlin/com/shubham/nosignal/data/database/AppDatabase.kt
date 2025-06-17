package com.shubham.nosignal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shubham.nosignal.data.database.dao.SpeedSnapshotDao
import com.shubham.nosignal.data.database.dao.SpeedTestDao
import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity
import com.shubham.nosignal.data.database.entity.SpeedTestResultEntity

/**
 * Main Room database for the NoSignal app
 * Contains speed test results and speed snapshots
 */
@Database(
    entities = [
        SpeedTestResultEntity::class,
        SpeedSnapshotEntity::class
    ],
    version = 3, // Incremented version for new entity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun speedTestDao(): SpeedTestDao
    abstract fun speedSnapshotDao(): SpeedSnapshotDao

    companion object {
        @Volatile 
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "speed_test_db"
                )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build().also { INSTANCE = it }
            }
    }
} 