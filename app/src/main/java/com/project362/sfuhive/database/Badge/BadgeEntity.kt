package com.project362.sfuhive.database.Badge


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badge_table")
data class BadgeEntity (

    @PrimaryKey(autoGenerate = false)
    var badgeId: Long = 0L,

    @ColumnInfo(name = "is_locked")
    var isLocked: Boolean = true,
    )
