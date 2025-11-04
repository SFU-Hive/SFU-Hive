package com.project362.sfuhive.Assignments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.Assignments.RateSubmissionDialog.RatedAssignment

// adapted from Daniel Dawda's MyRuns3 with assistance from ChatGPT
class RatedAssignmentAdapter(private val assignments: List<RatedAssignment>, private val onItemClick: (RatedAssignment) -> Unit) : RecyclerView.Adapter<RatedAssignmentAdapter.AssignmentViewHolder>() {

    inner class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assignmentNameText: TextView = itemView.findViewById(R.id.text)

        // assistance from ChatGPT
        init {
            itemView.setOnClickListener {
                val position = getBindingAdapterPosition()
                if (position != RecyclerView.NO_POSITION) {
                    val itemId = assignments[position]
                    onItemClick(itemId)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssignmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item, parent, false)
        return AssignmentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AssignmentViewHolder,
        position: Int
    ) {
        holder.assignmentNameText.text = assignments[position].assignmentName
    }

    override fun getItemCount(): Int {
        return assignments.size
    }
}