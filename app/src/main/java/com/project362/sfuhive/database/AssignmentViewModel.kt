package com.project362.sfuhive.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import java.lang.IllegalArgumentException

// adapted from RoomDatabase demo
class AssignmentViewModel(private val repository: AssignmentRepository) : ViewModel() {

    val allAssignmentsLiveData: LiveData<List<Assignment>> = repository.allAssignments.asLiveData()

    fun insert(assignment: Assignment) {
        repository.insert(assignment)
    }

    fun getAssignment(id: Long): Assignment? {
        return repository.getAssignment(id)
    }

    fun deleteAll(){
        repository.deleteAll()
    }
}

class AssignmentViewModelFactory (private val repository: AssignmentRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(AssignmentViewModel::class.java))
            return AssignmentViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}