package com.example.personaltraining.viewModel

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
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

    private enum class Stage { PREPARATION, EXERCISE, REST }
    private var exerciseIndex = 0
    private var preparationDone = false
    private var currentStage = Stage.PREPARATION
    private var currentTimer: CountDownTimer? = null
    private var pausedTimeRemaining: Long = 0
    var isPaused: Boolean = false
        private set //solo aqui se puede modificar? Wow
    private var isAddingSeconds = false

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

    fun isInPreparation(): Boolean {
        return currentStage == Stage.PREPARATION
    }

    private fun startPreparation() {
        currentTimer?.cancel()
        _timeLeft.value = 10L * 1000L // 10 segundos de preparación
        _isResting.value = false
        currentStage = Stage.PREPARATION

        currentTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (isPaused) {
                    pausedTimeRemaining = millisUntilFinished
                    cancel()
                } else {
                    _timeLeft.value = millisUntilFinished
                }
            }

            override fun onFinish() {
                preparationDone = true
                startExercise()
            }
        }.also {
            it.start()
        }
    }

    private fun startExercise() {
        currentTimer?.cancel()
        preparationDone = true
        if (exerciseIndex < exerciseList.value?.size ?: 0) {
            _currentExercise.value = exerciseList.value?.get(exerciseIndex)
            _timeLeft.value = mmssToSeconds(_currentExercise.value?.DEjercicio ?: "00:00")
            _isResting.value = false
            currentStage = Stage.EXERCISE

            currentTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    if (isPaused) {
                        pausedTimeRemaining = millisUntilFinished
                        cancel()
                    } else {
                        _timeLeft.value = millisUntilFinished
                    }
                }

                override fun onFinish() {
                    startRest()
                }
            }.also {
                it.start()
            }
        } else {
            // Manejar el fin de la rutina
        }
    }

    private fun startRest() {
        currentTimer?.cancel()
        _isResting.value = true
        _timeLeft.value = mmssToSeconds(_currentExercise.value?.DDescanso ?: "00:00")
        currentStage = Stage.REST

        currentTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (isPaused) {
                    pausedTimeRemaining = millisUntilFinished
                    cancel()
                } else {
                    _timeLeft.value = millisUntilFinished
                }
            }

            override fun onFinish() {
                exerciseIndex++
                startExercise()
            }
        }.also {
            it.start()
        }
    }

    fun onNextStageButtonPressed() {
        currentTimer?.cancel()
        when (currentStage) {
            Stage.PREPARATION -> startExercise()
            Stage.EXERCISE -> startRest()
            Stage.REST -> {
                exerciseIndex++
                startExercise()
            }
        }
    }

    fun onPreviousStageButtonPressed() {
        currentTimer?.cancel()
        when (currentStage) {
            Stage.EXERCISE -> {
                if (exerciseIndex > 0) {
                    exerciseIndex--
                    currentStage = Stage.REST
                    startRest() // Vuelve al descanso del ejercicio anterior
                } else {
                    // Si está en el primer ejercicio, no hacer nada
                    startExercise()
                }
            }
            Stage.REST -> {
                currentStage = Stage.EXERCISE
                startExercise() // Vuelve al ejercicio actual
            }
            Stage.PREPARATION -> {
                startPreparation()
            }
        }
    }

    fun onPauseButtonPressed() {
        if (isPaused) {
            // Reanudar el temporizador desde el tiempo pausado
            isPaused = false
            currentTimer = object : CountDownTimer(pausedTimeRemaining, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    if (isPaused) {
                        pausedTimeRemaining = millisUntilFinished
                        cancel()
                    } else {
                        _timeLeft.value = millisUntilFinished
                    }
                }

                override fun onFinish() {
                    when (currentStage) {
                        Stage.PREPARATION -> startExercise()
                        Stage.EXERCISE -> startRest()
                        Stage.REST -> {
                            exerciseIndex++
                            startExercise()
                        }
                    }
                }
            }.also { it.start() }
        } else {
            // Pausar el temporizador
            currentTimer?.cancel()
            pausedTimeRemaining = _timeLeft.value ?: 0L // Guardar el tiempo restante
            isPaused = true
        }
    }

    fun addSecondsToTimer(seconds: Long) {
        if (isPaused) {
            pausedTimeRemaining += seconds * 1000L
        } else {
            currentTimer?.cancel()
            _timeLeft.value = (_timeLeft.value ?: 0L) + seconds * 1000L
            currentTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    if (isPaused) {
                        pausedTimeRemaining = millisUntilFinished
                        cancel()
                    } else {
                        _timeLeft.value = millisUntilFinished
                    }
                }

                override fun onFinish() {
                    when (currentStage) {
                        Stage.PREPARATION -> startExercise()
                        Stage.EXERCISE -> startRest()
                        Stage.REST -> {
                            exerciseIndex++
                            startExercise()
                        }
                    }
                }
            }.also { it.start() }
        }
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
