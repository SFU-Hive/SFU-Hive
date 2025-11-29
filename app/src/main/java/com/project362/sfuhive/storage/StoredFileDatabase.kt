package com.project362.sfuhive.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [StoredFileEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StoredFileDatabase : RoomDatabase()
{
    abstract val storedFileDatabaseDao: StoredFileDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: StoredFileDatabase? = null

        fun getInstance(context: Context): StoredFileDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        StoredFileDatabase::class.java,
                        "stored_file_database"
                    )
                        .fallbackToDestructiveMigration(false)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }

        }
    }
}