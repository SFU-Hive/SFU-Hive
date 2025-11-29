package com.project362.sfuhive.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.project362.sfuhive.Wellness.GoalDatabase
import com.project362.sfuhive.database.Badge.BadgeDatabase
import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.Streak.StreakEntity
import com.project362.sfuhive.database.Wellness.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import java.util.Calendar

// adapted from RoomDatabase demo
class DataViewModel(private val repository: DataRepository) : ViewModel() {

    val allMyAssignmentsLiveData: LiveData<List<Assignment>> = repository.allAssignments.asLiveData()
    val myUniqueCourseIdsLiveData: LiveData<List<Long>> = repository.myUniqueCourseIds.asLiveData()

    fun insertAssignment(assignment: Assignment) {
        repository.insertAssignment(assignment)
    }

    fun getAssignment(id: Long): Assignment? {
        return repository.getAssignment(id)
    }

    fun deleteAllAssignments(){
        repository.deleteAllAssignments()
    }

    // File section
    val allFilesLiveData: LiveData<List<File>> = repository.allFiles.asLiveData()

    fun insertFile(file: File) {
        repository.insertFile(file)
    }

    fun getFile(id: Long): File? {
        return repository.getFile(id)
    }

    fun deleteAllFiles(){
        repository.deleteAllFiles()
    }


    // Badge Section
    fun isBadgeLocked(id: Long): Boolean? {
        val badge=repository.getBadge(id)
        return badge?.isLocked

    }

    fun lockBadge(id: Long) {
        repository.lockBadge(id)
    }

    fun unlockBadge(id: Long) {
        repository.unlockBadge(id)
        // Idea: trigger unlocked badge dialog here

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

    // goals section
    fun initializeGoals(context: Context) {
        viewModelScope.launch {
            Log.d("ViewModel", "Starting goal initialization...")

            // Ensure badges exist
            val badgeDb = BadgeDatabase.getInstance(context)
            for (id in 1..3L) {
                val badge = badgeDb.badgeDatabaseDao.getBadge(id)
                Log.d("ViewModel", "Badge $id exists: ${badge != null}")
            }

            // Initialize goals via GoalDatabase function
            val goalDb = GoalDatabase.getInstance(context)
            goalDb.initializeDefaultGoals()  // THIS will insert goals if none exist

            // Collect goals
            goalDb.goalDatabaseDao().getAllGoals().collect { goals ->
                Log.d("ViewModel", "All goals in DB:")
                goals.forEach { goal ->
                    Log.d("ViewModel", "Goal id=${goal.id}, badgeId=${goal.badgeId}, completionCount=${goal.completionCount}")
                }
            }
        }
    }

    val allGoals: LiveData<List<Goal>> = repository.getAllGoals().asLiveData()

    fun getAllGoals(): Flow<List<Goal>> {
        return repository.getAllGoals()
    }

    fun getGoalById(goalId: Long): Flow<Goal> {
        return repository.getGoalById(goalId)
    }

    fun updateGoalName(goalId: Long, name: String) {
        viewModelScope.launch {
            repository.updateGoal(goalId, name)
        }
    }

    fun incrementCompletion(goalId: Long) {
        viewModelScope.launch {
            // update count
            repository.incrementCompleteCount(goalId)
            // get today's date and also update the completion date
            val today = System.currentTimeMillis()
            repository.updateLastCompletionDate(goalId, today)

            // check if completion count is 10
            val goal = repository.getGoalById(goalId).first()

            // compute new count (Room hasn't reloaded yet)
            val newCount = goal.completionCount + 1

            // check against default
            if (goal.completionCount + 1 >= 10) {
                goal.badgeId?.let { badgeId ->
                    repository.unlockBadge(badgeId)

                }
            }
        }
    }

    fun updateCompletionCount(goalId: Long, count: Int) {
        viewModelScope.launch {
            repository.updateCompletionCount(goalId, count)
        }
    }

    // daily reset
    fun resetDailyGoalsIfNeeded() = viewModelScope.launch {
        val todayStartMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        repository.getAllGoals().first().forEach { goal ->
            if (goal.lastCompletionDate != todayStartMillis) {
                // reset completion count and update lastCompletionDate
                repository.updateLastCompletionDate(goal.id, todayStartMillis)
                repository.updateCompletionCount(goal.id, 0)
            }
        }
    }

    fun updateNfcTag(goalId: Long, tag: String?) {
        viewModelScope.launch {
            repository.updateNfcTag(goalId, tag)
        }
    }

    suspend fun getGoalByNfcTag(tag: String): Goal? = repository.getGoalByNfc(tag)

    suspend fun isNfcAssigned(tagId: String): Boolean =
        repository.isNfcAssigned(tagId)

    fun getNfcById(goalId: Long): Flow<String?> = repository.getNfcById(goalId)

    // to compute streak
    fun computeStreak(lastDate: Long, completionCount: Int): Int {
        if (lastDate == 0L) return 0

        val today = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000

        val diff = ((today - lastDate) / oneDay).toInt()

        return if (diff == 1) completionCount else 0
    }

    fun updateLastCompletionDate(key: Long, date: Long) {
        viewModelScope.launch {
            repository.updateLastCompletionDate(key, date)
        }
    }

    // progress bar
    fun computeProgress(count: Int): Int {
        val max = 10f // unlock badge at 10 completions
        return ((count / max) * 100).toInt().coerceIn(0, 100)
    }

    // streaks section

    fun addStreak(type:String, calendar: Calendar ){
        repository.addStreak(type, calendar)
    }

    fun getStreaksOfType(type:String): Flow<List<StreakEntity?>>{
        return repository.getStreaksOfType(type)
    }

    fun getAllStreaks(): Flow<List<StreakEntity?>>{
        return repository.getAllStreaks()
    }

    fun deleteStreaksOfType(type:String){
        repository.deleteStreaksOfType(type)
    }

    fun deleteAllStreaks(){
        repository.deleteAllStreaks()
    }




}

class DataViewModelFactory (private val repository: DataRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(DataViewModel::class.java))
            return DataViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}