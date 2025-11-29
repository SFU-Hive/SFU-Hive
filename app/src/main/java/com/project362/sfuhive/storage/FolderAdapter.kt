package com.project362.sfuhive.storage

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project362.sfuhive.R

class FolderAdapter(
    private val onItemClicked: (StoredFileEntity) -> Unit,
    private val onDeleteClicked: (StoredFileEntity) -> Unit,
    private val onRenameClicked: (StoredFileEntity) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var filesList = emptyList<StoredFileEntity>()

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.file_name)
        private val iconImageView: ImageView = itemView.findViewById(R.id.file_icon)
        private val moreOptionsIcon: ImageView = itemView.findViewById(R.id.more_options_icon)

        fun bind(file: StoredFileEntity){
            nameTextView.text = file.name

            if(file.type == "folder"){
                iconImageView.setImageResource(R.drawable.ic_directory)
            }else{
                iconImageView.setImageResource(R.drawable.file_icon)
            }

            itemView.setOnClickListener {
                onItemClicked(file)
            }

            moreOptionsIcon.setOnClickListener {
                showPopupMenu(it, file)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.folder_list_item, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val currentFile = filesList[position]
        holder.bind(currentFile)
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(files: List<StoredFileEntity>) {
        this.filesList = files
        notifyDataSetChanged()
    }

    //https://www.geeksforgeeks.org/android/popup-menu-in-android-with-example/
    private fun showPopupMenu(view: View, file: StoredFileEntity) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.folder_option_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename_folder -> {
                    onRenameClicked(file)
                    true
                }
                R.id.action_delete_folder -> {
                    onDeleteClicked(file)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
class FolderDiffCallback : DiffUtil.ItemCallback<StoredFileEntity>(){
    override fun areItemsTheSame(oldItem: StoredFileEntity, newItem: StoredFileEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StoredFileEntity, newItem: StoredFileEntity): Boolean {
        return oldItem == newItem
    }
}

