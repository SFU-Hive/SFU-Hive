package com.project362.sfuhive.database.Streak

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// example of one row: [type: "login", year: 2025, month: 11, day: 20]
@Entity(tableName = "streak_table",primaryKeys=["type","year","month","day"])
data class StreakEntity (
    @ColumnInfo(name = "type")
    var type: String,

    @ColumnInfo(name = "year")
    var year: Int,

    @ColumnInfo(name = "month")
    var month: Int,

    @ColumnInfo(name = "day")
    var day: Int
)