package com.project362.sfuhive.database.Calendar

class CustomTaskRepository(private val dao: CustomTaskDao) {

    fun getAll() = dao.getAllTasks()

    fun getByDate(date: String) = dao.getTasksForDate(date)

    suspend fun insert(task: CustomTaskEntity) {
        dao.insert(task)
    }
}
