package com.project362.sfuhive.database.storage

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StoredFileRepository(private val storedFileDatabaseDao: StoredFileDatabaseDao) {
    val allFiles: Flow<List<StoredFileEntity>> = storedFileDatabaseDao.getAllStoredFilesSortedByLastAccessed()

    fun getFilesInFolder(parentId: Long?): LiveData<List<StoredFileEntity>> {
        return storedFileDatabaseDao.getFilesInFolder(parentId)
    }

    fun insertFile(file: StoredFileEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            storedFileDatabaseDao.insertFile(file)
        }
    }

    fun updateFile(file: StoredFileEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            storedFileDatabaseDao.updateFile(file)
        }
    }

    fun deleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            storedFileDatabaseDao.deleteAll()
        }
    }
    fun deleteFile(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            storedFileDatabaseDao.deleteFile(id)
        }
    }

    fun getRecentFiles(limit: Int): LiveData<List<StoredFileEntity>> {
        return storedFileDatabaseDao.getRecentFiles(limit)
    }

    suspend fun getChildCount(parentId: Long?): Int {
        return storedFileDatabaseDao.getChildCount(parentId)
    }

}