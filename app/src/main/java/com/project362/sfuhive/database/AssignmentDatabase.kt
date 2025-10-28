package com.project362.sfuhive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// adapted from RoomDatabase demo
@Database(entities = [Assignment::class], version = 1)
abstract class AssignmentDatabase : RoomDatabase() {
    abstract val assignmentDatabaseDao: AssignmentDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: AssignmentDatabase? = null

        fun getInstance(context: Context): AssignmentDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AssignmentDatabase::class.java,
                        "Assignment_table"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}