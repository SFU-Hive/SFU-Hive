package com.project362.sfuhive.database.Badge

import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.Badge.BadgeDatabaseDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project362.sfuhive.Progress.Badges.BadgeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

// adapted from RoomDatabase demo
@Database(entities = [BadgeEntity::class], version = 1, exportSchema = false)
abstract class BadgeDatabase : RoomDatabase() {

    abstract val badgeDatabaseDao: BadgeDatabaseDao

    companion object{
        private var allBadges = BadgeFactory().getAllBadges()
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
                    // Initalize the database to contain all of the badges
                    CoroutineScope(IO).launch {
                        for (badge in allBadges) {
                            val badgeEntity = BadgeEntity(badge.getId(), true)
                            instance.badgeDatabaseDao.insertBadge(badgeEntity)
                        }
                    }
                    INSTANCE = instance
                }else{
                    // check to make sure every badge is in the database
                    CoroutineScope(IO).launch {
                        for (badge in allBadges) {

                            if(instance.badgeDatabaseDao.getBadge(badge.getId()) == null){
                                // the instance doesnt exist in our database so we add it
                                val badgeEntity = BadgeEntity(badge.getId(), true)
                                instance.badgeDatabaseDao.insertBadge(badgeEntity)
                            }
                        }
                    }
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
