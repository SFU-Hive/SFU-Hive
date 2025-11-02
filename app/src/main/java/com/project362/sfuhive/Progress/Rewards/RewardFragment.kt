package com.project362.sfuhive.Progress.Rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project362.sfuhive.R

class RewardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view =inflater.inflate(R.layout.fragment_rewards, container, false)

        return view
    }
}