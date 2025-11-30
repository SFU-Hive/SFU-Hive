package com.project362.sfuhive.database.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project362.sfuhive.storage.FileSource

@Entity(tableName = "stored_file_table")
data class StoredFileEntity(
    @PrimaryKey val id: Long,

    @ColumnInfo(name = "parent_id")
    var parentId: Long? = null,

    @ColumnInfo(name = "stored_file_name")
    var name: String = "",

    @ColumnInfo(name = "stored_file_type")
    var type: String = "",

    @ColumnInfo(name = "stored_file_size")
    var size: Long = 0L,

    @ColumnInfo(name = "stored_file_last_accessed")
    var lastAccessed: Long = 0L,

    @ColumnInfo(name = "stored_file_upload_date")
    var uploadDate: Long? = null,

    @ColumnInfo(name = "stored_file_url")
    var url: String? = null,

    @ColumnInfo(name = "stored_file_source")
    var source: FileSource = FileSource.USER_UPLOAD,

    )