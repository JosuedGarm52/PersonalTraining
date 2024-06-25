package com.example.personaltraining.UI

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltraining.R
import com.example.personaltraining.adapter.EjercicioAdapterVistaPrevia
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.AddEjerFragmentBinding
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.viewModel.AddEjerFragmentViewModel
import com.example.personaltraining.viewModel.AddEjerFragmentViewModelFactory
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddEjerFragment : Fragment() {

    private var negacion = false
    private var rutina = Rutina(nombre = "")

    val viewModel: AddEjerFragmentViewModel by viewModels {
        AddEjerFragmentViewModelFactory((requireActivity().application as RutinasApplication).repository)
    }

    val ejercicioList = mutableListOf<Ejercicio>()

    private var _binding:AddEjerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AddEjerFragmentBinding.inflate(inflater, container, false)

        val adapter = EjercicioAdapterVistaPrevia()
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
            if(binding.edtNombreRutina.text.toString().isNotEmpty()){
                binding.imgCargar.isEnabled = true
                binding.edtNombreEjercicio.isEnabled = true
                binding.edtimeDuracionEjercicio.isEnabled = true
                binding.edtimeDuracionDescanso.isEnabled = true
                binding.btnGuardarEjercicio.isEnabled = true
                binding.btnConfRutina.isEnabled  = true
                binding.btnAsignar.isEnabled = false
                rutina = Rutina(nombre = binding.edtNombreRutina.text.toString())
                //viewModel.insertRutina(rutina)
                // Insertar la rutina en la base de datos y obtener el ID
                viewModel.viewModelScope.launch {
                    val rutinaId = viewModel.insertRutinaAndGetId(rutina)
                    rutina = rutina.copy(ID = rutinaId.toInt()) // Convertir Long a Int si es necesario
                }
            }else{
                Toast.makeText(requireContext(), "Pulso el boton asignar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnConfRutina.setOnClickListener {
            if (ejercicioList.isNotEmpty()) {
                viewModel.insertRutina(rutina)
                // Guardar los ejercicios en la base de datos
                viewModel.viewModelScope.launch {
                    viewModel.insertarAllEjercicios(ejercicioList)
                }
                Toast.makeText(requireContext(), "Rutina guardada", Toast.LENGTH_SHORT).show()
                // Aquí podrías navegar a otra pantalla o hacer alguna otra acción después de guardar la rutina
                findNavController().navigate(R.id.action_AddEjerFragment_to_ListRecyclerFragment)
            } else {
                Toast.makeText(requireContext(), "Debes agregar al menos un ejercicio a la rutina", Toast.LENGTH_SHORT).show()
            }
        }

        // Agregar TextWatcher a los EditText
        addTimeTextWatcher(binding.edtimeDuracionEjercicio)
        addTimeTextWatcher(binding.edtimeDuracionDescanso)


        binding.btnGuardarEjercicio.setOnClickListener {
            if (binding.edtNombreEjercicio.text.isNotEmpty() &&
                binding.edtimeDuracionEjercicio.text.isNotEmpty() &&
                binding.edtimeDuracionDescanso.text.isNotEmpty() &&
                !negacion) {
                val nombreEjercicio = binding.edtNombreEjercicio.text.toString()
                val duracionEjercicio = darFormato(binding.edtimeDuracionEjercicio.text.toString())
                val duracionDescanso = darFormato(binding.edtimeDuracionDescanso.text.toString())

                val ejercicio = Ejercicio(
                    ID = 0,
                    Nombre = nombreEjercicio,
                    DEjercicio = duracionEjercicio,
                    DDescanso = duracionDescanso,
                    rutinaId = rutina!!.ID
                )
                ejercicioList.add(ejercicio)
                (binding.recyclerListaEjercicios.adapter as EjercicioAdapterVistaPrevia).submitList(ejercicioList.toList())
            }else{
                Toast.makeText(requireContext(), "Debes llenar todos los campos para introducir un ejercicio", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun darFormato(string: String) : String{
        val partes = string.split(":")
        val minutos = partes[0].padStart(2, '0')
        val segundos = partes[1].padStart(2, '0')
        return "${minutos}:${segundos}"
    }
    private fun addTimeTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    if (isValidTime(s.toString())) {
                        editText.error = null
                        negacion = false
                    } else {
                        editText.error = "Formato incorrecto. Usa MM:SS"
                        negacion = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isValidTime(time: String): Boolean {
        // Expresión regular para verificar el formato MM:SS
        val regex = "^([01]?\\d|2[0-3]):([0-5]?\\d)$".toRegex()
        return time.matches(regex)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}