package com.project362.sfuhive.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// adapted from RoomDatabase demo
@Dao
interface AssignmentDatabaseDao {

    @Insert
    suspend fun insertAssignment(assignment: Assignment)

    @Query("SELECT * FROM Assignment_table")
    fun getAllActivities(): Flow<List<Assignment>>

    @Query("SELECT * FROM Assignment_table WHERE id = :key LIMIT 1")
    suspend fun getAssignment(key: Long): Assignment?
}