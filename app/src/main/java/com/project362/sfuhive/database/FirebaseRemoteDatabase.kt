package com.project362.sfuhive.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment
import kotlinx.coroutines.tasks.await

// Firebase implementation assisted by ChatGPT
class FirebaseRemoteDatabase {

    data class Course(
        val id: Long = 0L,
        val name: String = ""
    )

    suspend fun fetchAllCoursesAndAssignments(): Pair<List<Course>, List<RatedAssignment>> {
        val ratedAssignmentsRef = Firebase.database.getReference("rated_assignments")
        val allAssignments = mutableListOf<RatedAssignment>()
        val courseMap = mutableMapOf<Long, String>()

        val snapshot = ratedAssignmentsRef.get().await()


        snapshot.children.forEach { userNode ->
            userNode.children.forEach { assignmentNode ->
                val assignment = assignmentNode.getValue(RatedAssignment::class.java)
                if (assignment != null) {
                    allAssignments.add(assignment)
                    courseMap[assignment.courseId] = assignment.courseName
                }
            }
        }

        val courseList = courseMap.map { Course(it.key, it.value) }
//        Log.d("FirebaseDB", "Found assignment: $courseList")

        return Pair(courseList, allAssignments)

    }
}
