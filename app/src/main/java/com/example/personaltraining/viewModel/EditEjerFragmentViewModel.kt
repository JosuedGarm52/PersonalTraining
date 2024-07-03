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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class EditEjerFragmentViewModel(private val rutinasRepository: RutinasRepository) : ViewModel() {
    private val _rutina = MutableLiveData<Rutina?>()
    val rutina: LiveData<Rutina?> get() = _rutina

    private val _ejercicios = MutableLiveData<List<Ejercicio>>()
    val ejercicios: LiveData<List<Ejercicio>> get() = _ejercicios

    init {
        // Inicializa la lista de ejercicios vacía
        _ejercicios.value = listOf()
    }

    fun getRutinaById(rutinaId: Int) {
        viewModelScope.launch {
            val result = rutinasRepository.getRutinaById(rutinaId)
            Log.d("EditEjerFragmentViewModel", "Ejercicios obtenidos: $result")
            _rutina.value = result
            if (result == null) {
                Log.e("EditEjerFragmentViewModel", "Rutina no encontrada")
            }
        }
    }

    fun getEjerciciosByRutinaId(rutinaId: Int) {
        viewModelScope.launch {
            val result = rutinasRepository.getEjerciciosByRutinaId(rutinaId).firstOrNull()
            _ejercicios.value = result ?: listOf()
        }
    }
    fun addEjercicio(ejercicio: Ejercicio) {
        viewModelScope.launch {
            rutinasRepository.insertEjercicio(ejercicio)
            // Actualiza la lista de ejercicios
            getEjerciciosByRutinaId(ejercicio.rutinaId)
        }
    }
    fun updateRutina(rutina: Rutina) {
        viewModelScope.launch {
            rutinasRepository.updateRutina(rutina)
            getEjerciciosByRutinaId(rutina.ID)
        }
    }
    fun deleteEjercicio(ejercicio: Ejercicio) {
        viewModelScope.launch {
            rutinasRepository.deleteEjercicioById(ejercicio.ID)
            getEjerciciosByRutinaId(ejercicio.rutinaId)
        }
    }
    fun soloAdd(ejercicio: Ejercicio) {
        viewModelScope.launch {
            rutinasRepository.soloAdd(ejercicio)
            getEjerciciosByRutinaId(ejercicio.rutinaId)
        }
    }
    fun deleteRutinaWithExercises(rutinaId: Int) {
        viewModelScope.launch(SupervisorJob()) {
            try {
                rutinasRepository.deleteRutinaWithExercises(rutinaId)
                Log.d("ListRecyclerFragmentViewModel", "Rutina with id $rutinaId deleted")
            } catch (e: Exception) {
                Log.e("ListRecyclerFragmentViewModel", "Error deleting rutina with id $rutinaId: ${e.message}", e)
            }
        }
    }
}

class EditEjerFragmentViewModelFactory(private val rutinasRepository: RutinasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditEjerFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditEjerFragmentViewModel(rutinasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}