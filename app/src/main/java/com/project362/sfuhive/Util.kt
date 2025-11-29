package com.project362.sfuhive

import android.Manifest
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.project362.sfuhive.Progress.Badges.BadgeFactory
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.BANK_BREAKER
import com.project362.sfuhive.Progress.Badges.BadgeUtils
import com.project362.sfuhive.Progress.Badges.UnlockedDialog
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.project362.sfuhive.Wellness.GoalDatabase
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.AssignmentDatabase
import com.project362.sfuhive.database.AssignmentDatabaseDao
import com.project362.sfuhive.database.Badge.BadgeDatabase
import com.project362.sfuhive.database.Badge.BadgeDatabaseDao
import com.project362.sfuhive.database.Wellness.GoalDatabaseDao
import com.project362.sfuhive.database.DataRepository
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.DataViewModelFactory
import com.project362.sfuhive.database.File
import com.project362.sfuhive.database.FileDatabase
import com.project362.sfuhive.database.FirebaseRemoteDatabase
import com.project362.sfuhive.database.Streak.StreakDatabase
import com.project362.sfuhive.database.Streak.StreakDatabaseDao
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Util {

    const val PREFS_KEY = "app_prefs"

    const val LAST_SYNC_KEY = "last_sync"

    const val ASSIGNMENT_ID_KEY = "assignment_id"
    const val ASSIGNMENT_NAME_KEY = "assignment_name"
    const val COURSE_ID_KEY = "course_id"
    const val COURSE_NAME_KEY = "course_name"
    const val GRADE_KEY = "grade_key"
    const val NAME_KEY = "name_key"

    const val COIN_KEY = "coin_key"

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


    private fun getToken(context: Context): String? {
        // get key from manifest and set URL
        val ai: ApplicationInfo = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val token = ai.metaData.getString("keyValue")

        return token
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCanvasData(owner: ViewModelStoreOwner, context: Context) {
        try {
            viewModelFactory = getViewModelFactory(context)
            dataViewModel =
                ViewModelProvider(owner, viewModelFactory).get(DataViewModel::class.java)

            // delete all before inserting for fresh restart
            dataViewModel.deleteAllAssignments()
            dataViewModel.deleteAllFiles()

            val token = getToken(context)

            // get students name
            val userURL = URL("https://canvas.sfu.ca/api/v1/users/self")
            val userObject = getJsonObjectFromURL(userURL, token)
            val name = userObject.optString("name")

            Log.d("CanvasAPI_name", "Name: $name")

            // add name to prefs
            val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString(NAME_KEY, name)
            editor.apply()

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

                // get assignment group weights
                val groupsURL =
                    URL("https://canvas.sfu.ca/api/v1/courses/$courseId/assignment_groups")
                val groupsArray = getJsonArrayFromURL(groupsURL, token)

                val groupWeightMap = mutableMapOf<Long, Double>()

                for (j in 0 until groupsArray.length()) {
                    val group = groupsArray.getJSONObject(j)
                    val groupId = group.optLong("id")
                    val groupWeight = group.optDouble("group_weight", 0.0)

                    // Populate the map
                    groupWeightMap[groupId] = groupWeight
                }

                // get assignments for each course
                val assignmentsURL =
                    URL("https://canvas.sfu.ca/api/v1/courses/$courseId/assignments")
                val assignmentArray = getJsonArrayFromURL(assignmentsURL, token)

                // read assignments
                for (j in 0 until assignmentArray.length()) {

                    // get all relevant assignment info
                    val canvasAssignment = assignmentArray.getJSONObject(j)
                    val assignmentId = canvasAssignment.optLong("id")
                    val assnDue = canvasAssignment.optString("due_at", "")
                    val assnName = canvasAssignment.optString("name", "")
                    val assnPoints = canvasAssignment.optDouble("points_possible", 0.0)
                    val assnGroupId = canvasAssignment.optLong("assignment_group_id")

                    // map group id
                    val groupWeight = groupWeightMap[assnGroupId] ?: 0.0

                    // use if u wanna log assignments
                    Log.d(
                        "CanvasAPI_assignments",
                        "Course Name: $courseName, Assignment Name: $assnName, Assignment Points: $assnPoints, Assignment Due: $assnDue, Group Weight: $groupWeight"
                    )

                    val assignment = Assignment()
                    // manually setting id
                    assignment.assignmentId = assignmentId
                    assignment.courseName = courseName
                    assignment.courseId = courseId.toLong()
                    assignment.assignmentName = assnName
                    assignment.pointsPossible = assnPoints
                    assignment.dueAt = assnDue
                    assignment.groupWeight = groupWeight

                    dataViewModel.insertAssignment(assignment)

                    // schedule reminder
                    scheduleReminder(context, assignmentId, assnName, assnDue)
                }

                var filesTabIsPublic = false

                // find if course has public files
                val tabsURL = URL("https://canvas.sfu.ca/api/v1/courses/$courseId/tabs")
                val tabsArray = getJsonArrayFromURL(tabsURL, token)

                // search through tabs
                for (j in 0 until tabsArray.length()) {
                    val tab = tabsArray.getJSONObject(j)
                    val tabId = tab.optString("id")
                    val tabVisibility = tab.optString("visibility")

                    // if files tab exists and is public then set flag to true
                    if (tabId == "files" && tabVisibility == "public") {
                        filesTabIsPublic = true
                        break
                    }
                }

                // skip course files if files unavailable
                if (!filesTabIsPublic) {
                    Log.d(
                        "CanvasAPI_Files",
                        "Skipping Course $courseId: Files tab is not public or missing."
                    )
                    continue
                }

                // get files for each course
                val filesURL = URL("https://canvas.sfu.ca/api/v1/courses/$courseId/files")
                val fileArray = getJsonArrayFromURL(filesURL, token)

                // read files
                for (j in 0 until fileArray.length()) {

                    // get all relevant file info
                    val canvasFile = fileArray.getJSONObject(j)
                    val fileId = canvasFile.optLong("id")
                    val fileName = canvasFile.optString("display_name", "")
                    val fileUrl = canvasFile.optString("url", "")


                    // use if u wanna log file
                    Log.d(
                        "CanvasAPI_Files",
                        "Course Name: $courseName, File Name: $fileName, File URL: $fileUrl"
                    )


                    val file = File()
                    // manually setting id
                    file.fileId = fileId
                    file.courseId = courseId.toLong()
                    file.courseName = courseName
                    file.fileName = fileName
                    file.fileURL = fileUrl

                    dataViewModel.insertFile(file)
                }


            }

//            // test reminder scheduling
//            scheduleReminder(context, 9999, "Final Project", "2025-11-29T23:12:00Z")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CanvasAPI", "Error: ", e)
        }
    }

    private fun getRecentSubmissions(context: Context): JSONArray? {
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

            Log.d(
                "Submission", "Fetched $state, ASSIGNMENT: ${assignmentObj.optString("name")}, " +
                        "COURSE: ${courseObj.optString("name")}, SCORE: ${submissionObj.optDouble("score")}"
            )

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
                        grade = submissionObj.optDouble("score")
                    )

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
        val array = JSONArray(tokener)

        // log response
        //Log.d("CanvasResp", response)

        return array
    }

    private fun getJsonObjectFromURL(
        coursesURL: URL,
        token: String?
    ): JSONObject {
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
        val jsonObject = JSONObject(tokener)

        // log response
        //Log.d("CanvasResp", response)

        return jsonObject
    }

    fun getViewModelFactory(context: Context): DataViewModelFactory {
        // assignment database
        database = AssignmentDatabase.getInstance(context)
        databaseDao = database.assignmentDatabaseDao

        // file database
        val fileDatabase = FileDatabase.getInstance(context)
        val fileDatabaseDao = fileDatabase.fileDatabaseDao

        // remote database
        val remoteDatabase = FirebaseRemoteDatabase()

        // badge database
        val badgeDatabase = BadgeDatabase.getInstance(context)
        val badgeDatabaseDao = badgeDatabase.badgeDatabaseDao

        // goal database
        val goalDatabase = GoalDatabase.getInstance(context)
        val goalDatabaseDao = goalDatabase.goalDatabaseDao()

        repository = DataRepository(
            databaseDao,
            fileDatabaseDao,
            remoteDatabase,
            badgeDatabaseDao,
            goalDatabaseDao
        )


    fun formatDoubleToText(value: Double): String {
        return String.format("%.1f", value)
    }

// streaks database
        val streakDatabase = StreakDatabase.getInstance(context)
        val streakDatabaseDao = streakDatabase.streakDatabaseDao

        repository = DataRepository(databaseDao, fileDatabaseDao, remoteDatabase,badgeDatabaseDao, goalDatabaseDao, streakDatabaseDao)
        viewModelFactory = DataViewModelFactory(repository)
        return viewModelFactory
    }
    fun updateCoinTotal(context: Context, newTotal: Long?) {
        // add name to prefs
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(COIN_KEY, newTotal!!)
        editor.apply()

        // Probably better ways to do this -Miro
        var toastText = "Coins Spent! +"
        val oldTotal=getCoinTotal(context)
        val difference = newTotal - oldTotal!!

        Log.d("Coin Update","Old total: ${oldTotal}")
        Log.d("Coin Update","New total: ${newTotal}")
        Log.d("Coin Update","Coin difference: ${difference}")

        if(oldTotal!!>newTotal){
            toastText = "Coins Earned! +"
            val coinToast = Toast.makeText(context,"${toastText}${difference}",Toast.LENGTH_LONG)
            coinToast.show()
        }else if(oldTotal!!<newTotal){
            toastText = "Coins Spent! -"
            val coinToast = Toast.makeText(context,"${toastText}${difference}",Toast.LENGTH_LONG)
            coinToast.show()
        }else{
            Log.d("Coin Update","Coin value didn't change")

        }
        // notify user of coin gain via toa
    }

    fun getCoinTotal(context: Context): Long? {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val total = prefs.getLong(COIN_KEY, 0)

        return total
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleReminder(context: Context, assignmentId: Long, title: String, dueDate: String) {

        // skip if no due date
        if (dueDate.isNullOrBlank()) {
            return
        }

        // convert due date to milliseconds
        // conversion process adapted from ChatGPT implementation
        val dueAtMilli =
            try {
                val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                val instant = Instant.from(formatter.parse(dueDate))
                instant.toEpochMilli()
            } catch (e: Exception) {
                Log.e("Reminder", "Error parsing due_at: $dueDate", e)
                return
            }

        // set reminder time 24 hours before due date
        val reminderTime = dueAtMilli - 24 * 60 * 60 * 1000

        // skip due dates from the past
        val delay = reminderTime - System.currentTimeMillis()
        if (delay <= 0) {
            Log.d("Reminder", "Skipping past reminder for $title")
            return
        }

        // prep data for worker
        val data = Data.Builder()
            .putString("title", title)
            .putLong("id", assignmentId)
            .build()

        val work = OneTimeWorkRequestBuilder<AssignmentReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("assignment_reminder")
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "assignment_$assignmentId",
            ExistingWorkPolicy.REPLACE,
            work
        )

        Log.d("ReminderDebug", "Scheduled reminder for '${title}' in $dueDate")
    }

    fun coinsSpentToast(context: Context,amountSpent: Long){
        val toastText = "Coins Spent! -"
        val coinToast = Toast.makeText(context,"${toastText}${amountSpent}",Toast.LENGTH_LONG)
        coinToast.show()
    }
    // MIRO test in coin spending!
    fun UnlockBadgeDialog(badgeId : Long, theSupportFragmentManager : FragmentManager){
        val badgeFactory=BadgeFactory()
        val badge=badgeFactory.getBageById(badgeId)
        if(badge!=null){
            val dialog=UnlockedDialog(badge)
            dialog.show(theSupportFragmentManager, badgeId.toString())
        }

    }
}