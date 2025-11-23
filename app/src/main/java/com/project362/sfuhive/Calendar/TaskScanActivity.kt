package com.project362.sfuhive.Calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.project362.sfuhive.Calendar.AzureOcrHelper   // ✅ FIXED IMPORT
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Assignment
import com.project362.sfuhive.database.DataViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskScanActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText

    private lateinit var dataViewModel: DataViewModel
    private lateinit var remoteConfig: FirebaseRemoteConfig

    private var selectedBytes: ByteArray? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val bmp = it.data?.extras?.get("data") as? Bitmap
            if (bmp != null) {
                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                selectedBytes = stream.toByteArray()
                runOCR(selectedBytes!!)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { loadGalleryImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_scan)

        // ✅ FIXED ViewModel initialization
        dataViewModel = ViewModelProvider(
            this,
            Util.getViewModelFactory(this)
        ).get(DataViewModel::class.java)

        remoteConfig = FirebaseRemoteConfig.getInstance()

        etTitle = findViewById(R.id.etTaskTitle)
        etDate = findViewById(R.id.etDueDate)
        etTime = findViewById(R.id.etTime)

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.btnUploadFromGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        etDate.setOnClickListener { openDatePicker() }
        etTime.setOnClickListener { openTimePicker() }

        findViewById<Button>(R.id.btnSubmitTask).setOnClickListener { saveTask() }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun loadGalleryImage(uri: Uri) {
        try {
            val source = ImageDecoder.createSource(contentResolver, uri)
            val bmp = ImageDecoder.decodeBitmap(source)

            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            selectedBytes = stream.toByteArray()

            runOCR(selectedBytes!!)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runOCR(bytes: ByteArray) {
        val key = remoteConfig.getString("AZURE_OCR_KEY")
        val endpoint = remoteConfig.getString("AZURE_OCR_ENDPOINT")

        if (key.isBlank() || endpoint.isBlank()) {
            Toast.makeText(this, "OCR config missing!", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ FIXED AzureOcrHelper usage
        val helper = AzureOcrHelper(
            activity = this,
            subscriptionKey = key,
            endpoint = endpoint
        ) { resultText ->
            processOCR(resultText)
        }

        helper.run(bytes)
    }

    private fun processOCR(raw: String) {
        val lines = raw.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        // Title = first meaningful line
        if (lines.isNotEmpty()) etTitle.setText(lines[0])

        fillDateAndTime(raw)

        Toast.makeText(this, "OCR done!", Toast.LENGTH_SHORT).show()
    }

    private fun fillDateAndTime(text: String) {
        // DATE
        val dateRegex = Regex("""\b(\d{1,2}\s+[A-Za-z]{3,9}\s+\d{4})\b""")
        val match = dateRegex.find(text)

        match?.let {
            try {
                val inputFmt = SimpleDateFormat("d MMM yyyy", Locale.US)
                val outputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val parsed = inputFmt.parse(it.value)
                etDate.setText(outputFmt.format(parsed!!))
            } catch (_: Exception) {}
        }

        // TIME
        val timeRegex = Regex("""\b(\d{1,2}:\d{2}\s*(AM|PM|am|pm))\b""")
        val timeMatch = timeRegex.find(text)

        timeMatch?.let {
            try {
                val inputFmt = SimpleDateFormat("h:mm a", Locale.US)
                val outputFmt = SimpleDateFormat("HH:mm", Locale.US)
                val parsed = inputFmt.parse(it.value)
                etTime.setText(outputFmt.format(parsed!!))
            } catch (_: Exception) {}
        }
    }

    private fun openDatePicker() {
        val cal = Calendar.getInstance()
        val dlg = DatePickerDialog(
            this,
            { _, y, m, d -> etDate.setText("$y-${m + 1}-$d") },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dlg.show()
    }

    private fun openTimePicker() {
        val cal = Calendar.getInstance()
        val dlg = TimePickerDialog(
            this,
            { _, h, min -> etTime.setText(String.format(Locale.US, "%02d:%02d", h, min)) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(this)
        )
        dlg.show()
    }

    private fun saveTask() {
        val title = etTitle.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()

        if (title.isBlank() || date.isBlank()) {
            Toast.makeText(this, "Enter title & date!", Toast.LENGTH_SHORT).show()
            return
        }

        val dueAt = if (time.isBlank()) "${date}T00:00" else "${date}T$time"

        val assignment = Assignment(
            assignmentId = 0L,
            courseName = "Task",
            assignmentName = title,
            dueAt = dueAt,
            pointsPossible = 0.0
        )

        dataViewModel.insertAssignment(assignment)

        Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
