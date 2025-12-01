package com.project362.sfuhive.database.EventPriority

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room entity that stores a mapping from an assignment (or event) identifier to a user-assigned priority string.
@Entity(tableName = "assignment_priorities")
data class AssignmentPriority(
    @PrimaryKey val assignmentId: String,
    val priority: String // "high", "medium", "low"
)