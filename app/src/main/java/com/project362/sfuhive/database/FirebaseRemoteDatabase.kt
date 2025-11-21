package com.project362.sfuhive.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.project362.sfuhive.Assignments.AssignmentFragment.Course
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment
import kotlinx.coroutines.tasks.await

class FirebaseRemoteDatabase {

    data class Course(
        val id: Long = 0L,
        val name: String = ""
    )

    // adapted from Firebase Docs and ChatGPT
    suspend fun loadCoursesFromFirebase() {
        val auth = FirebaseAuth.getInstance()

        // check if already signed in
        if (auth.currentUser != null) {
            // user is signed in
            fetchAllCoursesAndAssignments()
        } else {
            // sign in anonymously (or use Google sign-in if available)
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("FirebaseAuth", "Signed in anonymously: ${it.user?.uid}")
                    //fetchAllCoursesAndAssignments()
                }
                .addOnFailureListener { e ->
                    Log.d("FirebaseAuth", "Failed to sign in")
                }
        }
    }

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
