package com.example.personaltraining.viewModel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.repository.RutinasRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CronoFragmentViewModel(
    private val rutinasRepository: RutinasRepository,
    private val rutinaId: Int
) : ViewModel() {

    private val _TAG = "CronoFragmentViewModel"
    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long> get() = _timeLeft

    private val _isResting = MutableLiveData<Boolean>()
    val isResting: LiveData<Boolean> get() = _isResting

    private val _currentExercise = MutableLiveData<Ejercicio>()
    val currentExercise: LiveData<Ejercicio> get() = _currentExercise

    private val _exerciseList = MutableLiveData<List<Ejercicio>>()
    val exerciseList: LiveData<List<Ejercicio>> get() = _exerciseList

    private var exerciseIndex = 0

    init {
        viewModelScope.launch {
            loadExercises()
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            rutinasRepository.getEjerciciosByRutinaId(rutinaId)
                .catch { e ->
                    Log.e(_TAG, "Error fetching exercises: $e")
                    // Manejar el error apropiadamente
                }
                .collect { ejercicios ->
                    _exerciseList.value = ejercicios
                    if (ejercicios.isNotEmpty()) {
                        startPreparation()
                    }
                }
        }
    }

    private fun startPreparation() {
        _timeLeft.value = 10L * 1000L // 10 segundos de preparación
        _isResting.value = false

        val preparationTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
                Log.d(_TAG, "Cuenta regresiva: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                startExercise()
            }
        }
        preparationTimer.start()
    }

    private fun startExercise() {
        if (exerciseIndex < exerciseList.value?.size ?: 0) {
            _currentExercise.value = exerciseList.value?.get(exerciseIndex)
            _timeLeft.value = mmssToSeconds(_currentExercise.value?.DEjercicio ?: "00:00")
            Log.d(_TAG, "Tiempo del ejercicio empezando: ${_currentExercise.value?.DEjercicio}, en segundos: ${_timeLeft.value}")
            _isResting.value = false

            startExerciseTimer()
        } else {
            // Aquí podrías manejar el fin de la rutina o navegar a otro fragmento si es necesario
        }
    }

    private fun startExerciseTimer() {
        val exerciseTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
                Log.d(_TAG, "Ejercicio...: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                startRest()
            }
        }
        exerciseTimer.start()
    }

    private fun startRest() {
        _isResting.value = true
        _timeLeft.value = mmssToSeconds(_currentExercise.value?.DDescanso ?: "00:00")
        Log.d(_TAG, "Tiempo del descanso empezando: ${_currentExercise.value?.DDescanso}, en segundos: ${_timeLeft.value}")

        val restTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
                Log.d(_TAG, "Descanso...: ${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                exerciseIndex++
                startExercise()
            }
        }
        restTimer.start()
    }

    fun mmssToSeconds(timeMMSS: String): Long {
        val parts = timeMMSS.split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid time format: $timeMMSS")
        }
        val minutes = parts[0].toLong()
        val seconds = parts[1].toLong()
        return (minutes * 60 + seconds) * 1000 // Convertir a milisegundos
    }

    fun secondsToMMSS(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}


class CronoFragmentViewModelFactory(
    private val rutinasRepository: RutinasRepository,
    private val rutinaID: Int
        ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CronoFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CronoFragmentViewModel(rutinasRepository, rutinaID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
