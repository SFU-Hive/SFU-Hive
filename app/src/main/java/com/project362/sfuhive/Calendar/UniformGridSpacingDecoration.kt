package com.project362.sfuhive.Calendar

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max


 // A RecyclerView.ItemDecoration that applies equal spacing between grid items.
class UniformGridSpacingDecoration(
    private val spanCount: Int,
    private val spacingPx: Int
) : RecyclerView.ItemDecoration() {

     //Calculates the offset for each item in the RecyclerView grid.
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos == RecyclerView.NO_POSITION) return

        val column = pos % spanCount
        // Calculate left/right spacing so that total space between items equals spacingPx
        outRect.left   = spacingPx * column / spanCount
        outRect.right  = spacingPx * (spanCount - 1 - column) / spanCount

        // Top spacing: give full spacing to items in the first row, half spacing otherwise
        outRect.top    = if (pos < spanCount) spacingPx else spacingPx / 2
        // Bottom spacing is half so that combined vertical spacing between rows equals spacingPx
        outRect.bottom = spacingPx / 2
    }
}