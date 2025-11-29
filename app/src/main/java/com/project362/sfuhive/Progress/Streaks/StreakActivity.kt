package com.project362.sfuhive.Progress.Streaks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project362.sfuhive.R
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.DataViewModel
import com.project362.sfuhive.database.Streak.StreakEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class StreakActivity : AppCompatActivity() {
    private lateinit var repoVM : DataViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_streaks)

        val vmFactory =  Util.getViewModelFactory(this)

        repoVM=ViewModelProvider(this, vmFactory).get(DataViewModel::class.java)


        val theDate=Calendar.getInstance()
        val date2022=Calendar.getInstance()
        val date2017=Calendar.getInstance()
        val year = theDate.get(Calendar.YEAR)
        val month = theDate.get(Calendar.MONTH)
        val day = theDate.get(Calendar.DAY_OF_MONTH)
        date2022.set(Calendar.YEAR,2022)
        date2017.set(Calendar.YEAR,2017)

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

    override fun onResume() {
        super.onResume()

    }

    public suspend fun collectFlow(theFlow: Flow<List<StreakEntity>>){
        runBlocking {
            val theFlow=repoVM.getStreaksOfType("login")
            theFlow.collect(){ it ->
                Log.d("Streak Activity","streak login type: ${it}")
            }
        }

    }
}