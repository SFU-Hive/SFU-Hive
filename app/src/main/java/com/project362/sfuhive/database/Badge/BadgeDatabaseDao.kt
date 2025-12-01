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

    // add badges to the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badgeEntity: BadgeEntity)

    // get a badge by id from the database
    @Query("SELECT * FROM badge_table WHERE badgeId = :key LIMIT 1")
    suspend fun getBadge(key: Long): BadgeEntity?

    // get a flow of all badges in the database
    @Query("SELECT * FROM badge_table")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    // update the "is locked" value of our BadgeEntity
    @Query("UPDATE badge_table SET is_locked=:isLocked WHERE badgeId = :id")
    fun updateIsLocked(id : Long, isLocked: Boolean)

    // get a single flow of a BadgeEntity -- i.e [badgeId, isBadgeLocked state]
    @Query("SELECT * FROM badge_table WHERE badgeId = :key LIMIT 1")
    fun getBadgeFlow(key: Long): Flow<BadgeEntity>

}