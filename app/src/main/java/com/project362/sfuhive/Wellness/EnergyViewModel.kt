package com.project362.sfuhive.Wellness

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EnergyViewModel : ViewModel() {
    private val _energies = MutableLiveData<List<Pair<String, Int>>>() // storing both date and energy

    val energies: LiveData<List<Pair<String, Int>>> = _energies

    // functions
    fun loadEnergies(context: Context) {
        val data = CsvHelper.readEnergies(context)
        _energies.value = data
    }

    // add new energy
    fun addEnergies(context: Context, energy: Int) {
        CsvHelper.writeEnergies(context, energy) // write new energy
        loadEnergies(context) // load it again
    }
}