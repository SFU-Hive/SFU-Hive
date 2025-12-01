package com.project362.sfuhive

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.DataViewModelFactory


class MainActivity : AppCompatActivity() {
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

        // check for notification permission
        checkNotificationPermission()

        // add notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "assignment_channel",
                "Assignment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // init firebase
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        // firebase anon sign in (needed for database access)
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

        // hide start button until thread done getting submission data
        loadButton = findViewById(R.id.load)
        loadButton.visibility = View.GONE

        loadButton.setOnClickListener {
            // put all assignments into database on start if not there
            Thread {
                Util.getCanvasData(this, this)
            }.start()

            // open to dashboard
            intent = Intent(this, NavActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        // when the app starts, launch a thread that gets new submissions from Canvas API
        Thread {
            val newSubmissions = Util.getNewlySubmittedAssignments(this)
            Log.d("Submission", "New submissions count: ${newSubmissions.size}")
            if (newSubmissions.isNotEmpty()) {
                runOnUiThread {
                    handleNewSubmissions(newSubmissions)

                    // when done show button
                    loadButton.visibility = View.VISIBLE
                    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                    loadButton.startAnimation(animation)
                }
            } else {
                runOnUiThread {
                    // when done show button
                    loadButton.visibility = View.VISIBLE
                    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                    loadButton.startAnimation(animation)
                }
            }
        }.start()
    }

    private fun handleNewSubmissions(newSubmissions: List<SubmittedAssignment>, index: Int = 0) {
        // creates a custom dialog for each new submitted assignment
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
        try {
            dialog.show(
                supportFragmentManager,
                "RateSubmission"
            ) // Causes "IllegalStateException: Can not perform this action after onSaveInstanceState" Issue on Miro's Device
        }catch(e :IllegalStateException){
            Log.d("handleNewSubmissions", "IllegalStateException THROWN")
        }
         //get 10 coins per submitted assignment
        var coins=Util.getCoinTotal(this)
        coins= coins!! + 10
        Util.updateCoinTotal(this,coins)

        // this part assisted by ChatGPT
        // Use a listener for when the dialog is dismissed
        supportFragmentManager.setFragmentResultListener("RateSubmission", this) { _, _ ->
            // Show next dialog
            handleNewSubmissions(newSubmissions, index + 1)
        }

    }

    fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}