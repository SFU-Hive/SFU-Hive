package com.project362.sfuhive.storage

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow

class StoredFileRepository(private val storedFileDatabaseDao: StoredFileDatabaseDao) {
    val allFiles: Flow<List<StoredFileEntity>> = storedFileDatabaseDao.getAllStoredFilesSortedByLastAccessed()

    fun insertFile(file: StoredFileEntity) {
        CoroutineScope(IO).launch {
            storedFileDatabaseDao.insertFile(file)
        }
    }

    fun updateFile(file: StoredFileEntity) {
        CoroutineScope(IO).launch {
            storedFileDatabaseDao.updateFile(file)
        }
    }

    fun deleteAll() {
        CoroutineScope(IO).launch {
            storedFileDatabaseDao.deleteAll()
        }
    }
    fun deleteFile(id: Long) {
        CoroutineScope(IO).launch {
            storedFileDatabaseDao.deleteFile(id)
        }
    }

}