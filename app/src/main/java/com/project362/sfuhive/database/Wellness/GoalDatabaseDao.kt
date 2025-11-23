package com.project362.sfuhive.database.Wellness

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDatabaseDao {
    // initial insert
    @Insert
    suspend fun insertInitialGoals(goals: List<Goal>)

    // get all goals
    @Query("SELECT * FROM goal_table")
    fun getAllGoals(): Flow<List<Goal>>

    // update goal name
    @Query("UPDATE goal_table SET name_column = :goalName WHERE id = :key")
    suspend fun updateGoal(key: Long, goalName: String)

    // update complete count
    @Query("UPDATE goal_table SET completion_count_column = completion_count_column + 1 WHERE id = :key")
    suspend fun incrementCompletionCount(key: Long)

    // update last completion date
    @Query("UPDATE goal_table SET last_completion_date_column = :date WHERE id = :key")
    suspend fun updateLastCompletionDate(key: Long, date: Long)

    // update nfc tag id
    @Query("UPDATE goal_table SET nfc_tag_id_column = :tag WHERE id = :key")
    suspend fun updateNfcTag(key: Long, tag: String?)

    // query entire goal
    @Query("SELECT * FROM goal_table WHERE id = :key LIMIT 1")
    fun getGoalById(key: Long): Flow<Goal>

    // query complete count
    @Query("SELECT completion_count_column FROM goal_table WHERE id=:key LIMIT 1")
    fun getCompletionCount(key: Long): Flow<Int>

    // query last completion date
    @Query("SELECT last_completion_date_column FROM goal_table WHERE id=:key LIMIT 1")
    fun getLastCompletionDateById(key: Long): Flow<Long>

    // query nfc tag id
    @Query("SELECT nfc_tag_id_column FROM goal_table WHERE id=:key LIMIT 1")
    fun getNfcById(key: Long): Flow<String?>

    // do i need to delete goal? i mean i could be it would just display the default card

}