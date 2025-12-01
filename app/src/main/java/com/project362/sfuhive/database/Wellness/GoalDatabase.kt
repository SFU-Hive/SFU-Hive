package com.project362.sfuhive.Wellness

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project362.sfuhive.database.Badge.BadgeDatabaseDao
import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.Wellness.Goal
import com.project362.sfuhive.database.Wellness.GoalDatabaseDao
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking


@Database(entities = [Goal::class, BadgeEntity::class], version = 2, exportSchema = false)
abstract class GoalDatabase : RoomDatabase() {
    abstract fun goalDatabaseDao(): GoalDatabaseDao
    abstract fun badgeDatabaseDao(): BadgeDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: GoalDatabase? = null

        fun getInstance(context: Context): GoalDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    Log.d("GoalDB", "Creating GoalDatabase instance...")
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GoalDatabase::class.java,
                        "goal_table"
                    ).fallbackToDestructiveMigration(true)
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    suspend fun initializeDefaultGoals() {
        val badgeDao = badgeDatabaseDao()
        val dao = goalDatabaseDao()

        // ensure badges exist first before inserting goals, otherwise FK constraint error
        for (id in 1..3L) {
            val existing = badgeDao.getBadge(id)
            if (existing == null) {
                badgeDao.insertBadge(BadgeEntity(id, true))
                Log.d("GoalDB", "Inserted badge id=$id into GoalDatabase")
            }
        }

        // want to insert 3 goals right away, there can only be 3 goals in this database
        val existingGoals = dao.getAllGoals().firstOrNull() ?: emptyList()
        if (existingGoals.isEmpty()) {
            Log.d("GoalDB", "No goals found, inserting default goals...")
            val defaultGoals = listOf(
                Goal(lastCompletionDate = 0L, badgeId = 1L),
                Goal(lastCompletionDate = 0L, badgeId = 2L),
                Goal(lastCompletionDate = 0L, badgeId = 3L)
            )
            dao.insertInitialGoals(defaultGoals)
            Log.d("GoalDB", "Inserted default goals successfully")
        }
    }
}