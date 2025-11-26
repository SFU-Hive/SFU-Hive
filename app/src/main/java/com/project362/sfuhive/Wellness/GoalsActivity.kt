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

    // UI references (3 cards)
    private lateinit var card1: CardView
    private lateinit var card2: CardView
    private lateinit var card3: CardView
    private lateinit var menu1: ImageButton
    private lateinit var menu2: ImageButton
    private lateinit var menu3: ImageButton

    // title TextViews (will be updated from DB)
    private lateinit var goal1Title: TextView
    private lateinit var goal2Title: TextView
    private lateinit var goal3Title: TextView

    // checkboxes
    private lateinit var goal1Cb: CheckBox
    private lateinit var goal2Cb: CheckBox
    private lateinit var goal3Cb: CheckBox


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // set up the data base stuff
        val badgeDb = BadgeDatabase.getInstance(this) // Ensure badges are inserted
        val factory = getViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(DataViewModel::class.java)

        bindViews()
        setupGoalCardClicks()

        viewModel.resetDailyGoalsIfNeeded()

        lifecycleScope.launch {
            // initialize goals (will insert defaults if empty)
            Log.d("goalActivity", "Calling initializeGoals()")
            viewModel.initializeGoals(this@GoalsActivity)

            // Collect all goals and update UI on changes
            viewModel.getAllGoals().collect { goals ->
                Log.d("goalActivity", "Received ${goals.size} goals")
                // Guard: if not exactly 3 rows we still handle gracefully
                goals.forEach { goal ->
                    when (goal.id) {
                        1L -> updateGoalRow(1, goal.goalName, goal.completionCount)
                        2L -> updateGoalRow(2, goal.goalName, goal.completionCount)
                        3L -> updateGoalRow(3, goal.goalName, goal.completionCount)
                        else -> Log.d("goalActivity", "Unexpected goal id=${goal.id}")
                    }
                }
            }
        }

        attachCheckboxGuard(findViewById(R.id.goal1_cb), findViewById(R.id.goal1_title), 1)
        attachCheckboxGuard(findViewById(R.id.goal2_cb), findViewById(R.id.goal2_title), 2)
        attachCheckboxGuard(findViewById(R.id.goal3_cb), findViewById(R.id.goal3_title), 3)
    }

    // bind to update the goals
    private fun bindViews() {
        card1 = findViewById(R.id.goal_card1)
        card2 = findViewById(R.id.goal_card2)
        card3 = findViewById(R.id.goal_card3)

        menu1 = findViewById(R.id.goal1_menu)
        menu2 = findViewById(R.id.goal2_menu)
        menu3 = findViewById(R.id.goal3_menu)

        goal1Title = findViewById(R.id.goal1_title)
        goal2Title = findViewById(R.id.goal2_title)
        goal3Title = findViewById(R.id.goal3_title)

        goal1Cb = findViewById(R.id.goal1_cb)
        goal2Cb = findViewById(R.id.goal2_cb)
        goal3Cb = findViewById(R.id.goal3_cb)
    }
    private fun setupGoalCardClicks() {
        card1.setOnClickListener { openGoalDialog(1L) }
        menu1.setOnClickListener { openGoalDialog(1L) }

        card2.setOnClickListener { openGoalDialog(2L) }
        menu2.setOnClickListener { openGoalDialog(2L) }

        card3.setOnClickListener { openGoalDialog(3L) }
        menu3.setOnClickListener { openGoalDialog(3L) }
    }

    private fun openGoalDialog(goalId: Long) {
        val dialog = GoalDialog.newInstance(goalId)
        dialog.show(supportFragmentManager, "GOAL_DIALOG")
    }

    // prevent user from clicking before setting goal
    private fun attachCheckboxGuard(
        checkBox: CheckBox,
        titleView: TextView,
        goalId: Long
    ) {
        checkBox.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                val title = titleView.text?.toString()?.trim()

                // Block checking if name not set
                if (title.isNullOrEmpty() || title == "Tap to set goal") {
                    button.isChecked = false
                    Toast.makeText(this, "Please set a goal name first!", Toast.LENGTH_SHORT).show()
                    return@setOnCheckedChangeListener
                }

                // Allowed → mark complete
                viewModel.incrementCompletion(goalId)
                Toast.makeText(this, "Marked complete!", Toast.LENGTH_SHORT).show()

                // Lock checkbox
                button.isEnabled = false

            } else {
                // Prevent unchecking after marking complete
                button.isChecked = true
                button.isEnabled = false
            }
        }
    }

    // Update the row UI
    private fun updateGoalRow(index: Int, name: String?, completion: Int) {
        runOnUiThread {

            val title = if (!name.isNullOrBlank()) name else "Tap to set goal"
            val checkBox: CheckBox = when (index) {
                1 -> goal1Cb
                2 -> goal2Cb
                3 -> goal3Cb
                else -> return@runOnUiThread
            }

            when (index) {
                1 -> goal1Title.text = title
                2 -> goal2Title.text = title
                3 -> goal3Title.text = title
            }

            // checkbox is checked only if completionCount > 0
            checkBox.isChecked = completion > 0

            Log.d(
                "goalActivity",
                "Updated UI goal $index name='$name' completion=$completion checked=${checkBox.isChecked}"
            )
        }
    }
}