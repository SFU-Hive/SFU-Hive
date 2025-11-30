package com.project362.sfuhive.database.EventPriority

import androidx.room.*
import com.project362.sfuhive.database.EventPriority.AssignmentPriority
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentPriorityDao {

    @Query("SELECT priority FROM assignment_priorities WHERE assignmentId = :id")
    suspend fun getPriority(id: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPriority(priority: AssignmentPriority)

    @Query("SELECT * FROM assignment_priorities WHERE priority = 'high'")
    fun getHighPriorityAssignments(): Flow<List<AssignmentPriority>>
}
