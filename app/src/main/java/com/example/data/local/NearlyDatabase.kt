package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.NearlyDao
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.UserProfileEntity

@Database(
    entities = [
        SavedPlaceEntity::class,
        SearchHistoryEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NearlyDatabase : RoomDatabase() {

    abstract fun nearlyDao(): NearlyDao

    companion object {
        @Volatile
        private var INSTANCE: NearlyDatabase? = null

        fun getInstance(context: Context): NearlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NearlyDatabase::class.java,
                    "nearly_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
