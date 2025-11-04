package com.project362.sfuhive.Assignments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.project362.sfuhive.R
import com.project362.sfuhive.RatedAssignment

class AssignmentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_assignments, container, false)

        recyclerView = view.findViewById(R.id.course_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadCoursesFromFirebase()



        return view
    }


    // adapted from Firebase Docs and ChatGPT
    private fun loadCoursesFromFirebase() {
        val auth = FirebaseAuth.getInstance()

        // check if already signed in
        if (auth.currentUser != null) {
            // user is signed in
            getCourses()
        } else {
            // sign in anonymously (or use Google sign-in if available)
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Log.d("FirebaseAuth", "Signed in anonymously: ${it.user?.uid}")
                    getCourses()
                }
                .addOnFailureListener { e ->
                    Log.d("FirebaseAuth", "Failed to sign in")
                }
        }
    }

    private fun getCourses() {
        val ratedAssignmentsRef = Firebase.database.getReference("rated_assignments")

        ratedAssignmentsRef.get().addOnSuccessListener { snapshot ->
            // map id to assignment name
            val courseMap = mutableMapOf<Long, Int>()

            snapshot.children.forEach { userNode  ->
                userNode.children.forEach { assignmentNode ->
                    val assignment = assignmentNode.getValue(RatedAssignment::class.java)
                    if (assignment != null && courseMap[assignment.courseId] == null) {
                        courseMap[assignment.courseId] = 1

                    }
                }
            }

            val courseList = courseMap.values.toList()
//            Log.d("FirebaseDB", "Found assignment: $courseList")
            adapter = CourseAdapter(courseList)
            recyclerView.adapter = adapter
        }.addOnFailureListener { e ->
            Log.d("FirebaseDB", "Failed to load courses")
        }
    }
}