package com.project362.sfuhive.Assignments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

// adapted from Daniel Dawda's MyRuns3 with assistance from ChatGPT
class CourseAdapter(private val courses: List<String>) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameText: TextView = itemView.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CourseViewHolder,
        position: Int
    ) {
        holder.courseNameText.text = courses[position]
    }

    override fun getItemCount(): Int {
        return courses.size
    }
}