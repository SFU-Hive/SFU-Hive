package com.project362.sfuhive.Progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project362.sfuhive.Progress.Badges.Badge
import com.project362.sfuhive.R
import android.widget.ImageView

class ProgressFragment : Fragment() {
    private lateinit var progressViewModel : ProgressViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_progress, container, false)
        progressViewModel=ProgressViewModel()

        val pinnedBadge1View: ImageView=view.findViewById<ImageView>(R.id.pinned_badge1)
        val pinnedBadge2View: ImageView = view.findViewById<ImageView>(R.id.pinned_badge2)
        val pinnedBadge3View: ImageView=view.findViewById<ImageView>(R.id.pinned_badge3)

        pinnedBadge1View.setImageResource(progressViewModel.getPinnedBadge(0).getIconId())
        pinnedBadge2View.setImageResource(progressViewModel.getPinnedBadge(1).getIconId())
        pinnedBadge3View.setImageResource(progressViewModel.getPinnedBadge(2).getIconId())

        return view
    }
}