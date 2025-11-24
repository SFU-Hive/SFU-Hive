package com.project362.sfuhive.Wellness

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class ChartFormatter (
    private val allDays: List<String>,      // day for each entry
    private val uniqueDays: List<String>    // distinct day list
) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        if (index !in allDays.indices) return ""

        val currentDay = allDays[index]

        // Show label only for the FIRST occurrence of the day
        val firstIndex = allDays.indexOf(currentDay)
        return if (index == firstIndex) {
            formatDayLabel(currentDay)
        } else {
            ""  // hide duplicate labels
        }
    }

    private fun formatDayLabel(day: String): String {
        return try {
            val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inFormat.parse(day)
            val outFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            outFormat.format(date!!)
        } catch (e: Exception) {
            day
        }
    }
}