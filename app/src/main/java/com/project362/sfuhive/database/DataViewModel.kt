package com.project362.sfuhive.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

// adapted from RoomDatabase demo
class DataViewModel(private val repository: DataRepository) : ViewModel() {

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

    // This section for remote database
    // 💡 Expose the course data to the Fragment for the RecyclerView
    val courseListLiveData = repository.courseFlow.asLiveData()

    // You can expose all assignments as needed, or let the Fragment access the repo data
    val allAssignmentsStateFlow = repository.allAssignmentsFlow.asLiveData()

    // Function to trigger data loading
    fun loadCourses() {
        viewModelScope.launch {
            repository.fetchAssignmentData()
        }
    }
}

class DataViewModelFactory (private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(DataViewModel::class.java))
            return DataViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}