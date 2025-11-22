package com.project362.sfuhive.database.Badge

import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.Badge.BadgeDatabaseDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// adapted from RoomDatabase demo
@Database(entities = [BadgeEntity::class], version = 1, exportSchema = false)
abstract class BadgeDatabase : RoomDatabase() {

    abstract val badgeDatabaseDao: BadgeDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: BadgeDatabase? = null
        fun getInstance(context: Context): BadgeDatabase{
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BadgeDatabase::class.java,
                        "badge_table"
                    ).fallbackToDestructiveMigration(false).build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
