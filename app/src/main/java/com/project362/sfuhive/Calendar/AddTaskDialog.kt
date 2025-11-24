package com.project362.sfuhive.Calendar

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.project362.sfuhive.R
import java.text.SimpleDateFormat
import java.util.*

class AddTaskDialog(
    private val context: Context,
    private val fm: FragmentManager,
    private val onSave: (title: String, date: String, start: String, end: String) -> Unit
) {

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null)

        val etTitle = view.findViewById<EditText>(R.id.etTaskTitle)
        val etDate = view.findViewById<EditText>(R.id.etTaskDate)
        val etStart = view.findViewById<EditText>(R.id.etStartTime)
        val etEnd = view.findViewById<EditText>(R.id.etEndTime)

        /** Date picker */
        etDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .build()

            picker.addOnPositiveButtonClickListener { millis ->
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                etDate.setText(format.format(Date(millis)))
            }

            picker.show(fm, "date_picker")
        }

        /** Time picker helper */
        fun showTimePicker(target: EditText) {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select time")
                .build()

            picker.addOnPositiveButtonClickListener {
                val h = "%02d".format(picker.hour)
                val m = "%02d".format(picker.minute)
                target.setText("$h:$m")
            }

            picker.show(fm, "time_picker")
        }

        etStart.setOnClickListener { showTimePicker(etStart) }
        etEnd.setOnClickListener { showTimePicker(etEnd) }

        AlertDialog.Builder(context)
            .setTitle("Add Task")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                onSave(
                    etTitle.text.toString(),
                    etDate.text.toString(),
                    etStart.text.toString(),
                    etEnd.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
