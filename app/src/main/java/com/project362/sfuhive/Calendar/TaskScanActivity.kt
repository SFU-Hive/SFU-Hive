package com.project362.sfuhive.Calendar

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
import java.util.Date

class TaskScanActivity : FragmentActivity() {

    private lateinit var dataViewModel: DataViewModel

    private lateinit var etTitle: EditText
    private lateinit var etCourse: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText

    private lateinit var btnAssignment: Button
    private lateinit var btnLab: Button
    private lateinit var btnMidterm: Button
    private lateinit var btnDiscussion: Button

    private var selectedType: String? = null

    // Gallery picker
    private val galleryPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    GlobalScope.launch(Dispatchers.IO) {
                        val source = ImageDecoder.createSource(contentResolver, it)
                        val bmp = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.isMutableRequired = true
                        }

                        withContext(Dispatchers.Main) {
                            runAzureRead(bmp)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Camera picker
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                runAzureRead(bitmap)
            } else {
                Toast.makeText(this, "No photo captured.", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_scan)

        dataViewModel =
            ViewModelProvider(this, Util.getViewModelFactory(this))
                .get(DataViewModel::class.java)

        etTitle = findViewById(R.id.etTaskTitle)
        etCourse = findViewById(R.id.etCourse)
        etDate = findViewById(R.id.etDueDate)
        etTime = findViewById(R.id.etTime)

        btnAssignment = findViewById(R.id.btnTypeAssignment)
        btnLab = findViewById(R.id.btnTypeLab)
        btnMidterm = findViewById(R.id.btnTypeMidterm)
        btnDiscussion = findViewById(R.id.btnTypeDiscussion)

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            cameraLauncher.launch(null)
        }

        findViewById<Button>(R.id.btnUploadFromGallery).setOnClickListener {
            galleryPicker.launch("image/*")
        }

        etDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().build()
            picker.addOnPositiveButtonClickListener { millis ->
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                etDate.setText(fmt.format(Date(millis)))
            }
            picker.show(supportFragmentManager, "date_picker")
        }

        etTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(23)
                .setMinute(59)
                .setTitleText("Select time")
                .build()

            picker.addOnPositiveButtonClickListener {
                etTime.setText("%02d:%02d".format(picker.hour, picker.minute))
            }
            picker.show(supportFragmentManager, "time_picker")
        }

        btnAssignment.setOnClickListener { chooseType("Assignment", btnAssignment) }
        btnLab.setOnClickListener { chooseType("Lab", btnLab) }
        btnMidterm.setOnClickListener { chooseType("Midterm", btnMidterm) }
        btnDiscussion.setOnClickListener { chooseType("Discussion", btnDiscussion) }

        findViewById<Button>(R.id.btnSubmitTask).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveTask()
            } else {
                Toast.makeText(this, "Requires Android O+", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun chooseType(type: String, btn: Button) {
        selectedType = type
        listOf(btnAssignment, btnLab, btnMidterm, btnDiscussion).forEach {
            it.alpha = if (it == btn) 1f else 0.4f
        }
    }

    /** Rotate vertical photos so handwriting becomes horizontal */
    private fun rotateIfNeeded(bitmap: Bitmap): Bitmap {
        return if (bitmap.height > bitmap.width) {
            val matrix = Matrix()
            matrix.postRotate(90f)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    /** Call Azure READ API on a background thread */
    private fun runAzureRead(originalBitmap: Bitmap) {
        Toast.makeText(this, "Analyzing with Azure (Read API)...", Toast.LENGTH_SHORT).show()

        val fixedBitmap = rotateIfNeeded(originalBitmap)

        GlobalScope.launch(Dispatchers.IO) {
            val text = AzureOcrHelper.analyzeHandwriting(fixedBitmap)

            withContext(Dispatchers.Main) {
                if (text.isNullOrBlank()) {
                    Toast.makeText(
                        this@TaskScanActivity,
                        "Could not extract text from image.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        fillFieldsWithParsedText(text)
                    } else {
                        Toast.makeText(
                            this@TaskScanActivity,
                            "Parsed, but Android O+ needed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /** Parse text into title / course / date / time / type and fill only empty fields */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillFieldsWithParsedText(text: String) {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val lower = text.lowercase(Locale.US)

        // Title: preferred line contains "assignment" / "lab" / "midterm", else first line
        val titleLine = lines.firstOrNull { it.lowercase(Locale.US).contains("assignment") }
            ?: lines.firstOrNull()
        if (!titleLine.isNullOrBlank() && etTitle.text.isBlank()) {
            etTitle.setText(titleLine)
        }

        // Course: CMPT 362 style
        val courseRegex = Regex("""\b([A-Z]{3,4}\s?\d{3})\b""")
        val courseMatch = courseRegex.find(text)
        if (courseMatch != null && etCourse.text.isBlank()) {
            etCourse.setText(courseMatch.value)
        }

        // Date: e.g. 26 Nov 2025
        val dateRegex = Regex("""\b(\d{1,2})\s+([A-Za-z]{3,})\s+(\d{4})\b""")
        val dateMatch = dateRegex.find(text)
        if (dateMatch != null && etDate.text.isBlank()) {
            val day = dateMatch.groupValues[1].toInt()
            val monthStr = dateMatch.groupValues[2].lowercase(Locale.US).take(3)
            val year = dateMatch.groupValues[3].toInt()

            val months = listOf(
                "jan","feb","mar","apr","may","jun",
                "jul","aug","sep","oct","nov","dec"
            )
            val monthIndex = months.indexOf(monthStr) + 1
            if (monthIndex > 0) {
                val formatted = "%04d-%02d-%02d".format(year, monthIndex, day)
                etDate.setText(formatted)
            }
        }

        // Time: 23:59 or 11:59 PM
        val timeRegex = Regex("""\b(\d{1,2}):(\d{2})\s*(AM|PM|am|pm)?\b""")
        val timeMatch = timeRegex.find(text)
        if (timeMatch != null && etTime.text.isBlank()) {
            var hour = timeMatch.groupValues[1].toInt()
            val minute = timeMatch.groupValues[2].toInt()
            val ampm = timeMatch.groupValues.getOrNull(3)?.lowercase(Locale.US)

            if (ampm == "pm" && hour in 1..11) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            val formatted = "%02d:%02d".format(hour, minute)
            etTime.setText(formatted)
        }

        // Type detection
        when {
            "midterm" in lower -> btnMidterm.performClick()
            "lab" in lower -> btnLab.performClick()
            "discussion" in lower -> btnDiscussion.performClick()
            "assignment" in lower || "hw" in lower || "homework" in lower -> btnAssignment.performClick()
        }
    }

    /** Save the task into your existing Assignment table */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {
        val title = etTitle.text.toString().trim()
        val course = etCourse.text.toString().ifBlank { "Custom Task" }
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()

        if (title.isBlank() || date.isBlank()) {
            Toast.makeText(this, "Please fill at least title and date.", Toast.LENGTH_SHORT).show()
            return
        }

        val name = if (!selectedType.isNullOrBlank()) "$title (${selectedType})" else title

        val dueAt = if (time.isBlank()) {
            "${date}T00:00"
        } else {
            "${date}T$time"
        }

        val assignment = Assignment(
            assignmentId = 0L,
            courseName = course,
            assignmentName = name,
            dueAt = dueAt,
            pointsPossible = 0.0
        )

        dataViewModel.insertAssignment(assignment)
        Toast.makeText(this, "Task created!", Toast.LENGTH_LONG).show()
        finish()
    }
}
