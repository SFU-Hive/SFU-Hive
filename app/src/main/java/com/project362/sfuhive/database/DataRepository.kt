package com.project362.sfuhive.database

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.project362.sfuhive.Assignments.RateSubmissionDialog
import com.project362.sfuhive.database.Badge.BadgeDatabaseDao
import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.Streak.StreakDatabaseDao
import com.project362.sfuhive.database.Streak.StreakEntity
import com.project362.sfuhive.database.Wellness.Goal
import com.project362.sfuhive.database.Wellness.GoalDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

// adapted from RoomDatabase demo
class DataRepository(private val assignmentDatabaseDao: AssignmentDatabaseDao,
                     private val fileDatabaseDao: FileDatabaseDao,
                     private val remoteDatabase: FirebaseRemoteDatabase,
                     private val badgeDatabaseDao: BadgeDatabaseDao,
                     private val goalDatabaseDao: GoalDatabaseDao,
                     private val streakDatabaseDao: StreakDatabaseDao
) {

    val allAssignments: Flow<List<Assignment>> = assignmentDatabaseDao.getAllActivities()

    fun insertAssignment(assignment: Assignment) {
        CoroutineScope(IO).launch {
            assignmentDatabaseDao.insertAssignment(assignment)
        }
    }

    fun getAssignment(id: Long): Assignment? {
        return runBlocking(IO) {
            assignmentDatabaseDao.getAssignment(id)
        }
    }

    fun deleteAllAssignments() {
        CoroutineScope(IO).launch {
            assignmentDatabaseDao.deleteAll()
        }
    }

    // Files section
    val  allFiles: Flow<List<File>> = fileDatabaseDao.getAllFiles()

    fun insertFile(file: File) {
        CoroutineScope(IO).launch {
            fileDatabaseDao.insertFile(file)
        }
    }

    fun getFile(id: Long): File? {
        return runBlocking(IO) {
            fileDatabaseDao.getFile(id)
        }
    }

    fun deleteAllFiles() {
        CoroutineScope(IO).launch {
            fileDatabaseDao.deleteAll()
        }
    }

    // badge section
    fun getBadge(id: Long): BadgeEntity?{
        return runBlocking(IO) {
            badgeDatabaseDao.getBadge(id)
        }
    }

    fun lockBadge(id: Long){
        return runBlocking(IO) {
            badgeDatabaseDao.updateIsLocked(id, true)
        }
    }

    fun unlockBadge(id: Long){

        return runBlocking(IO) {
            badgeDatabaseDao.updateIsLocked(id, false)
        }
    }

    fun getAllBadgesState(): Flow<List<BadgeEntity>>{

        return badgeDatabaseDao.getAllBadges()

    }

    fun getBadgeFlow(id:Long):Flow<BadgeEntity>{

        return badgeDatabaseDao.getBadgeFlow(id)

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

    // section for goals
    fun updateGoal(key: Long, goalName: String){
        CoroutineScope(IO).launch {
            goalDatabaseDao.updateGoal(key, goalName)
        }
    }

    fun incrementCompleteCount(key: Long) {
        CoroutineScope(IO).launch {
            goalDatabaseDao.incrementCompletionCount(key)
        }
    }

    fun updateLastCompletionDate(key: Long, date:Long) {
        CoroutineScope(IO).launch {
            goalDatabaseDao.updateLastCompletionDate(key, date)
        }
    }

    fun updateNfcTag(key: Long, tag: String?) {
        CoroutineScope(IO).launch {
            goalDatabaseDao.updateNfcTag(key, tag)
        }
    }

    fun updateCompletionCount(key: Long, count: Int) {
        CoroutineScope(IO).launch {
            goalDatabaseDao.updateCompletionCount(key, count)
        }
    }

    // get all goals
    fun getAllGoals(): Flow<List<Goal>> = goalDatabaseDao.getAllGoals()
    fun getGoalById(goalId: Long): Flow<Goal> = goalDatabaseDao.getGoalById(goalId)
    fun getCompletionCount(goalId: Long): Flow<Int> = goalDatabaseDao.getCompletionCount(goalId)
    fun getLastCompletionDateById(goalId: Long): Flow<Long> = goalDatabaseDao.getLastCompletionDateById(goalId)
    fun getNfcById(goalId: Long): Flow<String?> = goalDatabaseDao.getNfcById(goalId)
    suspend fun getGoalByNfc(tag: String) = goalDatabaseDao.getGoalByNfc(tag)

    suspend fun isNfcAssigned(tag: String): Boolean {
        return goalDatabaseDao.getGoalByNfcTag(tag) != null
    }

    // section for streaks
    fun addStreak(type:String, date : Calendar){
        CoroutineScope(IO).launch {

            val year = date.get(Calendar.YEAR)
            val month = date.get(Calendar.MONTH)
            val day = date.get(Calendar.DAY_OF_MONTH)
            Log.d("StreakDB","adding streak with date = ${year}, ${month}, ${day}")
            var newStreak = StreakEntity(type,year,month,day)
            streakDatabaseDao.insertStreak(newStreak)
        }
    }

    fun getStreaksOfType(type: String): Flow<List<StreakEntity?>> {
        val selectedStreaks= streakDatabaseDao.getStreaksOfType(type)
        Log.d("StreakDB","selecting streak with type = ${type}")
        Log.d("StreakDB","getStreaksOfType(type) returning ${selectedStreaks}")
        return selectedStreaks
    }

    fun getAllStreaks():Flow<List<StreakEntity?>>{
        return streakDatabaseDao.getAllStreaks()
    }

    fun deleteStreaksOfType(type :String){
        streakDatabaseDao.deleteStreaksOfType(type)
    }

    fun deleteAllStreaks(){
        streakDatabaseDao.deleteAll()
    }

}