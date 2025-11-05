package com.project362.sfuhive.Wellness

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.project362.sfuhive.R

class WellnessFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wellness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val energyLabel = view.findViewById<TextView>(R.id.energy_label)
        val goalsLabel = view.findViewById<TextView>(R.id.goals_label)

        energyLabel.setOnClickListener {
            val intent = Intent(requireContext(), EnergyMgmtActivity::class.java)
            startActivity(intent)
        }

        goalsLabel.setOnClickListener {
            val intent = Intent(requireContext(), GoalsActivity::class.java)
            startActivity(intent)
        }
    }
}
