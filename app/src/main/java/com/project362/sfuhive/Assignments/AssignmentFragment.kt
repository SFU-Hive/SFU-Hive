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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment
import com.project362.sfuhive.R
import com.project362.sfuhive.Util.getViewModelFactory
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.FirebaseRemoteDatabase.Course
import java.util.*

class AssignmentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private val courseList = mutableListOf<Course>()
    private val allAssignments = mutableListOf<RatedAssignment>()
    private lateinit var viewModel: DataViewModel
    private var myCourses: List<Course> = emptyList()
    private var otherCourses: List<Course> = emptyList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_assignments, container, false)

        // fetch and load data from firebase
        val factory = getViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory).get(DataViewModel::class.java)
        viewModel.loadCourses()

        setObservers(viewModel)

        recyclerView = view.findViewById(R.id.course_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CourseAdapter { selectedCourse: Course ->
            showAssignmentsForCourse(selectedCourse)
        }
        recyclerView.adapter = adapter

        // setup tool bar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Courses"
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        setHasOptionsMenu(true)

        return view
    }

    private fun setObservers(viewModel: DataViewModel) {
        viewModel.courseListLiveData.observe(viewLifecycleOwner) { newCourseList ->
            courseList.clear()
            courseList.addAll(newCourseList)

            // all of the users assignments
            viewModel.myUniqueCourseIdsLiveData.observe(viewLifecycleOwner) { myCourseIds ->
                myCourses = courseList.filter { myCourseIds.contains(it.id) }
                otherCourses = courseList.filter { !myCourseIds.contains(it.id) }
                adapter.setCourses(myCourses, otherCourses)
            }
        }

        // all rated assignments from Firebase
        viewModel.allAssignmentsStateFlow.observe(viewLifecycleOwner) { assignments ->
            allAssignments.clear()
            allAssignments.addAll(assignments)
        }
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
        val filteredMyCourses = ArrayList<Course>()
        val filteredOtherCourses = ArrayList<Course>()

        for (item in myCourses) {
            // perform search
            if (item.name.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filteredMyCourses.add(item)
            }
        }
        for (item in otherCourses) {
            // perform search
            if (item.name.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filteredOtherCourses.add(item)
            }
        }

        adapter.setCourses(filteredMyCourses, filteredOtherCourses)
    }

    private fun showAssignmentsForCourse(course: Course) {
        val assignmentsForCourse = allAssignments.filter { it.courseId == course.id }
        Log.d("AssignmentsFragment", "Showing assignments for course: ${assignmentsForCourse.size}")
        val intent = Intent(requireContext(), ListAssignmentsActivity::class.java)
        intent.putParcelableArrayListExtra("assignments", ArrayList(assignmentsForCourse))
        startActivity(intent)
    }
}