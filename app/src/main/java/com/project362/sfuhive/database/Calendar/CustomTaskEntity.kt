package com.project362.sfuhive.database.Calendar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_task_table")
data class CustomTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,   // MUST remain 0 so Room autogenerates a new ID

    val title: String,
    val date: String,     // yyyy-MM-dd
    val startTime: String?,
    val endTime: String?
)
