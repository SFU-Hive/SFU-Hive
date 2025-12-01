package com.project362.sfuhive.database.Calendar

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project362.sfuhive.database.Calendar.GoogleEventDao
import com.project362.sfuhive.database.Calendar.GoogleEventEntity

/**
 * Room database that stores a local cache of Google Calendar events. This cache
 * allows the app to show Google events offline or quickly after fetching.
 */
@Database(entities = [GoogleEventEntity::class], version = 1)
abstract class GoogleEventDatabase : RoomDatabase() {
    abstract fun googleEventDao(): GoogleEventDao

    companion object {
        @Volatile private var INSTANCE: GoogleEventDatabase? = null

        fun getInstance(context: Context): GoogleEventDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    GoogleEventDatabase::class.java,
                    "google_event_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
