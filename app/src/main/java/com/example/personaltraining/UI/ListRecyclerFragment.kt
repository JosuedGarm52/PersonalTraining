package com.example.personaltraining.UI

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personaltraining.R
import com.example.personaltraining.adapter.RutinasAdapter
import com.example.personaltraining.appFiles.FileManager
import com.example.personaltraining.application.RutinasApplication
import com.example.personaltraining.databinding.ListRecyclerFragmentBinding
import com.example.personaltraining.model.Ejercicio
import com.example.personaltraining.model.Media
import com.example.personaltraining.model.MediaTipo
import com.example.personaltraining.model.Rutina
import com.example.personaltraining.viewModel.ListRecyclerFragmentViewModelFactory
import com.example.personaltraining.viewModel.ListRecyclerFragmentViewModel
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListRecyclerFragment : Fragment() {

    private var _binding: ListRecyclerFragmentBinding? = null
    private val fileManager by lazy { FileManager(requireContext()) }

    private val binding get() = _binding!!

    private val listRecyclerFragmentViewModel: ListRecyclerFragmentViewModel by viewModels {
        ListRecyclerFragmentViewModelFactory((requireActivity().application as RutinasApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListRecyclerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()

        // Verificar y crear la carpeta personalTraining si no existe
        fileManager.getMediaDirectory()

        // Handle back press event
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun setupViews() {
        binding.btnAddRutina.setOnClickListener {
            findNavController().navigate(R.id.action_ListRecyclerFragment_to_AddEjerFragment)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RutinasAdapter(
                clickt = { rutina -> onItemClick(rutina) },
                deletet = { rutina -> onItemDelete(rutina) },
                editt = { rutina -> onItemEdit(rutina) },
                listEjercicios = null // Datos iniciales vacíos
            )
        }
    }

    private fun observeViewModel() {
        listRecyclerFragmentViewModel.ejerciciosList.observe(viewLifecycleOwner) { ejercicios ->
            val adapter = binding.recyclerView.adapter as? RutinasAdapter
            adapter?.updateEjercicios(ejercicios)
        }

        listRecyclerFragmentViewModel.rutinasKardex.observe(viewLifecycleOwner) { rutinas ->
            rutinas?.let {
                // Verificar si es la primera vez que se abre la aplicación
                val isFirstTime = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    .getBoolean("isFirstTime", true)

                if (isFirstTime && rutinas.isEmpty()) {
                    createDefaultRoutine()
                    // Marcar que ya no es la primera vez que se abre la aplicación
                    requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isFirstTime", false)
                        .apply()
                }

                val adapter = binding.recyclerView.adapter as? RutinasAdapter
                adapter?.submitList(it)
            }
        }
        listRecyclerFragmentViewModel.actualizarEjercicios()
    }

    private fun createDefaultRoutine() {
        listRecyclerFragmentViewModel.viewModelScope.launch {
            // Crear la rutina predeterminada y obtener su ID insertado
            val rutina = Rutina(
                ID = 0,
                nombre = "Predeterminada",
                fechaCreacion = "03-07-2024"
            )
            val rutinaId = listRecyclerFragmentViewModel.addRutinaAndGetId(rutina)

            // Crear y agregar el primer ejercicio
            val ejercicio1 = Ejercicio(
                ID = 0,
                Nombre = "Ejercicio 1",
                DEjercicio = "00:30",
                DDescanso = "00:15",
                isObjetivo = false,
                Objetivo = null,
                rutinaId = rutinaId.toInt() // Convertir el Long a Int si es necesario
            )
            val ejercicio1Id = listRecyclerFragmentViewModel.addEjercicioAndGetId(ejercicio1)

            // Agregar medios asociados al ejercicio 1
            val img1Path = fileManager.copyRawResourceToPrivateStorage(R.drawable.flexion_lagartija1, "img_ejercicio_1.jpg")
            val img2Path = fileManager.copyRawResourceToPrivateStorage(R.drawable.flexion_lagartija2, "img_ejercicio_2.jpg")
            listRecyclerFragmentViewModel.addMedia(Media(ejercicioId = ejercicio1Id.toInt(), tipo = MediaTipo.IMAGE, ruta = img1Path?.absolutePath ?: ""))
            listRecyclerFragmentViewModel.addMedia(Media(ejercicioId = ejercicio1Id.toInt(), tipo = MediaTipo.IMAGE, ruta = img2Path?.absolutePath ?: ""))

            // Crear y agregar el segundo ejercicio
            val ejercicio2 = Ejercicio(
                ID = 0,
                Nombre = "Ejercicio 2",
                DEjercicio = "00:30",
                DDescanso = "00:15",
                isObjetivo = false,
                Objetivo = null,
                rutinaId = rutinaId.toInt() // Utilizar el mismo rutinaId para el segundo ejercicio
            )
            val ejercicio2Id = listRecyclerFragmentViewModel.addEjercicioAndGetId(ejercicio2)

            // Agregar medios asociados al ejercicio 2
            val img3Path = fileManager.copyRawResourceToPrivateStorage(R.drawable.sentadilla1, "img_ejercicio_3.jpg")
            val img4Path = fileManager.copyRawResourceToPrivateStorage(R.drawable.sentadilla2, "img_ejercicio_4.jpg")
            listRecyclerFragmentViewModel.addMedia(Media(ejercicioId = ejercicio2Id.toInt(), tipo = MediaTipo.IMAGE, ruta = img3Path?.absolutePath ?: ""))
            listRecyclerFragmentViewModel.addMedia(Media(ejercicioId = ejercicio2Id.toInt(), tipo = MediaTipo.IMAGE, ruta = img4Path?.absolutePath ?: ""))
        }
    }

    private fun onItemClick(rutina: Rutina) {
        val action = ListRecyclerFragmentDirections.actionListRecyclerFragmentToCronoFragment(rutina.ID)
        findNavController().navigate(action)
    }

    private fun onItemDelete(rutina: Rutina) {
        listRecyclerFragmentViewModel.deleteRutina(rutina.ID)
    }

    private fun onItemEdit(rutina: Rutina) {
        val action = ListRecyclerFragmentDirections.actionListRecyclerFragmentToEditEjerFragment(rutina.ID)
        findNavController().navigate(action)
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("¿Estás seguro de que deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ ->
                requireActivity().finishAffinity()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
