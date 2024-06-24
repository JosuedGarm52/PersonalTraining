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

    private var negacion = false

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
            //Toast.makeText(requireContext(), "Pulso el boton asignar", Toast.LENGTH_SHORT).show()
            binding.imgCargar.isEnabled = true
            binding.edtNombreEjercicio.isEnabled = true
            binding.edtimeDuracionEjercicio.isEnabled = true
            binding.edtimeDuracionDescanso.isEnabled = true
            binding.btnGuardarEjercicio.isEnabled = true
        }

        // Agregar TextWatcher a los EditText
        addTimeTextWatcher(binding.edtimeDuracionEjercicio)
        addTimeTextWatcher(binding.edtimeDuracionDescanso)

        binding.btnGuardarEjercicio.setOnClickListener {
            if(binding.edtimeDuracionEjercicio.text != null
                && binding.edtNombreEjercicio.text != null
                && binding.edtimeDuracionDescanso.text != null
                && negacion){
                val DurEjercicio = binding.edtimeDuracionEjercicio.text.toString()
                val strDE = darFormato(DurEjercicio)
                val strDD = darFormato(binding.edtimeDuracionDescanso.text.toString())
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