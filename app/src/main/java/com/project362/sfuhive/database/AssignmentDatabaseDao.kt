package com.project362.sfuhive.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// adapted from RoomDatabase demo
@Dao
interface AssignmentDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment)

    @Query("DELETE FROM assignment_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM assignment_table")
    fun getAllActivities(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignment_table WHERE assignmentId = :key LIMIT 1")
    suspend fun getAssignment(key: Long): Assignment?
}