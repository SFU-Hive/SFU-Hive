package com.project362.sfuhive.Wellness

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.project362.sfuhive.R
import com.project362.sfuhive.Util.getViewModelFactory
import com.project362.sfuhive.database.DataViewModel
import androidx.lifecycle.lifecycleScope
import com.project362.sfuhive.Util
import com.project362.sfuhive.database.Badge.BadgeDatabase
import com.project362.sfuhive.database.Badge.BadgeEntity
import com.project362.sfuhive.database.DataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL1
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL2
import com.project362.sfuhive.Progress.Badges.BadgeFactory.Companion.GOAL3


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

    // for nfc to receive scans while open
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    var pendingGoalAssignId: Long? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        // nfc stuff
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "This device does not support NFC", Toast.LENGTH_LONG).show()
        }
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // set up the data base stuff
        //val badgeDb = BadgeDatabase.getInstance(this) // Ensure badges are inserted
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

        setBadgeObservers() // Miro added to display badges when they are unlocked
    }

    // enable scanning
    override fun onResume() {
        super.onResume()

        val adapter = nfcAdapter ?: return // check if adapter actually assigned

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )

        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        adapter.enableForegroundDispatch(this, pendingIntent, filters, null)
    }


    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
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
            val isComplete = completion > 0
            checkBox.isChecked = isComplete
            checkBox.isEnabled = !isComplete // auto reset daily

            Log.d(
                "goalActivity",
                "Updated UI goal $index name='$name' completion=$completion checked=${checkBox.isChecked}"
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val tagId = tag?.id?.joinToString("") { "%02X".format(it) } ?: return

            lifecycleScope.launch {
                handleNfcScan(tagId)
            }
        }
    }

    private fun handleNfcScan(tagId: String) {
        // CASE 1: Assigning NFC from dialog
        pendingGoalAssignId?.let { goalId ->
            assignNfcToGoal(goalId, tagId)
            pendingGoalAssignId = null  // clear ONLY after successful assignment
            return
        }

        // CASE 2: Completing goal
        lifecycleScope.launch {
            val goal = viewModel.getGoalByNfcTag(tagId)

            if (goal != null) {
                Log.d("goalActivity", "Unlock Goal Badge")
                // Found a goal → mark complete
                viewModel.incrementCompletion(goal.id)

                Toast.makeText(
                    this@GoalsActivity,
                    "Completed: ${goal.goalName}",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                // Unknown tag
                Toast.makeText(
                    this@GoalsActivity,
                    "This NFC tag is not linked to any goal",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun assignNfcToGoal(goalId: Long, tagId: String) {
        lifecycleScope.launch {

            // Prevent duplicate tags
            val existingGoal = viewModel.getGoalByNfcTag(tagId)
            if (existingGoal != null) {
                Toast.makeText(
                    this@GoalsActivity,
                    "This tag is already assigned to '${existingGoal.goalName}'",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Assign
            viewModel.updateNfcTag(goalId, tagId)

            Toast.makeText(this@GoalsActivity, "NFC assigned!", Toast.LENGTH_SHORT).show()

            // notify dialog UI
            val dialog = supportFragmentManager.findFragmentByTag("GOAL_DIALOG")
            if (dialog is GoalDialog) {
                dialog.updateNfcStatus(tagId)
            }
        }
    }

    private fun setBadgeObservers(){

        viewModel.goal1BadgeEntity.observe(this, Observer({
            if(viewModel.goal1BadgeEntity.value.isLocked==false){
                Util.UnlockBadgeDialog(GOAL1, supportFragmentManager)
            }
        }))

        viewModel.goal2BadgeEntity.observe(this, Observer({
            if(viewModel.goal2BadgeEntity.value.isLocked==false){
                Util.UnlockBadgeDialog(GOAL2, supportFragmentManager)
            }
        }))

        viewModel.goal3BadgeEntity.observe(this, Observer({
            if(viewModel.goal3BadgeEntity.value.isLocked==false){
                Util.UnlockBadgeDialog(GOAL3, supportFragmentManager)
            }
        }))
        //Util.UnlockBadgeDialog(goal.id, supportFragmentManager)

    }
}