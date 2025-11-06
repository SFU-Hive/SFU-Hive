package com.project362.sfuhive.Calendar

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class UniformGridSpacingDecoration(
    private val spanCount: Int,
    private val spacingPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos == RecyclerView.NO_POSITION) return

        val column = pos % spanCount
        outRect.left   = spacingPx * column / spanCount
        outRect.right  = spacingPx * (spanCount - 1 - column) / spanCount
        outRect.top    = if (pos < spanCount) spacingPx else spacingPx / 2
        outRect.bottom = spacingPx / 2
    }
}