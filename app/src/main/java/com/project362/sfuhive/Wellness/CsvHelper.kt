package com.project362.sfuhive.Wellness

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvHelper {

    private fun getFile(context: Context): File {
        return File(context.filesDir, "energy.csv")
    }

    // write to csv
    fun writeEnergies(context: Context, energy: Int) {
        val file = getFile(context)
        val date = Date()
        val line = "$date,$energy\n"

        file.appendText(line)
    }

    // read from csv
    fun readEnergies(context: Context): List<Pair<String, Int>> {
        val file = getFile(context)
        if (!file.exists()) return emptyList()

        // maps date and energy
        return file.readLines().mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 2) {
                val date = parts[0]
                val energy = parts[1].toIntOrNull()
                if (energy != null) Pair(date, energy) else null
            } else null
        }
    }
}