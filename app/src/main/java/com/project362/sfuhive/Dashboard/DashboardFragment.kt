package com.project362.sfuhive.Dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.project362.sfuhive.R
import com.project362.sfuhive.storage.StoredFileDisplayActivity

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var streakIcons: Array<ImageView?>

    private lateinit var importantDateAdapter: ImportantDateAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val importantDatesListView = view.findViewById<ListView>(R.id.list_view)
        val recentFilesGridView = view.findViewById<GridView>(R.id.recent_files)
        val welcomeMessageView = view.findViewById<TextView>(R.id.welcome)
        val showMoreFilesButton = view.findViewById<ImageView>(R.id.more_files_arrow)


        streakIcons = arrayOf(
            view.findViewById(R.id.streak_item1),
            view.findViewById(R.id.streak_item2),
            view.findViewById(R.id.streak_item3),
            view.findViewById(R.id.streak_item4),
            view.findViewById(R.id.streak_item5),
            view.findViewById(R.id.streak_item6),
            view.findViewById(R.id.streak_item7)
        )

        importantDateAdapter = ImportantDateAdapter(requireContext(), mutableListOf()) {
            viewModel.deleteImportantDate(it)
        }

        importantDatesListView.adapter = importantDateAdapter

        viewModel.welcomeMessage.observe(viewLifecycleOwner) { message ->
            welcomeMessageView.text = message
        }

        viewModel.importantDates.observe(viewLifecycleOwner) { dates ->
            importantDateAdapter.updateData(dates)
        }

        viewModel.recentFiles.observe(viewLifecycleOwner) { files ->
            recentFilesGridView.adapter = RecentFilesAdapter(requireContext(), files)
        }


        viewModel.streakStatus.observe(viewLifecycleOwner) { statusList ->
            statusList.forEachIndexed { index, isComplete ->
                if(index < streakIcons.size){
                    val icon = when (isComplete) {
                        true -> R.drawable.ic_checkmark
                        false -> R.drawable.ic_cross
                        null -> R.drawable.ic_ring
                    }
                    streakIcons[index]?.setImageResource(icon)
                }
            }
        }

        showMoreFilesButton.setOnClickListener {
            val intent = Intent(requireContext(), StoredFileDisplayActivity::class.java)
            startActivity(intent)
        }
    }
}