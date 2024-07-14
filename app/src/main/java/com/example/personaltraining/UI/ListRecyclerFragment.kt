package com.example.personaltraining.UI

import android.app.AlertDialog
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

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        /*
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_ListRecyclerFragment_to_AddEjerFragment)
        }*/

        // Verificar y crear la carpeta personalTraining si no existe
        fileManager.getMediaDirectory()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Diálogo de confirmación específico para el primer fragmento
                AlertDialog.Builder(requireContext())
                    .setMessage("¿Estás seguro de que deseas salir de la aplicacion?")
                    .setPositiveButton("Sí") { _, _ ->
                        isEnabled = false
                        requireActivity().finishAffinity()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        })

        binding.btnAddRutina.setOnClickListener{
            findNavController().navigate(R.id.action_ListRecyclerFragment_to_AddEjerFragment)
        }
        val adapter = RutinasAdapter(
            clickt = { rutina -> onItemClick(rutina) },
            deletet = { rutina -> onItemDelete(rutina) },
            editt = { rutina -> onItemEdit(rutina) },
            listEjercicios = null // Datos iniciales vacíos
        )

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        listRecyclerFragmentViewModel.ejerciciosList.observe(viewLifecycleOwner) { ejercicios ->
            //Log.d("ListRecyclerFragment", "Lista de ejercicios actualizada: $ejercicios")
            adapter.updateEjercicios(ejercicios)
        }
        listRecyclerFragmentViewModel.actualizarEjercicios()

        try {
            listRecyclerFragmentViewModel.rutinasKardex.observe(viewLifecycleOwner) { rutinas ->
                rutinas?.let {
                    Log.d("ListRecyclerFragment", "Lista de rutinas actualizada: $it")
                    adapter.submitList(it)
                }
            }
        } catch (e: Exception) {
            Log.e("ListRecyclerFragment", "Error al observar rutinasKardex: ${e.message}", e)
        }

    }

    private fun onItemClick(it: Rutina) {
        Log.d("ListRecyclerFragment", "onItem clic")
        Toast.makeText(requireContext(), "Clic a ${it.nombre}", Toast.LENGTH_SHORT).show()

        val action = ListRecyclerFragmentDirections.actionListRecyclerFragmentToCronoFragment(it.ID)
        findNavController().navigate(action)
    }

    private fun onItemDelete(rutina: Rutina) {
        Log.d("ListRecyclerFragment", "onItem delete")
        Toast.makeText(requireContext(), "Eliminar ${rutina.nombre}", Toast.LENGTH_SHORT).show()

        listRecyclerFragmentViewModel.deleteRutina(rutina.ID)
    }

    private fun onItemEdit(rutina: Rutina) {
        Log.d("ListRecyclerFragment", "onItem edit")
        Toast.makeText(requireContext(), "Editar ${rutina.nombre}", Toast.LENGTH_SHORT).show()
        // Implementa lógica para editar la rutina

        val action = ListRecyclerFragmentDirections.actionListRecyclerFragmentToEditEjerFragment(rutina.ID)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}