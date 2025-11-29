package com.project362.sfuhive.storage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.databinding.ActivityFileDisplayBinding
import kotlinx.coroutines.launch

class StoredFileDisplayActivity : AppCompatActivity(), DeleteConfirmationDialogFragment.ConfirmationListener {

    private lateinit var binding: ActivityFileDisplayBinding

    private var fileIdToDelete: Long? = null

    private val viewModel: StoredFileViewModel by viewModels(){
        val database = StoredFileDatabase.getInstance(this)
        val repository = StoredFileRepository(database.storedFileDatabaseDao)
        StoredFileViewModel.StoredFileViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = FolderAdapter(
            onItemClicked = { file -> onFileClicked(file) },
            onDeleteClicked = { file ->
                lifecycleScope.launch {
                    if(file.type == "folder"){
                        if(viewModel.isFolderEmpty(file.id)){
                            fileIdToDelete = file.id
                            DeleteConfirmationDialogFragment().show(
                                supportFragmentManager,
                                "delete_confirmation"
                            )
                        }else
                            Toast.makeText(this@StoredFileDisplayActivity, "Folder is not empty", Toast.LENGTH_SHORT).show()
                    }else{
                        fileIdToDelete = file.id
                        DeleteConfirmationDialogFragment().show(
                            supportFragmentManager,
                            "delete_confirmation"
                        )
                    }
                }
            },
            onRenameClicked = { file ->
                showRenameDialog(file)
            }
        )
        binding.fileRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.fileRecyclerView.adapter = adapter

        Log.d("xd", "Hello")
        viewModel.filesInFolder.observe(this) { files ->
            Log.d("xd", "Files in folder: $files")
            files?.let {
                adapter.setData(it)
                //if the list is empty, show the empty view
                binding.emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.addButton.setOnClickListener {
            val areButtonsVisible = binding.addFile.visibility == View.VISIBLE
            if (areButtonsVisible){
                binding.addFile.visibility = View.GONE
                binding.addDirectory.visibility = View.GONE
            }else{
                binding.addFile.visibility = View.VISIBLE
                binding.addDirectory.visibility = View.VISIBLE
            }
        }

        binding.addFile.setOnClickListener {
            addFile()
            binding.addFile.visibility = View.GONE
            binding.addDirectory.visibility = View.GONE
        }
        binding.addDirectory.setOnClickListener {
            showNewFolderDialog()
            binding.addFile.visibility = View.GONE
            binding.addDirectory.visibility = View.GONE
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.goBack()) {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        viewModel.currFolderId.observe(this) { folderId ->
            callback.isEnabled = folderId != null && folderId != 0L
        }
    }

    private fun onFileClicked(file: StoredFileEntity) {
        if (file.type == "folder") {
            viewModel.openFolder(file.id)
        }else{
            Toast.makeText(this, "File clicked: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addFile(){
        addDummyFile()
    }

    private fun addFolder(folderName: String){
        val timestamp = System.currentTimeMillis()
        val folder = StoredFileEntity(
            id = timestamp,
            parentId = viewModel.getCurrFolderId(),
            name = folderName,
            type = "folder",
            size = 0L,
            lastAccessed = timestamp,
            uploadDate = timestamp,
            source = FileSource.USER_UPLOAD
        )
        viewModel.insertFile(folder)
    }


    override fun onDeleteConfirmed() {
        fileIdToDelete?.let {
            viewModel.deleteFile(it)
        }
        fileIdToDelete = null
    }


    //test code
    private fun addDummyFile(){
        val timestamp = System.currentTimeMillis()
        val dummyFile = StoredFileEntity(
            id = timestamp,
            parentId = viewModel.getCurrFolderId(),
            name = "Report_Q4_${timestamp % 1000}.pdf",
            type = "PDF",
            size = (10000..5000000).random().toLong(), // Random size
            lastAccessed = timestamp,
            uploadDate = timestamp,
            source = FileSource.USER_UPLOAD
        )

        viewModel.insertFile(dummyFile)
    }

    private fun showNewFolderDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_folder_name_dialog, null)
        val folderNameInput = dialogView.findViewById<EditText>(R.id.folder_name)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val folderName = folderNameInput.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    addFolder(folderName)
                } else {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun showRenameDialog(fileRename: StoredFileEntity){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_folder_name_dialog, null)
        val folderNameInput = dialogView.findViewById<EditText>(R.id.folder_name)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Rename") { _, _ ->
                val folderName = folderNameInput.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    fileRename.name = folderName
                    viewModel.updateFile(fileRename)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }
}