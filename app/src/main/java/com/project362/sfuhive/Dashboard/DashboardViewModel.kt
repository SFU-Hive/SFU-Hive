package com.project362.sfuhive.Dashboard

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project362.sfuhive.Dashboard.DashboardFragment.*


class DashboardViewModel : ViewModel(){
    private val _welcomeMessage = MutableLiveData<String>().apply {
        value = "Welcome to SFU Hive, ${user?.displayName}!"
    }
    val welcomeMessage: LiveData<String> = _welcomeMessage

    private val _importantDates = MutableLiveData<List<DashboardFragment.ImportantDate>>()
    val importantDates: LiveData<List<ImportantDate>> = _importantDates

    private val _streakIcons = MutableLiveData<List<StreakInfo>>()
    val streakIcons: LiveData<List<ImageView>> = _streakIcons

    private val _recentFiles = MutableLiveData<List<RecentFile>>()
    val recentFiles: LiveData<List<RecentFile>> = _recentFiles

    init{
        loadDashboardData()
    }

    private fun loadDashboardData(){
        fetchStreakInfo()
        fetchRecentFiles()
        fetchImportantDates()
    }

    private fun fetchUsername(){
        //TODO fetch username from database
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


}