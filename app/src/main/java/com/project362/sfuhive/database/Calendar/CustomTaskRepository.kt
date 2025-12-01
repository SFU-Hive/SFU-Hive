package com.project362.sfuhive.database.Calendar


 //Lightweight repository that exposes DAO functions to the higher-level ViewModel.
class CustomTaskRepository(private val dao: CustomTaskDao) {

    fun getAll() = dao.getAllTasks()

    fun getByDate(date: String) = dao.getTasksForDate(date)

    // Suspend insert that should be called from a coroutine context (e.g., viewModelScope)
    suspend fun insert(task: CustomTaskEntity) {
        dao.insert(task)
    }
}
