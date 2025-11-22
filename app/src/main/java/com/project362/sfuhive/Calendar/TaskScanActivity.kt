package com.project362.sfuhive.Calendar

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

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

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val galleryPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val image = InputImage.fromFilePath(this, it)
                runOcr(image)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                val img = InputImage.fromBitmap(bitmap, 0)
                runOcr(img)
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
            picker.addOnPositiveButtonClickListener {
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                etDate.setText(fmt.format(Date(it)))
            }
            picker.show(supportFragmentManager, "date_picker")
        }

        etTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
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
            saveTask()
        }
    }

    private fun chooseType(type: String, btn: Button) {
        selectedType = type
        listOf(btnAssignment, btnLab, btnMidterm, btnDiscussion).forEach {
            it.alpha = if (it == btn) 1f else 0.4f
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun runOcr(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val text = result.text
                if (text.isBlank()) {
                    Toast.makeText(this, "No text detected.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val parsed = parseText(text)

                if (etTitle.text.isBlank() && parsed.title != null) etTitle.setText(parsed.title)
                if (etCourse.text.isBlank() && parsed.course != null) etCourse.setText(parsed.course)
                if (etDate.text.isBlank() && parsed.date != null) etDate.setText(parsed.date)
                if (etTime.text.isBlank() && parsed.time != null) etTime.setText(parsed.time)

                parsed.type?.let {
                    when (it) {
                        "Assignment" -> btnAssignment.performClick()
                        "Lab" -> btnLab.performClick()
                        "Midterm" -> btnMidterm.performClick()
                        "Discussion" -> btnDiscussion.performClick()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to analyze image.", Toast.LENGTH_SHORT).show()
            }
    }

    data class Parsed(val title: String?, val course: String?, val date: String?, val time: String?, val type: String?)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseText(text: String): Parsed {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val raw = text.replace("\n", " ")

        val title = lines.firstOrNull()

        val courseRegex = Regex("""\b([A-Z]{3,4}\s?\d{3})\b""")
        val course = courseRegex.find(raw)?.value

        val type = when {
            "midterm" in raw.lowercase() -> "Midterm"
            "lab" in raw.lowercase() -> "Lab"
            "assignment" in raw.lowercase() -> "Assignment"
            "discussion" in raw.lowercase() -> "Discussion"
            else -> null
        }

        val isoRegex = Regex("""\b\d{4}-\d{2}-\d{2}\b""")
        val isoDate = isoRegex.find(raw)?.value

        val monthNames = listOf("Jan","January","Feb","February","Mar","March","Apr","April","May","Jun","June","Jul","July","Aug","August","Sep","Sept","September","Oct","October","Nov","November","Dec","December")
        val monthRegex = Regex("""(${monthNames.joinToString("|")})\s+(\d{1,2})""", RegexOption.IGNORE_CASE)
        val mm = monthRegex.find(raw)
        val detectedDate = isoDate ?: run {
            if (mm != null) {
                val mStr = mm.groupValues[1]
                val d = mm.groupValues[2]
                val mIndex = monthNames
                    .chunked(2)
                    .indexOfFirst { it[0].equals(mStr, true) || it.getOrNull(1)?.equals(mStr, true) == true } + 1
                if (mIndex > 0) "%04d-%02d-%02d".format(LocalDate.now().year, mIndex, d.toInt()) else null
            } else null
        }

        val timeRegex = Regex("""\b\d{1,2}:\d{2}\b""")
        val time = timeRegex.find(raw)?.value

        return Parsed(title, course, detectedDate, time, type)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {
        val title = etTitle.text.toString()
        val course = etCourse.text.toString().ifBlank { "Custom Task" }
        val date = etDate.text.toString()
        val time = etTime.text.toString()

        if (title.isBlank() || date.isBlank()) {
            Toast.makeText(this, "Please fill at least title and due date.", Toast.LENGTH_SHORT).show()
            return
        }

        val name = if (selectedType != null) "$title (${selectedType})" else title

        val assignment = Assignment(
            assignmentId = 0L,
            courseName = course,
            assignmentName = name,
            dueAt = if (time.isBlank()) "${date}T00:00" else "${date}T$time",
            pointsPossible = 0.0
        )

        dataViewModel.insertAssignment(assignment)
        Toast.makeText(this, "Task Created!", Toast.LENGTH_LONG).show()
        finish()
    }
}
