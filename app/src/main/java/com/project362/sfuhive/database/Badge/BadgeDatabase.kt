package com.project362.sfuhive.database.Badge

import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.Badge.BadgeDatabaseDao
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project362.sfuhive.Progress.Badges.BadgeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// adapted from RoomDatabase demo
@Database(entities = [BadgeEntity::class], version = 1, exportSchema = false)
abstract class BadgeDatabase : RoomDatabase() {

    abstract val badgeDatabaseDao: BadgeDatabaseDao

    companion object {
        private var allBadges = BadgeFactory().getAllBadges()
        @Volatile
        private var INSTANCE: BadgeDatabase? = null
        // Populates database with the badges from BadgeFactory if the badge database doesn't exist
        fun getInstance(context: Context): BadgeDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    Log.d("BadgeDB", "Creating BadgeDatabase instance...")
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        BadgeDatabase::class.java,
                        "badge_table"
                    ).fallbackToDestructiveMigration(false)
                        .build()

                    // Insert badges synchronously
                    runBlocking {
                        Log.d("BadgeDB", "Inserting badges into BadgeDatabase...")
                        for (badge in allBadges) {
                            val existing = instance.badgeDatabaseDao.getBadge(badge.getId())
                            if (existing == null) {
                                val badgeEntity = BadgeEntity(badge.getId(), true)
                                instance.badgeDatabaseDao.insertBadge(badgeEntity)
                                Log.d("BadgeDB", "Inserted badge id=${badge.getId()}")
                            }
                        }
                        Log.d("BadgeDB", "All badges inserted")
                    }

                    INSTANCE = instance
                } else {
                    // An instance of the badge database already exists ==> no need to populate it with badges
                    Log.d("BadgeDB", "Returning existing BadgeDatabase instance")
                }
                return instance
            }
        }
    }
}