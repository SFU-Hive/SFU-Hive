package com.project362.sfuhive.database

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.project362.sfuhive.Assignments.RateSubmissionDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// adapted from RoomDatabase demo
class DataRepository(private val assignmentDatabaseDao: AssignmentDatabaseDao,
                     private val remoteDatabase: FirebaseRemoteDatabase) {

    val allAssignments: Flow<List<Assignment>> = assignmentDatabaseDao.getAllActivities()

    fun insert(assignment: Assignment) {
        CoroutineScope(IO).launch {
            assignmentDatabaseDao.insertAssignment(assignment)
        }
    }

    fun getAssignment(id: Long): Assignment? {
        return runBlocking(IO) {
            assignmentDatabaseDao.getAssignment(id)
        }
    }

    fun deleteAll() {
        CoroutineScope(IO).launch {
            assignmentDatabaseDao.deleteAll()
        }
    }

    // This section for the remote database
    // Remote database MVVM refactor assisted by Gemini
    private val _courseFlow = MutableStateFlow<List<FirebaseRemoteDatabase.Course>>(emptyList())
    val courseFlow: StateFlow<List<FirebaseRemoteDatabase.Course>> = _courseFlow.asStateFlow()

    private val _allAssignmentsFlow =
        MutableStateFlow<List<RateSubmissionDialog.RatedAssignment>>(emptyList())
    val allAssignmentsFlow: StateFlow<List<RateSubmissionDialog.RatedAssignment>> =
        _allAssignmentsFlow.asStateFlow()

    suspend fun fetchAssignmentData() {
        withContext(IO) {
            try {

                // ensure we are authorized
                ensureFirebaseAuth()

                val (courses, assignments) = remoteDatabase.fetchAllCoursesAndAssignments()

                _courseFlow.value = courses
                _allAssignmentsFlow.value = assignments

            } catch (e: Exception) {
                Log.e("Repo", "Error fetching data", e)
            }
        }
    }

    // adapted from Firebase Docs and ChatGPT
    suspend fun ensureFirebaseAuth() {
        val auth = FirebaseAuth.getInstance()

        // check if already signed in
        if (auth.currentUser != null) {
            // user is signed in
            Log.d("FirebaseAuth", "Already signed in: ${auth.currentUser?.uid}")
            return
        }

        try {
            val tryAuth = auth.signInAnonymously().await()
            Log.d("FirebaseAuth", "Signed in anonymously: ${tryAuth.user?.uid}")
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error signing in", e)
            // throw exception to caller
            throw e
        }
    }
}