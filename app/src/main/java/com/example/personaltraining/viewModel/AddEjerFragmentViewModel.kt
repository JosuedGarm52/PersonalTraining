package com.example.personaltraining.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.Rutina
import kotlinx.coroutines.launch
import com.example.personaltraining.repository.RutinasRepository

class AddEjerFragmentViewModel(private val rutinasRepository: RutinasRepository) : ViewModel() {
    fun insertRutina(rutina: Rutina) = viewModelScope.launch {
        rutinasRepository.insertRutina(rutina)
    }

    suspend fun insertRutinaAndGetId(rutina: Rutina): Long {
        return rutinasRepository.insertRutinaAndGetId(rutina)
    }
    suspend fun insertarAllEjercicios(listEjercicio : List<Ejercicio>){
        rutinasRepository.insertEjercicios(listEjercicio)
    }
}

class AddEjerFragmentViewModelFactory(private val rutinasRepository: RutinasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEjerFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEjerFragmentViewModel(rutinasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}