package com.project362.sfuhive.storage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project362.sfuhive.R
import com.project362.sfuhive.databinding.ActivityFileDisplayBinding
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.project362.sfuhive.database.storage.StoredFileDatabase
import com.project362.sfuhive.database.storage.StoredFileEntity
import com.project362.sfuhive.database.storage.StoredFileRepository

class StoredFileDisplayActivity : AppCompatActivity(), DeleteConfirmationDialogFragment.ConfirmationListener {

    private lateinit var binding: ActivityFileDisplayBinding

    private var fileIdToDelete: Long? = null

    private val viewModel: StoredFileViewModel by viewModels(){
        //gets the database and repository from the view model factory
        val database = StoredFileDatabase.getInstance(this)
        val repository = StoredFileRepository(database.storedFileDatabaseDao)
        StoredFileViewModel.StoredFileViewModelFactory(repository)
    }

    //https://developer.android.com/training/basics/intents/result
    //https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.OpenDocument
    //Registers a launcher for the file picker to select a file
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ uri ->
        if(uri != null){
            handleSelectedFile(uri)
        }else{
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the RecyclerView
        val adapter = FolderAdapter(
            //opens the file in the default application
            onItemClicked = { file -> onFileClicked(file) },
            //deletes the file
            onDeleteClicked = { file ->
                lifecycleScope.launch {
                    if(file.type == "folder"){
                        if(viewModel.isFolderEmpty(file.id)){
                            fileIdToDelete = file.id
                            DeleteConfirmationDialogFragment().show(
                                supportFragmentManager,
                                "delete_confirmation"
                            )
                        }else {
                            //if the folder is not empty, tell the user that the folder is not empty
                            Toast.makeText(
                                this@StoredFileDisplayActivity,
                                "Folder is not empty",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }else{
                        //if the file is not a folder, delete it
                        fileIdToDelete = file.id
                        DeleteConfirmationDialogFragment().show(
                            supportFragmentManager,
                            "delete_confirmation"
                        )
                    }
                }
            },
            //renames the file
            onRenameClicked = { file ->
                showRenameDialog(file)
            }
        )
        binding.fileRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.fileRecyclerView.adapter = adapter

        viewModel.filesInFolder.observe(this) { files ->
            files?.let {
                adapter.setData(it)
                //if the list is empty, show the empty view
                binding.emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        // Set up the add button to display the file button and add folder button
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

        // Set up the add file button to open the file picker and hide the add file and folder button
        binding.addFile.setOnClickListener {
            filePickerLauncher.launch(arrayOf("*/*"))
            binding.addFile.visibility = View.GONE
            binding.addDirectory.visibility = View.GONE
        }

        // Set up the add folder button to show the new folder dialog and hide the add file and folder button
        binding.addDirectory.setOnClickListener {
            showNewFolderDialog()
            binding.addFile.visibility = View.GONE
            binding.addDirectory.visibility = View.GONE
        }

        // Set up the back button to go back to the previous folder
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.goBack()) {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
        // Observe the current folder ID to update the callback's enabled state
        viewModel.currFolderId.observe(this) { folderId ->
            callback.isEnabled = folderId != null && folderId != 0L
        }
    }

    private fun onFileClicked(file: StoredFileEntity) {
        //opens the folder if it is a folder, otherwise opens the file
        if (file.type == "folder") {
            viewModel.openFolder(file.id)
        }else{
            //if the file doesn't have a url, the folder cannot be opened
            if(file.url == null) {
                Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show()
                return
            }
            //get the file type and open it in the default application
            val uri = file.url!!.toUri()
            val mimeType = contentResolver.getType(uri)

            val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try{
                startActivity(openFileIntent)
            }catch (e: Exception){
                Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSelectedFile(uri: Uri){
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //get the file details
        val (fileName, fileSize) = getFileDetails(uri)
        val timestamp = System.currentTimeMillis()
        //add the file to the database
        val file = StoredFileEntity(
            id = timestamp,
            parentId = viewModel.getCurrFolderId(),
            name = fileName,
            type = getFileExtension(fileName),
            size = fileSize,
            lastAccessed = timestamp,
            uploadDate = timestamp,
            url = uri.toString(),
            source = FileSource.USER_UPLOAD
        )
        viewModel.insertFile(file)
        Toast.makeText(this, "File added: $fileName", Toast.LENGTH_SHORT).show()

    }

    private fun getFileDetails(uri: Uri): Pair<String, Long>{
        var fileName = ""
        var fileSize = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if(cursor.moveToFirst()){
                fileName = cursor.getString(nameIndex)
            }
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if(cursor.moveToFirst()){
                fileSize = cursor.getLong(sizeIndex)
            }
        }

        return Pair(fileName, fileSize)
    }

    private fun getFileExtension(fileName: String): String{
        return fileName.substringAfterLast('.', "file")
    }

    //adds a folder to the database
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

    //deletes the file from the database
    override fun onDeleteConfirmed() {
        fileIdToDelete?.let {
            viewModel.deleteFile(it)
        }
        fileIdToDelete = null
    }

    //Opens the dialog to add a new folder
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

    //Shows the dialog to rename the file
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