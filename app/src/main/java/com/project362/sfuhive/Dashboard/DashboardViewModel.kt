package com.project362.sfuhive.Dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.AssignmentDatabase
import com.project362.sfuhive.database.AssignmentDatabaseDao
import com.project362.sfuhive.database.EventPriority.EventPriorityDatabase
import com.project362.sfuhive.database.storage.StoredFileDatabase
import com.project362.sfuhive.database.storage.StoredFileEntity
import com.project362.sfuhive.database.storage.StoredFileRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardViewModel (application: Application): AndroidViewModel(application) {
    private val _welcomeMessage = MutableLiveData<String>()
    val welcomeMessage: LiveData<String> = _welcomeMessage

    private val _importantDates = MutableLiveData<List<Assignment>>()
    val importantDates: LiveData<List<Assignment>> = _importantDates

    private val _recentFiles = MutableLiveData<List<StoredFileEntity>>()
    var recentFiles: LiveData<List<StoredFileEntity>> = _recentFiles

    private val assignmentDatabase =
        AssignmentDatabase.getInstance(application).assignmentDatabaseDao
    private val priorityDatabase =
        EventPriorityDatabase.getInstance(application).assignmentPriorityDao()


    init {
        // Initialize the repository and fetch recent files
        val storedFileDatabaseDao =
            StoredFileDatabase.getInstance(application).storedFileDatabaseDao
        val repository = StoredFileRepository(storedFileDatabaseDao)

        recentFiles = repository.getRecentFiles(6)
        loadDashboardData()
    }

    fun loadDashboardData() {
        //sets the welcome message that can be adapted to show the user name in future updates
        _welcomeMessage.value = "Welcome!"

        fetchImportantDates()
    }

    //fetch the high priority assignment from the database
    fun fetchImportantDates() {
        viewModelScope.launch {
            val highPriorityList = priorityDatabase.getHighPriorityAssignments().first()
            val priorityIds = highPriorityList.mapNotNull { it.assignmentId.toLongOrNull() }
            val assignments = if(priorityIds.isNotEmpty()) {
                assignmentDatabase.getAssignmentsByAssignmentId(priorityIds).filter { it.isNotEmpty() }.first()
            }else{
                emptyList()
            }

            _importantDates.postValue(assignments)
        }
    }
}