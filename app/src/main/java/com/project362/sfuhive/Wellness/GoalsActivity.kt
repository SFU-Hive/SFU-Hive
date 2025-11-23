package com.project362.sfuhive.Wellness

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.project362.sfuhive.R
import com.project362.sfuhive.Util.getViewModelFactory
import com.project362.sfuhive.database.DataViewModel
import androidx.lifecycle.lifecycleScope
import com.project362.sfuhive.database.Badge.BadgeDatabase
import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.DataRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class GoalsActivity : AppCompatActivity() {
    private lateinit var viewModel: DataViewModel

    // for testing
    private lateinit var goalName: TextView
    private lateinit var cbGoal: CheckBox


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // set up the data base stuff
        val badgeDb = BadgeDatabase.getInstance(this) // Ensure badges are inserted
        val factory = getViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(DataViewModel::class.java)

        lifecycleScope.launch {
            Log.d("goalActivity", "Initializing goals...")
            viewModel.initializeGoals(this@GoalsActivity) // suspend until insert completes

            viewModel.getAllGoals().collect { goals ->
                Log.d("goalActivity", "All goals in DB:")
                goals.forEach { goal ->
                    Log.d(
                        "goalActivity",
                        "Goal id=${goal.id}, name=${goal.goalName}, completionCount=${goal.completionCount}, badgeId=${goal.badgeId}"
                    )
                }
            }
        }
    }
}