package com.project362.sfuhive.Calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TaskAdapter(private var items: List<Assignment>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTaskName: TextView = view.findViewById(R.id.tvTaskName)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]

        // Extract short code from "CMPT362 D100 Mobile Apps"
        val courseCode = task.courseName.split(" ").firstOrNull() ?: "Course"

        // Display: CMPT362: Assignment title
        holder.tvTaskName.text = "$courseCode: ${task.assignmentName}"

        // Format due date
        val date = task.dueAt.substringBefore("T")
        holder.tvDueDate.text = "Due: $date"

        // Always High Priority (as requested)
        holder.tvPriority.text = "High"
        holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_high)
    }

    fun update(newList: List<Assignment>) {
        items = newList
        notifyDataSetChanged()
    }
}