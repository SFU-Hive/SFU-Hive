package com.project362.sfuhive.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// adapted from RoomDatabase demo
class DataRepository(private val assignmentDatabaseDao: AssignmentDatabaseDao) {

    val allAssignments: Flow<List<Assignment>> = assignmentDatabaseDao.getAllActivities()

    fun insert(assignment: Assignment){
        CoroutineScope(IO).launch {
            assignmentDatabaseDao.insertAssignment(assignment)
        }
    }

    fun getAssignment(id: Long): Assignment? {
        return runBlocking(IO) {
            assignmentDatabaseDao.getAssignment(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(IO).launch {
            assignmentDatabaseDao.deleteAll()
        }
    }

}