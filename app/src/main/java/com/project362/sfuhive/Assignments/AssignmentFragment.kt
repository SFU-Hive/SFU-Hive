package com.project362.sfuhive.Assignments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment
import com.project362.sfuhive.R
import java.util.*


class AssignmentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private val courseList = mutableListOf<Course>()
    private val allAssignments = mutableListOf<RatedAssignment>()

    data class Course(
        val id: Long = 0L,
        val name: String = ""
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_assignments, container, false)

        recyclerView = view.findViewById(R.id.course_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CourseAdapter(courseList) { selectedCourse ->
            showAssignmentsForCourse(selectedCourse)
        }

        recyclerView.adapter = adapter

        // setup tool bar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Courses"
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        setHasOptionsMenu(true)

        loadCoursesFromFirebase()

        return view
    }

    // implementation adapted from https://www.geeksforgeeks.org/android/searchview-in-android-with-recyclerview/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.actionSearch)
        val searchView = searchItem.actionView as SearchView

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        return
    }

    private fun filter(text: String) {
        val filtered = ArrayList<Course>()

        for (item in courseList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filtered.add(item)
            }
            adapter.filterList(filtered)
        }
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
            val courseMap = mutableMapOf<Long, String>()

            snapshot.children.forEach { userNode  ->
                userNode.children.forEach { assignmentNode ->
                    val assignment = assignmentNode.getValue(RatedAssignment::class.java)
                    if (assignment != null) {
                        allAssignments.add(assignment)
                        courseMap[assignment.courseId] = assignment.courseName
                    }
                }
            }


//            Log.d("FirebaseDB", "Found assignment: $courseList")
            courseList.clear()
            courseList.addAll(courseMap.map { Course(it.key, it.value) })
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { e ->
            Log.d("FirebaseDB", "Failed to load courses")
        }
    }

    private fun showAssignmentsForCourse(course: Course) {
        val assignmentsForCourse = allAssignments.filter { it.courseId == course.id }
        Log.d("AssignmentsFragment", "Showing assignments for course: ${assignmentsForCourse.size}")
        val intent = Intent(requireContext(), ListAssignmentsActivity::class.java)
        intent.putParcelableArrayListExtra("assignments", ArrayList(assignmentsForCourse))
        startActivity(intent)
    }
}