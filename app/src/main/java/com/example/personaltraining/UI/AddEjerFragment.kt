package com.example.personaltraining.UI

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
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
    private var negacion2 = false
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

        val adapter = EjercicioAdapterVistaPrevia { ejercicio ->
            mostrarMenuOpciones(ejercicio)
        }
        binding.recyclerListaEjercicios.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerListaEjercicios.adapter = adapter

        binding.chkDurTiempo.isChecked = true
        binding.chkRepeticion.isEnabled = false
        binding.chkDurTiempo.isEnabled = false

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_AddEjerFragment_to_ListRecyclerFragment)
        }*/
        //menu que pregunta si quiere salir de la pantalla actual
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if(rutina.nombre != ""){
                // Mostrar diálogo de confirmación
                AlertDialog.Builder(requireContext())
                    .setMessage("¿Estás seguro de que deseas salir?, Se borrara tu progreso.")
                    .setPositiveButton("Sí") { _, _ ->
                        // Permitir el regreso
                        Log.d("AddEjerFragment", "rutina no null")
                        viewModel.viewModelScope.launch {
                            viewModel.deleteRutina(rutina.ID)
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }else{
                Log.d("AddEjerFragment", "rutina null")

                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.btnAsignar.setOnClickListener {
            if(binding.edtNombreRutina.text.toString().isNotEmpty()){
                binding.imgCargar.isEnabled = true
                binding.edtNombreEjercicio.isEnabled = true
                binding.edtimeDuracionEjercicio.isEnabled = true
                binding.edtimeDuracionDescanso.isEnabled = true

                binding.chkRepeticion.isEnabled = true
                binding.chkDurTiempo.isEnabled = true
                binding.edtCantRep.isEnabled = false

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

        binding.chkRepeticion.setOnClickListener {
            val isChecked = binding.chkRepeticion.isChecked
            binding.chkDurTiempo.isChecked = !isChecked
            binding.edtCantRep.isEnabled = isChecked
            binding.edtimeDuracionEjercicio.hint = if (isChecked) "Estimacion del ejercicio" else "Duracion del ejercicio"
        }
        binding.chkDurTiempo.setOnClickListener {
            binding.chkRepeticion.isChecked = !binding.chkDurTiempo.isChecked
            binding.edtCantRep.isEnabled = !binding.chkDurTiempo.isChecked
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
        addNumberTextWatcher(binding.edtCantRep)


        binding.btnGuardarEjercicio.setOnClickListener {
            val tipo = binding.chkRepeticion.isChecked
            if (binding.edtNombreEjercicio.text.isNotEmpty() &&
                binding.edtimeDuracionEjercicio.text.isNotEmpty() &&
                binding.edtimeDuracionDescanso.text.isNotEmpty() &&
                !negacion &&
                (!tipo || (tipo && !negacion2))) {
                val nombreEjercicio = binding.edtNombreEjercicio.text.toString()
                val duracionEjercicio = darFormato(binding.edtimeDuracionEjercicio.text.toString())
                val duracionDescanso = darFormato(binding.edtimeDuracionDescanso.text.toString())

                val objetivo = if (tipo) binding.edtCantRep.text?.toString() else null
                val ejercicio = Ejercicio(
                    ID = 0,
                    Nombre = nombreEjercicio,
                    DEjercicio = duracionEjercicio,
                    DDescanso = duracionDescanso,
                    objetivo,
                    tipo,
                    rutinaId = rutina.ID
                )
                binding.edtNombreEjercicio.text.clear()
                binding.edtimeDuracionEjercicio.text.clear()
                binding.edtimeDuracionDescanso.text.clear()
                binding.edtCantRep.text?.clear()
                ejercicioList.add(ejercicio)
                (binding.recyclerListaEjercicios.adapter as EjercicioAdapterVistaPrevia).submitList(ejercicioList.toList())
                Toast.makeText(requireContext(), "Ejercicio agregado", Toast.LENGTH_SHORT).show()
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

    private fun addNumberTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    val number = input.toIntOrNull()
                    if (number != null && number > 0) {
                        editText.error = null
                        negacion2 = false
                    } else {
                        editText.error = "Ingresa un número mayor a cero"
                        negacion2 = true
                    }
                } else {
                    editText.error = "El campo no puede estar vacío"
                    negacion2 = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun mostrarMenuOpciones(ejercicio: Ejercicio) {
        val opciones = arrayOf("Copiar", "Duplicar", "Eliminar", "Cancelar")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccione una opción")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> copiarEjercicio(ejercicio)
                    1 -> duplicarEjercicio(ejercicio)
                    2 -> eliminarEjercicio(ejercicio)
                    3 -> {} // Cancelar
                }
            }
            .show()
    }

    private fun copiarEjercicio(ejercicio: Ejercicio) {
        binding.edtNombreEjercicio.setText(ejercicio.Nombre)
        binding.edtimeDuracionEjercicio.setText(ejercicio.DEjercicio)
        binding.edtimeDuracionDescanso.setText(ejercicio.DDescanso)
        binding.chkRepeticion.isChecked = ejercicio.isObjetivo
        binding.chkDurTiempo.isChecked = !ejercicio.isObjetivo
        binding.edtCantRep.setText(if (ejercicio.isObjetivo) ejercicio.Objetivo else "")
    }

    private fun duplicarEjercicio(ejercicio: Ejercicio) {
        ejercicioList.add(ejercicio)
        (binding.recyclerListaEjercicios.adapter as EjercicioAdapterVistaPrevia).submitList(ejercicioList.toList())
        Toast.makeText(requireContext(), "Ejercicio agregado", Toast.LENGTH_SHORT).show()
    }

    private fun eliminarEjercicio(ejercicio: Ejercicio) {
        viewModel.viewModelScope.launch {
            ejercicioList.remove(ejercicio)
            (binding.recyclerListaEjercicios.adapter as EjercicioAdapterVistaPrevia).submitList(ejercicioList.toList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}