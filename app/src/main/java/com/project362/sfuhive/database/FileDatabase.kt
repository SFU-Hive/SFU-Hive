package com.project362.sfuhive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// adapted from RoomDatabase demo
@Database(entities = [Assignment::class], version = 3, exportSchema = false)
abstract class FileDatabase : RoomDatabase() {
    abstract val fileDatabaseDoa: FileDatabaseDoa

    companion object {
        @Volatile
        private var INSTANCE: FileDatabase? = null

        fun getInstance(context: Context): FileDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FileDatabase::class.java,
                        "file_table"
                    ).fallbackToDestructiveMigration(false).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}