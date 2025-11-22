package com.project362.sfuhive.database.Badge

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project362.sfuhive.database.Assignment
import kotlinx.coroutines.flow.Flow
import com.project362.sfuhive.database.Badge.BadgeEntity

@Dao
interface BadgeDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badgeEntity: BadgeEntity)

    @Query("SELECT * FROM badge_table WHERE badgeId = :key LIMIT 1")
    suspend fun getBadge(key: Long): BadgeEntity?

    @Query("SELECT * FROM badge_table")
    fun getAllBadges(): Flow<List<BadgeEntity>>


}