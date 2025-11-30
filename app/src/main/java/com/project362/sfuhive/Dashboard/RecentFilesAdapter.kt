package com.project362.sfuhive.Dashboard

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.project362.sfuhive.R
import com.project362.sfuhive.database.storage.StoredFileEntity


class RecentFilesAdapter(
    private val context: Context,
    private var recentFiles: List<StoredFileEntity>?,
    private val onFileClickListener: (StoredFileEntity) -> Unit
) : BaseAdapter() {

    private val inflator: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getCount(): Int {
        return recentFiles?.size ?: 0
    }

    override fun getItem(pos: Int): Any? {
        return recentFiles?.get(pos)
    }

    override fun getItemId(pos: Int): Long {
        return recentFiles?.get(pos)?.id ?: pos.toLong()
    }

    override fun getView(
        pos: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val view : View
        val viewHolder: ViewHolder

        if(convertView == null){
            view = inflator.inflate(R.layout.item_recent_file, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val file = getItem(pos) as? StoredFileEntity
        viewHolder.fileName.text = file?.name ?: "..."

        view.setOnClickListener {
            file?.let { onFileClickListener(it) }
        }


        return view
    }

    fun updateData(newRecentFiles: List<StoredFileEntity>?) {
        recentFiles = newRecentFiles
        notifyDataSetChanged()
    }



    private class ViewHolder(view: View){
        val fileName: TextView = view.findViewById(R.id.file_name)
    }
}