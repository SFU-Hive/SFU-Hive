package com.project362.sfuhive.Calendar

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.project362.sfuhive.R
import com.project362.sfuhive.database.Calendar.CustomTaskDatabase
import com.project362.sfuhive.database.Calendar.CustomTaskEntity
import com.project362.sfuhive.database.Calendar.CustomTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // Date picker
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

        // Time picker helper
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
                val title = etTitle.text.toString().trim()
                val date = etDate.text.toString().trim()
                val start = etStart.text.toString().trim()
                val end = etEnd.text.toString().trim()

                if (title.isEmpty() || date.isEmpty()) {
                    Toast.makeText(context, "Please enter title and date", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Original callback (unchanged)
                onSave(title, date, start, end)

                // ✅ Persist task in CustomTaskDatabase
                val db = CustomTaskDatabase.getInstance(context)
                val repo = CustomTaskRepository(db.customDao())

                // Use lifecycle-safe coroutine to ensure DB commit
                view.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
                    repo.insert(
                        CustomTaskEntity(
                            title = title,
                            date = date,
                            startTime = if (start.isNotBlank()) start else null,
                            endTime = if (end.isNotBlank()) end else null
                        )
                    )
                }

                Toast.makeText(context, "Task saved!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
