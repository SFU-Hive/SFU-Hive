package com.project362.sfuhive.database.Calendar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CustomTaskEntity::class], version = 1, exportSchema = false)
abstract class CustomTaskDatabase : RoomDatabase() {

    abstract fun customDao(): CustomTaskDao

    companion object {
        @Volatile
        private var INSTANCE: CustomTaskDatabase? = null

        fun getInstance(context: Context): CustomTaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CustomTaskDatabase::class.java,
                    "custom_task_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
