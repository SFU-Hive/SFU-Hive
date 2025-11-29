package com.project362.sfuhive.database.Calendar

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GoogleEventDao {

    @Query("SELECT * FROM google_events")
    suspend fun getAllEvents(): List<GoogleEventEntity>

    @Query("SELECT * FROM google_events WHERE date = :date")
    suspend fun getEventsForDate(date: String): List<GoogleEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<GoogleEventEntity>)

    @Query("DELETE FROM google_events")
    suspend fun deleteAllEvents()

    // ⭐ NEW — fixes your unresolved reference
    @Query("SELECT COUNT(*) FROM google_events")
    suspend fun getEventCount(): Int
}
