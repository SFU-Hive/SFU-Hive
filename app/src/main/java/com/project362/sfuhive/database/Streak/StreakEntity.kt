package com.project362.sfuhive.database.Streak

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "streak_table",primaryKeys=["type","date"])
data class StreakEntity (
    @ColumnInfo(name = "type")
    var type: String,

    @ColumnInfo(name = "date")
    var date: String
)