package com.project362.sfuhive.storage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

//Recycler view adapter: https://www.geeksforgeeks.org/android/android-recyclerview/
class StoredFileAdapter : RecyclerView.Adapter<StoredFileAdapter.FileViewHolder>() {

    private var filesList = emptyList<StoredFileEntity>()

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val fileNameTextView: TextView = itemView.findViewById(R.id.file_name)
        val fileDetailsTextView: TextView = itemView.findViewById(R.id.file_details)
        val fileIconImageView: ImageView = itemView.findViewById(R.id.file_icon)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.file_list_item, parent, false)
        return FileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val currentFile = filesList[position]
        holder.fileNameTextView.text = currentFile.name
        val fileDetails = "${currentFile.type} • ${currentFile.size} bytes"
        holder.fileDetailsTextView.text = fileDetails
        holder.fileIconImageView.setImageResource(R.drawable.file_icon)
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(files: List<StoredFileEntity>) {
        this.filesList = files
        notifyDataSetChanged()
    }
}
