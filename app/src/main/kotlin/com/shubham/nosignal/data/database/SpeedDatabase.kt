package com.shubham.nosignal.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.shubham.nosignal.data.database.dao.SpeedSnapshotDao
import com.shubham.nosignal.data.database.entity.SpeedSnapshotEntity

/**
 * Room database for storing speed snapshots
 */
@Database(
    entities = [SpeedSnapshotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpeedDatabase : RoomDatabase() {
    
    abstract fun speedSnapshotDao(): SpeedSnapshotDao
    
    companion object {
        @Volatile
        private var INSTANCE: SpeedDatabase? = null
        
        fun getDatabase(context: Context): SpeedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpeedDatabase::class.java,
                    "speed_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
