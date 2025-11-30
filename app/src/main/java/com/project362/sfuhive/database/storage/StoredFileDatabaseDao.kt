package com.project362.sfuhive.database.storage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project362.sfuhive.database.storage.StoredFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredFileDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertFile(file: StoredFileEntity)

    @Update
    suspend fun updateFile(file: StoredFileEntity)

    @Query("SELECT * FROM stored_file_table")
    fun getAllStoredFiles(): Flow<List<StoredFileEntity>>

    @Query("SELECT * FROM stored_file_table ORDER BY stored_file_last_accessed DESC")
    fun getAllStoredFilesSortedByLastAccessed(): Flow<List<StoredFileEntity>>


    @Query("SELECT * FROM stored_file_table WHERE id = :id LIMIT 1")
    suspend fun getFile(id: Long): StoredFileEntity?

    @Query("DELETE FROM stored_file_table")
    suspend fun deleteAll()

    @Query("DELETE FROM stored_file_table WHERE id = :id")
    suspend fun deleteFile(id: Long)

    @Query("SELECT * FROM stored_file_table WHERE parent_id = :parentId")
    fun getFilesInFolder(parentId: Long?): LiveData<List<StoredFileEntity>>

    @Query("SELECT COUNT(*) FROM stored_file_table WHERE parent_id = :parentId")
    suspend fun getChildCount(parentId: Long?): Int

    @Query("SELECT * FROM stored_file_table WHERE stored_file_type <> 'folder' ORDER BY stored_file_last_accessed DESC LIMIT :limit")
    fun getRecentFiles(limit: Int): LiveData<List<StoredFileEntity>>
}