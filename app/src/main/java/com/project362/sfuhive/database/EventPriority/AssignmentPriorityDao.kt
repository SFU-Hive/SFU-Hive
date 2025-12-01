package com.project362.sfuhive.database.EventPriority

import androidx.room.*
import com.project362.sfuhive.database.EventPriority.AssignmentPriority
import kotlinx.coroutines.flow.Flow


 //DAO for assignment priority records.

@Dao
interface AssignmentPriorityDao {

    // Return the stored priority string for an assignment ID, or null if absent
    @Query("SELECT priority FROM assignment_priorities WHERE assignmentId = :id")
    suspend fun getPriority(id: String): String?

    // Insert or replace the priority for a given assignment ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPriority(priority: AssignmentPriority)

    // Reactive query that emits all high-priority assignments; useful for dashboards
    @Query("SELECT * FROM assignment_priorities WHERE priority = 'high'")
    fun getHighPriorityAssignments(): Flow<List<AssignmentPriority>>
}
