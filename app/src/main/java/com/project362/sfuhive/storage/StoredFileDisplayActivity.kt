package com.project362.sfuhive.storage

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project362.sfuhive.databinding.ActivityFileDisplayBinding

class StoredFileDisplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDisplayBinding
    private val viewModel: StoredFileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = StoredFileAdapter()
        binding.fileRecyclerView.adapter = adapter

        viewModel.allFiles.observe(this) { files ->
            files?.let {
                adapter.setData(it)
                //if the list is empty, show the empty view
                binding.emptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.addFileButton.setOnClickListener {

        }
    }


}