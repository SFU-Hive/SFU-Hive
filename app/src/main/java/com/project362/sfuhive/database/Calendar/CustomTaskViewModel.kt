package com.project362.sfuhive.database.Calendar

import androidx.lifecycle.*
import kotlinx.coroutines.launch

 //Exposes user-created tasks as LiveData and provides a simple insert API that runs on the ViewModel's scope.
class CustomTaskViewModel(private val repo: CustomTaskRepository) : ViewModel() {

    val allTasks = repo.getAll().asLiveData()

    fun insert(task: CustomTaskEntity) {
        viewModelScope.launch {
            repo.insert(task)
        }
    }

    // Convenience helper to expose tasks for a single date as LiveData
    fun getTasksForDate(date: String) =
        repo.getByDate(date).asLiveData()
}

// Factory for creating `CustomTaskViewModel` with a repository parameter.
class CustomTaskVMFactory(private val repo: CustomTaskRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomTaskViewModel::class.java)) {
            return CustomTaskViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown VM class")
    }
}