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
import com.example.personaltraining.UI.CronoFragmentDirections
import com.example.personaltraining.model.Media

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

    private val _mediaList = MutableLiveData<List<Media>>(emptyList())
    val mediaList: LiveData<List<Media>> get() = _mediaList

    enum class Stage { PREPARATION, EXERCISE, REST }
    private var exerciseIndex = 0
    private var index = 0
    private var preparationDone = false
    private var currentStage = Stage.PREPARATION
    private var currentTimer: CountDownTimer? = null
    private var pausedTimeRemaining: Long = 0
    var isPaused: Boolean = false
        private set //solo aqui se puede modificar? Wow
    private var isAddingSeconds = false

    private var startTime: Long = 0L
    private var startRutina: Long = 0L
    private var elapsedTime: Long = 0L
    private var isTimerRunning = false

    init {
        viewModelScope.launch {

            loadExercises()
            startTime = System.currentTimeMillis()
            startRutina = startTime
        }
    }
    fun getCurrentStage(): Stage {
        return currentStage
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
        resetTimer()
        startTimer()
        currentTimer?.cancel()
        _timeLeft.value = 10L * 1000L // 10 segundos de preparación
        _isResting.value = false
        currentStage = Stage.PREPARATION
        cambiarMedia()

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

    //BUG: durante el ejercicio y es por objetivo y pulsa en mas cinco al acabarse pasa a la siguiente etapa, si no lo pulsas si se detiene como se espera
    private fun startExercise() {
        // Iniciar el temporizador global al comenzar la preparación
        startTimer()
        currentTimer?.cancel()
        preparationDone = true
        if (exerciseIndex < exerciseList.value?.size ?: 0) {
            _currentExercise.value = exerciseList.value?.get(exerciseIndex)
            _timeLeft.value = mmssToSeconds(_currentExercise.value?.DEjercicio ?: "00:00")
            _isResting.value = false
            currentStage = Stage.EXERCISE
            cambiarMedia()

            val isObjetivo = _currentExercise.value?.isObjetivo ?: false

            currentTimer = object : CountDownTimer(_timeLeft.value ?: 0L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    if (isPaused) {
                        pausedTimeRemaining = millisUntilFinished
                        cancel()
                    } else {
                        _timeLeft.value = millisUntilFinished
                        //Log.d(_TAG, "Tiempo transcurrido en ejercicio: ${secondsToMMSS(millisUntilFinished / 1000)}")
                    }
                }

                override fun onFinish() {
                    if (!isObjetivo) {
                        startRest()
                    }
                    // No se inicia el descanso automáticamente si es objetivo
                }
            }.also {
                it.start()
            }
        } else {
            // Manejar el fin de la rutina
            val elapsedTimeInMillis = System.currentTimeMillis() - (startRutina + 1)
            Log.d(_TAG, "Fin de la rutina")
            Log.d(_TAG, "Tiempo total transcurrido: ${secondsToMMSS(elapsedTimeInMillis / 1000)}")
            navigationListener?.navigateToResultFragment(elapsedTimeInMillis)
        }
    }

    private fun startRest() {
        startTimer()
        currentTimer?.cancel()
        _isResting.value = true
        _timeLeft.value = mmssToSeconds(_currentExercise.value?.DDescanso ?: "00:00")
        currentStage = Stage.REST
        index++
        cambiarMedia()

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
        // Detener el temporizador global cuando se avanza al siguiente estado
        stopTimer()

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
        // Detener el temporizador global cuando se avanza al siguiente estado
        stopTimer()

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

    // Función para iniciar el temporizador
    fun startTimer() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis()
            isTimerRunning = true
        }
    }

    // Función para detener el temporizador
    fun stopTimer() {
        if (isTimerRunning) {
            val endTime = System.currentTimeMillis()
            elapsedTime += endTime - startTime
            isTimerRunning = false
        }
    }

    // Función para obtener el tiempo transcurrido formateado
    fun getElapsedTimeFormatted(): String {
        val seconds = elapsedTime / 1000
        val minutes = seconds / 60
        val remainderSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainderSeconds)
    }

    // Función para reiniciar el temporizador
    fun resetTimer() {
        elapsedTime = 0L
        isTimerRunning = false
    }

    // Función para obtener el tiempo total transcurrido en milisegundos
    fun getTotalElapsedTimeInMillis(): Long {
        return elapsedTime
    }

    private var navigationListener: NavigationListener? = null

    fun setNavigationListener(listener: NavigationListener) {
        navigationListener = listener
    }

    fun loadMediaForCurrentExercise(ejercicioId: Int?) {
        viewModelScope.launch {
            if (ejercicioId == null) {
                _mediaList.postValue(emptyList())
            }else{
                val media = rutinasRepository.getMediaForExercise(ejercicioId)
                _mediaList.postValue(media)
            }
        }
    }
    fun getExerciseIdAtIndex(index: Int): Int? {
        val exercises = exerciseList.value
        if (exercises != null && index in exercises.indices) {
            return exercises[index].ID
        }
        return null // En caso de índice fuera de rango o lista vacía
    }
    fun cambiarMedia(){
        loadMediaForCurrentExercise(getExerciseIdAtIndex(index))
    }
    override fun onCleared() {
        stopTimer()
        currentTimer?.cancel()
        super.onCleared()
    }

}

interface NavigationListener {
    fun navigateToResultFragment(elapsedTimeInMillis: Long)
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
