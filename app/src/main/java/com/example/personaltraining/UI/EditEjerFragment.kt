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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltraining.R
import com.example.personaltraining.adapter.EditEjerAdapter
import com.example.personaltraining.adapter.EjercicioAdapterVistaPrevia
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.EditEjerFragmentBinding
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.viewModel.EditEjerFragmentViewModel
import com.example.personaltraining.viewModel.EditEjerFragmentViewModelFactory


/**
 * A simple [Fragment] subclass.
 * Use the [EditEjerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditEjerFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private val args: EditEjerFragmentArgs by navArgs()
    private lateinit var viewModel: EditEjerFragmentViewModel
    private lateinit var viewModelFactory: EditEjerFragmentViewModelFactory
    private lateinit var adapter: EditEjerAdapter

    private var negacion = false
    private var negacion2 = false
    private var rutinaID = 0

    private var _binding: EditEjerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (requireActivity().application as RutinasApplication).repository
        viewModelFactory = EditEjerFragmentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(EditEjerFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EditEjerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el RecyclerView y el adaptador
        adapter = EditEjerAdapter { ejercicio ->
            showOptionsMenu(ejercicio)
        }
        binding.recyclerListaEjerciciosEdit.adapter = adapter
        binding.recyclerListaEjerciciosEdit.layoutManager = LinearLayoutManager(requireContext())

        // Observa los LiveData del ViewModel
        viewModel.rutina.observe(viewLifecycleOwner) { rutina ->
            if (rutina != null) {
                // Maneja la rutina
                binding.edtNombreRutinaEdit.setText(rutina.nombre)
            } else {
                // Maneja el caso cuando la rutina no se encuentra, por ejemplo, mostrar un mensaje de error
                Toast.makeText(requireContext(), "Rutina no encontrada", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.ejercicios.observe(viewLifecycleOwner, Observer { ejercicios ->
            //Log.d("EditEjerFragment", "Lista de ejercicios actualizada: $ejercicios")
            adapter.submitList(ejercicios)
        })

        // Obtén la rutina y los ejercicios por el ID de la rutina
        if (args.ID != 0){
            viewModel.getRutinaById(args.ID)
            viewModel.getEjerciciosByRutinaId(args.ID)
            rutinaID = args.ID
        }

        binding.chkDurTiempoEdit.isChecked = true

        binding.btnCambiarNombre.setOnClickListener {
            val newName = binding.edtNombreRutinaEdit.text.toString()
            if (newName.isNotEmpty()) {
                var currentRutina = viewModel.rutina.value
                if (currentRutina != null) {
                    currentRutina.nombre = newName
                    viewModel.updateRutina(currentRutina)
                    Toast.makeText(requireContext(), "Nombre de la rutina cambiado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Rutina no encontrada", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnEditarEjercicio.setOnClickListener{
            val tipo = binding.chkRepeticionEdit.isChecked
            if (binding.edtNombreEjercicioEdit.text.isNotEmpty() &&
                binding.edtimeDuracionEjercicioEdit.text.isNotEmpty() &&
                binding.edtimeDuracionDescansoEdit.text.isNotEmpty() &&
                !negacion &&
                (!tipo || (tipo && !negacion2))) {
                val nombreEjercicio = binding.edtNombreEjercicioEdit.text.toString()
                val duracionEjercicio = darFormato(binding.edtimeDuracionEjercicioEdit.text.toString())
                val duracionDescanso = darFormato(binding.edtimeDuracionDescansoEdit.text.toString())

                val objetivo = if (tipo) binding.edtCantRepEdit.text?.toString() else null
                val ejercicio = Ejercicio(
                    ID = 0,
                    Nombre = nombreEjercicio,
                    DEjercicio = duracionEjercicio,
                    DDescanso = duracionDescanso,
                    objetivo,
                    tipo,
                    rutinaId = rutinaID
                )
                binding.edtNombreEjercicioEdit.text.clear()
                binding.edtimeDuracionEjercicioEdit.text.clear()
                binding.edtimeDuracionDescansoEdit.text.clear()
                binding.edtCantRepEdit.text?.clear()
                viewModel.addEjercicio(ejercicio)
                Toast.makeText(requireContext(), "Ejercicio agregado", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "Debes llenar todos los campos para introducir un ejercicio", Toast.LENGTH_SHORT).show()
            }
        }

        // Agregar TextWatcher a los EditText
        addTimeTextWatcher(binding.edtimeDuracionEjercicioEdit)
        addTimeTextWatcher(binding.edtimeDuracionDescansoEdit)
        addNumberTextWatcher(binding.edtCantRepEdit)

        binding.chkRepeticionEdit.setOnClickListener {
            val isChecked = binding.chkRepeticionEdit.isChecked
            binding.chkDurTiempoEdit.isChecked = !isChecked
            binding.edtCantRepEdit.isEnabled = isChecked
            binding.edtimeDuracionEjercicioEdit.hint = if (isChecked) "Estimacion del ejercicio" else "Duracion del ejercicio"
        }
        binding.chkDurTiempoEdit.setOnClickListener {
            binding.chkRepeticionEdit.isChecked = !binding.chkDurTiempoEdit.isChecked
            binding.edtCantRepEdit.isEnabled = binding.chkRepeticionEdit.isChecked
            binding.edtimeDuracionEjercicioEdit.hint = if (binding.chkRepeticionEdit.isChecked) "Estimacion del ejercicio" else "Duracion del ejercicio"
        }

    }

    private fun showOptionsMenu(ejercicio: Ejercicio) {
        // Mostrar un menú con opciones de copiar, duplicar o eliminar el ejercicio
        AlertDialog.Builder(requireContext())
            .setItems(arrayOf("Copiar", "Duplicar", "Eliminar","Cancelar")) { _, which ->
                when (which) {
                    0 -> copyEjercicio(ejercicio)
                    1 -> duplicateEjercicio(ejercicio)
                    2 -> deleteEjercicio(ejercicio)
                    3 -> {}
                }
            }
            .show()
    }

    private fun copyEjercicio(ejercicio: Ejercicio) {
        binding.edtNombreEjercicioEdit.setText(ejercicio.Nombre)
        binding.edtimeDuracionEjercicioEdit.setText(ejercicio.DEjercicio)
        binding.edtimeDuracionDescansoEdit.setText(ejercicio.DDescanso)
        binding.chkRepeticionEdit.isChecked = ejercicio.isObjetivo
        binding.chkDurTiempoEdit.isChecked = !ejercicio.isObjetivo
        binding.edtCantRepEdit.setText(if (ejercicio.isObjetivo) ejercicio.Objetivo else "")
        binding.edtCantRepEdit.isEnabled = binding.chkRepeticionEdit.isChecked
    }

    private fun duplicateEjercicio(ejercicio: Ejercicio) {
        val newEjercicio = Ejercicio(
            ID = 0,
            Nombre = ejercicio.Nombre,
            DEjercicio = ejercicio.DEjercicio,
            DDescanso = ejercicio.DDescanso,
            Objetivo = ejercicio.Objetivo,
            isObjetivo = ejercicio.isObjetivo,
            rutinaId = rutinaID
        )
        viewModel.soloAdd(newEjercicio)
    }

    private fun deleteEjercicio(ejercicio: Ejercicio) {
        viewModel.deleteEjercicio(ejercicio)
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
}