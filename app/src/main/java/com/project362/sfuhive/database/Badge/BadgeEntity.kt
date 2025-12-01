package com.project362.sfuhive.database.Badge


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// example of one row: [badgeId: 1L, isLocked: false]
// NOTE: Badge ids 1-3 are reserved for goal specific badges
@Entity(tableName = "badge_table")
data class BadgeEntity (

    @PrimaryKey(autoGenerate = false)
    var badgeId: Long = 0L,

    @ColumnInfo(name = "is_locked")
    var isLocked: Boolean = true,
    )
