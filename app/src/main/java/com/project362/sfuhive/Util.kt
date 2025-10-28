package com.project362.sfuhive

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONTokener
import java.net.HttpURLConnection
import java.net.URL

object Util {

    fun getCanvasAssignments(context: Context) {
        Thread {
            try {

                // get key from manifest and set URL
                val ai: ApplicationInfo = context.packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                val token =  ai.metaData.getString("keyValue")
                val coursesURL = URL("https://canvas.sfu.ca/api/v1/courses")

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
                        val assignment = assignmentArray.getJSONObject(i)
                        val assnDue = assignment.optInt("due_at")
                        val assnName = assignment.optString("name")
                        val assnPoints = assignment.optInt("points_possible")

                        //TODO: CREATE ASSIGNMENT OBJECT THAT STORES ASSIGNMENT NAME, POINTS, AND DUE DATE etc.

                        // use if u wanna log assignments
                        Log.d(
                            "CanvasAPI",
                            "Course Name: $courseName, Assignment Name: $assnName, Assignment Points: $assnPoints, Assignment Due: $assnDue"
                        )

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("CanvasAPI", "Error: ${e.message}")
            }


        }.start()
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
        Log.d("CanvasAPI", response)

        return coursesArray
    }
}