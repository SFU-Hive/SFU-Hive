package com.project362.sfuhive.Dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.project362.sfuhive.R


class ImportantDateAdapter(
    private val context: Context,
    private val importantDates: List<ImportantDate>
) : BaseAdapter(){

    private val inflator: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return importantDates.size
    }

    override fun getItem(pos: Int): Any? {
        return importantDates[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
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
        val checkbox = rowView.findViewById<CheckBox>(R.id.checkbox)

        val importantDate = getItem(pos) as ImportantDate

        courseNameTask.text = importantDate.name + " " + importantDate.task
        dateText.text = importantDate.date
        checkbox.isChecked = importantDate.isComplete

        if (importantDate.name.isNotEmpty()) {
            courseImageIdentifier.text = importantDate.name.first().toString()
        }

        return rowView
    }

}