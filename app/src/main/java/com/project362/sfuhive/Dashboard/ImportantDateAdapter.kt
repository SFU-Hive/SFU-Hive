package com.project362.sfuhive.Dashboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Assignment


class ImportantDateAdapter(
    private val context: Context,
    private val importantDates: MutableList<Assignment>,
) : BaseAdapter(){

    private val inflator: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return importantDates.size
    }

    override fun getItem(pos: Int): Any? {
        return importantDates[pos]
    }

    override fun getItemId(pos: Int): Long {
        return importantDates[pos].assignmentId
    }

    override fun getView(
        pos: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val rowView = convertView?: inflator.inflate(R.layout.important_date_item, parent, false)

        val courseImageIdentifier = rowView.findViewById<TextView>(R.id.important_date_list_item_image)
        val courseNameTask = rowView.findViewById<TextView>(R.id.course_name)
        val dateText = rowView.findViewById<TextView>(R.id.date_text)

        val assignment = getItem(pos) as Assignment

        courseNameTask.text = assignment.assignmentName
        dateText.text = assignment.dueAt


        //takes the first letter of the course name and displays it on the file icon
        if (assignment.courseName.isNotEmpty()) {
            courseImageIdentifier.text = assignment.courseName.first().toString()
        }else{
            //if the course name is empty, display a question mark
            courseImageIdentifier.text = "?"
        }
        return rowView
    }
    //updates the data in the adapter
    fun updateData(newDates: List<Assignment>) {
        importantDates.clear()
        importantDates.addAll(newDates)
        notifyDataSetChanged()
    }


}