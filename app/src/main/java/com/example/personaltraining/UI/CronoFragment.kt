package com.example.personaltraining.UI

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var viewModel: CronoFragmentViewModel
    private lateinit var viewModelFactory: CronoFragmentViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CronoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup any initial UI or logic
        setupUI()
    }
    private fun setupUI() {
        // Example: Setup initial state based on ViewModel data
        viewModel.exerciseList.observe(viewLifecycleOwner) { exercises ->
            // Update UI based on exercises
        }
    }
    fun showCrono() {
        binding.layoutCrono.visibility = View.VISIBLE
        binding.layoutReps.visibility = View.GONE
    }

    fun showReps() {
        binding.layoutCrono.visibility = View.GONE
        binding.layoutReps.visibility = View.VISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}