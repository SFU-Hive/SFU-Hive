package com.project362.sfuhive.storage

import androidx.lifecycle.*

class StoredFileViewModel(private val repository: StoredFileRepository) : ViewModel() {

    val allFiles: LiveData<List<StoredFileEntity>> = repository.allFiles.asLiveData()

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