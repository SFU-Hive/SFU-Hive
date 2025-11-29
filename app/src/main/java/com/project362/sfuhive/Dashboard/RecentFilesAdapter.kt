package com.project362.sfuhive.Dashboard

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.project362.sfuhive.R


class RecentFilesAdapter(
    private val context: Context,
    private val recentFiles: List<RecentFile>
) : BaseAdapter() {

    private val inflator: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun getCount(): Int {
        return recentFiles.size
    }

    override fun getItem(pos: Int): Any? {
        return recentFiles[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(
        pos: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val view : View
        if(convertView == null){
            view = inflator.inflate(R.layout.item_recent_file, parent, false)
        } else {
            view = convertView
        }

        val recentFile = getItem(pos) as RecentFile

        val fileName = view.findViewById<TextView>(R.id.file_name)
        fileName.text = recentFile.fileName

        return view
    }

}