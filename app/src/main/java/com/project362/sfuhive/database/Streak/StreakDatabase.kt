package com.project362.sfuhive.database.Streak

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project362.sfuhive.Wellness.GoalDatabase

@Database(entities = [StreakEntity::class], version =1 , exportSchema = false)
abstract class StreakDatabase : RoomDatabase(){

    abstract val streakDatabaseDao : StreakDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: StreakDatabase?= null

        fun getInstance(context: Context) : StreakDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance ==null){
                    Log.d("StreakDB","Creating StreakDatabase instance...")
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        StreakDatabase::class.java,
                        "streak_table"
                    ).fallbackToDestructiveMigration(true).build()
                    INSTANCE = instance
                }
                return instance
            }

        }

    }
}