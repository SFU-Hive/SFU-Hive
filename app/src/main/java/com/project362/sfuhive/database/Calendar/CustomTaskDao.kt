package com.project362.sfuhive.database.Calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: CustomTaskEntity)
    // IGNORE ensures tasks NEVER replace each other

    @Query("SELECT * FROM custom_task_table ORDER BY date ASC")
    fun getAllTasks(): Flow<List<CustomTaskEntity>>

    @Query("SELECT * FROM custom_task_table WHERE date = :date ORDER BY id ASC")
    fun getTasksForDate(date: String): Flow<List<CustomTaskEntity>>
}
