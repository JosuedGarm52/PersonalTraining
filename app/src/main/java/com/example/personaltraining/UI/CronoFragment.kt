package com.example.personaltraining.UI

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.personaltraining.R
import com.example.personaltraining.adapter.MediaItem
import com.example.personaltraining.adapter.MediaPagerAdapter
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.CronoFragmentBinding
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.MediaTipo
import com.example.personaltraining.viewModel.CronoFragmentViewModel
import com.example.personaltraining.viewModel.CronoFragmentViewModelFactory
import com.example.personaltraining.viewModel.NavigationListener

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class CronoFragment : Fragment(), NavigationListener {
    private var _binding: CronoFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: CronoFragmentArgs by navArgs()
    private val TAG = "CronoFragment"
    private var primero = true

    private lateinit var viewModel: CronoFragmentViewModel
    private lateinit var viewModelFactory: CronoFragmentViewModelFactory
    private lateinit var mediaPagerAdapter: MediaPagerAdapter
    private lateinit var viewPager: ViewPager2

    private var timer: CountDownTimer? = null // Temporizador para el cronómetro

    private var autoScrollHandler: Handler? = null
    private var autoScrollRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CronoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar que args.ID no sea cero
        if (args.ID == 0) {
            Log.e(TAG, "ID no válido")
            Toast.makeText(requireContext(), getString(R.string.invalid_id), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        // Initialize ViewModel and ViewModelFactory
        val application = requireActivity().application as RutinasApplication
        val repository = application.repository
        viewModelFactory = CronoFragmentViewModelFactory(application, repository, args.ID)
        viewModel = ViewModelProvider(this, viewModelFactory)[CronoFragmentViewModel::class.java]

        viewPager = binding.viewPagerMedia
        mediaPagerAdapter = MediaPagerAdapter(emptyList())
        viewPager.adapter = mediaPagerAdapter

        viewModel.mediaList.observe(viewLifecycleOwner) { mediaList ->
            // Mapea la lista de Media a MediaItem
            val mediaItems = mediaList.map { media ->
                when (media.tipo) {
                    MediaTipo.IMAGE, MediaTipo.GIF, MediaTipo.IMAGE_SEQUENCE -> MediaItem.Image(media.ruta)
                    MediaTipo.VIDEO -> MediaItem.Video(media.ruta)
                }
            }

            // Crea el adaptador con la nueva lista de MediaItem
            mediaPagerAdapter = MediaPagerAdapter(mediaItems)
            viewPager.adapter = mediaPagerAdapter

            // Inicia el auto-scroll si la lista no está vacía
            if (mediaItems.isNotEmpty()) {
                startAutoScroll()
            }
        }

        observeViewModel()

        // Configura el NavigationListener
        viewModel.setNavigationListener(this)

        binding.btnSiguiente.setOnClickListener {
            viewModel.onNextStageButtonPressed()
        }
        binding.btnAnterior.setOnClickListener {
            viewModel.onPreviousStageButtonPressed()
        }
        binding.btnPausa.setOnClickListener {
            viewModel.onPauseButtonPressed()
            updatePauseButtonUI(binding.btnPausa) // Método para actualizar el UI del botón
        }
        binding.btnMasCinco.setOnClickListener {
            val cincoSegundo : Long = 5
            viewModel.addSecondsToTimer(cincoSegundo)
        }
    }
    private fun observeViewModel() {
        viewModel.currentExercise.observe(viewLifecycleOwner) { exercise ->
            handleCurrentExercise(exercise)
        }

        viewModel.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            updateTimeLeft(timeLeft)
        }

        viewModel.isResting.observe(viewLifecycleOwner) { isResting ->
            updateRestingState(isResting)
        }
    }

    private fun handleCurrentExercise(exercise: Ejercicio?) {
        exercise?.let {
            isObjetive = it.isObjetivo
            if (it.isObjetivo) {
                showReps()
                binding.tvNumeroReps.text = it.Objetivo ?: "XX"
            } else {
                showCrono()
            }
        } ?: run {
            // Manejar caso donde exercise es nulo si es necesario
            showCrono() // Mostrar por defecto el cronómetro si no hay ejercicio actual
        }
    }

    private fun updateTimeLeft(timeLeft: Long) {
        // Actualizar el tiempo restante en la UI
        val timeString = viewModel.secondsToMMSS(timeLeft / 1000)
        if (viewModel.isInPreparation()) {
            binding.tvCronoEstado.text = getString(R.string.preparation_crono)
            updateBackgroundColor(R.color.orange)
            binding.tvCronoTiempo.text = timeString
        } else if (descanso) {
            binding.tvCronoTiempo.text = timeString
        } else if (!isObjetive) {
            binding.tvCronoTiempo.text = timeString
        } else {
            binding.tvCronoReps.text = timeString
        }
    }

    private fun updateRestingState(isResting: Boolean) {
        // Lógica para manejar cambios en el estado de descanso en la UI si es necesario
        descanso = isResting
        if (isResting) {
            binding.tvCronoEstado.text = getString(R.string.rest_crono)
            updateBackgroundColor(R.color.resting_background)
            showCrono()
        } else {
            if (!primero) {
                binding.tvCronoEstado.text = viewModel.currentExercise.value?.Nombre ?: getString(R.string.exercise)
                updateBackgroundColor(R.color.exercise_background)
            }
            primero = false
        }
    }


    private fun updateBackgroundColor(colorResId: Int) {
        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
    }

    // Método para actualizar el UI del botón de pausa
    private fun updatePauseButtonUI(yourPauseButton : ImageButton) {
        if (viewModel.isPaused) {
            // Cambiar a ícono de reanudar y texto de reanudar
            yourPauseButton.setImageResource(R.drawable.ic_play_arrow)
        } else {
            // Cambiar a ícono de pausa y texto de pausa
            yourPauseButton.setImageResource(R.drawable.ic_pause)
        }
    }

    private var isObjetive = false
    private var descanso = false

    private fun showCrono() {
        binding.layoutCrono.visibility = View.VISIBLE
        binding.layoutReps.visibility = View.GONE
    }

    private fun showReps() {
        binding.layoutCrono.visibility = View.GONE
        binding.layoutReps.visibility = View.VISIBLE
    }

    // Implementación de la interfaz NavigationListener
    override fun navigateToResultFragment(elapsedTimeInMillis: Long) {
        Log.d("ResultFragment", "Navigating to ResultFragment")
        val action = CronoFragmentDirections.actionCronoFragmentToResultFragment(elapsedTimeInMillis)
        findNavController().navigate(action)
    }

    private fun startAutoScroll() {
        autoScrollHandler = Handler(Looper.getMainLooper())
        autoScrollRunnable = object : Runnable {
            override fun run() {
                // Verifica si la vista del fragmento aún está disponible
                if (view != null) {
                    val itemCount = mediaPagerAdapter.itemCount
                    if (itemCount > 0) {
                        val currentItem = viewPager.currentItem
                        val nextItem = (currentItem + 1) % itemCount
                        viewPager.setCurrentItem(nextItem, true)
                    }
                    // Programa el siguiente ciclo del autoscroll
                    autoScrollHandler?.postDelayed(this, 2000) // 1000 ms = 1 second
                }
            }
        }
        // Inicia el autoscroll por primera vez
        autoScrollHandler?.post(autoScrollRunnable!!)
    }


    override fun onDestroyView() {
        autoScrollHandler?.removeCallbacks(autoScrollRunnable!!)
        autoScrollHandler = null
        autoScrollRunnable = null
        _binding = null
        timer?.cancel() // Detener el temporizador al destruir la vista
        super.onDestroyView()
    }

}