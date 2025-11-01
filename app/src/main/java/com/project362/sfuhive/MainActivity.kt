package com.project362.sfuhive

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.project362.sfuhive.Assignments.RateSubmissionDialog
import com.project362.sfuhive.Util.SubmittedAssignment
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: AssignmentViewModelFactory
    private lateinit var assignmentViewModel: AssignmentViewModel
    private lateinit var loadButton: Button


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadButton = findViewById(R.id.load)

        viewModelFactory = Util.getViewModelFactory(this)
        assignmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(AssignmentViewModel::class.java)

        // observe and log local database changes
        assignmentViewModel.allAssignmentsLiveData.observe(this, Observer { it ->
            Log.d("DatabaseCheck", "Assignments count: ${it.size}")
        })

        Thread {
            val newSubmissions = Util.getNewlySubmittedAssignments(this)
            Log.d("Submission", "New submissions count: ${newSubmissions.size}")
            if (newSubmissions.isNotEmpty()) {
                runOnUiThread {
                    handleNewSubmissions(newSubmissions)
                }
            }
        }.start()


        loadButton.setOnClickListener {
            // put all assignments into database on start if not there
            Thread {
                Util.getCanvasAssignments(this, this)
            }.start()

            // open to dashboard
            intent = Intent(this, NavActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleNewSubmissions(newSubmissions: List<SubmittedAssignment>, index: Int = 0) {
        if (index >= newSubmissions.size) return // all done

        val dialog = RateSubmissionDialog()
        val bundle = Bundle()
        bundle.putLong(Util.ASSIGNMENT_ID_KEY, newSubmissions[index].assignmentId)
        bundle.putString(Util.ASSIGNMENT_NAME_KEY, newSubmissions[index].assignmentName)
        bundle.putString(Util.COURSE_NAME_KEY, newSubmissions[index].courseName)
        bundle.putDouble(Util.GRADE_KEY, newSubmissions[index].grade)

        dialog.arguments = bundle
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "RateSubmission")

        // this part assisted by ChatGPT
        // Use a listener for when the dialog is dismissed
        supportFragmentManager.setFragmentResultListener("RateSubmission", this) { _, _ ->
            // Show next dialog
            handleNewSubmissions(newSubmissions, index + 1)
        }
    }
}