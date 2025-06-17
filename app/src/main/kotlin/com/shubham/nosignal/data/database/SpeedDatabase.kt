package com.shubham.nosignal.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.shubham.nosignal.data.database.dao.SpeedSnapshotDao
import com.shubham.nosignal.data.database.dao.SpeedTestResultDao
import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity
import com.shubham.nosignal.model.SpeedTestResult

/**
 * Room database for storing speed snapshots and test results
 */
@Database(
    entities = [SpeedSnapshotEntity::class, SpeedTestResult::class],
    version = 2,
    exportSchema = false
)
abstract class SpeedDatabase : RoomDatabase() {
    
    abstract fun speedSnapshotDao(): SpeedSnapshotDao
    abstract fun speedTestResultDao(): SpeedTestResultDao
    
    companion object {
        @Volatile
        private var INSTANCE: SpeedDatabase? = null
        
        fun getDatabase(context: Context): SpeedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpeedDatabase::class.java,
                    "speed_database"
                ).fallbackToDestructiveMigration() // Allow destructive migration for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
