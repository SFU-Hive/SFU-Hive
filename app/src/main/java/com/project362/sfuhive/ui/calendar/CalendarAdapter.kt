package com.project362.sfuhive.ui.calendar

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<LocalDate?>,
    private val assignmentsByDate: Map<LocalDate, List<String>>, // date → priority list
    private var selectedDate: LocalDate?,
    private val onDayClicked: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val eventIndicator: View = itemView.findViewById(R.id.eventIndicator)
        val bgHighlight: View? = itemView.findViewById(R.id.bgHighlight) // optional in layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]

        holder.eventIndicator.visibility = View.GONE
        holder.dayText.text = ""
        holder.bgHighlight?.visibility = View.GONE

        if (date != null) {
            holder.dayText.text = date.dayOfMonth.toString()

            // ✅ Highlight selected date
            if (date == selectedDate) {
                holder.bgHighlight?.visibility = View.VISIBLE
                holder.bgHighlight?.setBackgroundResource(R.drawable.bg_day_selected)
                holder.dayText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
            } else {
                holder.bgHighlight?.visibility = View.GONE
                holder.dayText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.black)
                )
            }

            // ✅ Show dot if tasks exist
            val tasks = assignmentsByDate[date]
            if (!tasks.isNullOrEmpty()) {
                holder.eventIndicator.visibility = View.VISIBLE

                // Pick color based on priority tag stored
                val priority = tasks.first()
                val colorRes = when (priority.lowercase()) {
                    "high" -> R.color.priority_high
                    "medium" -> R.color.priority_medium
                    "low" -> R.color.priority_low
                    else -> R.color.priority_low
                }
                holder.eventIndicator.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, colorRes)
            }

            holder.itemView.setOnClickListener {
                onDayClicked(date)
            }
        }
    }

    override fun getItemCount() = days.size

    fun updateSelectedDate(newDate: LocalDate?) {
        selectedDate = newDate
        notifyDataSetChanged()
    }
}
