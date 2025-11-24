package com.project362.sfuhive.Wellness

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
            viewModel.initializeGoals(this@GoalsActivity) // suspend until insert completes

            viewModel.getAllGoals().collect { goals ->
                goals.forEach { goal ->
//                    Log.d(
//                        "goalActivity",
//                        "Goal id=${goal.id}, name=${goal.goalName}, completionCount=${goal.completionCount}, badgeId=${goal.badgeId}"
//                    )
                }
            }
        }

        setupGoalCardClicks()

        attachCheckboxGuard(
            findViewById(R.id.goal1_cb),
            findViewById(R.id.goal1_title)
        )

        attachCheckboxGuard(
            findViewById(R.id.goal2_cb),
            findViewById(R.id.goal2_title)
        )

        attachCheckboxGuard(
            findViewById(R.id.goal3_cb),
            findViewById(R.id.goal3_title)
        )
    }

    private fun setupGoalCardClicks() {

        // === CARD 1 ===
        val card1 = findViewById<CardView>(R.id.goal_card1)
        val menu1 = findViewById<ImageButton>(R.id.goal1_menu)

        card1.setOnClickListener { openGoalDialog(1) }
        menu1.setOnClickListener { openGoalDialog(1) }

        // === CARD 2 ===
        val card2 = findViewById<CardView>(R.id.goal_card2)
        val menu2 = findViewById<ImageButton>(R.id.goal2_menu)

        card2.setOnClickListener { openGoalDialog(2) }
        menu2.setOnClickListener { openGoalDialog(2) }

        // === CARD 3 ===
        val card3 = findViewById<CardView>(R.id.goal_card3)
        val menu3 = findViewById<ImageButton>(R.id.goal3_menu)

        card3.setOnClickListener { openGoalDialog(3) }
        menu3.setOnClickListener { openGoalDialog(3) }
    }

    private fun openGoalDialog(goalIndex: Int) {
        val dialog = GoalDialog.newInstance(goalIndex)
        dialog.show(supportFragmentManager, "GOAL_DIALOG")
    }

    // prevent user from clicking before setting goal
    private fun attachCheckboxGuard(
        checkBox: CheckBox,
        titleView: TextView
    ) {
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->

            val title = titleView.text?.toString()?.trim()

            // If no goal title exists → block checking
            if (title.isNullOrEmpty() || title == "Tap to set goal") {
                buttonView.isChecked = false

                Toast.makeText(
                    this,
                    "Please set a goal name first!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}