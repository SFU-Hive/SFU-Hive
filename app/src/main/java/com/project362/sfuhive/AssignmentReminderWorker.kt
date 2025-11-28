package com.project362.sfuhive

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

// Class builds worker to send a notification
// https://developer.android.com/reference/androidx/work/Worker
class AssignmentReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val title = inputData.getString("title")
        val id = inputData.getInt("id", 0)

        // build notification
        val notification = NotificationCompat.Builder(applicationContext, "assignment_channel")
            .setContentTitle(title)
            .setContentTitle("Upcoming Assignment")
            .setContentText("$title is due tomorrow!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = NotificationManagerCompat.from(applicationContext)
        manager.notify(id, notification)

        return Result.success()
    }
}