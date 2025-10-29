package com.project362.sfuhive

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.AssignmentDatabase
import com.project362.sfuhive.database.AssignmentDatabaseDao
import com.project362.sfuhive.database.AssignmentRepository
import com.project362.sfuhive.database.AssignmentViewModel
import com.project362.sfuhive.database.AssignmentViewModelFactory
import org.json.JSONArray
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL

object Util {

    private lateinit var database: AssignmentDatabase
    private lateinit var databaseDao: AssignmentDatabaseDao
    private lateinit var repository: AssignmentRepository
    private lateinit var viewModelFactory: AssignmentViewModelFactory

    private lateinit var assignmentViewModel: AssignmentViewModel

    fun getCanvasAssignments(owner: ViewModelStoreOwner, context: Context) {
            try {
                viewModelFactory = getViewModelFactory(context)
                assignmentViewModel = ViewModelProvider(owner, viewModelFactory).get(AssignmentViewModel::class.java)

                // delete all before inserting for fresh restart
                assignmentViewModel.deleteAll()

                // get key from manifest and set URL
                val ai: ApplicationInfo = context.packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val token =  ai.metaData.getString("keyValue")
                val coursesURL = URL("https://canvas.sfu.ca/api/v1/courses?enrollment_state=active")

                val coursesArray = getJsonArrayFromURL(coursesURL, token)

                // for course id and course names
                for (i in 0 until coursesArray.length()) {
                    val course = coursesArray.getJSONObject(i)
                    val courseId = course.optInt("id")
                    val courseName = course.optString("name")

                    if (courseName == null || courseName == "") {
                        continue
                    }


                    // get assignments for each course
                    val assignmentsURL = URL("https://canvas.sfu.ca/api/v1/courses/$courseId/assignments")
                    val assignmentArray = getJsonArrayFromURL(assignmentsURL, token)

                    // read assignments
                    for (i in 0 until assignmentArray.length()) {

                        // get all relevant assignment info
                        val canvasAssignment = assignmentArray.getJSONObject(i)
                        val assignmentId = canvasAssignment.optLong("id")
                        val assnDue = canvasAssignment.optString("due_at", "")
                        val assnName = canvasAssignment.optString("name", "")
                        val assnPoints = canvasAssignment.optDouble("points_possible", 0.0)

                        // use if u wanna log assignments
                        Log.d("CanvasAPI", "Course Name: $courseName, Assignment Name: $assnName, Assignment Points: $assnPoints, Assignment Due: $assnDue")

                        val assignment = Assignment()
                        // manually setting id
                        assignment.assignmentId = assignmentId
                        assignment.courseName = courseName
                        assignment.assignmentName = assnName
                        assignment.pointsPossible = assnPoints
                        assignment.dueAt = assnDue

                        assignmentViewModel.insert(assignment)
                    }
                }

                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("assignments_loaded", true).apply()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("CanvasAPI", "Error: ${e.message}")
            }
    }

    private fun getJsonArrayFromURL(
        coursesURL: URL,
        token: String?
    ): JSONArray {
        // open connection request with token for course ids
        val conn = coursesURL.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        // read response and build string
        conn.connect()
        val reader = conn.inputStream.bufferedReader()
        val sb = StringBuilder()
        reader.forEachLine { sb.append(it) }
        val response = sb.toString()

        // store response as tokened json array
        val tokener = JSONTokener(response)
        val coursesArray = JSONArray(tokener)

        // log response
        Log.d("CanvasResp", response)

        return coursesArray
    }

    fun getViewModelFactory(context: Context): AssignmentViewModelFactory {
        database = AssignmentDatabase.getInstance(context)
        databaseDao = database.assignmentDatabaseDao
        repository = AssignmentRepository(databaseDao)
        viewModelFactory = AssignmentViewModelFactory(repository)
        return viewModelFactory
    }
}