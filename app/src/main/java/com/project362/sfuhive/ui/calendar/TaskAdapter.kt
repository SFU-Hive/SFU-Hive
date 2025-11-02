package com.project362.sfuhive.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class TaskAdapter(private var items: List<Assignment>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvPriorityBadge: TextView = itemView.findViewById(R.id.tvPriorityBadge)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount() = items.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]

        // ✅ Combine Course + Assignment name
        val courseName = if (!task.courseName.isNullOrBlank()) "${task.courseName}: " else ""
        holder.tvTaskTitle.text = "$courseName${task.assignmentName}"

        // ✅ Format due date
        holder.tvDueDate.text = try {
            val parsed = OffsetDateTime.parse(task.dueAt)
            "Due: ${parsed.format(DateTimeFormatter.ofPattern("MMM dd"))}"
        } catch (_: Exception) {
            "Due: ${task.dueAt.substringBefore('T')}"
        }

        // ✅ Static priority for now
        holder.tvPriorityBadge.text = "High"
        holder.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_badge)
    }

    fun update(newList: List<Assignment>) {
        items = newList
        notifyDataSetChanged()
    }
}
