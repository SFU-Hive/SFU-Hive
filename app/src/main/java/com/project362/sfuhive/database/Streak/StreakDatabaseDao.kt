package com.project362.sfuhive.database.Streak
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streakEntity : StreakEntity)

    // get all of the streaks of a certian type
    @Query("SELECT * FROM streak_table WHERE type= :requestType")
    fun getStreaksOfType(requestType : String): Flow<List<StreakEntity?>>

    @Query("SELECT * FROM streak_table")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    @Query("DELETE FROM streak_table WHERE type= :requestType")
    fun deleteStreaksOfType(requestType : String)

    @Query("DELETE FROM streak_table")
    fun deleteAll()


}