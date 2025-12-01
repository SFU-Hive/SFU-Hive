package com.project362.sfuhive.database.Calendar

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 Room entity that stores a small subset of Google Calendar event data
 * needed by the app:
  * the Google event ID,
  * title,
  * date string in yyyy-MM-dd format
 */
@Entity(tableName = "google_events")
data class GoogleEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,        // Google event ID
    val title: String,
    val date: String,           // "yyyy-MM-dd"
    val startTime: String?,     // optional
    val endTime: String?        // optional
)
