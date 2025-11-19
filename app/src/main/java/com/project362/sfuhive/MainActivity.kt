package com.project362.sfuhive

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.project362.sfuhive.Assignments.RateSubmissionDialog
import com.project362.sfuhive.Util.LAST_SYNC_KEY
import com.project362.sfuhive.Util.PREFS_KEY
import com.project362.sfuhive.Util.SubmittedAssignment
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: AssignmentViewModelFactory
    private lateinit var assignmentViewModel: AssignmentViewModel
    private lateinit var loadButton: Button
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // use this to reset your sync window (if you wanna push more duplicates into the database)
//        val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
//        prefs.edit().putLong(LAST_SYNC_KEY, 0).apply()

        // init firebase
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FirebaseAuth", "signInAnonymously:success")
                    user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FirebaseAuth", "signInAnonymously:failure", task.exception)
                }
            }

        loadButton = findViewById(R.id.load)

        viewModelFactory = Util.getViewModelFactory(this)
        assignmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(AssignmentViewModel::class.java)

        // observe and log local database changes
        assignmentViewModel.allAssignmentsLiveData.observe(this, Observer { it ->
            Log.d("DatabaseCheck", "Assignments count: ${it.size}")
        })



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

    // Put thread in onStart
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        Thread {
            val newSubmissions = Util.getNewlySubmittedAssignments(this)
            Log.d("Submission", "New submissions count: ${newSubmissions.size}")
            if (newSubmissions.isNotEmpty()) {
                runOnUiThread {
                    handleNewSubmissions(newSubmissions)
                }
            }
        }.start()
    }

    private fun handleNewSubmissions(newSubmissions: List<SubmittedAssignment>, index: Int = 0) {
        if (index >= newSubmissions.size) {
            val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
            prefs.edit().putLong(LAST_SYNC_KEY, System.currentTimeMillis()).apply()
            return
        }

        val dialog = RateSubmissionDialog()
        val bundle = Bundle()
        bundle.putLong(Util.ASSIGNMENT_ID_KEY, newSubmissions[index].assignmentId)
        bundle.putString(Util.ASSIGNMENT_NAME_KEY, newSubmissions[index].assignmentName)
        bundle.putLong(Util.COURSE_ID_KEY, newSubmissions[index].courseId)
        bundle.putString(Util.COURSE_NAME_KEY, newSubmissions[index].courseName)
        bundle.putDouble(Util.GRADE_KEY, newSubmissions[index].grade)
        bundle.putString("USER_UID", user?.uid)


        dialog.arguments = bundle
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "RateSubmission") // Causes "IllegalStateException: Can not perform this action after onSaveInstanceState" Issue on Miro's Device

        // this part assisted by ChatGPT
        // Use a listener for when the dialog is dismissed
        supportFragmentManager.setFragmentResultListener("RateSubmission", this) { _, _ ->
            // Show next dialog
            handleNewSubmissions(newSubmissions, index + 1)
        }
    }
}