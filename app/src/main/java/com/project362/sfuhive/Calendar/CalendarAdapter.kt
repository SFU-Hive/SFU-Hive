package com.project362.sfuhive.Calendar

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<LocalDate?>,
    private var assignmentsByDate: MutableMap<LocalDate, List<String>>,
    private var selectedDate: LocalDate?,
    private val onDayClicked: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val bgHighlight: View = itemView.findViewById(R.id.bgHighlight)
        val dotContainer: LinearLayout = itemView.findViewById(R.id.eventIndicatorContainer)
        val dots: List<View> = listOf(
            itemView.findViewById(R.id.dot1),
            itemView.findViewById(R.id.dot2),
            itemView.findViewById(R.id.dot3)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]
        holder.dayText.text = ""
        holder.bgHighlight.visibility = View.GONE
        holder.dotContainer.visibility = View.GONE
        holder.dots.forEach { it.visibility = View.GONE }

        if (date != null) {
            holder.dayText.text = date.dayOfMonth.toString()

            // Selected date highlight
            if (date == selectedDate) {
                holder.bgHighlight.visibility = View.VISIBLE
                holder.bgHighlight.setBackgroundResource(R.drawable.bg_day_selected)
                holder.dayText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                )
            } else {
                holder.bgHighlight.visibility = View.GONE
                holder.dayText.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, android.R.color.black)
                )

                val today = LocalDate.now()
                if (date == today) {
                    holder.bgHighlight.visibility = View.VISIBLE
                    holder.bgHighlight.setBackgroundResource(R.drawable.bg_day_today)
                    holder.dayText.setTextColor(
                        ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                    )
                }
            }

            // Show up to 3 event dots
            val tasks = assignmentsByDate[date].orEmpty()
            if (tasks.isNotEmpty()) {
                holder.dotContainer.visibility = View.VISIBLE
                tasks.take(3).forEachIndexed { index, task ->
                    val dot = holder.dots[index]
                    dot.visibility = View.VISIBLE

                    val colorRes = when {
                        task.contains("high", true) -> R.color.priority_high
                        task.contains("medium", true) -> R.color.priority_medium
                        task.contains("low", true) -> R.color.priority_low
                        else -> R.color.priority_default
                    }

                    dot.backgroundTintList =
                        ContextCompat.getColorStateList(holder.itemView.context, colorRes)
                }
            }

            holder.itemView.setOnClickListener { onDayClicked(date) }
        }
    }

    override fun getItemCount() = days.size

    fun updateSelectedDate(newDate: LocalDate?) {
        selectedDate = newDate
        notifyDataSetChanged()
    }

    fun updateAssignments(newData: Map<LocalDate, List<String>>) {
        assignmentsByDate.clear()
        assignmentsByDate.putAll(newData)
        notifyDataSetChanged()
    }
}
