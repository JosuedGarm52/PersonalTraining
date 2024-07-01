package com.example.personaltraining.UI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.personaltraining.R
import com.example.personaltraining.databinding.EditEjerFragmentBinding
import com.example.personaltraining.model.Ejercicio


/**
 * A simple [Fragment] subclass.
 * Use the [EditEjerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditEjerFragment : Fragment() {
    // TODO: Rename and change types of parameters

    val ejercicioList = mutableListOf<Ejercicio>()

    private var _binding: EditEjerFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.edit_ejer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCambiarNombre.setOnClickListener {

        }

        binding.btnEditarEjercicio.setOnClickListener{

        }
    }
}