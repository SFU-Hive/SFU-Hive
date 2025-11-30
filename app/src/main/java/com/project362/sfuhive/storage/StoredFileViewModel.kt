package com.project362.sfuhive.storage

import androidx.lifecycle.*
import com.project362.sfuhive.database.storage.StoredFileEntity
import com.project362.sfuhive.database.storage.StoredFileRepository

class StoredFileViewModel(private val repository: StoredFileRepository) : ViewModel() {

    companion object{
        private const val ROOT_FOLDER_ID = 0L
    }

    private val _currFolderId = MutableLiveData<Long?>()
    val currFolderId: LiveData<Long?> = _currFolderId

    private val folderHistory = ArrayDeque<Long?>()

    val filesInFolder: LiveData<List<StoredFileEntity>> = _currFolderId.switchMap{ parentId ->
        repository.getFilesInFolder(parentId)
    }

    init {
        openFolder(null)
    }

    fun openFolder(folderId: Long?) {
        val targetFolderId = folderId ?: ROOT_FOLDER_ID
        if(_currFolderId.value != targetFolderId) {
            folderHistory.addLast(_currFolderId.value)
        }
        _currFolderId.value = targetFolderId
    }

    fun goBack(): Boolean {
        val previousFolderId = folderHistory.removeLastOrNull()
        return if(previousFolderId != null) {
            _currFolderId.value = previousFolderId
            true
        } else {
            false
        }
    }

    val allFiles: LiveData<List<StoredFileEntity>> = repository.allFiles.asLiveData()

    fun getCurrFolderId(): Long {
        return _currFolderId.value ?: ROOT_FOLDER_ID
    }

    fun insertFile(file: StoredFileEntity) {
        repository.insertFile(file)
    }

    fun updateFile(file: StoredFileEntity) {
        repository.updateFile(file)
    }

    fun deleteFile(id: Long) {
        repository.deleteFile(id)
    }

    fun deleteAll(){
        repository.deleteAll()
    }

    suspend fun isFolderEmpty(parentId: Long?): Boolean {
        return repository.getChildCount(parentId) == 0
    }

    class StoredFileViewModelFactory(private val repository: StoredFileRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoredFileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoredFileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}