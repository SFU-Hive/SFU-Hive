package com.project362.sfuhive.Assignments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.FirebaseRemoteDatabase.Course
import kotlin.collections.distinct

private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

// adapted from Daniel Dawda's MyRuns3 with assistance from ChatGPT
class CourseAdapter(private val onItemClick: (Course) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var displayList: List<Any> = emptyList()

    // class for title list items
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerTitle)

        // assistance from ChatGPT
        fun bind(title: String) {
            headerText.text = title
        }
    }

    // class for course list items
    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseNameText: TextView = itemView.findViewById(R.id.text)

        // assistance from ChatGPT
        fun bind(course: Course) {
            courseNameText.text = course.name
            itemView.setOnClickListener {
                onItemClick(course)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        // set ViewHolder based on viewType
        if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header, parent, false)
            return HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
            return CourseViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = displayList[position]
        if (holder is HeaderViewHolder && item is String) {
            holder.bind(item)
        } else if (holder is CourseViewHolder && item is Course) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int {
        return displayList.size
    }

    fun setCourses(myCourses: List<Course>, restCourses: List<Course>) {

        // sets all content in the recylcer view
        val list = mutableListOf<Any>()
        if (myCourses.isNotEmpty()) {
            list.add("My Courses")
            list.addAll(myCourses.distinct())
        }
        if (restCourses.isNotEmpty()) {
            list.add("Other Courses")
            list.addAll(restCourses.distinct())
        }
        displayList = list
        notifyDataSetChanged()
    }
}