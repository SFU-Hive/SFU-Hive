package com.project362.sfuhive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.project362.sfuhive.Calendar.CalendarFragment
import com.project362.sfuhive.Dashboard.DashboardFragment
import com.project362.sfuhive.Progress.ProgressFragment
import com.project362.sfuhive.Assignments.AssignmentFragment
import com.project362.sfuhive.Wellness.WellnessFragment
import java.util.ArrayList

// adapted from Actiontabs demo
class NavActivity : AppCompatActivity() {

    private lateinit var calenderFrag: CalendarFragment
    private lateinit var dashboardFrag: DashboardFragment
    private lateinit var progressFrag: ProgressFragment
    private lateinit var tasksFrag: AssignmentFragment
    private lateinit var wellnessFrag: WellnessFragment
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var navFragmentAdapter: NavFragmentAdapter
    private lateinit var fragments: ArrayList<Fragment>
    private val tabTitles = arrayOf("Dashboard", "Calendar", "Progress", "Tasks", "Wellness")
    private lateinit var tabConfigurationStrategy: TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)


        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tab)

        calenderFrag = CalendarFragment()
        dashboardFrag = DashboardFragment()
        progressFrag = ProgressFragment()
        tasksFrag = AssignmentFragment()
        wellnessFrag = WellnessFragment()

        fragments = ArrayList()
        fragments.add(calenderFrag)
        fragments.add(dashboardFrag)
        fragments.add(progressFrag)
        fragments.add(tasksFrag)
        fragments.add(wellnessFrag)

        navFragmentAdapter = NavFragmentAdapter(this, fragments)
        viewPager2.adapter = navFragmentAdapter

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            // Set icon for each tab
            when (position) {
                0 -> tab.setIcon(R.drawable.rounded_dashboard_24)
                1 -> tab.setIcon(R.drawable.rounded_calendar_today_24)
                2 -> tab.setIcon(R.drawable.rounded_assignment_24)
                3 -> tab.setIcon(R.drawable.rounded_heart_smile_24)
                4 -> tab.setIcon(R.drawable.round_card_giftcard_24)
            }
        }

        tabLayoutMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}