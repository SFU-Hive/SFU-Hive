package com.project362.sfuhive.ui.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.databinding.ItemCalendarDayBinding
import java.time.LocalDate

class CalendarAdapter(
    private val days: List<LocalDate?>,
    private val onDayClicked: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]

        if (date == null) {
            holder.binding.dayText.text = ""
            holder.binding.eventIndicator.visibility = View.INVISIBLE
        } else {
            holder.binding.dayText.text = date.dayOfMonth.toString()
            holder.binding.eventIndicator.visibility = View.INVISIBLE

            holder.itemView.setOnClickListener { onDayClicked(date) }
        }
    }

    override fun getItemCount(): Int = days.size
}
