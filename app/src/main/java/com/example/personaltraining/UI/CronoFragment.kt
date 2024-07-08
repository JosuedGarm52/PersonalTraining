package com.example.personaltraining.UI

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.personaltraining.R
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.CronoFragmentBinding
import com.example.personaltraining.viewModel.CronoFragmentViewModel
import com.example.personaltraining.viewModel.CronoFragmentViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [CronoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CronoFragment : Fragment() {
    private var _binding: CronoFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: CronoFragmentArgs by navArgs()
    private val TAG = "CronoFragment"
    private var primero = true

    private lateinit var viewModel: CronoFragmentViewModel
    private lateinit var viewModelFactory: CronoFragmentViewModelFactory

    private var timer: CountDownTimer? = null // Temporizador para el cronómetro

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CronoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar que args.ID no sea cero
        if (args.ID == 0) {
            Log.e(TAG, "ID no válido")
            Toast.makeText(requireContext(), "ID no válido", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        // Initialize ViewModel and ViewModelFactory
        val repository = (requireActivity().application as RutinasApplication).repository
        viewModelFactory = CronoFragmentViewModelFactory(repository, args.ID)
        viewModel = ViewModelProvider(this, viewModelFactory)[CronoFragmentViewModel::class.java]

        observeViewModel()
    }
    private fun observeViewModel() {

        viewModel.currentExercise.observe(viewLifecycleOwner) { exercise ->
            exercise?.let {
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


        viewModel.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            // Actualizar el tiempo restante en la UI
            binding.tvCronoTiempo.text = viewModel.secondsToMMSS(timeLeft / 1000)
        }

        viewModel.isResting.observe(viewLifecycleOwner) { isResting ->
            // Lógica para manejar cambios en el estado de descanso en la UI si es necesario
            if (isResting) {
                binding.tvCronoEstado.text = "Descanso"
                binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.resting_background))
            } else {
                if (!primero) {
                    binding.tvCronoEstado.text = "Ejercicio"
                    binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.exercise_background))
                }
                primero = false
            }
        }
    }

    private fun showCrono() {
        binding.layoutCrono.visibility = View.VISIBLE
        binding.layoutReps.visibility = View.GONE
    }

    private fun showReps() {
        binding.layoutCrono.visibility = View.GONE
        binding.layoutReps.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        timer?.cancel() // Detener el temporizador al destruir la vista
    }

}