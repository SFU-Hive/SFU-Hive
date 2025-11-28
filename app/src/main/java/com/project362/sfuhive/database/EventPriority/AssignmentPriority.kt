package com.project362.sfuhive.database.EventPriority

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignment_priorities")
data class AssignmentPriority(
    @PrimaryKey val assignmentId: String,
    val priority: String // "high", "medium", "low"
)