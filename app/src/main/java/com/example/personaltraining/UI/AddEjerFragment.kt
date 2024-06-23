package com.example.personaltraining.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltraining.R
import com.example.personaltraining.adapter.EjercicioAdapterVistaPrevia
import com.example.personaltraining.databinding.AddEjerFragmentBinding
import com.example.personaltraining.model.Ejercicio

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddEjerFragment : Fragment() {

    private var _binding:AddEjerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AddEjerFragmentBinding.inflate(inflater, container, false)

        val ejercicioList = listOf(
            Ejercicio(1, "Push-up", "30s", "15s"),
            Ejercicio(2, "Sit-up", "45s", "20s"),
            Ejercicio(3, "Plank", "60s", "30s")
        )

        val adapter = EjercicioAdapterVistaPrevia(ejercicioList)
        binding.recyclerListaEjercicios.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerListaEjercicios.adapter = adapter

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_AddEjerFragment_to_ListRecyclerFragment)
        }*/

        binding.btnAsignar.setOnClickListener {
            Toast.makeText(requireContext(), "Pulso el boton asignar", Toast.LENGTH_SHORT).show()
            binding.imgCargar.isEnabled = true
            binding.edtNombreEjercicio.isEnabled = true
            binding.edtimeDuracionEjercicio.isEnabled = true
            binding.edtimeDuracionDescanso.isEnabled = true
            binding.btnGuardarEjercicio.isEnabled = true
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}