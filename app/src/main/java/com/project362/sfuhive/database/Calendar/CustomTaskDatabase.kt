package com.project362.sfuhive.database.Calendar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database storing `CustomTaskEntity` rows created by the user.
 * The builder uses destructive migration fallback so schema mismatches reset the DB.
 * (this is intentional for the app's simple use-case)
 */
@Database(entities = [CustomTaskEntity::class], version = 1, exportSchema = false)
abstract class CustomTaskDatabase : RoomDatabase() {

    abstract fun customDao(): CustomTaskDao

    companion object {
        @Volatile private var INSTANCE: CustomTaskDatabase? = null

        fun getInstance(context: Context): CustomTaskDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CustomTaskDatabase::class.java,
                    "custom_tasks.db"     // DO NOT CHANGE NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
