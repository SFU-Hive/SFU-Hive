package com.project362.sfuhive.database.Calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTaskDao {

    @Insert
    suspend fun insert(task: CustomTaskEntity)

    @Query("SELECT * FROM custom_task_table")
    fun getAllTasks(): Flow<List<CustomTaskEntity>>

    @Query("SELECT * FROM custom_task_table WHERE date = :date")
    fun getTasksForDate(date: String): Flow<List<CustomTaskEntity>>
}
