package com.project362.sfuhive.Calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment

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

        val courseCode = task.courseName.split(" ").firstOrNull() ?: "Course"
        holder.tvTaskName.text = "$courseCode: ${task.assignmentName}"

        val date = task.dueAt.substringBefore("T")
        holder.tvDueDate.text = "Due: $date"

        // Default priority (until user sets custom)
        holder.tvPriority.text = "No Priority"
        holder.tvPriority.setBackgroundResource(R.drawable.bg_priority_default)
    }

    fun update(newList: List<Assignment>) {
        items = newList
        notifyDataSetChanged()
    }
}
