package com.project362.sfuhive.database.Calendar

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class CustomTaskViewModel(private val repo: CustomTaskRepository) : ViewModel() {

    val allTasks = repo.getAll().asLiveData()

    fun insert(task: CustomTaskEntity) {
        viewModelScope.launch {
            repo.insert(task)
        }
    }

    fun getTasksForDate(date: String) =
        repo.getByDate(date).asLiveData()
}

class CustomTaskVMFactory(private val repo: CustomTaskRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomTaskViewModel::class.java)) {
            return CustomTaskViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown VM class")
    }
}