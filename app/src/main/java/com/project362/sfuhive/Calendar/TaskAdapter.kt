package com.project362.sfuhive.Calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.EventPriority.AssignmentPriority
import com.project362.sfuhive.database.EventPriority.EventPriorityDatabase
import kotlinx.coroutines.*

class TaskAdapter(
    private var items: List<Assignment>,
    private var priorityIds: List<String>,
    private val onPriorityChanged: (() -> Unit)? = null   // 🔥 NEW CALLBACK
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        val ctx = holder.itemView.context

        val courseCode = task.courseName.split(" ").firstOrNull() ?: "Task"
        holder.tvTaskName.text = "$courseCode: ${task.assignmentName}"

        holder.tvDueDate.text = "Due: ${task.dueAt.substringBefore("T")}"

        val priorityId = priorityIds[position]
        val dao = EventPriorityDatabase.getInstance(ctx).assignmentPriorityDao()

        CoroutineScope(Dispatchers.IO).launch {
            val stored = dao.getPriority(priorityId) ?: "default"

            withContext(Dispatchers.Main) {
                applyPriorityUI(holder.tvPriority, stored)
                holder.tvPriority.setOnClickListener {
                    showPriorityMenu(holder, priorityId, stored)
                }
            }
        }
    }

    private fun applyPriorityUI(tv: TextView, p: String) {
        val ctx = tv.context
        when (p) {
            "high" -> {
                tv.text = "High"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_high)
            }
            "medium" -> {
                tv.text = "Medium"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_medium)
            }
            "low" -> {
                tv.text = "Low"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_low)
            }
            else -> {
                tv.text = "No Priority"
                tv.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_default)
            }
        }
    }

    private fun showPriorityMenu(holder: TaskViewHolder, id: String, old: String) {
        val ctx = holder.itemView.context
        val popup = PopupMenu(ctx, holder.tvPriority)

        popup.menu.add("High")
        popup.menu.add("Medium")
        popup.menu.add("Low")
        popup.menu.add("Remove Priority")

        popup.setOnMenuItemClickListener { item ->
            val chosen = when (item.title.toString()) {
                "High" -> "high"
                "Medium" -> "medium"
                "Low" -> "low"
                else -> "default"
            }

            val dao = EventPriorityDatabase.getInstance(ctx).assignmentPriorityDao()

            CoroutineScope(Dispatchers.IO).launch {
                dao.setPriority(AssignmentPriority(id, chosen))
                withContext(Dispatchers.Main) {
                    applyPriorityUI(holder.tvPriority, chosen)
                    onPriorityChanged?.invoke()   // 🔥 TRIGGER CALLBACK
                }
            }
            true
        }

        popup.show()
    }

    fun update(newItems: List<Assignment>, newIds: List<String>) {
        items = newItems
        priorityIds = newIds
        notifyDataSetChanged()
    }
}
