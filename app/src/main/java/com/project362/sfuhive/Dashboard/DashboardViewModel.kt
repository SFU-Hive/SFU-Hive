package com.project362.sfuhive.Dashboard

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardViewModel : ViewModel(){
    private val _welcomeMessage = MutableLiveData<String>()
    val welcomeMessage: LiveData<String> = _welcomeMessage

    private val _importantDates = MutableLiveData<List<ImportantDate>>()
    val importantDates: LiveData<List<ImportantDate>> = _importantDates

    private val _streakStatus = MutableLiveData<List<Boolean?>>()
    val streakStatus: LiveData<List<Boolean?>> = _streakStatus

    private val _recentFiles = MutableLiveData<List<RecentFile>>()
    val recentFiles: LiveData<List<RecentFile>> = _recentFiles

    init{
        loadDashboardData()
    }

    private fun loadDashboardData(){
        _welcomeMessage.value = "Welcome, ${fetchUsername()}!"

        initializeDummyStreak()
        initializeDummyData()
        initializeDummyFiles()

        //fetchStreakInfo()
        //fetchRecentFiles()
        //fetchImportantDates()
    }

    fun deleteImportantDate(date: ImportantDate) {
        val currentDates = _importantDates.value ?: return
        val updatedDates = currentDates.toMutableList()
        updatedDates.remove(date)
        _importantDates.value = updatedDates
    }

    private fun fetchUsername(): String{
        //TODO fetch username from database
        return "John Doe"
    }

    private fun fetchStreakInfo(){
        //TODO fetch streak info from database
    }

    private fun fetchRecentFiles() {
        //TODO fetch recent files from database
    }

    private fun fetchImportantDates() {
        //TODO fetch important dates from database
    }

    //Testing Code for Important Dates
    private fun initializeDummyData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val datesData = listOf(
            ImportantDate("CMPT 362", today, "Milestone 3 Due", false),
            ImportantDate("CMPT 354", "2025-12-05", "Final Exam", false)
        )
        _importantDates.value = datesData
    }
    //Testing Code for Files
    private fun initializeDummyFiles() {

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recentFilesData = listOf(
            RecentFile("Milestone3_Report.pdf", today, "1.2 MB"),
            RecentFile("Lecture_Slides_Week10.pptx", "2025-11-15", "5.8 MB"),
            RecentFile("Lab08_Instructions.docx", "2025-11-14", "312 KB"),
            RecentFile("Final_Exam_Study_Guide.pdf", "2025-11-12", "850 KB")
        )
        _recentFiles.value = recentFilesData
    }

    private fun initializeDummyStreak() {
        _streakStatus.value = listOf(true, true, true, true, false, false, null)
    }

}