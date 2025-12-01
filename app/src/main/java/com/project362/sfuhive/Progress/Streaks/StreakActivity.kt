package com.project362.sfuhive.Progress.Streaks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

// Note: Streaks feature was not implemented due to time constraints
class StreakActivity : AppCompatActivity() {
    private lateinit var repoVM : DataViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_streaks)

        val vmFactory =  Util.getViewModelFactory(this)

        repoVM=ViewModelProvider(this, vmFactory).get(DataViewModel::class.java)

        // Code written to test out StreakDatabase functionality
        val theDate=Calendar.getInstance()
        val date2022=Calendar.getInstance()
        val date2017=Calendar.getInstance()
        val year = theDate.get(Calendar.YEAR)
        val month = theDate.get(Calendar.MONTH)
        val day = theDate.get(Calendar.DAY_OF_MONTH)

        // get instances of different years
        date2022.set(Calendar.YEAR,2022)
        date2017.set(Calendar.YEAR,2017)

        // get instances of different dates to populate database
        repoVM.addStreak("login",theDate)
        repoVM.addStreak("login",date2022)
        repoVM.addStreak("login",date2017)
        val theFlow = repoVM.getStreaksOfType("login")

        val allStreaksFlow = repoVM.getAllStreaks()
        val allStreaks = allStreaksFlow.asLiveData()
        val scope = CoroutineScope(Dispatchers.Main)

        Log.d("Streak Activity","All Streaks: ${allStreaks}")
        Log.d("Streak Activity","Saved Streak: ${allStreaks}")

    }
}