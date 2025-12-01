package com.project362.sfuhive.Dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.project362.sfuhive.R
import com.project362.sfuhive.storage.StoredFileDisplayActivity
import com.project362.sfuhive.database.storage.StoredFileEntity
import androidx.core.net.toUri

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var importantDateAdapter: ImportantDateAdapter
    private lateinit var recentFilesAdapter: RecentFilesAdapter



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


        importantDateAdapter = ImportantDateAdapter(requireContext(), mutableListOf())

        recentFilesAdapter = RecentFilesAdapter(requireContext(), null){ file ->
            openFile(file)
        }

        importantDatesListView.adapter = importantDateAdapter
        recentFilesGridView.adapter = recentFilesAdapter

        viewModel.welcomeMessage.observe(viewLifecycleOwner) { message ->
            welcomeMessageView.text = message
        }

        viewModel.importantDates.observe(viewLifecycleOwner) { dates ->
            importantDateAdapter.updateData(dates)
        }

        viewModel.recentFiles.observe(viewLifecycleOwner) { files ->
            recentFilesAdapter.updateData(files)
        }


        showMoreFilesButton.setOnClickListener {
            val intent = Intent(requireContext(), StoredFileDisplayActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        //refresh the data when the fragment is resumed
        viewModel.loadDashboardData()
    }

    //opens the file in the default application
    private fun openFile(file: StoredFileEntity){
        val intent = Intent(Intent.ACTION_VIEW)
        //Get the file URL
        val uri = file.url?.toUri()
        if (uri == null) {
            Toast.makeText(requireContext(), "Cannot open file", Toast.LENGTH_SHORT).show()
            return
        }
        //Get the file type
        val mimeType = requireContext().contentResolver.getType(uri)
        intent.setDataAndType(uri, mimeType)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening file", Toast.LENGTH_SHORT).show()
        }
    }
}