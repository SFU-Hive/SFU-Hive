package com.project362.sfuhive.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// adapted from RoomDatabase demo
@Entity(tableName = "file_table")
data class File (
    @PrimaryKey(autoGenerate = false)
    var fileId: Long = 0L,

    @ColumnInfo(name = "file_name_column")
    var fileName: String = "",

    @ColumnInfo(name = "file_url_column")
    var fileURL: String = "",
)
