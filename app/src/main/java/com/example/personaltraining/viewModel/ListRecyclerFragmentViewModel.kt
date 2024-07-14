package com.example.personaltraining.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.repository.RutinasRepository
import kotlinx.coroutines.launch

class ListRecyclerFragmentViewModel( private val rutinasRepository: RutinasRepository) : ViewModel() {
    // Utiliza una variable mutable para almacenar los elementos del flujo
    private val _rutinasKardex = MutableLiveData<List<Rutina>>()

    // Exponer el LiveData como una propiedad pública
    val rutinasKardex: LiveData<List<Rutina>> get() = _rutinasKardex

    private val _ejerciciosList = MutableLiveData<List<Ejercicio>>()
    val ejerciciosList: LiveData<List<Ejercicio>> get() = _ejerciciosList

    init {
        viewModelScope.launch {
            // Recolectar elementos del flujo y asignarlos a la variable mutable
            rutinasRepository.getAllRutinas().collect {
                _rutinasKardex.value = it // Asigna los elementos del flujo a la variable mutable
            }
            rutinasRepository.getAllEjercicios().collect {
                //Log.d("ListRecyclerFragmentViewModel", "Lista ejer comp: $it")
                _ejerciciosList.value = it
            }
        }
    }

    fun deleteRutina(rutinaId: Int) {
        viewModelScope.launch {
            rutinasRepository.deleteRutinaWithExercises(rutinaId)
        }
    }
    fun actualizarEjercicios(){
        viewModelScope.launch {
            rutinasRepository.getAllEjercicios().collect {
                //Log.d("ListRecyclerFragmentViewModel", "Lista ejer comp: $it")
                _ejerciciosList.value = it
            }
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