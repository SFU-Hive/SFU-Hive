package com.project362.sfuhive

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.AssignmentDatabase
import com.project362.sfuhive.database.AssignmentDatabaseDao
import com.project362.sfuhive.database.DataRepository
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.DataViewModelFactory
import com.project362.sfuhive.database.FirebaseRemoteDatabase
import org.json.JSONArray
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object Util {

    const val PREFS_KEY = "app_prefs"

    const val LAST_SYNC_KEY = "last_sync"

    const val ASSIGNMENT_ID_KEY = "assignment_id"
    const val ASSIGNMENT_NAME_KEY = "assignment_name"
    const val COURSE_ID_KEY = "course_id"
    const val COURSE_NAME_KEY = "course_name"
    const val GRADE_KEY = "grade_key"

    private lateinit var database: AssignmentDatabase
    private lateinit var databaseDao: AssignmentDatabaseDao
    private lateinit var repository: DataRepository
    private lateinit var viewModelFactory: DataViewModelFactory

    private lateinit var dataViewModel: DataViewModel

    // data class for assignment submission
    data class SubmittedAssignment(
        val assignmentId: Long,
        val assignmentName: String,
        val courseId: Long,
        val courseName: String,
        val grade: Double,
    )

    fun getCanvasAssignments(owner: ViewModelStoreOwner, context: Context) {
            try {
                viewModelFactory = getViewModelFactory(context)
                dataViewModel = ViewModelProvider(owner, viewModelFactory).get(DataViewModel::class.java)

                // delete all before inserting for fresh restart
                dataViewModel.deleteAll()

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

                        dataViewModel.insert(assignment)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CanvasAPI", "Error: ", e)
            }
    }

    private fun getRecentSubmissions(context: Context) : JSONArray? {
        try {
            // get key from manifest and set URL
            val ai: ApplicationInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val token = ai.metaData.getString("keyValue")
            // master return array
            val allSubmissions = JSONArray()

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

                // the URL address is from ChatGPT
                val submissionsURL =
                    URL("https://canvas.sfu.ca/api/v1/courses/$courseId/assignments?include[]=submission")

                // fetch submissions
                val submissionsArray = getJsonArrayFromURL(submissionsURL, token)
//                Log.d("Submission", "Fetched ${submissionsArray.length()} submissions")
//                Log.d("Submission", "Fetched DATA $submissionsArray")

                // append each item to master array
                for (j in 0 until submissionsArray.length()) {
                    val assignment = submissionsArray.getJSONObject(j)

                    // add course info to the assignment
                    assignment.put("course", course)
                    allSubmissions.put(assignment)
//                    Log.d("Submission", "Fetched ${allSubmissions.length()} submissions")
                }
            }
            return allSubmissions
        } catch (e: Exception) {
            Log.d("Submission", "ERROR IN TRY getRecentSubmissions", e)
            e.printStackTrace()
            return null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNewlySubmittedAssignments(context: Context): List<SubmittedAssignment> {

        // return object
        val newAssignmentSubmissions = mutableListOf<SubmittedAssignment>()

        // get prefs for last assignment sync
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val lastSync = prefs.getLong(LAST_SYNC_KEY, 0L)

        // get submissions list as JSON array
        val assignments = getRecentSubmissions(context)

        // return empty if no new submissions
        if (assignments == null) {
            return emptyList()
        }

        // extract all JSON objects
        for (i in 0 until assignments.length()) {
            val assignmentObj = assignments.getJSONObject(i)
            val submissionObj = assignmentObj.getJSONObject("submission")
            val courseObj = assignmentObj.getJSONObject("course")
            val state = submissionObj.optString("workflow_state")
            val submittedAt = submissionObj.optString("submitted_at")

            Log.d("Submission", "Fetched $state, ASSIGNMENT: ${assignmentObj.optString("name")}, " +
                    "COURSE: ${courseObj.optString("name")}, SCORE: ${submissionObj.optDouble("score")}")

            // check if assignment has been submitted
            if (submissionObj != null && (state == "submitted" || state == "graded") && submittedAt != null && submittedAt != "null") {

                // this part generated by ChatGPT
                // parses the submission date into a long millisecond stamp
                val submittedTime = OffsetDateTime
                    .parse(submittedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toInstant()
                    .toEpochMilli()

                // check if assignment was submitted after last sync
                if (submittedTime > lastSync) {

                    // build submitted assignment object
                    val assignment = SubmittedAssignment(
                        assignmentId = assignmentObj.optLong("id"),
                        assignmentName = assignmentObj.optString("name"),
                        courseId = courseObj.optLong("id"),
                        courseName = courseObj.optString("name"),
                        grade = submissionObj.optDouble("score"))

                    // add submitted assignment to return list
                    newAssignmentSubmissions.add(assignment)
                }
            }
        }

        return newAssignmentSubmissions
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
        //Log.d("CanvasResp", response)

        return coursesArray
    }

    fun getViewModelFactory(context: Context): DataViewModelFactory {
        database = AssignmentDatabase.getInstance(context)
        databaseDao = database.assignmentDatabaseDao
        val remoteDatabase = FirebaseRemoteDatabase()
        repository = DataRepository(databaseDao, remoteDatabase)
        viewModelFactory = DataViewModelFactory(repository)
        return viewModelFactory
    }
}