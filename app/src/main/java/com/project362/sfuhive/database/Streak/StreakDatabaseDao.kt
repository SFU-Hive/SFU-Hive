package com.project362.sfuhive.database.Streak
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDatabaseDao {

    // insert sterak Entity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streakEntity : StreakEntity)

    // get all of the streaks of a certain type: e.g "login", "goals", "assignments submitted"
    @Query("SELECT * FROM streak_table WHERE type= :requestType")
    fun getStreaksOfType(requestType : String): Flow<List<StreakEntity?>>

    // Get a flow of all streaks
    @Query("SELECT * FROM streak_table")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    // Reset and delete all of the streak type in the table
    @Query("DELETE FROM streak_table WHERE type= :requestType")
    fun deleteStreaksOfType(requestType : String)

    // reset the entire streak table
    @Query("DELETE FROM streak_table")
    fun deleteAll()


}