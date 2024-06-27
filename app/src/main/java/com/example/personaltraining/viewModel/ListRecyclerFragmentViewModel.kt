package com.example.personaltraining.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.repository.RutinasRepository
import kotlinx.coroutines.launch

class ListRecyclerFragmentViewModel( private val rutinasRepository: RutinasRepository) : ViewModel() {
    // Utiliza una variable mutable para almacenar los elementos del flujo
    private val _rutinasKardex = MutableLiveData<List<Rutina>>()

    // Exponer el LiveData como una propiedad pública
    val rutinasKardex: LiveData<List<Rutina>> get() = _rutinasKardex

    init {
        viewModelScope.launch {
            // Recolectar elementos del flujo y asignarlos a la variable mutable
            rutinasRepository.getAllRutinas().collect {
                _rutinasKardex.value = it // Asigna los elementos del flujo a la variable mutable
            }
        }
    }

    fun deleteRutina(rutinaId: Int) {
        viewModelScope.launch {
            rutinasRepository.deleteRutinaWithExercises(rutinaId)
        }
    }
}

class ListRecyclerFragmentViewModelFactory(private val rutinasRepository: RutinasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListRecyclerFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListRecyclerFragmentViewModel(rutinasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}