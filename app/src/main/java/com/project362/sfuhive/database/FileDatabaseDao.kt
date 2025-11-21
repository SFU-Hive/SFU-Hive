package com.project362.sfuhive.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// adapted from RoomDatabase demo
@Dao
interface FileDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: File)

    @Query("DELETE FROM file_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM file_table")
    fun getAllFiles(): Flow<List<File>>

    @Query("SELECT * FROM file_table WHERE fileId = :key LIMIT 1")
    suspend fun getFile(key: Long): File?
}