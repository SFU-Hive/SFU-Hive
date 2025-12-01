package com.project362.sfuhive.database.EventPriority

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


 // Room database that stores `AssignmentPriority` entities.
@Database(
    entities = [AssignmentPriority::class],
    version = 1,
    exportSchema = false
)
abstract class EventPriorityDatabase : RoomDatabase() {

    abstract fun assignmentPriorityDao(): AssignmentPriorityDao

    companion object {
        @Volatile private var INSTANCE: EventPriorityDatabase? = null

        fun getInstance(context: Context): EventPriorityDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    EventPriorityDatabase::class.java,
                    "event_priority_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
