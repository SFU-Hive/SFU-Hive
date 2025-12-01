package com.project362.sfuhive.Calendar

import android.content.Context
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
import com.project362.sfuhive.database.EventPriority.EventPriorityDatabase
import kotlinx.coroutines.*
import java.time.LocalDate

// Displays a month grid where each cell represents a day.
class CalendarAdapter(
    private val days: MutableList<LocalDate?>,
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

        // Default cell state: empty text and hidden indicators
        holder.dayText.text = ""
        holder.bgHighlight.visibility = View.GONE
        holder.dotContainer.visibility = View.GONE
        holder.dots.forEach { it.visibility = View.GONE }

        if (date != null) {
            holder.dayText.text = date.dayOfMonth.toString()

            // Highlight selected or today's date. Keep UI updates on main thread.
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
                    // Special style for today so users can spot it quickly
                    holder.bgHighlight.visibility = View.VISIBLE
                    holder.bgHighlight.setBackgroundResource(R.drawable.bg_day_today)
                    holder.dayText.setTextColor(
                        ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                    )
                }
            }

            // SHOW PRIORITY DOTS: resolve priority strings from the EventPriority DB
            val ids = assignmentsByDate[date].orEmpty()

            if (ids.isNotEmpty()) {
                holder.dotContainer.visibility = View.VISIBLE

                val ctx = holder.itemView.context
                val dao = EventPriorityDatabase.getInstance(ctx).assignmentPriorityDao()

                // Kick off DB reads on IO dispatcher to avoid blocking UI thread
                CoroutineScope(Dispatchers.IO).launch {

                    // Fetch priorities for up to three items; default to "default" when missing
                    val priorities = ids.take(3).map { id ->
                        dao.getPriority(id) ?: "default"
                    }

                    // Switch back to Main to update view state safely
                    withContext(Dispatchers.Main) {
                        priorities.forEachIndexed { index, p ->
                            val dot = holder.dots[index]
                            dot.visibility = View.VISIBLE

                            val colorRes = when (p) {
                                "high" -> R.color.priority_high
                                "medium" -> R.color.priority_medium
                                "low" -> R.color.priority_low
                                else -> R.color.priority_default
                            }

                            // Apply tint using a ColorStateList so shapes keep their padding and size
                            dot.backgroundTintList =
                                ContextCompat.getColorStateList(ctx, colorRes)
                        }
                    }
                }
            }

            // Report clicks back to the fragment/activity so it can manage selection/navigation
            holder.itemView.setOnClickListener { onDayClicked(date) }
        }
    }

    override fun getItemCount() = days.size

    fun updateSelectedDate(newDate: LocalDate?) {
        selectedDate = newDate
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAssignments(newMap: Map<LocalDate, List<String>>) {
        assignmentsByDate = newMap.toMutableMap()
        notifyDataSetChanged()
    }

    fun setDays(newDays: List<LocalDate?>) {
        (this.days as MutableList).clear()
        (this.days as MutableList).addAll(newDays)
        notifyDataSetChanged()
    }
}
